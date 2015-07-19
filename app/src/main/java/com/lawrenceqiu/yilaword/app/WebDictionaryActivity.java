package com.lawrenceqiu.yilaword.app;

import android.app.Activity;
import android.os.Bundle;
import android.webkit.WebView;
import android.webkit.WebViewClient;

/**
 * Created by Lawrence on 7/1/2015.
 */
public class WebDictionaryActivity extends Activity {
    private WebView mWebView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Bundle bundle = getIntent().getExtras();
        String url = bundle.getString("url");

        mWebView = new WebView(this);
        //Zooms out to file the content by width
        mWebView.getSettings().setLoadWithOverviewMode(true);
        //Support for wideport HTML meta
        mWebView.getSettings().setUseWideViewPort(true);
        //Enables the ability for zooming in and zooming out
        mWebView.getSettings().setBuiltInZoomControls(true);
        mWebView.setWebViewClient(new WebViewClient() {
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                mWebView.loadUrl(url);           //Allow users to go to other links on the page
                return false;
            }
        });
        mWebView.loadUrl(url);

        setContentView(mWebView);    //Setting this as content view, means no need to create a layout file
    }
}
