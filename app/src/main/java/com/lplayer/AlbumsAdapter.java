package com.lplayer;

import android.content.Context;
import android.database.Cursor;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.TextView;

/**
 * Created by irly on 14.12.2017.
 */

public class AlbumsAdapter extends CursorAdapter {
    public AlbumsAdapter(Context context, Cursor c) {
        super(context, c,0);
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        return LayoutInflater.from(context).inflate(R.layout.list_item_track, parent, false);
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {
        TextView albumName = (TextView) view.findViewById(R.id.track_name);
        TextView artistName = (TextView) view.findViewById(R.id.artist_name);
        String album=cursor.getString(1);
        albumName.setText(album);
        String artist=cursor.getString(2);
        artistName.setText(artist);
    }
}
