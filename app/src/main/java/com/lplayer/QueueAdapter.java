package com.lplayer;

import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.ImageView;
import android.widget.TextView;

/**
 * Created by irly on 14.12.2017.
 */

public class QueueAdapter extends CursorAdapter {

    SharedPreferences sharedPreferences;
    public int itemAudioId;

    public QueueAdapter(Context context, Cursor c) {
        super(context, c,0);
        sharedPreferences=context.getSharedPreferences("options",Context.MODE_PRIVATE);
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        return LayoutInflater.from(context).inflate(R.layout.list_item_queue, parent, false);
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {
        TextView trackName = (TextView) view.findViewById(R.id.track_name);
        TextView artistName = (TextView) view.findViewById(R.id.artist_name);
        ImageView isActive = (ImageView) view.findViewById(R.id.is_active_icon);
        String track=cursor.getString(1);
        trackName.setText(track);
        String artist=cursor.getString(2);
        itemAudioId=cursor.getInt(0);
        artistName.setText(artist);
        isActive.setVisibility(View.INVISIBLE);
        if (sharedPreferences.getInt("idActiveTrack",cursor.getInt(0))==itemAudioId) {
            isActive.setVisibility(View.VISIBLE);
        }
        trackName.setSelected(true);
    }
}
