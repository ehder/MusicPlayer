package com.der.musicplayer;

import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.content.Intent;
import android.graphics.PorterDuff;
import android.media.Image;
import android.media.MediaParser;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.gauravk.audiovisualizer.visualizer.BarVisualizer;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class PlayerActivity extends AppCompatActivity {

    ImageView play, forward, next, rewind, previous, image_view;
    TextView tv_songName,tv_start, tv_stop;
    SeekBar seekMusic;
    BarVisualizer visualizer;

    String sname;
    public static final String EXTRA_NAME = "song_name";
    static MediaPlayer mediaPlayer;
    int position;
    ArrayList<File> mySongs;

    Thread updateSeekBar;

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home){
            onBackPressed();
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onDestroy() {
        if (visualizer != null){
            visualizer.release();
        }
        super.onDestroy();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_player);
        getSupportActionBar().setTitle("Now Playing");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        initial();

        if (mediaPlayer != null){
            mediaPlayer.stop();
            mediaPlayer.release();
        }

        Intent intent = getIntent();
        Bundle bundle = intent.getExtras();

        mySongs = (ArrayList)bundle.getParcelableArrayList("songs");
        String songName = intent.getStringExtra("songname");
        position = bundle.getInt("pos", 0);
        Uri uri = Uri.parse(mySongs.get(position).toString());
        sname = mySongs.get(position).getName();

        tv_songName.setSelected(true);
        tv_songName.setText(sname);

        mediaPlayer = MediaPlayer.create(getApplicationContext(), uri);
        mediaPlayer.start();
        play.setImageResource(R.drawable.ic_pause);

        updateSeekBar = new Thread(){
            @Override
            public void run() {
                super.run();
                int totalDuration = mediaPlayer.getDuration();
                int currentPosition = 0;
                while (currentPosition < totalDuration){
                    try {
                        sleep(500);
                        currentPosition = mediaPlayer.getCurrentPosition();
                        seekMusic.setProgress(currentPosition);

                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        };

        seekMusic.setMax(mediaPlayer.getDuration());
        updateSeekBar.start();
        seekMusic.getProgressDrawable().setColorFilter(getResources().getColor(R.color.av_deep_orange), PorterDuff.Mode.MULTIPLY);
        seekMusic.getThumb().setColorFilter(getResources().getColor(R.color.av_dark_blue), PorterDuff.Mode.SRC_IN);

        seekMusic.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {

            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                mediaPlayer.seekTo(seekBar.getProgress());
            }
        });

        String endTime = createTime(mediaPlayer.getDuration());
        tv_stop.setText(endTime);

        final Handler handler = new Handler();
        final int delay = 1000;

        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                String currentTime = createTime(mediaPlayer.getCurrentPosition());
                tv_start.setText(currentTime);
                handler.postDelayed(this, delay);
            }
        }, delay);


        play.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mediaPlayer.isPlaying()){
                    play.setImageResource(R.drawable.ic_play);
                    mediaPlayer.pause();
                }else {
                    play.setImageResource(R.drawable.ic_pause);
                    mediaPlayer.start();
                }
            }
        });

        //next listener
        mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mediaPlayer) {
                next.performClick();
            }
        });

        int audioSessionId = mediaPlayer.getAudioSessionId();
        if (audioSessionId != -1){
            visualizer.setAudioSessionId(audioSessionId);
        }

        forward.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mediaPlayer.isPlaying()){
                    mediaPlayer.seekTo(mediaPlayer.getCurrentPosition() + 10000);
                }
            }
        });

        rewind.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mediaPlayer.isPlaying()){
                    mediaPlayer.seekTo(mediaPlayer.getCurrentPosition() - 10000);
                }
            }
        });

        next.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mediaPlayer.stop();
                mediaPlayer.release();
                position = (position+1) % mySongs.size();
                Uri u = Uri.parse(mySongs.get(position).toString());
                mediaPlayer = MediaPlayer.create(getApplicationContext(), u);
                sname = mySongs.get(position).getName();
                tv_songName.setText(sname);
                mediaPlayer.start();
                play.setImageResource(R.drawable.ic_pause);
                nextStartAnimation(image_view);

                int audioSessionId = mediaPlayer.getAudioSessionId();
                if (audioSessionId != -1){
                    visualizer.setAudioSessionId(audioSessionId);
                }
            }
        });

        previous.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mediaPlayer.stop();
                mediaPlayer.release();
                position = (position-1) < 0 ? mySongs.size()-1 : position-1;
                Uri u = Uri.parse(mySongs.get(position).toString());
                mediaPlayer = MediaPlayer.create(getApplicationContext(), u);
                sname = mySongs.get(position).getName();
                tv_songName.setText(sname);
                mediaPlayer.start();
                play.setImageResource(R.drawable.ic_pause);
                previousStartAnimation(image_view);

                int audioSessionId = mediaPlayer.getAudioSessionId();
                if (audioSessionId != -1){
                    visualizer.setAudioSessionId(audioSessionId);
                }
            }
        });
    }

    public String createTime(int duration){
        String time = "";
        int min = duration/1000/60;
        int sec = duration/1000%60;
        time+=min+":";
        if (sec < 10 ){
            time += "0";
        }
        time += sec;
        return time;

    }

    public void nextStartAnimation(View view){
        ObjectAnimator animator = ObjectAnimator.ofFloat(image_view, "rotation", 0, 360f);
        animator.setDuration(1000);
        AnimatorSet animatorSet = new AnimatorSet();
        animatorSet.playTogether(animator);
        animatorSet.start();
    }

    public void previousStartAnimation(View view){
        ObjectAnimator animator = ObjectAnimator.ofFloat(image_view, "rotation", 360f, 0);
        animator.setDuration(1000);
        AnimatorSet animatorSet = new AnimatorSet();
        animatorSet.playTogether(animator);
        animatorSet.start();
    }


    private void initial() {
        play = findViewById(R.id.play);
        forward = findViewById(R.id.forward);
        next = findViewById(R.id.next);
        rewind = findViewById(R.id.rewind);
        previous = findViewById(R.id.previous);
        tv_songName = findViewById(R.id.tv_songName);
        tv_start = findViewById(R.id.tv_start);
        tv_stop = findViewById(R.id.tv_stop);
        seekMusic = findViewById(R.id.seek_bar);
        visualizer = findViewById(R.id.bar);
        image_view = findViewById(R.id.img_view);
    }


}