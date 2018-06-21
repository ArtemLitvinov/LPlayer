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

public class TrackAdapter extends CursorAdapter {

    public TrackAdapter(Context context, Cursor c) {
        super(context, c,0);
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        return LayoutInflater.from(context).inflate(R.layout.list_item_track, parent, false);
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {
        TextView trackName = (TextView) view.findViewById(R.id.track_name);
        TextView artistName = (TextView) view.findViewById(R.id.artist_name);
        String track=cursor.getString(1);
        trackName.setText(track);
        String artist=cursor.getString(2);
        artistName.setText(artist);
        trackName.setSelected(true);
    }
}
