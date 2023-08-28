package com.example.moodio;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.squareup.picasso.Picasso;

import java.util.ArrayList;

public class PlayListAdapter extends ArrayAdapter<PlaylistInfo> {
private Context mContext;
private int mResource;

    public PlayListAdapter(@NonNull Context context, int resource, @NonNull ArrayList<PlaylistInfo> objects) {
        super(context, resource, objects);
        this.mContext=context;
        this.mResource=resource;
    }
    public String getPlaylistUrl(int position){
       return getItem(position).getPlaylistUrl();

    }

    public String getPlaylistName(int position){
        return getItem(position).getPlaylistName();

    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {

        LayoutInflater layoutInflater=LayoutInflater.from(mContext);
        convertView=layoutInflater.inflate(mResource,parent,false);
        ImageView imageView=convertView.findViewById(R.id.image);
        TextView playlistName=convertView.findViewById(R.id.playlistName);
        Picasso.get().load(getItem(position).getImage()).into(imageView);
        playlistName.setText(getItem(position).getPlaylistName());
        return convertView;

    }
}
