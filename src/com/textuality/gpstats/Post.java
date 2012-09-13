package com.textuality.gpstats;

import org.json.JSONException;
import org.json.JSONObject;

import android.net.Uri;

public class Post {
    private int mID = -1;
    private String mText = null;
    private final int mPlusOnes;
    private final JSONObject mJSON;
    private Uri mUri = null;

    public static final String FEED_FIELDS = "items/id," +
            "items/url," +
            "items/verb," +
            "items/title," +
            "items/object/plusoners," +
            "items/object/attachments/displayName," +
            "nextPageToken";

    public Post(JSONObject json) throws JSONException {
        // let’s be a little lazy; we only need to compute the plusOners for basics
        mJSON = json;
        JSONObject object = json.getJSONObject("object");
        mPlusOnes = object.getJSONObject("plusoners").getInt("totalItems");
    }

    public int plusOnes() {
        return mPlusOnes;
    }

    public Uri uri() {
        if (mUri == null) {
            try {
                mUri = Uri.parse(mJSON.getString("url"));
            } catch (JSONException e) {
                throw new RuntimeException(e);
            }
        } 
        return mUri;
    }
    public long id() {
        if (mID == -1) {
            try {
                mID = mJSON.getString("id").hashCode();
            } catch (JSONException e) {
                throw new RuntimeException(e);
            }
        }
        return mID;
    }
    public String text() {
        if (mText == null) {
            String title = mJSON.optString("title");
            if ((title != null) && !title.isEmpty()) {
                mText = title;
            } else {
                try {
                    mText = mJSON.getJSONObject("object").
                            getJSONArray("attachments").getJSONObject(0).getString("displayName");
                } catch (JSONException e) {
                    mText = "(untitled)";
                }
            }
            mText = mText.replaceAll("(\\n\\s*)+", "\n");
        } 
        return mText;
    }

    public String toString() {
        return "+" + mPlusOnes + ": " + text();
    }

}
