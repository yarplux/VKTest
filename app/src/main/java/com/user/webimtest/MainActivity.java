package com.user.webimtest;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.vk.sdk.VKAccessToken;
import com.vk.sdk.VKCallback;
import com.vk.sdk.VKSdk;
import com.vk.sdk.api.VKApi;
import com.vk.sdk.api.VKApiConst;
import com.vk.sdk.api.VKError;
import com.vk.sdk.api.VKParameters;
import com.vk.sdk.api.VKRequest;
import com.vk.sdk.api.VKResponse;
import com.vk.sdk.api.model.VKScopes;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Random;

import static org.apache.commons.lang3.math.NumberUtils.min;

public class MainActivity extends AppCompatActivity {

    private Button bLogin;
    private TextView vText;
    private View vProgress;

    private int FRIENDS_LIMIT = 5;

    private VKRequest requestFriends;
    private VKRequest requestProfile;

    private VKRequest.VKRequestListener requestFriendsListener;
    private VKRequest.VKRequestListener requestProfileListener;

    private StringBuilder text = new StringBuilder();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        vProgress = findViewById(R.id.progress);

        requestFriends = VKApi.friends().get(VKParameters.from(VKApiConst.FIELDS, "nickname"));
        requestFriendsListener = new VKRequest.VKRequestListener() {
            @Override
            public void onComplete(VKResponse response) {
                super.onComplete(response);
                try {
                    JSONArray jsonFriends = response.json
                            .getJSONObject("response")
                            .getJSONArray("items");

                    Random generator = new Random();

                    text.append(FRIENDS_LIMIT).append(" случайных друзей:\n");

                    for (int i = 0; i< min(jsonFriends.length(), FRIENDS_LIMIT); i++) {

                        int index = generator.nextInt(jsonFriends.length());

                        JSONObject friend = jsonFriends.getJSONObject(index);
                        String firstname = (String) friend.get("first_name");
                        String lastname = (String) friend.get("last_name");
                        text.append(firstname).append(" ").append(lastname).append("\n");
                    }
                    show();

                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        };

        requestProfile = new VKRequest("account.getProfileInfo", VKParameters.from(VKApiConst.FIELDS, "firstname"));
        requestProfileListener = new VKRequest.VKRequestListener() {
            @Override
            public void onComplete(VKResponse response) {
                super.onComplete(response);
                try {
                    JSONObject jsonProfile = response.json
                            .getJSONObject("response");

                    String firstname = (String) jsonProfile.get("first_name");
                    String lastname = (String) jsonProfile.get("last_name");
                    text.append(firstname).append(" ").append(lastname).append(":\n\n");

                    requestFriends.executeWithListener(requestFriendsListener);

                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        };

        bLogin = findViewById(R.id.login);
        bLogin.setOnClickListener(view -> VKSdk.login(this, VKScopes.FRIENDS));

        vText = findViewById(R.id.text);

        if (VKSdk.isLoggedIn()) {
            vProgress.setVisibility(View.VISIBLE);
            bLogin.setVisibility(View.GONE);
            requestProfile.executeWithListener(requestProfileListener);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (!VKSdk.onActivityResult(requestCode, resultCode, data, new VKCallback<VKAccessToken>() {
            @Override
            public void onResult(VKAccessToken res) {
                vProgress.setVisibility(View.VISIBLE);
                bLogin.setVisibility(View.GONE);
                requestProfile.executeWithListener(requestProfileListener);
            }
            @Override
            public void onError(VKError error) {
            }
        })) {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }

    public void show(){
        vText.setText(text.toString());
        vProgress.setVisibility(View.GONE);
        bLogin.setVisibility(View.GONE);
        vText.setVisibility(View.VISIBLE);
    }
}

