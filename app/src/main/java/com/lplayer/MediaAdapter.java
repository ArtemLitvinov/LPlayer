    package com.lplayer;

    import android.content.Context;
    import android.database.Cursor;
    import android.media.Image;
    import android.net.Uri;
    import android.view.LayoutInflater;
    import android.view.View;
    import android.view.ViewGroup;
    import android.widget.CursorAdapter;
    import android.widget.ImageView;
    import android.widget.TextView;

    import com.lplayer.values.LibraryName;

    import java.io.File;

    /**
     * Created by irly on 01.12.2017.
     */

    final class MediaAdapter extends CursorAdapter {
        public MediaAdapter(Context context, Cursor cursor){
            super(context,cursor,0);
        }

        @Override
        public View newView(Context context, Cursor cursor, ViewGroup parent) {
            return LayoutInflater.from(context).inflate(R.layout.list_item, parent, false);
        }

        @Override
        public void bindView(View view, Context context, Cursor cursor) {
            TextView tvBody = (TextView) view.findViewById(R.id.name);
            String body=cursor.getString(1);
            tvBody.setText(body);
        }
    }
