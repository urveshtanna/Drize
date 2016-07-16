package com.urvesh.drize.Adapter;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaMetadataRetriever;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.urvesh.drize.Activity.MainActivity;
import com.urvesh.drize.R;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by Urvesh on 07-Mar-16.
 */
public class SongListAdapter extends RecyclerView.Adapter<SongListAdapter.Holder> {

    ArrayList<HashMap<String, String>> songsListData;
    Activity activity;

    public SongListAdapter(Activity activity,ArrayList<HashMap<String, String>> songsListData){
        this.activity = activity;
        this.songsListData = songsListData;
    }

    @Override
    public Holder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater layoutInflater = activity.getLayoutInflater();
        View view = layoutInflater.inflate(R.layout.song_list_inner_view,null);
        return new Holder(view);
    }

    @Override
    public void onBindViewHolder(Holder holder, final int position) {
        holder.txtSongName.setText(songsListData.get(position).get("songTitle"));
        try {
            MediaMetadataRetriever mmr = new MediaMetadataRetriever();
            mmr.setDataSource(songsListData.get(position).get("songPath"));
            byte[] artBytes = mmr.getEmbeddedPicture();
            if (artBytes != null) {
                InputStream is = new ByteArrayInputStream(mmr.getEmbeddedPicture());
                Bitmap bm = BitmapFactory.decodeStream(is);
                holder.imgAlbumArt.setImageBitmap(bm);
            } else {
                holder.imgAlbumArt.setImageDrawable(activity.getResources().getDrawable(R.drawable.default_album_art));
            }
        }
        catch (Exception e){
            e.printStackTrace();
        }
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int songIndex = position;

                // Starting new intent
                Intent in = new Intent(activity.getApplicationContext(),
                        MainActivity.class);
                // Sending songIndex to PlayerActivity
                in.putExtra("songIndex", songIndex);
                activity.setResult(100, in);
                // Closing PlayListView
                activity.finish();
            }
        });
    }

    @Override
    public int getItemCount() {
        return songsListData.size();
    }

    public static class Holder extends RecyclerView.ViewHolder{

        TextView txtSongName;
        ImageView imgAlbumArt;

        public Holder(View itemView) {
            super(itemView);
            txtSongName = (TextView) itemView.findViewById(R.id.listSongName);
            imgAlbumArt = (ImageView) itemView.findViewById(R.id.listAlbumArt);
        }
    }
}
