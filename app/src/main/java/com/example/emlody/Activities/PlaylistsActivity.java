package com.example.emlody.Activities;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.webkit.WebView;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.emlody.PlayListAdapter;
import com.example.emlody.Playlist;
import com.example.emlody.R;
import com.example.emlody.Utils.ResponseServer;
import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class PlaylistsActivity extends AppCompatActivity {


    private ListView playListsListView;
    PlayListAdapter playListAdapter;
    ArrayList<Playlist> playlistsArray;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_playlists);
        playListsListView=findViewById(R.id.listView);
        playlistsArray =new ArrayList<>();

        String playlistsJson = getIntent().getStringExtra("EXTRA_MESSAGE");
        Gson gson = new Gson();
        ResponseServer res = gson.fromJson(playlistsJson, ResponseServer.class);
        playListAdapter=new PlayListAdapter(this,R.layout.list_row,playlistsArray);
        playListsListView.setAdapter(playListAdapter);

        addPlaylists(res);
        System.out.println("On create PlaylistActivity");//ToDo:delete

    }
private void addPlaylists(ResponseServer res){
    for (Map.Entry<String, String> entry: res.getPlaylistsUrls().entrySet()) {
        Playlist playlist=new Playlist(R.drawable.spotify,entry.getKey(),entry.getValue());
        playlistsArray.add(playlist);
        playListsListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String playlistUrl=playListAdapter.getPlaylistUrl(position);
                Intent intent = new Intent(PlaylistsActivity.this, SpotifyActivity.class);
                intent.putExtra("EXTRA_MESSAGE", playlistUrl);
                startActivity(intent);
            }
        });

    }


}






}
