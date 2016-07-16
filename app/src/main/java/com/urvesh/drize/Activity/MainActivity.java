package com.urvesh.drize.Activity;

import android.Manifest;
import android.annotation.TargetApi;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.media.MediaMetadataRetriever;
import android.media.MediaPlayer;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.ActivityOptionsCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.urvesh.drize.OnRevealAnimationListener;
import com.urvesh.drize.R;
import com.urvesh.drize.SongManager;
import com.urvesh.drize.Utils.Utilities;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener, MediaPlayer.OnCompletionListener, SeekBar.OnSeekBarChangeListener {

    private ImageView imgThumbnail, imgShuffle, imgRepeat, imgSongList, imgForward, imgRewind;
    private FloatingActionButton fab_next, fab_previous, fab_play;
    private TextView txtSongName, txtCurrentDuration, txtTotalDuration;
    private SeekBar seekBar;
    private View view;
    // Media Player
    private MediaPlayer mediaPlayer;
    // Handler to update UI timer, progress bar etc,.
    private Handler mHandler = new Handler();
    LinearLayout mRevealView;
    private SongManager songManager;
    private Utilities utils;
    private int seekForwardTime = 5000; // 5000 milliseconds
    private int seekBackwardTime = 5000; // 5000 milliseconds
    private int currentSongIndex = 0;
    private boolean isShuffle = false;
    private boolean isRepeat = false;
    private LinearLayout musicToolbar;
    private ArrayList<HashMap<String, String>> songsList = new ArrayList<HashMap<String, String>>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        //setupWindowAnimations();
        musicToolbar = (LinearLayout) findViewById(R.id.musicToolbar);
        musicToolbar.setVisibility(View.INVISIBLE);
        imgThumbnail = (ImageView) findViewById(R.id.imgSongThumb);
        imgShuffle = (ImageView) findViewById(R.id.imgSongShuffle);
        imgRepeat = (ImageView) findViewById(R.id.imgSongRepeat);
        imgSongList = (ImageView) findViewById(R.id.imgSongList);
        imgForward = (ImageView) findViewById(R.id.imgSongForward);
        imgRewind = (ImageView) findViewById(R.id.imgSongRewind);
        fab_next = (FloatingActionButton) findViewById(R.id.fab_next);
        fab_previous = (FloatingActionButton) findViewById(R.id.fab_previous);
        fab_play = (FloatingActionButton) findViewById(R.id.fab_play);
        txtCurrentDuration = (TextView) findViewById(R.id.txtSongCurrentDuration);
        txtTotalDuration = (TextView) findViewById(R.id.txtSongTotalDuration);
        txtSongName = (TextView) findViewById(R.id.txtSongName);
        seekBar = (SeekBar) findViewById(R.id.songSeekBar);

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();
        //Always opening drawer for the first time of on create
        drawer.openDrawer(Gravity.LEFT);
        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        mediaPlayer = new MediaPlayer();
        songManager = new SongManager();
        utils = new Utilities();
        //Root view
        view = findViewById(android.R.id.content);
        // Listeners
        seekBar.setOnSeekBarChangeListener((SeekBar.OnSeekBarChangeListener) MainActivity.this); // Important
        mediaPlayer.setOnCompletionListener((MediaPlayer.OnCompletionListener) MainActivity.this); // Important

        int REQUEST_CODE = 123;
        int permission = ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.READ_EXTERNAL_STORAGE);
        if (permission != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, REQUEST_CODE);
        } else if (permission == PackageManager.PERMISSION_GRANTED) {

            enableAll();
        }


    }

    public void enableAll() {

        // Getting all songs list
        songsList = songManager.getPlayList();
        if (songsList.size() <= 0) {
            seekBar.setEnabled(false);
            Snackbar.make(view, "No Media Found", Snackbar.LENGTH_LONG).show();
        } else {
            seekBar.setEnabled(true);
        }

        imgSongList.setOnClickListener(new View.OnClickListener() {
            @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
            @Override
            public void onClick(View v) {

                Intent i = new Intent(getApplicationContext(), SongList.class);
                ActivityOptionsCompat options = ActivityOptionsCompat.
                        makeSceneTransitionAnimation(MainActivity.this, (View) imgSongList, "reveal");
                startActivityForResult(i, 100, options.toBundle());
            }
        });

        fab_play.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (songsList.size() > 0) {

                    if (mediaPlayer.isPlaying()) {
                        if (mediaPlayer != null) {
                            mediaPlayer.pause();
                            // Changing button image to play button
                            fab_play.setImageResource(R.drawable.ic_play_arrow_white_24dp);
                            Utilities.animateRevealHide(MainActivity.this, musicToolbar, R.color.colorPrimaryDark, fab_play.getWidth() / 2,
                                    new OnRevealAnimationListener() {
                                        @Override
                                        public void onRevealHide() {
                                        }

                                        @Override
                                        public void onRevealShow() {

                                        }
                                    });
                        }
                    } else {
                        // Resume song
                        if (mediaPlayer != null) {
                            mediaPlayer.start();
                            // Changing button image to pause button
                            fab_play.setImageResource(R.drawable.ic_pause_white_36dp);
                            animateRevealShow(musicToolbar);

                        }
                    }
                }

            }
        });

        imgForward.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (songsList.size() > 0) {
                    // get current song position
                    int currentPosition = mediaPlayer.getCurrentPosition();
                    // check if seekForward time is lesser than song duration
                    if (currentPosition + seekForwardTime <= mediaPlayer.getDuration()) {
                        // forward song
                        mediaPlayer.seekTo(currentPosition + seekForwardTime);
                    } else {
                        // forward to end position
                        mediaPlayer.seekTo(mediaPlayer.getDuration());
                    }
                }
            }

        });

        imgRewind.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (songsList.size() > 0) {
                    // get current song position
                    int currentPosition = mediaPlayer.getCurrentPosition();
                    // check if seekBackward time is greater than 0 sec
                    if (currentPosition - seekBackwardTime >= 0) {
                        // forward song
                        mediaPlayer.seekTo(currentPosition - seekBackwardTime);
                    } else {
                        // backward to starting position
                        mediaPlayer.seekTo(0);
                    }


                }
            }
        });

        fab_next.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (songsList.size() > 0) {
                    // check if next song is there or not
                    if (currentSongIndex < (songsList.size() - 1)) {
                        playSong(currentSongIndex + 1);
                        currentSongIndex = currentSongIndex + 1;
                    } else {
                        // play first song
                        playSong(0);
                        currentSongIndex = 0;
                    }
                }
            }
        });

        fab_previous.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (songsList.size() > 0) {
                    if (currentSongIndex > 0) {
                        playSong(currentSongIndex - 1);
                        currentSongIndex = currentSongIndex - 1;
                    } else {
                        // play last song
                        playSong(songsList.size() - 1);
                        currentSongIndex = songsList.size() - 1;
                    }
                }
            }
        });

        imgRepeat.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (songsList.size() > 0) {
                    if (isRepeat) {
                        isRepeat = false;
                        Toast.makeText(getApplicationContext(), "Repeat is OFF", Toast.LENGTH_SHORT).show();
                        imgRepeat.setImageResource(R.drawable.ic_repeat_black_36dp);
                        imgRepeat.setColorFilter(Color.BLACK);
                    } else {
                        // make repeat to true
                        isRepeat = true;
                        Toast.makeText(getApplicationContext(), "Repeat is ON", Toast.LENGTH_SHORT).show();
                        // make shuffle to false
                        isShuffle = false;
                        imgRepeat.setColorFilter(getResources().getColor(R.color.colorAccent));
                        imgShuffle.setImageResource(R.drawable.ic_shuffle_black_36dp);
                        imgShuffle.setColorFilter(Color.BLACK);
                    }
                }
            }
        });
        imgShuffle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (songsList.size() > 0) {
                    if (isShuffle) {
                        isShuffle = false;
                        Toast.makeText(getApplicationContext(), "Shuffle is OFF", Toast.LENGTH_SHORT).show();
                        imgShuffle.setImageResource(R.drawable.ic_shuffle_black_36dp);
                        imgShuffle.setColorFilter(Color.BLACK);
                    } else {
                        // make repeat to true
                        isShuffle = true;
                        Toast.makeText(getApplicationContext(), "Shuffle is ON", Toast.LENGTH_SHORT).show();
                        // make shuffle to false
                        isRepeat = false;
                        imgShuffle.setColorFilter(getResources().getColor(R.color.colorAccent));
                        imgRepeat.setImageResource(R.drawable.ic_repeat_black_36dp);
                        imgRepeat.setColorFilter(Color.BLACK);
                    }
                }
            }
        });
    }

    private void animateRevealShow(final View viewRoot) {
        int cx = (viewRoot.getLeft() + viewRoot.getRight()) / 2;
        int cy = (viewRoot.getTop() + viewRoot.getBottom()) / 2;
        Utilities.animateRevealShow(MainActivity.this, musicToolbar, fab_play.getWidth() / 2, R.color.colorAccent,
                cx, cy, new OnRevealAnimationListener() {
                    @Override
                    public void onRevealHide() {

                    }

                    @Override
                    public void onRevealShow() {
                        //initViews();
                    }
                });
    }

    private void initViews() {
        new Handler(Looper.getMainLooper()).post(new Runnable() {
            @Override
            public void run() {
                Animation animation = AnimationUtils.loadAnimation(MainActivity.this, android.R.anim.fade_in);
                animation.setDuration(300);
                musicToolbar.startAnimation(animation);
                //mIvClose.startAnimation(animation);
                //musicToolbar.setVisibility(View.VISIBLE);
                //mIvClose.setVisibility(View.VISIBLE);
            }
        });
    }

    @Override
    public void onCompletion(MediaPlayer mp) {
        // check for repeat is ON or OFF
        if (isRepeat) {
            // repeat is on play same song again
            playSong(currentSongIndex);
        } else if (isShuffle) {
            // shuffle is on - play a random song
            Random r = new Random();
            currentSongIndex = r.nextInt((songsList.size() - 1) - 0 + 3) + 0;
            playSong(currentSongIndex);
        } else {
            // no repeat or shuffle ON - play next song
            if (currentSongIndex < (songsList.size() - 1)) {
                playSong(currentSongIndex + 1);
                currentSongIndex = currentSongIndex + 1;
            } else {
                // play first song
                playSong(0);
                currentSongIndex = 0;
            }
        }
    }

    public void updateProgressBar() {
        mHandler.postDelayed(mUpdateTimeTask, 100);
    }

    /**
     * Background Runnable thread
     */
    private Runnable mUpdateTimeTask = new Runnable() {
        public void run() {
            long totalDuration = mediaPlayer.getDuration();
            long currentDuration = mediaPlayer.getCurrentPosition();

            // Displaying Total Duration time
            txtTotalDuration.setText("" + utils.milliSecondsToTimer(totalDuration));
            // Displaying time completed playing
            txtCurrentDuration.setText("" + utils.milliSecondsToTimer(currentDuration));

            // Updating progress bar
            int progress = (int) (utils.getProgressPercentage(currentDuration, totalDuration));
            //Log.e("Progress", ""+progress);
            seekBar.setProgress(progress);

            // Running this thread after 100 milliseconds
            mHandler.postDelayed(this, 100);
        }
    };

    /**
     *
     * */
    @Override
    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromTouch) {

    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {
        // remove message Handler from updating progress bar
        mHandler.removeCallbacks(mUpdateTimeTask);
    }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {
        mHandler.removeCallbacks(mUpdateTimeTask);
        int totalDuration = mediaPlayer.getDuration();
        int currentPosition = utils.progressToTimer(seekBar.getProgress(), totalDuration);

        // forward or backward to certain seconds
        mediaPlayer.seekTo(currentPosition);

        // update timer progress again
        updateProgressBar();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 123) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                enableAll();
            } else {
                Snackbar.make(view, "Permission Denied!", Snackbar.LENGTH_LONG).setAction("Okay", new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, 123);
                    }
                }).show();
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == 100) {
            currentSongIndex = data.getExtras().getInt("songIndex");
            // play selected song
            animateRevealShow(musicToolbar);
            playSong(currentSongIndex);
        }
    }

    public void playSong(int songIndex) {
        // Play song
        try {
            mediaPlayer.reset();
            mediaPlayer.setDataSource(songsList.get(songIndex).get("songPath"));
            mediaPlayer.prepare();
            mediaPlayer.start();
            // Displaying Song title
            String songTitle = songsList.get(songIndex).get("songTitle");
            txtSongName.setText(songTitle);

            MediaMetadataRetriever mmr = new MediaMetadataRetriever();
            mmr.setDataSource(songsList.get(songIndex).get("songPath"));
            byte[] artBytes = mmr.getEmbeddedPicture();
            if (artBytes != null) {
                InputStream is = new ByteArrayInputStream(mmr.getEmbeddedPicture());
                Bitmap bm = BitmapFactory.decodeStream(is);
                imgThumbnail.setImageBitmap(bm);
            } else {
                imgThumbnail.setImageDrawable(getResources().getDrawable(R.drawable.default_album_art));
            }// Changing Button Image to pause image
            fab_play.setImageResource(R.drawable.ic_pause_white_36dp);

            // set Progress bar values
            seekBar.setProgress(0);
            seekBar.setMax(100);

            // Updating progress bar
            updateProgressBar();
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
        } catch (IllegalStateException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_library) {
            Intent i = new Intent(getApplicationContext(), SongList.class);
            startActivityForResult(i, 100);
        } else if (id == R.id.nav_playlist) {

        } else if (id == R.id.nav_setting) {

        } else if (id == R.id.nav_help) {

        } else if (id == R.id.nav_now_playing) {

        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }
}
