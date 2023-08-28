
package com.example.moodio.activities;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.net.http.SslError;
import android.os.Bundle;

import android.webkit.CookieManager;
import android.webkit.PermissionRequest;
import android.webkit.SslErrorHandler;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceRequest;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import androidx.appcompat.app.AppCompatActivity;

import com.example.moodio.R;

import java.util.HashMap;
import java.util.Map;

public class SpotifyActivity extends AppCompatActivity {

    private static final String CLIENT_ID = "a7bff5059afb4b969966df56c651f6e8";
    private static final String REDIRECT_URI = "https://www.google.com";

    private WebView webView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_spotify);
        getWindow().setStatusBarColor(Color.BLACK);
        webView = findViewById(R.id.spotifyWebView);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        webView.getSettings().setJavaScriptEnabled(true);
        CookieManager cookieManager = CookieManager.getInstance();
        cookieManager.setAcceptCookie(true);
        webView.getSettings().setDomStorageEnabled(true);
        webView.setWebChromeClient(new WebChromeClient() {
            @Override
            public void onPermissionRequest(final PermissionRequest request) {
                request.grant(request.getResources());
            }
        });


        webView.setWebViewClient(new WebViewClient() {
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
                String url = request.getUrl().toString();
                if (url.startsWith(REDIRECT_URI)) {
                    handleRedirectUri(url);
                    return true;
                }
                return super.shouldOverrideUrlLoading(view, request);
            }
            @Override
            public void onReceivedSslError(WebView view, SslErrorHandler handler, SslError error) {
                handler.proceed();
            }
        });

        authenticate();
    }


    private void authenticate() {
        String authorizationUrl = "https://accounts.spotify.com/authorize";
        Map<String, String> queryParams = new HashMap<>();
        queryParams.put("client_id", CLIENT_ID);
        queryParams.put("response_type", "token");
        queryParams.put("redirect_uri", REDIRECT_URI);
        String url = authorizationUrl + "?" + buildQueryString(queryParams);

        webView.loadUrl(url);
    }

    private void handleRedirectUri(String redirectUri) {

        Uri uri = Uri.parse(redirectUri);
        String accessToken = uri.getFragment().split("=")[1];
        Intent intent = getIntent();
        String playlistUrl =  intent.getStringExtra("EXTRA_MESSAGE");;
        String url = playlistUrl + "?access_token=" + accessToken;

        webView.loadUrl(url);
    }

    private String buildQueryString(Map<String, String> params) {
        StringBuilder sb = new StringBuilder();
        for (Map.Entry<String, String> entry : params.entrySet()) {
            sb.append(entry.getKey());
            sb.append("=");
            sb.append(Uri.encode(entry.getValue()));
            sb.append("&");
        }
        return sb.substring(0, sb.length() - 1);
    }
}