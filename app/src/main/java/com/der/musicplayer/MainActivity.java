package com.der.musicplayer;

import android.Manifest;
import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.os.Parcelable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.karumi.dexter.Dexter;
import com.karumi.dexter.MultiplePermissionsReport;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.multi.DialogOnAnyDeniedMultiplePermissionsListener;
import com.karumi.dexter.listener.multi.MultiplePermissionsListener;

import java.io.File;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    ListView lv_songs;
    String[] items;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        init();
        runTimePermission();
        ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.RECORD_AUDIO},111);
    }

    public void runTimePermission(){
        Dexter.withContext(this)
                .withPermissions(Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.RECORD_AUDIO)
                .withListener(new MultiplePermissionsListener() {

                    @Override
                    public void onPermissionsChecked(MultiplePermissionsReport multiplePermissionsReport) {
                        displaySongs();
                    }

                    @Override
                    public void onPermissionRationaleShouldBeShown(List<PermissionRequest> list, PermissionToken permissionToken) {
                        permissionToken.continuePermissionRequest();
                    }
                }).check();
    }

    public void displaySongs(){

        //To get the internal SD card you can use (I/System.out: /sdcard)
        String sdcard = System.getenv("EXTERNAL_STORAGE");
        String dirPath2 = String.valueOf(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MUSIC));

        final List<File> songFiles = getSongs(new File(dirPath2));
        items = new String[songFiles.size()];

        for (int i = 0; i < songFiles.size(); i++) {
            items[i] = songFiles.get(i).getName().toString()
                    .replace(".mp3","")
                    .replace(".m4a","");//TODO: .m4a dosen't play
        }

        for (String str : items){
            System.out.println("===========> : " + str);
        }

        CustomAdapter adapter = new CustomAdapter();
        lv_songs.setAdapter(adapter);

        //TODO: while mediaplayer is playing if you click it that mediaplayer again it play over again
        lv_songs.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                String songName = (String) lv_songs.getItemAtPosition(i);
                Intent intent = new Intent(getApplicationContext(), PlayerActivity.class);
                    intent.putExtra("songs", (Serializable) songFiles);
                    intent.putExtra("songname", songName);
                    intent.putExtra("pos", i);
                startActivity(intent);
            }
        });
    }

    public List<File> getSongs(File file){
        List<File> list = new ArrayList<>();
        File[] fileArray = file.listFiles();

        if (fileArray != null){
            for (File singleFile : fileArray){
                if (singleFile.isDirectory() && !singleFile.isHidden()){
                    list.addAll(getSongs(singleFile));
                }else {
                    if (singleFile.getName().endsWith(".mp3") || singleFile.getName().endsWith(".m4a")) {
                        list.add(singleFile);
                    }
                }
            }
        }

        return list;
    }

    private void init(){
        lv_songs = findViewById(R.id.lv_songs);
    }

    class CustomAdapter extends BaseAdapter{

        @Override
        public int getCount() {
            return items.length;
        }

        @Override
        public Object getItem(int i) {
            return null;
        }

        @Override
        public long getItemId(int i) {
            return 0;
        }

        @Override
        public View getView(int i, View view, ViewGroup viewGroup) {
            View myView = getLayoutInflater().inflate(R.layout.list_item, null);
            TextView tv_songName = myView.findViewById(R.id.tv_songName);
            tv_songName.setSelected(true);
            tv_songName.setText(items[i]);
            return myView;
        }
    }

}

