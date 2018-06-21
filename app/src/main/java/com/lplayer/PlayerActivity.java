package com.lplayer;

import android.Manifest;
import android.app.Activity;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.ContentUris;
import android.content.Context;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.drawable.Drawable;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.session.PlaybackState;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.os.RemoteException;
import android.os.SystemClock;
import android.provider.MediaStore;
import android.support.annotation.RequiresApi;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.media.MediaMetadataCompat;
import android.support.v4.media.session.MediaControllerCompat;
import android.support.v4.media.session.PlaybackStateCompat;
import android.view.Display;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.SeekBar;
import android.widget.TextView;

import com.lplayer.service.LPlayerService;
import com.lplayer.service.MusicRepository;
import com.lplayer.values.LibraryName;

import java.io.IOException;
import java.security.Provider;
import java.util.Timer;
import java.util.TimerTask;

import static android.support.v4.media.session.PlaybackStateCompat.*;
import static com.lplayer.values.LibraryName.PLAYLISTS;

public class PlayerActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {
    LPlayerService.LPlayerServiceBinder playerServiceBinder;
    MediaControllerCompat mediaController;
    private MediaPlayer mediaPlayer;
    private long trackId;
    private long listId;
    private int listType;
    private SeekBar songTime;
    private Timer timer;
    int currentPosition;
    private MyBroadRequestReceiver receiver;
    ServiceConnection serviceConnection;
    private static final String GET_POSITION = "GET_POSITION" ;
    public static Activity fa;
    public static boolean play=false;
    public static int currTracksId;
    TextView currentTrack;
    TextView currentArtist;
    TextView currSongTime;
    TextView songDuration;

    @Override
    protected void onResume() {
        if (play) {
            LPlayerService.playerService.playTrackById(currTracksId);
            trackId=currTracksId;
            play=false;
        }
        super.onResume();
    }



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        fa=this;
        if (ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.READ_EXTERNAL_STORAGE)!= PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(this,new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},1998);
        }
        setContentView(R.layout.activity_player);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();
        songTime = (SeekBar) findViewById(R.id.song_time);
        currentTrack = (TextView) findViewById(R.id.current_song_name);
        currentTrack.setSelected(true);
        currentArtist = (TextView) findViewById(R.id.current_song_artist);
        songDuration=findViewById(R.id.songDuration);
        currSongTime=findViewById(R.id.currentSongTime);
        final ImageButton playButton = (ImageButton) findViewById(R.id.play_button);
        IntentFilter filter = new IntentFilter(GET_POSITION);
        receiver = new MyBroadRequestReceiver();
        registerReceiver(receiver, filter);
        Intent serviceIntent = new Intent(this, LPlayerService.class);

        stopService(serviceIntent);
        Intent intent = getIntent();
        trackId = intent.getLongExtra("trackId", -1);
        listId = intent.getLongExtra("idList", -1);
        listType = intent.getIntExtra("typeList", -1);
        serviceIntent.putExtra("trackId", trackId);
        serviceIntent.putExtra("idList", listId);
        serviceIntent.putExtra("typeList", listType);
        if (serviceConnection == null) {
            serviceConnection = new ServiceConnection() {
                @Override
                public void onServiceConnected(ComponentName name, IBinder service) {
                    playerServiceBinder = (LPlayerService.LPlayerServiceBinder) service;
                    try {
                        if (playerServiceBinder.get().getMediaSessionStatus()) {
                            playerServiceBinder.get().releaseMediaSession();
                        }
                        mediaController = new MediaControllerCompat(PlayerActivity.this, playerServiceBinder.getMediaSessionToken());
                        mediaController.registerCallback(new MediaControllerCompat.Callback() {
                            @Override
                            public void onPlaybackStateChanged(PlaybackStateCompat state) {
                                if (state == null)
                                    return;
                                boolean playing = state.getState() == STATE_PLAYING;
                                boolean changing = ((state.getState() == STATE_SKIPPING_TO_NEXT) || (state.getState() == STATE_SKIPPING_TO_PREVIOUS));
                                if (changing) {
                                    currentTrack.setText(mediaController.getMetadata().getString(MediaMetadataCompat.METADATA_KEY_TITLE));
                                    currentArtist.setText(mediaController.getMetadata().getString(MediaMetadataCompat.METADATA_KEY_ARTIST));
                                    long minutes=mediaController.getMetadata().getLong(MediaMetadataCompat.METADATA_KEY_DURATION)/60000;
                                    long seconds=(int) mediaController.getMetadata().getLong(MediaMetadataCompat.METADATA_KEY_DURATION)%60000;
                                    if (seconds>100){
                                        seconds=seconds/1000;
                                    }
                                    if (seconds>=10)
                                        songDuration.setText(minutes+":"+seconds);
                                    else
                                        songDuration.setText(minutes+":0"+seconds);
                                    songTime.setMax((int) mediaController.getMetadata().getLong(MediaMetadataCompat.METADATA_KEY_DURATION));
                                }
                                if (playing) {
                                    //startTimer();
                                }
                            }
                        });
                    } catch (RemoteException e) {
                        mediaController = null;
                    }

                    currentTrack.setText(mediaController.getMetadata().getString(MediaMetadataCompat.METADATA_KEY_TITLE));
                    currentArtist.setText(mediaController.getMetadata().getString(MediaMetadataCompat.METADATA_KEY_ARTIST));
                    long minutes=mediaController.getMetadata().getLong(MediaMetadataCompat.METADATA_KEY_DURATION)/60000;
                    long seconds=(int) mediaController.getMetadata().getLong(MediaMetadataCompat.METADATA_KEY_DURATION)%60000;
                    if (seconds>100){
                        seconds=seconds/1000;
                    }
                    if (seconds>=10)
                        songDuration.setText(minutes+":"+seconds);
                    else
                        songDuration.setText(minutes+":0"+seconds);
                    songTime.setMax((int) mediaController.getMetadata().getLong(MediaMetadataCompat.METADATA_KEY_DURATION));
                }

                @Override
                public void onServiceDisconnected(ComponentName name) {
                    playerServiceBinder = null;
                    mediaController = null;
                }
            };
        }
        bindService(serviceIntent, serviceConnection, BIND_AUTO_CREATE);
//        if (play)
//            lPlayerService.playTrackById(currTrackId);
        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
        /*
        MusicRepository musicRepository=new MusicRepository(this,listType,listId,trackId);

        Track currTrack=musicRepository.getCurrentTrack();
        Uri playTrack=currTrack.getTrackUri();
        startMP(playTrack);
        startTimer();*/

      /*  String[] projective={
                MediaStore.Audio.Media._ID,
                MediaStore.Audio.Media.TITLE,
                MediaStore.Audio.Media.ARTIST,
                MediaStore.Audio.Media.DURATION
        };

        String selection="_id = " + trackId;
        CursorLoader cursorLoader=new CursorLoader(this, MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,projective,selection,null,null);
        Cursor cursor=cursorLoader.loadInBackground();
        cursor.moveToFirst();
        */

   //     songTime.setMax((int)mediaController.getMetadata().getLong(MediaMetadataCompat.METADATA_KEY_DURATION));
        songTime.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                mediaController.getTransportControls().seekTo(seekBar.getProgress());
                currentPosition=seekBar.getProgress();
            }
        });

        //currentTrack.setText(mediaController.getMetadata().getString(MediaMetadataCompat.METADATA_KEY_TITLE));
      //  currentArtist.setText(mediaController.getMetadata().getString(MediaMetadataCompat.METADATA_KEY_ARTIST));
    }

    @Override
    protected void onDestroy() {
        this.unregisterReceiver(receiver);
        unbindService(serviceConnection);
    //    timer.cancel();
        super.onDestroy();
        //releaseMP();
   // Intent intent=new Intent(PlayerActivity.this,MainActivity.class);
   // startActivity(intent);
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
//            timer.cancel();
            super.onBackPressed();
        }
    }

   /* @Override
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
    }*/

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_gallery) {
            //releaseMP();
            Intent intent=new Intent(PlayerActivity.this,MainActivity.class);
            startActivity(intent);
        } else if (id == R.id.nav_eq) {
            Intent intent=new Intent(PlayerActivity.this,EqActivity.class);
            startActivity(intent);
        } else if (id == R.id.nav_manage) {

        } else if (id == R.id.nav_share) {

        } else if (id == R.id.nav_send) {

        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }


    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public void OnClickPlayButton(View view){
        /*if (mediaPlayer.isPlaying()){
            mediaPlayer.pause();
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                view.setBackground(getDrawable(R.drawable.ic_play));
            }
        } else {
            mediaPlayer.start();
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                view.setBackground(getDrawable(R.drawable.ic_pause));
            }
        }*/
        if (mediaController != null) {
            // startTimer();
            int state = mediaController.getPlaybackState().getState();
            if (state==PlaybackStateCompat.STATE_PLAYING) {
                mediaController.getTransportControls().pause();
                view.setBackground(getDrawable(R.drawable.ic_play));
            }else{
                mediaController.getTransportControls().play();
                view.setBackground(getDrawable(R.drawable.ic_pause));
            }
        }
           // mediaController.getTransportControls().play();
    }

    /*public void OnClickPauseButton(View view){
        if (mediaController != null)
            mediaController.getTransportControls().pause();
    }*/

    public void OnClickNextButton(View view){
        if (mediaController!=null)
            mediaController.getTransportControls().skipToNext();
            currentPosition=0;
    }

    public void OnClickRepeatButton(View view){
        if (mediaController!=null){
            mediaController.getRepeatMode();
        }
       // mediaController.getTransportControls().setRepeatMode();
    }

    public void OnClickPreviousButton(View view){
        if (mediaController!=null)
            mediaController.getTransportControls().skipToPrevious();
            currentPosition=0;
    }

    public void OnClickQuiteButton(View view) {
        Intent intent=new Intent(PlayerActivity.this,QueueActivity.class);
        intent.putExtra("trackId",trackId);
        intent.putExtra("listId",listId);
        intent.putExtra("listType",listType);
        startActivity(intent);
    }

    public void OnClickEqButton(View view) {
        Intent intent=new Intent(PlayerActivity.this,EqActivity.class);
        startActivity(intent);
    }

    public class MyBroadRequestReceiver extends BroadcastReceiver{

        @Override
        public void onReceive(Context context, Intent intent) {

           songTime.setProgress(intent.getIntExtra("time",0));
           long minutes=intent.getIntExtra("time",0)/60000;
           long seconds=intent.getIntExtra("time",0)%60000;
            if (seconds>100){
                seconds=seconds/1000;
            }
            if (seconds>=10)
                currSongTime.setText(minutes+":"+seconds);
            else
                currSongTime.setText(minutes+":0"+seconds);
        }


    }
}
