package com.urvesh.drize.Activity;

import android.annotation.TargetApi;
import android.os.Build;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;

import com.urvesh.drize.R;
import com.urvesh.drize.Adapter.SongListAdapter;
import com.urvesh.drize.SongManager;

import java.util.ArrayList;
import java.util.HashMap;

public class SongList extends AppCompatActivity {

    RecyclerView recyclerView;
    public ArrayList<HashMap<String, String>> songsList = new ArrayList<HashMap<String, String>>();
    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        overridePendingTransition(R.transition.do_not_move, R.transition.do_not_move);
        setContentView(R.layout.activity_song_list);

        Window window = getWindow();
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        window.setStatusBarColor(getResources().getColor(R.color.colorPrimaryDark));
        View view = findViewById(android.R.id.content);
        ArrayList<HashMap<String, String>> songsListData = new ArrayList<HashMap<String, String>>();
        SongManager songManager = new SongManager();
        this.songsList = songManager.getPlayList();
        for (int i = 0; i < songsList.size(); i++) {
            HashMap<String, String> song = songsList.get(i);

            songsListData.add(song);
        }
        if (songsListData.size() <= 0) {
            Snackbar.make(view, "No Media Found", Snackbar.LENGTH_LONG).show();
        }

        recyclerView = (RecyclerView) findViewById(R.id.recylerView);
        SongListAdapter songListAdapter = new SongListAdapter(SongList.this, songsListData);
        recyclerView.setLayoutManager(new LinearLayoutManager(SongList.this));
        recyclerView.setAdapter(songListAdapter);

    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        recyclerView.setVisibility(View.INVISIBLE);
    }
}
