package com.example.cwk_mwe;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.lifecycle.Observer;



public class MusicPlayer extends AppCompatActivity {

    private MusicService musicService;
    private boolean boundToService = true;
    private float playbackSpeed;
    private TextView progressText;

    private final String TAG = "MusicPlayer Activity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        Log.d(TAG, "musicPlayer Started");

        //check for song name in intent

        TextView songName = findViewById(R.id.textSong);
        Intent intent = getIntent();
        if (intent != null && intent.hasExtra("Key Message")) {
            String message = intent.getStringExtra("Key Message");
            songName.setText(message);
        }

        Button playButton = findViewById(R.id.buttonPlay);
        Button pauseButton = findViewById(R.id.buttonPause);
        Button skipButton = findViewById(R.id.buttonSkip);
        Button trackButton = findViewById(R.id.buttonTrack);
        Button stopButton = findViewById(R.id.buttonStop);
        Button bookmarkButton = findViewById(R.id.buttonBookmark);
        progressText = findViewById(R.id.textProgress);


        playButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (boundToService) {
                    musicService.onPlay();
                }
            }
        });

        pauseButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (boundToService) {
                    musicService.onPause();
                }
            }
        });

        skipButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (boundToService) {
                    String nameOfSong = musicService.skipSong(playbackSpeed);
                    songName.setText(nameOfSong);
                }
            }
        });

        bookmarkButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ProgramViewModel.setBookmark(musicService.getTimestamp(),(String)songName.getText());
            }
        });


        trackButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MusicPlayer.this, MainActivity.class);
                startActivity(intent);
            }
        });

        stopButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (boundToService) {
                    musicService.onStop();
                    unbindService(connection);
                    boundToService = false;
                }
            }
        });

        ProgramViewModel.getPlaybackSpeed().observe(this, new Observer<Float>() {
            @Override
            public void onChanged(Float newPlaybackSpeed) {
                playbackSpeed = newPlaybackSpeed;
                TextView songSpeed = findViewById(R.id.textViewPlayback);
                String songSpeedText = "Song Speed is: " +playbackSpeed;
                songSpeed.setText(songSpeedText);
            }
        });

        ProgramViewModel.getBackgroundColour().observe(this, new Observer<Integer>() {
            @Override
            public void onChanged(Integer colour) {
                findViewById(R.id.main).setBackgroundColor(colour);
            }
        });
    }



    public void onStart(){
        Log.d(TAG, "binding to music service");
        super.onStart();
        if (!ProgramViewModel.getAudioWalkStopped()) {
            boundToService = false;
        }
        Intent intent = new Intent(MusicPlayer.this,MusicService.class);
        bindService(intent,connection, Context.BIND_AUTO_CREATE);

    }

    public void onStop(){
        Log.d(TAG, "unbinding to music service");
        super.onStop();
        if(boundToService || !ProgramViewModel.getAudioWalkStopped()){
            unbindService(connection);
        }
    }

    private ServiceConnection connection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            Log.d(TAG, "onService connected");
            MusicService.LocalBinder binder = (MusicService.LocalBinder) service;
            musicService = binder.getService();

            musicService.setCallback(progress -> {
                runOnUiThread(()-> {
                    String progressMessage = "seconds into song : " +progress;
                    progressText.setText(progressMessage);
                });
            });
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            Log.d(TAG, "unbound from service");
            boundToService = false;
        }
    };



}