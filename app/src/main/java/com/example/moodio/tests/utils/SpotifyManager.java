package com.example.moodio.tests.utils;
import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;

import com.example.moodio.Utils.Playlist;
import com.example.moodio.Utils.ResponseServer;
import com.google.gson.Gson;
import com.spotify.android.appremote.api.ConnectionParams;
import com.spotify.android.appremote.api.Connector;
import com.spotify.android.appremote.api.SpotifyAppRemote;

import com.spotify.protocol.types.Track;


import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okio.BufferedSink;

public class SpotifyManager {

    private static final String CLIENT_ID = "2e42b3849b9d491790d9abf8bf66f16e";
    private static final String REDIRECT_URI = "https://www.google.com"; // Set this up on Spotify Developer Dashboard
    private SpotifyAppRemote mSpotifyAppRemote;

    private Context mContext;
    private OkHttpClient mHttpClient;

    private String currentEmotion;

    private String DEFAULT_EMOTION = "Happy";

    private String DEFAULT_PLAYLIST = "";

    public SpotifyManager(Context context) {
        mContext = context;
        currentEmotion = DEFAULT_EMOTION;
        mHttpClient = new OkHttpClient.Builder()
                .connectTimeout(60, TimeUnit.SECONDS)
                .writeTimeout(60, TimeUnit.SECONDS)
                .readTimeout(60, TimeUnit.SECONDS)
                .build();
    }

    public void initializeSpotify() {
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

                // Now you can start interacting with App Remote
                getPlaylist(DEFAULT_EMOTION);;
            }

            @Override
            public void onFailure(Throwable throwable) {
                Log.e("SpotifyManager", "Failed to connect to spotify " + throwable.getMessage(), throwable);
            }
        });
    }

    public void playPlaylist(ResponseServer responseServer ) throws MalformedURLException {
        if(!Objects.equals(currentEmotion, responseServer.getEmotion())){
            currentEmotion = responseServer.getEmotion();
            Playlist defaultPlaylist = new Playlist(responseServer.getPlaylistUrl(), null);
            URL url = new URL(
                    Objects.requireNonNull(
                            Objects.requireNonNull(
                                    responseServer
                                            .getPlaylistsUrls()
                                            .getOrDefault(currentEmotion, defaultPlaylist)
                                    )
                            .getPlaylistUrl()
            ));

            String playlistUri = url.getPath();
            playlistUri = playlistUri.replace("/",":");
            Log.d("SpotifyManager", "About to play " + playlistUri);
            connected(playlistUri);
        }
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
                .url("http://192.168.1.218:9000/app?emotions=" + emotion)
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
                        URL url = new URL(serverResponse.getPlaylistUrl());
                        DEFAULT_PLAYLIST = url.getPath();
                        DEFAULT_PLAYLIST = DEFAULT_PLAYLIST.replace("/",":");
                        Log.d("SpotifyManager", "Retrieved spotify link: " + DEFAULT_PLAYLIST);

                        connected(DEFAULT_PLAYLIST);

                    }
                    response.close();
                } else {
                    Log.e("SpotifyManager", "Failed to retrieve spotify link, null response body.\n" + "Response Code: " + response.code());

                }
            }
        });
    }

    public void disconnect() {
        SpotifyAppRemote.disconnect(mSpotifyAppRemote);
    }

    public void stopMusic() {
        mSpotifyAppRemote.getPlayerApi()
                .subscribeToPlayerState()
                .setEventCallback(playerState -> {

                    if(!playerState.isPaused){
                        mSpotifyAppRemote.getPlayerApi().pause();
                    }
                });

    }

    public void resumeMusic() {
        mSpotifyAppRemote.getPlayerApi()
                .subscribeToPlayerState()
                .setEventCallback(playerState -> {

                    if(playerState.isPaused){
                        mSpotifyAppRemote.getPlayerApi().resume();
                    }

                });
    }

    //TODO: response.close() to all HTTP responses with body
}
