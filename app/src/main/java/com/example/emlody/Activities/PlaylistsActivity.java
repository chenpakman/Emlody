package com.example.emlody.Activities;

import android.net.Uri;
import android.net.http.SslError;
import android.os.Bundle;
import android.view.View;
import android.webkit.CookieManager;
import android.webkit.PermissionRequest;
import android.webkit.SslErrorHandler;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceRequest;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.emlody.R;
import com.example.emlody.SpotifyImageView;
import com.example.emlody.Utils.ResponseServer;
import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class PlaylistsActivity extends AppCompatActivity {

    private static final String CLIENT_ID = "a7bff5059afb4b969966df56c651f6e8";

    private static final String REDIRECT_URI = "https://www.google.com";

    private ListView playLists;

    private ArrayList<SpotifyImageView> playList;

    private ArrayAdapter<SpotifyImageView> adapter;

    private WebView webView;

    private String accessToken;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_playlists);
        this.playLists = findViewById(R.id.chooseFrom);
        this.playList = new ArrayList<>();
        this.adapter =
                new ArrayAdapter<>(
                        this,
                        android.R.layout.simple_list_item_1,
                        this.playList
                );
        this.playLists.setAdapter(adapter);
        this.webView = findViewById(R.id.playlistView);
        this.webView.getSettings().setJavaScriptEnabled(true);
        CookieManager cookieManager = CookieManager.getInstance();
        cookieManager.setAcceptCookie(true);
        this.webView.getSettings().setDomStorageEnabled(true);
        this.webView.setWebChromeClient(new WebChromeClient() {
            @Override
            public void onPermissionRequest(final PermissionRequest request) {
                request.grant(request.getResources());
            }
        });


        this.webView.setWebViewClient(new WebViewClient() {
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
        System.out.println("On create PlaylistActivity");
    }


    private void authenticate() {
        String authorizationUrl = "https://accounts.spotify.com/authorize";
        Map<String, String> queryParams = new HashMap<>();
        queryParams.put("client_id", CLIENT_ID);
        queryParams.put("response_type", "token");
        queryParams.put("redirect_uri", REDIRECT_URI);
        String url = authorizationUrl + "?" + buildQueryString(queryParams);

        this.webView.loadUrl(url);
    }

    private void handleRedirectUri(String redirectUri) {

        Uri uri = Uri.parse(redirectUri);
        this.accessToken = uri.getFragment().split("=")[1];
        String playlistsJson = getIntent().getStringExtra("EXTRA_MESSAGE");
        Gson gson = new Gson();
        ResponseServer res = gson.fromJson(playlistsJson, ResponseServer.class);
        for (Map.Entry<String, String> entry: res.getPlaylistsUrls().entrySet()) {
            this.addPlaylistIcon(entry.getKey(), entry.getValue());
        }

        String playlistUrl = res.getPlaylistsUrls().get(res.getEmotion());
        String url = playlistUrl + "?access_token=" + this.accessToken;
        this.webView.loadUrl(url);
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

    public void playlistChosen(String playlistUrl){
        //getIntent().putExtra("EXTRA_MESSAGE", playlistUrl);
        this.webView.loadUrl(playlistUrl + "?access_token=" + this.accessToken);
    }

    private void addPlaylistIcon(String mood , String playListUri) {
        SpotifyImageView spotifyImageView = new SpotifyImageView(this);
        spotifyImageView.setSpotifyUri(playListUri);
        spotifyImageView.setMood(mood);
        spotifyImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                playlistChosen(spotifyImageView.getSpotifyUri());
                adapter.notifyDataSetChanged();
            }
        });
        this.playLists.addHeaderView(spotifyImageView);
    }
}
