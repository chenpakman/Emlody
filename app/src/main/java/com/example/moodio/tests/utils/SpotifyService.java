package com.example.moodio.tests.utils;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Header;
import retrofit2.http.POST;

public interface SpotifyService {
    @POST("/v1/me/player/play")
    Call<Void> startPlayback(
            @Header("Authorization") String accessToken,
            @Body PlaybackRequest playbackRequest
    );
}
