package com.example.emlody;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.ArrayList;
import java.util.List;

public class PlayListAdapter extends ArrayAdapter<Playlist> {
private Context mContext;
private int mResource;

    public PlayListAdapter(@NonNull Context context, int resource, @NonNull ArrayList<Playlist> objects) {
        super(context, resource, objects);
        this.mContext=context;
        this.mResource=resource;
    }
    public String getPlaylistUrl(int position){
       return getItem(position).getPlaylistUrl();


    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {

        LayoutInflater layoutInflater=LayoutInflater.from(mContext);
        convertView=layoutInflater.inflate(mResource,parent,false);
        ImageView imageView=convertView.findViewById(R.id.image);
        TextView playlistName=convertView.findViewById(R.id.playlistName);
        imageView.setImageResource(getItem(position).getImage());
        playlistName.setText(getItem(position).getPlaylistName());
        return convertView;

    }
}
