package com.textuality.gpstats;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.widget.ArrayAdapter;
import android.widget.TextView;

public class MostPopular {
    
    private final ArrayAdapter<Post> mAdapter;
    private final TextView mReadout;
    private final static int LIST_SIZE = 40;
    private int mThreshold = 0;
    private int mPostsProcessed = 0;
    
    public MostPopular(ArrayAdapter<Post> adapter, TextView readout) {
        mAdapter = adapter;
        mReadout = readout;
    }

    private void consider(JSONObject item) throws JSONException {
        Post post = new Post(item);
        if (post.plusOnes() > mThreshold) {
            int insertAt;
            for (insertAt = 0; insertAt < mAdapter.getCount(); insertAt++) {
                Post existing = mAdapter.getItem(insertAt);
                if (post.plusOnes() > existing.plusOnes()) {
                    break;
                }
            }
            mAdapter.insert(post, insertAt);
            if (mAdapter.getCount() > LIST_SIZE) {
                mAdapter.remove(mAdapter.getItem(mAdapter.getCount() - 1));
            }
            mAdapter.notifyDataSetChanged();
            mThreshold = mAdapter.getItem(mAdapter.getCount() - 1).plusOnes();
        }
    }

    public String processFeedPage(byte[] body) {
        String nextPageToken = null;
        try {
            JSONObject json = new JSONObject(new String(body));
            nextPageToken = json.optString("nextPageToken");
            JSONArray items = json.optJSONArray("items");
            if (items != null) {
                int length = items.length();
                for (int i = 0; i < length; i++) {
                    consider(items.getJSONObject(i));
                    mPostsProcessed++;
                    mReadout.setText("Processed " + mPostsProcessed + " posts.");
                }
            }
            if (nextPageToken.isEmpty()) {
                mReadout.setText("Processed " + mPostsProcessed + " posts. (ThatÕs all.)");
                return null;
            } else {
                return nextPageToken;
            }
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }
    }    
}
