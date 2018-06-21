package com.lplayer.service;

import com.lplayer.Track;
import android.content.ContentUris;
import android.content.Context;
import android.content.CursorLoader;
import android.database.Cursor;
import android.media.session.MediaSession;
import android.net.Uri;
import android.provider.MediaStore;

import com.lplayer.PlayerActivity;
import com.lplayer.values.LibraryName;

import java.util.ArrayList;

/**
 * Created by IRLY on 28.02.2018.
 */

public class MusicRepository {
    private Context context;
    private ArrayList<Track> currentList;
    private long currentTrack;
    Cursor cursor;
    Track track;
    boolean crutch=false;
    Track crutchTrack;
    public MusicRepository(Context parentContext, int listSource, long listSourceId, long currentTrackId){
        context=parentContext;
        currentList=new ArrayList<Track>();
        switch (listSource) {
            case 0:
                String[] projective={
                    MediaStore.Audio.Media._ID
                };
                CursorLoader cursorLoader=new CursorLoader(context, MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, projective, null, null, null);
                cursorLoader.loadInBackground();
                cursor=cursorLoader.loadInBackground();
                cursor.moveToFirst();
                while (cursor.getLong(0)!=currentTrackId){
                    cursor.moveToNext();
                }
                break;

            case 1:
                projective= new String[]{
                        MediaStore.Audio.Media._ID
                };
                String selection="album_id = " + listSourceId;
                cursorLoader=new CursorLoader(context, MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, projective, selection, null, MediaStore.Audio.Media.TRACK);
                cursor=cursorLoader.loadInBackground();
                cursor.moveToFirst();
                while (cursor.getLong(0)!=currentTrackId){
                    cursor.moveToNext();
                }
                break;

            case 2:
                break;

            case 3:
                projective= new String[]{
                        MediaStore.Audio.Genres.Members._ID,
                        MediaStore.Audio.Genres.Members.AUDIO_ID
                };
                cursorLoader=new CursorLoader(context,MediaStore.Audio.Genres.Members.getContentUri("external",listSourceId), projective, null, null, null);
                cursor=cursorLoader.loadInBackground();
                cursor.moveToFirst();
                while (cursor.getLong(1)!=currentTrackId){
                    cursor.moveToNext();
                }
                break;

            case 4:
                projective= new String[]{
                        MediaStore.Audio.Playlists.Members._ID,
                        MediaStore.Audio.Genres.Members.AUDIO_ID
                };
                cursorLoader=new CursorLoader(context, MediaStore.Audio.Playlists.Members.getContentUri("external", listSourceId), projective, null, null, MediaStore.Audio.Playlists.Members.PLAY_ORDER);
                cursor=cursorLoader.loadInBackground();
                cursor.moveToFirst();
                while (cursor.getLong(1)!=currentTrackId){
                    cursor.moveToNext();
                }
                break;
        }
    }

    public void setTrack(int id){
        cursor.moveToFirst();
        while (cursor.getLong(0)!=id){
            cursor.moveToNext();
        }
    }

    public Track getCurrentTrack(){
            track=new Track(context,cursor.getInt(0));
            return track;
    }

    public void nextTrack(){
        if (!cursor.isLast()){
            cursor.moveToNext();
        }else{
            cursor.moveToFirst();
        }
    }

    public void previousTrack(){
        if (!cursor.isFirst()){
            cursor.moveToPrevious();
        }else{
            cursor.moveToLast();
        }
    }

}
