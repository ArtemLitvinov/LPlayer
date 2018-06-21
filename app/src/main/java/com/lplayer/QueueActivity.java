package com.lplayer;

import android.Manifest;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.AdapterView;
import android.widget.ListView;

import com.lplayer.values.LibraryName;

import static com.lplayer.values.LibraryName.TRACKS;

public class QueueActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    long trackId,listId;
    int listType;
    Cursor cursor;
    MediaLibrary mediaLibrary;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.READ_EXTERNAL_STORAGE)!= PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(this,new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},1998);
        }
        setContentView(R.layout.activity_queue);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        ListView queueList=findViewById(R.id.quete_list);

        Intent intent=getIntent();

        trackId=intent.getLongExtra("trackId",0);
        listId=intent.getLongExtra("listId",0);
        listType=intent.getIntExtra("listType",0);

        mediaLibrary=new MediaLibrary(this,true);

        switch (listType) {
            case 0:
                queueList.setAdapter(mediaLibrary.getMediaAdapter(TRACKS));
                break;
            case 1:
                queueList.setAdapter(mediaLibrary.getAlbumsMembers(listId));
                break;
            case 2:
                queueList.setAdapter(mediaLibrary.getAlbumsMembers(listId));
                break;
            case 3:
                queueList.setAdapter(mediaLibrary.getGenresMembers(listId));
                break;
            case 4:
                queueList.setAdapter(mediaLibrary.getPlaylistMembers(listId));
                break;
        }

        queueList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if (id==getSharedPreferences("options",MODE_PRIVATE).getInt("idActiveTrack",0)){
                    Intent intent=new Intent(QueueActivity.this, PlayerActivity.class);
                    startActivity(intent);
                }else{
                    PlayerActivity.play=true;
                    PlayerActivity.currTracksId= (int) id;
                    Intent intent=new Intent(QueueActivity.this, PlayerActivity.class);
                    intent.putExtra("trackId",id);
                    intent.putExtra("idList",listId);
                    intent.putExtra("typeList",listType);
                    startActivity(intent);
                }
            }
        });

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
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

//    @Override
//    public boolean onCreateOptionsMenu(Menu menu) {
//        // Inflate the menu; this adds items to the action bar if it is present.
//        getMenuInflater().inflate(R.menu.quite, menu);
//        return true;
//    }
//
//    @Override
//    public boolean onOptionsItemSelected(MenuItem item) {
//        // Handle action bar item clicks here. The action bar will
//        // automatically handle clicks on the Home/Up button, so long
//        // as you specify a parent activity in AndroidManifest.xml.
//        int id = item.getItemId();
//
//        //noinspection SimplifiableIfStatement
//        if (id == R.id.action_settings) {
//            return true;
//        }
//
//        return super.onOptionsItemSelected(item);
//    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_gallery) {
            Intent intent = new Intent(QueueActivity.this,MainActivity.class);
            startActivity(intent);
        } else if (id == R.id.nav_eq) {
            Intent intent = new Intent(QueueActivity.this, EqActivity.class);
            startActivity(intent);
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }
}
