package com.lplayer;

import android.content.Context;
import android.content.CursorLoader;
import android.database.Cursor;
import android.provider.MediaStore;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;

import com.lplayer.values.LibraryName;

import java.security.PublicKey;

import static android.provider.MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;

/**
 * Created by irly on 01.12.2017.
 */

final class MediaLibrary {
    private Context context;
    boolean queue;
    MediaLibrary(Context context, boolean queue){
       this.context=context;
       this.queue=queue;
    }

    public CursorAdapter getMediaAdapter(LibraryName libraryName){
        MediaAdapter mediaAdapter;
        Cursor cursor = null;
        switch (libraryName){
            case ALBUMS:
                return getAlbums();
            case ARTISTS:
                return getArtists();
            case TRACKS:
                return getAllTracks();
            case GENRES:
                return getGenres();
            case PLAYLISTS:
                return getPlaylists();
        }
        mediaAdapter=new MediaAdapter(context,cursor);
        return mediaAdapter;
    }

    private CursorAdapter getAllTracks(){
        String[] projective=new String[]{
                MediaStore.Audio.Media._ID,
                MediaStore.Audio.Media.TITLE,
                MediaStore.Audio.Media.ARTIST
        };
        CursorLoader cursorLoader=new CursorLoader(context, EXTERNAL_CONTENT_URI,projective,null,null,null);
        Cursor cursor=cursorLoader.loadInBackground();
        if (!queue) {
            TrackAdapter trackAdapter = new TrackAdapter(context, cursor);
            return trackAdapter;
        }else {
            QueueAdapter queueAdapter = new QueueAdapter(context, cursor);
            return queueAdapter;
        }
    }

    private AlbumsAdapter getAlbums(){
        String[] projective={
                MediaStore.Audio.Albums._ID,
                MediaStore.Audio.Albums.ALBUM,
                MediaStore.Audio.Albums.ARTIST
        };
        CursorLoader cursorLoader=new CursorLoader(context, MediaStore.Audio.Albums.EXTERNAL_CONTENT_URI,projective,null,null,null);
        Cursor cursor=cursorLoader.loadInBackground();
        cursor.moveToFirst();
        AlbumsAdapter albumsAdapter= new AlbumsAdapter(context,cursor);
        return albumsAdapter;
    }

    private MediaAdapter getArtists(){
        String[] projective = new String[]{
                MediaStore.Audio.Artists._ID,
                MediaStore.Audio.Artists.ARTIST
        };
        CursorLoader cursorLoader = new CursorLoader(context, MediaStore.Audio.Artists.EXTERNAL_CONTENT_URI, projective, null, null, null);
        Cursor cursor=cursorLoader.loadInBackground();
        cursor.moveToFirst();
        MediaAdapter mediaAdapter=new MediaAdapter(context,cursor);
        return mediaAdapter;
    }

    private MediaAdapter getGenres(){
        String[] projective=new String[]{
                MediaStore.Audio.Genres._ID,
                MediaStore.Audio.Genres.NAME
        };
        CursorLoader cursorLoader=new CursorLoader(context,MediaStore.Audio.Genres.EXTERNAL_CONTENT_URI,projective,null,null,null);
        Cursor cursor=cursorLoader.loadInBackground();
        cursor.moveToFirst();
        MediaAdapter mediaAdapter=new MediaAdapter(context,cursor);
        return mediaAdapter;
    }

    private MediaAdapter getPlaylists() {
        String[] projective = {
                MediaStore.Audio.Playlists._ID,
                MediaStore.Audio.Playlists.NAME
        };
        CursorLoader cursorLoader = new CursorLoader(context, MediaStore.Audio.Playlists.EXTERNAL_CONTENT_URI, projective, null, null, null);
        Cursor cursor = cursorLoader.loadInBackground();
        cursor.moveToFirst();
        //TextView t=(TextView) findViewById(R.id.teext);
        //t.setText(cursor.getColumnName(0));
        MediaAdapter playlistAdapter = new MediaAdapter(context, cursor);
        return  playlistAdapter;
    }

    //_________________________________________________________________________________________________

    public PlaylistMembersAdapter getPlaylistMembers(long playlistId) {
        String[] projective = {
                MediaStore.Audio.Playlists.Members._ID,
                MediaStore.Audio.Playlists.Members.TITLE,
                MediaStore.Audio.Playlists.Members.ARTIST,
                MediaStore.Audio.Playlists.Members.AUDIO_ID
        };
        CursorLoader cursorLoader;
        cursorLoader = new CursorLoader(context, MediaStore.Audio.Playlists.Members.getContentUri("external", playlistId), projective, null, null, MediaStore.Audio.Playlists.Members.PLAY_ORDER);
        Cursor cursor = cursorLoader.loadInBackground();
        PlaylistMembersAdapter trackAdapter = new PlaylistMembersAdapter(context, cursor);
        return trackAdapter;
    }

    public CursorAdapter getAlbumsMembers(long albumId){
        String selection="album_id = "+ albumId;
        String[] projective = {
                MediaStore.Audio.Media._ID,
                MediaStore.Audio.Media.TITLE,
                MediaStore.Audio.Media.ARTIST
        };
        CursorLoader cursorLoader = new CursorLoader(context, MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, projective, selection, null, MediaStore.Audio.Media.TRACK);
        Cursor cursor = cursorLoader.loadInBackground();
        if (!queue) {
            TrackAdapter trackAdapter = new TrackAdapter(context, cursor);
            return trackAdapter;
        }else{
            QueueAdapter queueAdapter = new QueueAdapter(context,cursor);
            return queueAdapter;
        }
    }

    public CursorAdapter getGenresMembers(long genreId){
        String[] projective = {
                MediaStore.Audio.Genres.Members._ID,
                MediaStore.Audio.Genres.Members.TITLE,
                MediaStore.Audio.Genres.Members.ARTIST
        };
        CursorLoader cursorLoader=new CursorLoader(context, MediaStore.Audio.Genres.Members.getContentUri("external",genreId),projective,null,null,null);
        Cursor cursor= cursorLoader.loadInBackground();
        if (!queue) {
            TrackAdapter trackAdapter = new TrackAdapter(context, cursor);
            return trackAdapter;
        }else{
            QueueAdapter queueAdapter = new QueueAdapter(context,cursor);
            return queueAdapter;
        }
    }

    public AlbumsAdapter getArtistMembers(long artistsId){
        String[] projective = {
                MediaStore.Audio.Artists._ID,
                MediaStore.Audio.Artists.Albums.ALBUM,
                MediaStore.Audio.Artists.Albums.ARTIST
        };
        CursorLoader cursorLoader=new CursorLoader(context,MediaStore.Audio.Artists.Albums.getContentUri("external",artistsId),projective,null,null,null);
        Cursor cursor=cursorLoader.loadInBackground();
        AlbumsAdapter albumsAdapter=new AlbumsAdapter(context,cursor);
        return albumsAdapter;
    }

}
