package com.deny.GeoLogi;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.WindowManager;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import com.deny.GeoLogi.R;

public class Nastenka extends AppCompatActivity {
    private static final String TAG = "WebView";

    private WebView mWebView;

    @SuppressLint("SetJavaScriptEnabled")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_nastenka);

        mWebView = (WebView) findViewById( R.id.vysledky_webview);

        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        //to remove the action bar (title bar)
        getSupportActionBar().hide();

        //page to show - by default it's in the settings of the game
        String sPage = Nastaveni.getInstance(this).getsNastenka();

        try {
            Intent intentExtras = getIntent();
            Bundle params = intentExtras.getExtras();

            if (!params.isEmpty()) {
                sPage = params.getString("Page");
            }
        }
        catch (Exception e) {
                Log.d(TAG, "OnCreate " + e.getMessage());
        }

        WebSettings webSettings = mWebView.getSettings();
        webSettings.setJavaScriptEnabled(true);
        mWebView.getSettings().setBuiltInZoomControls(true);

        mWebView.setWebViewClient(new WebViewClient());

        Log.d(TAG, "Showing: "+sPage);
        mWebView.loadUrl(sPage);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }
}
