package com.lplayer.service;

import android.app.Activity;
import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.audiofx.Equalizer;
import android.net.Uri;
import android.net.rtp.AudioStream;
import android.os.Binder;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.media.MediaMetadataCompat;
import android.support.v4.media.session.MediaButtonReceiver;
import android.support.v4.media.session.MediaSessionCompat;
import android.support.v4.media.session.PlaybackStateCompat;

import com.lplayer.PlayerActivity;
import com.lplayer.R;
import com.lplayer.Track;

import java.io.IOException;
import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by irly on 25.02.2018.
 */

public class LPlayerService extends Service {

    private static final String GET_POSITION = "GET_POSITION" ;
    private static final String L = "GET_TRACK_INFO";
    final MediaMetadataCompat.Builder metadataBuilder = new MediaMetadataCompat.Builder();
    MediaPlayer mediaPlayer;
    private int listType;
    private long listId;
    private long trackId;
    private final int NOTIFICATION_ID=404;
    Track currTrack;
    MusicRepository musicRepository;
    Timer progressTimer=new Timer();
    Timer trackInfoTimer=new Timer();
    private int repeatMode=0;
    int bounded=0;
    private Equalizer equalizer;
    private short[] eqProperties= new short[5];
    SharedPreferences sharedPreferences,sharedOption;
    SharedPreferences.OnSharedPreferenceChangeListener sharedPreferenceChangeListener;
    boolean eqChange=false;
    boolean eqCrunch=false;
    static  public LPlayerService playerService;


    LPlayerServiceBinder binder;
    final PlaybackStateCompat.Builder stateBuilder = new PlaybackStateCompat.Builder()
            .setActions(
                    PlaybackStateCompat.ACTION_PLAY
                        | PlaybackStateCompat.ACTION_STOP
                        | PlaybackStateCompat.ACTION_PAUSE
                        | PlaybackStateCompat.ACTION_SKIP_TO_NEXT
                        | PlaybackStateCompat.ACTION_PLAY_PAUSE
                        | PlaybackStateCompat.ACTION_SKIP_TO_PREVIOUS);

    MediaSessionCompat mediaSession;

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        MediaButtonReceiver.handleIntent(mediaSession, intent);
//        trackId=intent.getLongExtra("trackId",-1);
//        listId=intent.getLongExtra("idList",-1);
//        listType=intent.getIntExtra("typeList", -1);
        return super.onStartCommand(intent, flags, startId);
    }


    @Override
    public void onCreate(){
        mediaSession = new MediaSessionCompat(this, "LPlayerService");
        playerService=this;
        mediaSession.setFlags(
                MediaSessionCompat.FLAG_HANDLES_MEDIA_BUTTONS
                | MediaSessionCompat.FLAG_HANDLES_TRANSPORT_CONTROLS);

        mediaSession.setCallback(mediaSessionCallback);
        Context appContext = getApplicationContext();
      //  equalizer=new Equalizer(1,R.integer.app_audio_session_id);
        Intent activityIntent = new Intent (this, PlayerActivity.class);
        mediaSession.setSessionActivity(PendingIntent.getActivities(appContext,0, new Intent[] {activityIntent},0));
        binder=new LPlayerServiceBinder();
        sharedPreferences=getSharedPreferences("eq",MODE_PRIVATE);
        sharedOption=getSharedPreferences("options",MODE_PRIVATE);
        sharedPreferenceChangeListener=new SharedPreferences.OnSharedPreferenceChangeListener() {
            @Override
            public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
                if (key.equals("lowBand")){
                    eqProperties[0]=(short)sharedPreferences.getInt("lowBand",0);
                    equalizer.setBandLevel((short)0,eqProperties[0]);
                }else if (key.equals("lowmiddleBand")){
                    eqProperties[1]=(short)sharedPreferences.getInt("lowmiddleBand",0);
                    equalizer.setBandLevel((short)1,eqProperties[1]);
                }else if (key.equals("middleBand")){
                    eqProperties[2]=(short)sharedPreferences.getInt("middleBand",0);
                    equalizer.setBandLevel((short)2,eqProperties[2]);
                }else if (key.equals("middlehighBand")){
                    eqProperties[3]= (short) sharedPreferences.getInt("middlehighBand",0);
                    equalizer.setBandLevel((short)3,eqProperties[3]);
                }else if (key.equals("highBand")){
                    eqProperties[4]= (short) sharedPreferences.getInt("highBand",0);
                    equalizer.setBandLevel((short) 4,eqProperties[4]);
                }
                eqChange=true;
            }
        };
        sharedPreferences.registerOnSharedPreferenceChangeListener(sharedPreferenceChangeListener);

        eqProperties[0]=(short)sharedPreferences.getInt("lowBand",0);
        eqProperties[1]=(short)sharedPreferences.getInt("lowmiddleBand",0);
        eqProperties[2]=(short)sharedPreferences.getInt("middleBand",0);
        eqProperties[3]=(short)sharedPreferences.getInt("middlehighBand",0);
        eqProperties[4]=(short)sharedPreferences.getInt("highBand",0);
        super.onCreate();
    }

    @Override
    public void onDestroy(){
        mediaPlayer.stop();
        mediaPlayer.release();
        mediaSession.release();
        equalizer.setEnabled(false);
        super.onDestroy();
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        if (!mediaSession.isActive()){
            mediaSession = new MediaSessionCompat(this,"LPlayerService");
        }
        mediaSession.setFlags(
                MediaSessionCompat.FLAG_HANDLES_MEDIA_BUTTONS
                        | MediaSessionCompat.FLAG_HANDLES_TRANSPORT_CONTROLS);
        mediaSession.setCallback(mediaSessionCallback);
        Context appContext = getApplicationContext();
        Intent activityIntent = new Intent (this, PlayerActivity.class);
        mediaSession.setSessionActivity(PendingIntent.getActivities(appContext,0, new Intent[] {activityIntent},0));
        if (trackId!=intent.getLongExtra("trackId",-1)) {
            trackId = intent.getLongExtra("trackId", -1);
            listId = intent.getLongExtra("idList", -1);
            listType = intent.getIntExtra("typeList", -1);
            musicRepository = new MusicRepository(this, listType, listId, trackId);
            currTrack = musicRepository.getCurrentTrack();
            MediaMetadataCompat metadata = metadataBuilder
                    .putString(MediaMetadataCompat.METADATA_KEY_TITLE, currTrack.getTrackName())
                    .putString(MediaMetadataCompat.METADATA_KEY_ALBUM, currTrack.getArtistName())
                    .putString(MediaMetadataCompat.METADATA_KEY_ARTIST, currTrack.getArtistName())
                    .putLong(MediaMetadataCompat.METADATA_KEY_DURATION, currTrack.getTrackDuration())
                    .build();
//                  .putBitmap(MediaMetadataCompat.METADATA_KEY_ART,
//                    BitmapFactory.decodeResource(getResources(), track.getBitmapResId()));
            mediaSession.setMetadata(metadata);
            Intent mediaButtonIntent = new Intent(Intent.ACTION_MEDIA_BUTTON, null, getApplicationContext(), MediaButtonReceiver.class);
            mediaSession.setMediaButtonReceiver(PendingIntent.getBroadcast(getApplicationContext(), 0, mediaButtonIntent, 0));
            mediaSessionCallback.onPlay();
            
            bounded++;
        }
        return binder;
    }

    @Override
    public void onRebind(Intent intent) {
        mediaPlayer.release();
        super.onRebind(intent);
    }

    @Override
    public boolean onUnbind(Intent intent) {
        progressTimer.cancel();
        bounded--;
        return super.onUnbind(intent);
    }

    public void releaseMediaSession(){
        if (bounded==0) {
            mediaPlayer.release();
            mediaSession.release();
        }
    }

    public boolean getMediaSessionStatus(){
        return mediaSession.isActive();
    }

    MediaSessionCompat.Callback mediaSessionCallback = new MediaSessionCompat.Callback() {
        @Override
        public void onPlay() {
            if (mediaPlayer==null) {
                startService(new Intent(getApplicationContext(), LPlayerService.class));
                currTrack = musicRepository.getCurrentTrack();
                MediaMetadataCompat metadata = metadataBuilder
                        .putString(MediaMetadataCompat.METADATA_KEY_TITLE, currTrack.getTrackName())
                        .putString(MediaMetadataCompat.METADATA_KEY_ALBUM, currTrack.getArtistName())
                        .putString(MediaMetadataCompat.METADATA_KEY_ARTIST, currTrack.getArtistName())
                        .putLong(MediaMetadataCompat.METADATA_KEY_DURATION, currTrack.getTrackDuration())
                        .build();
//                    .putBitmap(MediaMetadataCompat.METADATA_KEY_ART,
//                            BitmapFactory.decodeResource(getResources(), track.getBitmapResId()));
                mediaSession.setMetadata(metadata);
                mediaSession.setActive(true);
                SharedPreferences.Editor editor=sharedOption.edit();
                editor.putInt("idActiveTrack",currTrack.getId());
                editor.putInt("idActiveTrack",currTrack.getId());
                editor.apply();
                mediaSession.setPlaybackState(
                        stateBuilder.setState(PlaybackStateCompat.STATE_PLAYING,
                                PlaybackStateCompat.PLAYBACK_POSITION_UNKNOWN, 1).build());
                Uri playTrack = currTrack.getTrackUri();
                try {
                    mediaPlayer=new MediaPlayer();
                    mediaPlayer.setAudioSessionId((int)R.integer.app_audio_session_id);
                    mediaPlayer.setDataSource(LPlayerService.this, playTrack);
                    mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
                    mediaPlayer.prepare();
                    mediaPlayer.start();
                    if(equalizer==null) {
                        equalizer = new Equalizer(1, R.integer.app_audio_session_id);
                    }
                    equalizer.setEnabled(true);
                    equalizer.setBandLevel((short)0,eqProperties[0]);
                    equalizer.setBandLevel((short)1,eqProperties[1]);
                    equalizer.setBandLevel((short)2,eqProperties[2]);
                    equalizer.setBandLevel((short)3,eqProperties[3]);
                    equalizer.setBandLevel((short)4,eqProperties[4]);
                    progressTimer.scheduleAtFixedRate(new TimerTask() {
                        @Override
                        public void run() {
                            Intent broadcastIntent = new Intent();
                            broadcastIntent.putExtra("time",mediaPlayer.getCurrentPosition());
                            broadcastIntent.setAction(GET_POSITION);
                            sendBroadcast(broadcastIntent);
                            if (eqChange){
                                equalizer.setBandLevel((short)0,eqProperties[0]);
                                equalizer.setBandLevel((short)1,eqProperties[1]);
                                equalizer.setBandLevel((short)2,eqProperties[2]);
                                equalizer.setBandLevel((short)3,eqProperties[3]);
                                equalizer.setBandLevel((short)4,eqProperties[4]);
                                eqChange=false;
                            }
                        }
                    },0,500);

                } catch (IOException e) {
                    e.printStackTrace();
                }
            }else{
                if (mediaSession.getController().getPlaybackState().getState() == PlaybackStateCompat.STATE_PAUSED) {
                    mediaPlayer.start();
                    mediaSession.setPlaybackState(
                            stateBuilder.setState(PlaybackStateCompat.STATE_PLAYING,
                                    PlaybackStateCompat.PLAYBACK_POSITION_UNKNOWN, 1).build());
                }else{
                    mediaPlayer.stop();
                    mediaPlayer.release();
                    currTrack = musicRepository.getCurrentTrack();
                    MediaMetadataCompat metadata = metadataBuilder
                            .putString(MediaMetadataCompat.METADATA_KEY_TITLE, currTrack.getTrackName())
                            .putString(MediaMetadataCompat.METADATA_KEY_ALBUM, currTrack.getArtistName())
                            .putString(MediaMetadataCompat.METADATA_KEY_ARTIST, currTrack.getArtistName())
                            .putLong(MediaMetadataCompat.METADATA_KEY_DURATION, currTrack.getTrackDuration())
                            .build();
//                    .putBitmap(MediaMetadataCompat.METADATA_KEY_ART,
//                            BitmapFactory.decodeResource(getResources(), track.getBitmapResId()));
                    mediaSession.setMetadata(metadata);
                    mediaSession.setActive(true);
                    mediaSession.setPlaybackState(
                            stateBuilder.setState(PlaybackStateCompat.STATE_PLAYING,
                                    PlaybackStateCompat.PLAYBACK_POSITION_UNKNOWN, 1).build());
                    Uri playTrack = currTrack.getTrackUri();
                    try {
                        mediaPlayer=new MediaPlayer();
                        mediaPlayer.setDataSource(LPlayerService.this, playTrack);
                        mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
                        mediaPlayer.prepare();
                        mediaPlayer.start();
                        progressTimer.scheduleAtFixedRate(new TimerTask() {
                            @Override
                            public void run() {
                                Intent broadcastIntent = new Intent();
                                broadcastIntent.putExtra("time",mediaPlayer.getCurrentPosition());
                                broadcastIntent.setAction(GET_POSITION);
                                sendBroadcast(broadcastIntent);
                            }
                        },0,100);

                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
            refreshNotificationAndForegroundStatus(PlaybackStateCompat.STATE_PLAYING);
        }

        @Override
        public void onPause() {
            mediaPlayer.pause();
            mediaSession.setPlaybackState(
                    stateBuilder.setState(PlaybackStateCompat.STATE_PAUSED,
                            PlaybackStateCompat.PLAYBACK_POSITION_UNKNOWN, 1).build());
            refreshNotificationAndForegroundStatus(PlaybackStateCompat.STATE_PAUSED);
//            progressTimer.cancel();
        }

        @Override
        public void onSkipToNext() {
            musicRepository.nextTrack();
            mediaPlayer.release();
            mediaPlayer=new MediaPlayer();
            mediaPlayer.setAudioSessionId((int)R.integer.app_audio_session_id);
            currTrack=musicRepository.getCurrentTrack();
            MediaMetadataCompat metadata = metadataBuilder
                    .putString(MediaMetadataCompat.METADATA_KEY_TITLE, currTrack.getTrackName())
                    .putString(MediaMetadataCompat.METADATA_KEY_ALBUM, currTrack.getArtistName())
                    .putString(MediaMetadataCompat.METADATA_KEY_ARTIST, currTrack.getArtistName())
                    .putLong(MediaMetadataCompat.METADATA_KEY_DURATION, currTrack.getTrackDuration())
                    .build();
//                    .putBitmap(MediaMetadataCompat.METADATA_KEY_ART,
//                            BitmapFactory.decodeResource(getResources(), track.getBitmapResId()));
            mediaSession.setMetadata(metadata);
            mediaSession.setPlaybackState(
                    stateBuilder.setState(PlaybackStateCompat.STATE_SKIPPING_TO_NEXT,
                            PlaybackStateCompat.PLAYBACK_POSITION_UNKNOWN, 1).build());
            Uri playTrack=currTrack.getTrackUri();
            try {
                mediaPlayer.setDataSource(LPlayerService.this,playTrack);
                mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
                mediaSession.setPlaybackState(
                        stateBuilder.setState(PlaybackStateCompat.STATE_PLAYING,
                                PlaybackStateCompat.PLAYBACK_POSITION_UNKNOWN, 1).build());
                mediaPlayer.prepare();
                mediaPlayer.start();
                SharedPreferences.Editor editor=sharedOption.edit();
                editor.putInt("idActiveTrack",currTrack.getId());
                editor.apply();
            } catch (IOException e) {
                e.printStackTrace();
            }
            refreshNotificationAndForegroundStatus(mediaSession.getController().getPlaybackState().getState());
        }

        @Override
        public void onSkipToPrevious() {
            if (mediaPlayer.getCurrentPosition()<=2000){
                musicRepository.previousTrack();
                mediaPlayer.release();
                mediaPlayer=new MediaPlayer();
                mediaPlayer.setAudioSessionId((int)R.integer.app_audio_session_id);
                currTrack=musicRepository.getCurrentTrack();
                MediaMetadataCompat metadata = metadataBuilder
                        .putString(MediaMetadataCompat.METADATA_KEY_TITLE, currTrack.getTrackName())
                        .putString(MediaMetadataCompat.METADATA_KEY_ALBUM, currTrack.getArtistName())
                        .putString(MediaMetadataCompat.METADATA_KEY_ARTIST, currTrack.getArtistName())
                        .putLong(MediaMetadataCompat.METADATA_KEY_DURATION, currTrack.getTrackDuration())
                        .build();
//                    .putBitmap(MediaMetadataCompat.METADATA_KEY_ART,
//                            BitmapFactory.decodeResource(getResources(), track.getBitmapResId()));
                mediaSession.setMetadata(metadata);
                mediaSession.setPlaybackState(
                        stateBuilder.setState(PlaybackStateCompat.STATE_SKIPPING_TO_PREVIOUS,
                                PlaybackStateCompat.PLAYBACK_POSITION_UNKNOWN, 1).build());
                Uri playTrack=currTrack.getTrackUri();
                try {
                    mediaPlayer.setDataSource(LPlayerService.this,playTrack);
                    mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
                    mediaSession.setPlaybackState(
                            stateBuilder.setState(PlaybackStateCompat.STATE_PLAYING,
                                    PlaybackStateCompat.PLAYBACK_POSITION_UNKNOWN, 1).build());
                    mediaPlayer.prepare();
                    mediaPlayer.start();
                    SharedPreferences.Editor editor=sharedOption.edit();
                    editor.putInt("idActiveTrack",currTrack.getId());
                    editor.apply();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }else{
                mediaPlayer.seekTo(0);
            }
            refreshNotificationAndForegroundStatus(mediaSession.getController().getPlaybackState().getState());
        }

        @Override
        public void onSetRepeatMode(int repeatMode) {
            super.onSetRepeatMode(repeatMode);
            refreshNotificationAndForegroundStatus(mediaSession.getController().getPlaybackState().getState());
        }

        @Override
        public void onStop() {
            stopSelf();
        }

        @Override
        public void onSeekTo(long pos) {
            mediaPlayer.seekTo((int)pos);
            refreshNotificationAndForegroundStatus(mediaSession.getController().getPlaybackState().getState());
        }
    };

    void refreshNotificationAndForegroundStatus(int playbackState) {
        switch (playbackState) {
            case PlaybackStateCompat.STATE_PLAYING: {
                startForeground(NOTIFICATION_ID, getNotification(playbackState));
                break;
            }
            case PlaybackStateCompat.STATE_PAUSED: {
                // На паузе мы перестаем быть foreground, однако оставляем уведомление,
                // чтобы пользователь мог play нажать
                NotificationManagerCompat.from(LPlayerService.this)
                        .notify(NOTIFICATION_ID, getNotification(playbackState));
                stopForeground(false);
                break;
            }

            default: {
                // Все, можно прятать уведомление
                stopForeground(true);
                break;
            }
        }
    }

    Notification getNotification(int playbackState) {
        // MediaStyleHelper заполняет уведомление метаданными трека.
        // Хелпер любезно написал Ian Lake / Android Framework Developer at Google
        // и выложил здесь: https://gist.github.com/ianhanniballake/47617ec3488e0257325c
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this,MEDIA_SESSION_SERVICE);

        // Добавляем кнопки

        // ...на предыдущий трек
        builder.addAction(
                new NotificationCompat.Action(
                        android.R.drawable.ic_media_previous, getString(R.string.previous),
                        MediaButtonReceiver.buildMediaButtonPendingIntent(
                                this,
                                PlaybackStateCompat.ACTION_SKIP_TO_PREVIOUS)));

        // ...play/pause
        if (playbackState == PlaybackStateCompat.STATE_PLAYING)
            builder.addAction(
                    new NotificationCompat.Action(
                            android.R.drawable.ic_media_pause, getString(R.string.pause),
                            MediaButtonReceiver.buildMediaButtonPendingIntent(
                                    this,
                                    PlaybackStateCompat.ACTION_PLAY_PAUSE)));
        else
            builder.addAction(
                    new NotificationCompat.Action(
                            android.R.drawable.ic_media_play, getString(R.string.play),
                            MediaButtonReceiver.buildMediaButtonPendingIntent(
                                    this,
                                    PlaybackStateCompat.ACTION_PLAY_PAUSE)));

        // ...на следующий трек
        builder.addAction(
                new NotificationCompat.Action(android.R.drawable.ic_media_next, getString(R.string.next),
                        MediaButtonReceiver.buildMediaButtonPendingIntent(
                                this,
                                PlaybackStateCompat.ACTION_SKIP_TO_NEXT)));

        builder.setStyle(new android.support.v4.media.app.NotificationCompat.MediaStyle()
                // В компактном варианте показывать Action с данным порядковым номером.
                // В нашем случае это play/pause.
                .setShowActionsInCompactView(0,1,2)
                // Отображать крестик в углу уведомления для его закрытия.
                // Это связано с тем, что для API < 21 из-за ошибки во фреймворке
                // пользователь не мог смахнуть уведомление foreground-сервиса
                // даже после вызова stopForeground(false).
                // Так что это костыль.
                // На API >= 21 крестик не отображается, там просто смахиваем уведомление.
                .setShowCancelButton(true)
                // Указываем, что делать при нажатии на крестик или смахивании
                .setCancelButtonIntent(
                        MediaButtonReceiver.buildMediaButtonPendingIntent(
                                this,
                                PlaybackStateCompat.ACTION_STOP))
                // Передаем токен. Это важно для Android Wear. Если токен не передать,
                // кнопка на Android Wear будет отображаться, но не будет ничего делать
                .setMediaSession(mediaSession.getSessionToken()));

        builder.setSmallIcon(R.mipmap.ic_launcher);
        builder.setColor(ContextCompat.getColor(this, R.color.colorPrimaryDark));
        builder.setContentTitle(currTrack.getTrackName());
        builder.setContentText(currTrack.getArtistName());
        // Не отображать время создания уведомления. В нашем случае это не имеет смысла
        builder.setShowWhen(false);

        // Это важно. Без этой строчки уведомления не отображаются на Android Wear
        // и криво отображаются на самом телефоне.
        builder.setPriority(NotificationCompat.PRIORITY_HIGH);

        // Не надо каждый раз вываливать уведомление на пользователя
        builder.setOnlyAlertOnce(true);

        return builder.build();
    }

    public void playTrackById(long trackId, long idList, long listType){
        musicRepository = new MusicRepository(this, (int) listType, idList, trackId);
        musicRepository.setTrack((int) trackId);
        mediaPlayer.release();
        mediaPlayer=new MediaPlayer();
        mediaPlayer.setAudioSessionId((int)R.integer.app_audio_session_id);
        currTrack=musicRepository.getCurrentTrack();
        MediaMetadataCompat metadata = metadataBuilder
                .putString(MediaMetadataCompat.METADATA_KEY_TITLE, currTrack.getTrackName())
                .putString(MediaMetadataCompat.METADATA_KEY_ALBUM, currTrack.getArtistName())
                .putString(MediaMetadataCompat.METADATA_KEY_ARTIST, currTrack.getArtistName())
                .putLong(MediaMetadataCompat.METADATA_KEY_DURATION, currTrack.getTrackDuration())
                .build();
//                    .putBitmap(MediaMetadataCompat.METADATA_KEY_ART,
//                            BitmapFactory.decodeResource(getResources(), track.getBitmapResId()));
        mediaSession.setMetadata(metadata);
        mediaSession.setPlaybackState(
                stateBuilder.setState(PlaybackStateCompat.STATE_SKIPPING_TO_NEXT,
                        PlaybackStateCompat.PLAYBACK_POSITION_UNKNOWN, 1).build());
        Uri playTrack=currTrack.getTrackUri();
        try {
            mediaPlayer.setDataSource(LPlayerService.this,playTrack);
            mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
            mediaSession.setPlaybackState(
                    stateBuilder.setState(PlaybackStateCompat.STATE_PLAYING,
                            PlaybackStateCompat.PLAYBACK_POSITION_UNKNOWN, 1).build());
            mediaPlayer.prepare();
            mediaPlayer.start();
            SharedPreferences.Editor editor=sharedOption.edit();
            editor.putInt("idActiveTrack",currTrack.getId());
            editor.apply();
        } catch (IOException e) {
            e.printStackTrace();
        }
        refreshNotificationAndForegroundStatus(mediaSession.getController().getPlaybackState().getState());
    }

    public void playTrackById(long trackId){
        musicRepository.setTrack((int) trackId);
        mediaPlayer.release();
        mediaPlayer=new MediaPlayer();
        mediaPlayer.setAudioSessionId((int)R.integer.app_audio_session_id);
        currTrack=musicRepository.getCurrentTrack();
        MediaMetadataCompat metadata = metadataBuilder
                .putString(MediaMetadataCompat.METADATA_KEY_TITLE, currTrack.getTrackName())
                .putString(MediaMetadataCompat.METADATA_KEY_ALBUM, currTrack.getArtistName())
                .putString(MediaMetadataCompat.METADATA_KEY_ARTIST, currTrack.getArtistName())
                .putLong(MediaMetadataCompat.METADATA_KEY_DURATION, currTrack.getTrackDuration())
                .build();
//                    .putBitmap(MediaMetadataCompat.METADATA_KEY_ART,
//                            BitmapFactory.decodeResource(getResources(), track.getBitmapResId()));
        mediaSession.setMetadata(metadata);
        mediaSession.setPlaybackState(
                stateBuilder.setState(PlaybackStateCompat.STATE_SKIPPING_TO_NEXT,
                        PlaybackStateCompat.PLAYBACK_POSITION_UNKNOWN, 1).build());
        Uri playTrack=currTrack.getTrackUri();
        try {
            mediaPlayer.setDataSource(LPlayerService.this,playTrack);
            mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
            mediaSession.setPlaybackState(
                    stateBuilder.setState(PlaybackStateCompat.STATE_PLAYING,
                            PlaybackStateCompat.PLAYBACK_POSITION_UNKNOWN, 1).build());
            mediaPlayer.prepare();
            mediaPlayer.start();
            SharedPreferences.Editor editor=sharedOption.edit();
            editor.putInt("idActiveTrack",currTrack.getId());
            editor.apply();
        } catch (IOException e) {
            e.printStackTrace();
        }
        refreshNotificationAndForegroundStatus(mediaSession.getController().getPlaybackState().getState());
    }

    public class LPlayerServiceBinder extends Binder{
        public MediaSessionCompat.Token getMediaSessionToken() {
            return mediaSession.getSessionToken();
        }

        public LPlayerService get(){
            return LPlayerService.this;
        }
    }

    public int getListType(){
        return listType;
    }
}


