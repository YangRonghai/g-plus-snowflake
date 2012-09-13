/*
 * Copyright 2012 Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.textuality.gpstats;

import org.json.JSONException;
import org.json.JSONObject;

import android.accounts.AccountManager;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Window;
import android.widget.ListView;
import android.widget.TextView;

import com.google.android.gms.common.AccountPicker;
import com.textuality.authorized.AuthorizedActivity;
import com.textuality.authorized.Response;
import com.textuality.authorized.ResponseHandler;


public class GPlusSnowflake extends AuthorizedActivity  {

    private String mID;
    private MostPopular mPopular;
    private static final int GPS_REQUEST_CODE = 782049854;
    private String mAccount;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.main);  

        final ListView list = (ListView) findViewById(R.id.list);
        final PostArrayAdapter adapter = new PostArrayAdapter(this);

        mPopular = new MostPopular(adapter, (TextView) findViewById(R.id.output));
        list.setAdapter(adapter);
        list.setOnItemClickListener(adapter); 

        Intent intent = AccountPicker.newChooseAccountIntent(null, null, new String[]{"com.google"}, 
                false, null, null, null, null);  
        startActivityForResult(intent, GPS_REQUEST_CODE);
    }
    
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == GPS_REQUEST_CODE && resultCode == RESULT_OK) {
            mAccount = data.getStringExtra(AccountManager.KEY_ACCOUNT_NAME);
            get("https://www.googleapis.com/plus/v1/people/me", mAccount, "oauth2:https://www.googleapis.com/auth/plus.me", 
                    null, new ResponseHandler() {
                        
                @Override
                public void handle(Response response) {
                    if (response.status != 200) {
                        barf(response);
                        return;
                    }
                    try {
                        JSONObject json = new JSONObject(new String(response.body));
                        mID = json.optString("id");
                        readFeedPage(null);
                    } catch (JSONException je) {
                        throw new RuntimeException(je);
                    }                        
                }
            });        
        }
    }

    private void readFeedPage(String nextPageToken) {
        String target = "https://www.googleapis.com/plus/v1/people/" + mID + "/activities/public";
        target += "?fields=" + Post.FEED_FIELDS;
        if (nextPageToken != null) {
            target += "&pageToken=" + nextPageToken;
        }

        get(target, mAccount, "oauth2:https://www.googleapis.com/auth/plus.me", null, new ResponseHandler() {

            @Override
            public void handle(Response response) {
                if (response.status != 200) {
                    barf(response);
                    return;
                }
                String npt = mPopular.processFeedPage(response.body);
                if (npt != null) {
                    readFeedPage(npt);
                }
            }
        });
    }

    private void barf(Response response) {
        TextView tv = (TextView) findViewById(R.id.output);
        String b = new String(response.body);
        Log.d(AuthorizedActivity.TAG, "Error " + response.status + " body: " + b);
        tv.setText("OUCH!  The Internet never works!\n" + b);
    }
}

