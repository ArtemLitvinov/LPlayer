package com.lplayer;

import android.content.ContentUris;
import android.content.Context;
import android.content.CursorLoader;
import android.database.Cursor;
import android.net.Uri;
import android.provider.MediaStore;

/**
 * Created by User29 on 02.03.2018.
 */

public class Track{
private Context context;
private String trackName;
private String artistName;
private Uri albumCover;
private Long trackDuration;
private Uri trackUri;

        private int id;

        public Track(Context trackContext,int id){
        context=trackContext;
        String[] projective={
                MediaStore.Audio.Media._ID,
                MediaStore.Audio.Media.TITLE,
                MediaStore.Audio.Media.ARTIST,
                MediaStore.Audio.Media.ALBUM_ID,
                MediaStore.Audio.Media.DURATION
        };
        trackUri=ContentUris.withAppendedId(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, id);
        String selection="_id = " + id;
        CursorLoader cursorLoader=new CursorLoader(context, MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, projective, selection,null,null);
        Cursor cursor=cursorLoader.loadInBackground();
        cursor.moveToFirst();
        trackName=cursor.getString(1);
        artistName=cursor.getString(2);
        trackDuration=cursor.getLong(4);
        this.id=cursor.getInt(0);
        /*    projective = new String[]{
                    MediaStore.Audio.Albums._ID,
                    MediaStore.Audio.Albums.ALBUM_ART
            };
            selection="_id = " + cursor.getString(3);
            cursorLoader=new CursorLoader(context,MediaStore.Audio.Albums.EXTERNAL_CONTENT_URI, projective, selection, null,null);
            cursor=cursorLoader.loadInBackground();
            cursor.moveToFirst();
            albumCover = Uri.parse(cursor.getString(1));
            */
        }

public String getTrackName() {
        return trackName;
        }

public String getArtistName() {
        return artistName;
        }

public Uri getAlbumCover() {
        return albumCover;
        }

public Long getTrackDuration() {
        return trackDuration;
        }

public Uri getTrackUri() {
        return trackUri;
        }

public int getId() {
                return id;
        }

}

