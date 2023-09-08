package com.example.moodio.utils;
import android.util.Log;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.example.moodio.R;
import com.example.moodio.utils.ResponseServer;
import com.example.moodio.activities.LiveCameraActivity;
import com.google.gson.Gson;
import com.spotify.android.appremote.api.ConnectionParams;
import com.spotify.android.appremote.api.Connector;
import com.spotify.android.appremote.api.SpotifyAppRemote;

import com.spotify.protocol.types.Track;


import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
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

public class SpotifyManager {

    private static final String CLIENT_ID = "a7bff5059afb4b969966df56c651f6e8";
    private static final String REDIRECT_URI = "https://www.google.com";
    private SpotifyAppRemote mSpotifyAppRemote;

    private LiveCameraActivity mContext;
    private OkHttpClient mHttpClient;

    private String currentEmotion;

    private String DEFAULT_EMOTION = "Happy";

    private String DEFAULT_PLAYLIST = "";

    public SpotifyManager(LiveCameraActivity context) {
        mContext = context;
        currentEmotion = DEFAULT_EMOTION;
        mHttpClient = new OkHttpClient.Builder()
                .connectTimeout(60, TimeUnit.SECONDS)
                .writeTimeout(60, TimeUnit.SECONDS)
                .readTimeout(60, TimeUnit.SECONDS)
                .build();
    }

    public void initializeSpotify() {
        //disconnect();
        ConnectionParams connectionParams = new ConnectionParams.Builder(CLIENT_ID)
                .setRedirectUri(REDIRECT_URI)
                .showAuthView(true)
                .build();

        SpotifyAppRemote.connect(mContext, connectionParams, new Connector.ConnectionListener() {
            @Override
            public void onConnected(SpotifyAppRemote spotifyAppRemote) {
                mSpotifyAppRemote = spotifyAppRemote;
                // You're connected to Spotify!
                Log.d("SpotifyManager", "Connected to spotify!");
                getPlaylist(DEFAULT_EMOTION);
            }

            @Override
            public void onFailure(Throwable throwable) {
                Log.e("SpotifyManager", "Failed to connect to spotify " + throwable.getMessage(), throwable);
            }
        });

    }

    public void playPlaylist(String mixName, String playlistURL, String detectedEmotion, boolean isRelative) {

        try{
            if(isRelative){
                getPlaylistURLAndPlay(mixName, playlistURL);
            }
            else if(!Objects.equals(currentEmotion, detectedEmotion)){
                currentEmotion = detectedEmotion;
                getPlaylistURLAndPlay(mixName, playlistURL);
            }

        } catch (MalformedURLException e) {
            Log.e("SpotifyManager", "Couldn't form a URL " + e.getMessage(), e);
        }
    }

    private void getPlaylistURLAndPlay(String mixName, String playlistURL) throws MalformedURLException {
        String playlistUri = getPlaylistURL(playlistURL);
        Log.d("SpotifyManager", "About to play " + playlistUri);
        connected(playlistUri);
        mContext.updateTitle(mixName);
    }

    @NonNull
    private String getPlaylistURL(String playlistURL) throws MalformedURLException {
        URL url = new URL(playlistURL);
        String playlistUri = url.getPath();
        playlistUri = playlistUri.replace("/",":");
        return playlistUri;
    }

    private void connected(String playlistUri) {

        // Play a playlist
        mSpotifyAppRemote.getPlayerApi().play("spotify" + playlistUri);
        // Subscribe to PlayerState
        mSpotifyAppRemote.getPlayerApi()
                .subscribeToPlayerState()
                .setEventCallback(playerState -> {
                    final Track track = playerState.track;
                    if (track != null) {
                        Log.d("SpotifyManager", track.name + " by " + track.artist.name);
                    }
                });
    }

    private void getPlaylist(String emotion) {

        Request request = new Request.Builder()
                //.url("http://3.70.133.202:8080/app?emotions=" + emotion)
               // .url("http://192.168.1.218:9000/app?emotions=" + emotion)
                .url("http://192.168.1.35:9000/app?emotions=" + emotion)
                .put(new RequestBody() {
                    @Override
                    public MediaType contentType() {
                        return null;
                    }

                    @Override
                    public void writeTo(BufferedSink sink) throws IOException {

                    }
                })
                .build();

        mHttpClient.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                Log.e("SpotifyManager", "Failed to retrieve spotify link, couldn't connect to server.\n" + e.getMessage());
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                if (response.body() != null) {
                    if (response.isSuccessful()) {
                        String responseBody = response.body().string();
                        Gson gson = new Gson(); // Or use new GsonBuilder().create();
                        ResponseServer serverResponse = gson.fromJson(responseBody, ResponseServer.class);
                        URL url = new URL(serverResponse.getDefaultPlaylistUrl());
                        DEFAULT_PLAYLIST = url.getPath();
                        DEFAULT_PLAYLIST = DEFAULT_PLAYLIST.replace("/",":");
                        Log.d("SpotifyManager", "Retrieved spotify link: " + DEFAULT_PLAYLIST);
                        connected(DEFAULT_PLAYLIST);
                        mContext.updateTitle(DEFAULT_EMOTION);


                    }
                    response.close();
                } else {
                    Log.e("SpotifyManager", "Failed to retrieve spotify link, null response body.\n" + "Response Code: " + response.code());

                }
            }
        });
    }

    public void disconnect() {
        if(null != mSpotifyAppRemote) {
            SpotifyAppRemote.disconnect(mSpotifyAppRemote);
        }
    }

    public void stopMusic() {
        if(null != mSpotifyAppRemote && mSpotifyAppRemote.isConnected()) {
            mSpotifyAppRemote.getPlayerApi()
                    .subscribeToPlayerState()
                    .setEventCallback(playerState -> {

                        if (!playerState.isPaused) {
                            mSpotifyAppRemote.getPlayerApi().pause();
                            Log.d("SpotifyManager", "Paused music.\n");
                        }
                    });

        }

    }

    public void resumeMusic() {
        if(null != mSpotifyAppRemote && mSpotifyAppRemote.isConnected()) {
            mSpotifyAppRemote.getPlayerApi()
                    .subscribeToPlayerState()
                    .setEventCallback(playerState -> {

                        if (playerState.isPaused) {
                            mSpotifyAppRemote.getPlayerApi().resume();
                            Log.d("SpotifyManager", "Resumed music");
                        }

                    });
        }
    }

    //TODO: response.close() to all HTTP responses with body
}
