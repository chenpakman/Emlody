package com.example.moodio.tests.activities;

import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.net.http.SslError;
import android.os.Bundle;
import android.provider.Settings;
import android.webkit.CookieManager;
import android.webkit.PermissionRequest;
import android.webkit.SslErrorHandler;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceRequest;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.moodio.R;
import com.example.moodio.Utils.ResponseServer;
import com.google.gson.Gson;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okio.BufferedSink;

public class LiveStreamActivity extends AppCompatActivity {

    private static final String CLIENT_ID = "a7bff5059afb4b969966df56c651f6e8";
    private static final String REDIRECT_URI = "https://www.google.com";

    private static final String SPOTIFY_BASE_URL = "https://api.spotify.com/v1/";

    private static final String SPOTIFY_CLIENT_ID = "2e42b3849b9d491790d9abf8bf66f16e";

    private static final String SPOTIFY_CLIENT_SECRET = "78c59d0a90a5468681e892f205e513a7";

    private static final String SPOTIFY_ACCESS_TOKEN = "BQCpLvRs4xAFTNvYE1DKUis3blc_hJrimmGya4QlEj8cI6qs0UC4BPZ_GK_wMkxSEcF6H1igKwe_zreuPexBYOxEbpjEj5fcbMGByje8m9GbLSa3zHs";
    private WebView webView;

    private String currentDetectedMood = null;

    private String DEVICE_ID = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        DEVICE_ID = Settings.Secure.getString(getContentResolver(),
                Settings.Secure.ANDROID_ID);
        System.out.println("DEVICE ID : " + DEVICE_ID);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_live_stream);
        getWindow().setStatusBarColor(Color.BLACK);
        webView = findViewById(R.id.boomboxWebView);
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);
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

        requestPlaylist("Happy");

        webView.setWebViewClient(new WebViewClient() {
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
                String url = request.getUrl().toString();
                if (url.startsWith(REDIRECT_URI)) {
                    handleRedirectUri(url);
                    //startPlayback();
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
        String playlistUrl =  intent.getStringExtra("EXTRA_MESSAGE");
        String url = playlistUrl + "?access_token=" + accessToken;
        System.out.println(url);

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

    /*private void startPlayback() {
        String playlistUrl =  getIntent().getStringExtra("EXTRA_MESSAGE");
        String playlistUri = "spotify:playlist:" + playlistUrl; // Replace with your desired playlist URI

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(SPOTIFY_BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        SpotifyService spotifyService = retrofit.create(SpotifyService.class);
        PlaybackRequest request = new PlaybackRequest(Collections.singletonList(playlistUri));

        retrofit2.Call<Void> call = spotifyService.startPlayback(SPOTIFY_ACCESS_TOKEN,request);
        call.enqueue(new retrofit2.Callback<Void>() {
            @Override
            public void onResponse(@NonNull retrofit2.Call<Void> call, @NonNull retrofit2.Response<Void> response) {
                String finalMessage = null;
                if(response.body() != null) {
                    try {

                        String jsonString = response.body().toString();
                        JSONObject jsonObj = new JSONObject(jsonString);
                        finalMessage = jsonObj.get("message").toString();
                    } catch (JSONException e) {
                        finalMessage = e.getMessage();
                    }

                    String finalMessage1 = finalMessage;
                    runOnUiThread(() -> {
                        Toast.makeText(LiveStreamActivity.this, finalMessage1, Toast.LENGTH_LONG).show();
                    });
                }
            }

            @Override
            public void onFailure(@NonNull retrofit2.Call<Void> call, @NonNull Throwable t) {
                runOnUiThread(() -> {
                    Toast.makeText(LiveStreamActivity.this, "Failed " + t.getMessage(), Toast.LENGTH_LONG).show();
                });
            }
        });
    }*/

    public void requestPlaylist(String emotions) {

        OkHttpClient okHttpClient = new OkHttpClient.Builder()
                .connectTimeout(60, TimeUnit.SECONDS)
                .writeTimeout(60, TimeUnit.SECONDS)
                .readTimeout(60, TimeUnit.SECONDS)
                .build();

        Request request = new Request.Builder()
                //.url("http://3.70.133.202:8080/app?emotions=" + emotions)
                .url("http://192.168.1.218:9000/app?emotions=" + emotions)
                .put(new RequestBody() {
                    @Override
                    public MediaType contentType() {
                        return null;
                    }

                    @Override
                    public void writeTo(@NonNull BufferedSink sink) throws IOException {

                    }
                })
                .build();
        okHttpClient.newCall(request)
                .enqueue(new Callback() {
                    @Override
                    public void onFailure(@NonNull final Call call, @NonNull IOException e) {
                        runOnUiThread(() -> {
                            Toast.makeText(LiveStreamActivity.this, "Something went wrong, please try again." + e.getMessage(), Toast.LENGTH_LONG).show();

                        });
                    }
                    @Override
                    public void onResponse(@NonNull Call call, final Response response) throws IOException {
                        if(response.body()!=null){
                            Gson gson = new Gson(); // Or use new GsonBuilder().create();
                            String jsonString = response.body().string();
                            ResponseServer serverResponse = gson.fromJson(jsonString, ResponseServer.class);
                            if (response.code() == 200) {
                                getIntent().putExtra("EXTRA_MESSAGE", serverResponse.getDefaultPlaylistUrl());
                            }
                            else{

                                runOnUiThread(() -> {
                                    Toast.makeText(LiveStreamActivity.this, serverResponse.getError(), Toast.LENGTH_LONG).show();
                                });
                            }
                        }
                    }}  );


    }
}
