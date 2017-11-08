package com.olegsh.vkapp.activity;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import com.olegsh.vkapp.R;
import com.vk.sdk.VKAccessToken;
import com.vk.sdk.VKCallback;
import com.vk.sdk.VKScope;
import com.vk.sdk.VKSdk;
import com.vk.sdk.api.VKError;

/**
 * Created by Oleg on 07.11.2017.
 */

public class LoginActivity extends AppCompatActivity {

    private final String TAG = "LoginActivity";
    private String[] vkscope = new String[] {VKScope.VIDEO, VKScope.WALL, VKScope.FRIENDS};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        //String[] fingerprints = VKUtil.getCertificateFingerprint(this, this.getPackageName());
        //Log.d(TAG, "Fingerprints: " + Arrays.asList(fingerprints));

        if(!VKSdk.isLoggedIn()) {
            ((Button) findViewById(R.id.login_button)).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    VKSdk.login(LoginActivity.this, vkscope);
                }
            });
        } else {
            startNextActivity();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (!VKSdk.onActivityResult(requestCode, resultCode, data, new VKCallback<VKAccessToken>() {
            @Override
            public void onResult(VKAccessToken res) {
                Log.d(TAG, "Login!!!! Token: " + res.accessToken);
                // Пользователь успешно авторизовался
                startNextActivity();
            }
            @Override
            public void onError(VKError error) {
                Log.e(TAG, "Error login: " + error.errorMessage);
                // Произошла ошибка авторизации (например, пользователь запретил авторизацию)
            }
        })) {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }

    private void startNextActivity() {
        Intent intent = new Intent(LoginActivity.this, VideosActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
    }


}
