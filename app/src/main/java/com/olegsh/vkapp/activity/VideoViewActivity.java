package com.olegsh.vkapp.activity;

import android.app.Activity;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.ViewGroup;
import android.webkit.WebSettings;
import android.webkit.WebView;

import com.olegsh.vkapp.R;

public class VideoViewActivity extends Activity {

    WebView webView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        webView = new WebView(this);
        WebSettings webSettings = webView.getSettings();
        webSettings.setJavaScriptEnabled(true);
        ViewGroup.LayoutParams llParams = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        setContentView(webView, llParams);
        webView.loadUrl(getIntent().getAction());
    }

    @Override
    public void onBackPressed() {
        webView.destroy();
        finish();
        super.onBackPressed();
    }
}
