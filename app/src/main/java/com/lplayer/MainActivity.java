    package com.lplayer;

import android.Manifest;
import android.content.Context;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.RequiresApi;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CursorAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.lplayer.service.LPlayerService;
import com.lplayer.values.LibraryName;

import static com.lplayer.values.LibraryName.ALBUMS;
import static com.lplayer.values.LibraryName.ARTISTS;
import static com.lplayer.values.LibraryName.GENRES;
import static com.lplayer.values.LibraryName.PLAYLISTS;
import static com.lplayer.values.LibraryName.TRACKS;

    public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener{

    public Button playlistsButton,albumsButton,artistsButton,tracksButton,genresButton;
    
    private int currentList;
    private long listId;
    private final MediaLibrary mediaLibrary= new MediaLibrary(this,false);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.READ_EXTERNAL_STORAGE)!= PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(this,new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},1998);
        }
        setContentView(R.layout.library);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        final ListView listView = (ListView) findViewById(R.id.list);
       // ArrayAdapter<String> catAdapter = new ArrayAdapter<>(this,
       //         R.layout.list_item, R.id.name, catNames);
        listView.setAdapter(mediaLibrary.getMediaAdapter(PLAYLISTS));

        listView.setOnItemClickListener(itemClickListener(PLAYLISTS));
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
        playlistsButton=(Button)findViewById(R.id.playlists_button);
        albumsButton=(Button)findViewById(R.id.albums_button);
        artistsButton=(Button)findViewById(R.id.artists_button);
        tracksButton=(Button)findViewById(R.id.tracks_button);
        genresButton=(Button)findViewById(R.id.genres_button);
        currentList=4;
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

    //@Override
    //public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
    //    getMenuInflater().inflate(R.menu.main, menu);
    //    return true;
    //}

    //@Override
    //public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
    //    int id = item.getItemId();

        //noinspection SimplifiableIfStatement
     //   if (id == R.id.action_settings) {
     //       return true;
     //   }

//        return super.onOptionsItemSelected(item);
//    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

            //if (id == R.id.nav_playlists) {
        //    ListView listView = (ListView)findViewById(R.id.list);
        //    listView.setAdapter(mediaLibrary.getPlaylistAdapter());
        /*} else*/ if (id == R.id.nav_gallery) {
            ListView listView = (ListView)findViewById(R.id.list);
            listView.setAdapter(mediaLibrary.getMediaAdapter(PLAYLISTS));
            listView.setOnItemClickListener(itemClickListener(PLAYLISTS));
        } else if (id == R.id.nav_eq) {
            Intent intent = new Intent(MainActivity.this, EqActivity.class);
            startActivity(intent);
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    public void OnClickPlaylistButton(View v){
        final ListView listView = (ListView)findViewById(R.id.list);
        listView.setAdapter(mediaLibrary.getMediaAdapter(PLAYLISTS));
        listView.setOnItemClickListener(itemClickListener(PLAYLISTS));
    }

    public void OnClickAlbumsButton(View v){
        ListView listView = (ListView)findViewById(R.id.list);
        listView.setAdapter(mediaLibrary.getMediaAdapter(ALBUMS));
        listView.setOnItemClickListener(itemClickListener(ALBUMS));
    }

    public void OnClickTracksButton(View v){
        ListView listView = (ListView)findViewById(R.id.list);
        currentList=0;
        listId=-1;
        listView.setAdapter(mediaLibrary.getMediaAdapter(TRACKS));
        listView.setOnItemClickListener(itemClickListener(TRACKS));
    }

        public void OnClickGenresButton(View v){
        ListView listView = (ListView)findViewById(R.id.list);
        listView.setAdapter(mediaLibrary.getMediaAdapter(GENRES));
        listView.setOnItemClickListener(itemClickListener(GENRES));
    }

    public void OnClickArtistsButton(View v){
        ListView listView = (ListView)findViewById(R.id.list);
        listView.setAdapter(mediaLibrary.getMediaAdapter(ARTISTS));
        listView.setOnItemClickListener(itemClickListener(ARTISTS));
    }

    public AdapterView.OnItemClickListener itemClickListener(final LibraryName libraryName){
        final ListView listView=(ListView) findViewById(R.id.list);
        switch (libraryName){
            case PLAYLISTS:
                return new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                        listView.setAdapter(mediaLibrary.getPlaylistMembers(id));
                        currentList=4;
                        listId=id;
                        listView.setOnItemClickListener(playlistMembersItemListener());
                    }
                };
            case TRACKS:
                return new AdapterView.OnItemClickListener() {
                    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
                    @Override
                    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                        if ((LPlayerService.playerService != null)) {
                            if ((LPlayerService.playerService.getListType() == currentList))
                                LPlayerService.playerService.playTrackById(id);
                            else
                                LPlayerService.playerService.playTrackById(id, listId, currentList);
                        }
                            Intent intent = new Intent(MainActivity.this, PlayerActivity.class);
                            intent.putExtra("trackId", id);
                            intent.putExtra("typeList", currentList);
                            intent.putExtra("idList", listId);
                            startActivity(intent);
                    }
                };
            case ALBUMS:
                return new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                        listView.setAdapter(mediaLibrary.getAlbumsMembers(id));
                        currentList=1;
                        listId=id;
                        listView.setOnItemClickListener(itemClickListener(TRACKS));
                    }
                };
            case GENRES:
                return new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                        listView.setAdapter(mediaLibrary.getGenresMembers(id));
                        currentList=3;
                        listId=id;
                        listView.setOnItemClickListener(itemClickListener(TRACKS));
                    }
                };
            case ARTISTS:
                return new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                        listView.setAdapter(mediaLibrary.getArtistMembers(id));
                        listView.setOnItemClickListener(itemClickListener(ALBUMS));
                    }
                };
        }
        return null;
    }

    public AdapterView.OnItemClickListener playlistMembersItemListener(){
        return new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent intent=new Intent(MainActivity.this, PlayerActivity.class);
                intent.putExtra("trackId",(long)view.getId());
                intent.putExtra("typeList",currentList);
                intent.putExtra("idList",listId);
                startActivity(intent);
            }
        };
    }
    }
