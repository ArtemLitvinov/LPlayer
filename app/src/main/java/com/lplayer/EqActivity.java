package com.lplayer;


import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.audiofx.Equalizer;
import android.os.Build;
import android.os.Bundle;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ListView;
import android.widget.SeekBar;

import static com.lplayer.values.LibraryName.PLAYLISTS;

public class EqActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    Equalizer equalizer;
    SeekBar lowBand,lowmiddleBand,middleBand,middlehighBand,highBand;
    SharedPreferences sharedPreferences;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        sharedPreferences=getSharedPreferences("eq", MODE_PRIVATE);
        setContentView(R.layout.activity_eq);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

//        equalizer=new Equalizer(0,R.integer.app_audio_session_id);
//        equalizer.setEnabled(true);


        lowBand=(SeekBar) findViewById(R.id.low_band);
        lowBand.setMax(3000);
        int lowBandLevel=sharedPreferences.getInt("lowBand",0);
        lowBand.setProgress(lowBandLevel+1500);
        lowBand.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {

            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                SharedPreferences.Editor edit=sharedPreferences.edit();
                edit.putInt("lowBand",seekBar.getProgress()-1500);
                edit.commit();
            }
        });
        //____________________________________________________________
        lowmiddleBand=(SeekBar) findViewById(R.id.lowmiddle_band);
        lowmiddleBand.setMax(3000);
        sharedPreferences=getSharedPreferences("eq", Context.MODE_PRIVATE);
        int lowmiddleBandLevel=sharedPreferences.getInt("lowmiddleBand",0);
        lowmiddleBand.setProgress(lowmiddleBandLevel+1500);
        lowmiddleBand.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {

            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                SharedPreferences.Editor edit=sharedPreferences.edit();
                edit.putInt("lowmiddleBand",seekBar.getProgress()-1500);
                edit.commit();
            }
        });
        //__________________________________________________________________
        middleBand=(SeekBar) findViewById(R.id.middle_band);
        sharedPreferences=getSharedPreferences("eq", Context.MODE_PRIVATE);
        int middleBandLevel=sharedPreferences.getInt("middleBand",0);
        middleBand.setMax(3000);
        middleBand.setProgress(middleBandLevel+1500);
        middleBand.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {

            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                SharedPreferences.Editor edit=sharedPreferences.edit();
                edit.putInt("middleBand",seekBar.getProgress()-1500);
                edit.commit();
            }
        });
        //_________________________________________________________________
        middlehighBand=(SeekBar) findViewById(R.id.middlehigh_band);
        middlehighBand.setMax(3000);
        //middlehighBand.setProgress(equalizer.getBandLevel((short)3)+1500);
//        sharedPreferences=getSharedPreferences("eq", Context.MODE_PRIVATE);
        int middlehighBandLevel=sharedPreferences.getInt("middlehighBand",0);
        middlehighBand.setProgress(middlehighBandLevel+1500);
        middlehighBand.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {

            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                SharedPreferences.Editor edit=sharedPreferences.edit();
                edit.putInt("middlehighBand",seekBar.getProgress()-1500);
                edit.commit();
            }
        });
        //________________________________________________________________
        highBand=(SeekBar) findViewById(R.id.high_band);
        highBand.setMax(3000);
        int highBandLevel=sharedPreferences.getInt("highBand",0);
        highBand.setProgress(highBandLevel+1500);
        highBand.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {

            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                SharedPreferences.Editor edit=sharedPreferences.edit();
                edit.putInt("highBand",seekBar.getProgress()-1500);
                edit.commit();
            }
        });
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

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.eq, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putInt("lowBand",0);
            editor.apply();
            editor.putInt("lowmiddleBand",0);
            editor.apply();
            editor.putInt("middleBand",0);
            editor.apply();
            editor.putInt("middlehighBand",0);
            editor.apply();
            editor.putInt("highBand",0);
            editor.apply();
            lowBand.setProgress(1500);
            lowmiddleBand.setProgress(1500);
            middleBand.setProgress(1500);
            middlehighBand.setProgress(1500);
            highBand.setProgress(1500);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_gallery) {
            Intent intent = new Intent(EqActivity.this,MainActivity.class);
            startActivity(intent);
        } else if (id == R.id.nav_eq) {
            Intent intent = new Intent(EqActivity.this, EqActivity.class);
            startActivity(intent);
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }
}
