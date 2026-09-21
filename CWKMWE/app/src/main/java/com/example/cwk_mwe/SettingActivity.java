package com.example.cwk_mwe;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.graphics.Color;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.TextView;

import androidx.lifecycle.Observer;
import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class SettingActivity extends AppCompatActivity {

    private MusicService musicService;
    private final String TAG = "Setting Activity";
    private float playbackSpeed = (float)0.0;

    private GPSService audioWalkService;

    private Boolean audioWalkStopped;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_setting);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        Log.d(TAG, "Starting SettingActivity");

        audioWalkStopped = ProgramViewModel.getAudioWalkStopped();
        Button returnButton = findViewById(R.id.buttonReturn);

        Button greenBg = findViewById(R.id.buttonGreen);
        Button whiteBg = findViewById(R.id.buttonWhite);
        Button blueBg = findViewById(R.id.buttonBlue);
        Button redBg = findViewById(R.id.buttonRed);
        SeekBar playbackSeekbar = findViewById((R.id.seekBarPlayback));
        TextView songSpeed = findViewById(R.id.textSongSpeed);
        Button audioWalkButton = findViewById(R.id.buttonGPS);

        playbackSeekbar.setMin(1);
        playbackSeekbar.setMax(4);
        playbackSeekbar.setProgress(2);

        //sets the playback speed depending what the progress bar is

        playbackSeekbar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (progress >= 3) {
                    progress += progress - 2;
                }
                String textOfPlayback = "Speed Of Song : " + ((float) progress / 2);
                songSpeed.setText(textOfPlayback);
                if (musicService != null) {
                    musicService.setPlaybackSpeed((float) progress / 2);
                }
                playbackSpeed = ((float) progress / 2);
                ProgramViewModel.setPlaybackSpeed(playbackSpeed);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

        // returns to the Track display activity

        returnButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                audioWalkStopped = ProgramViewModel.getAudioWalkStopped();
                /*if(!audioWalkStopped){
                    Intent intentGPSStop = new Intent(SettingActivity.this, GPSService.class);
                    intentGPSStop.putExtra("Key Message", "STOP");
                    stopService(intentGPSStop);
                    audioWalkService.onStop();
                }
                ProgramViewModel.setAudioWalkStopped(true);*/

                Intent intent = new Intent(SettingActivity.this, MainActivity.class);
                startActivity(intent);
            }
        });

        //watches for track speed change

        ProgramViewModel.getPlaybackSpeed().observe(this, new Observer<Float>() {
            @Override
            public void onChanged(Float newPlaybackspeed) {
                playbackSpeed = newPlaybackspeed;
                if(playbackSpeed ==0.0){playbackSeekbar.setProgress(2);}
                String songSpeedText = "Song Speed is: " +playbackSpeed;
                songSpeed.setText(songSpeedText);
                playbackSeekbar.setProgress((int)playbackSpeed + 1);
            }
        });

        //watches for background speed change

        ProgramViewModel.getBackgroundColour().observe(this, new Observer<Integer>() {
            @Override
            public void onChanged(Integer colour) {
                findViewById(R.id.main).setBackgroundColor(colour);
            }
        });

        redBg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ProgramViewModel.setBackgroundColour(Color.parseColor("#FF0000"));
            }
        });

        whiteBg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ProgramViewModel.setBackgroundColour(Color.parseColor("#FFFFFF"));
            }
        });

        blueBg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ProgramViewModel.setBackgroundColour(Color.parseColor("#7799FF"));
            }
        });

        greenBg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ProgramViewModel.setBackgroundColour(Color.parseColor("#00FF00"));
            }
        });

        audioWalkButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view){
                audioWalkStopped = ProgramViewModel.getAudioWalkStopped();
                if(audioWalkStopped == true) {
                    //starts the GPS Service and binds to it
                    ProgramViewModel.setAudioWalkStopped(false);
                    String newMessage = "Audio Walk : on";
                    audioWalkButton.setText(newMessage);
                    Intent intent = new Intent(SettingActivity.this, GPSService.class);
                    intent.putExtra("Key Message", "STARTED");
                    startService(intent);
                    bindService(intent, connectionGPS, Context.BIND_AUTO_CREATE);//have to declare in manifest
                }else{
                    //stops the service and tells the viewmodel
                    Intent intent = new Intent(SettingActivity.this, GPSService.class);
                    intent.setAction("stopService");
                    stopService(intent);
                    String newMessage = "Audio Walk : off";
                    audioWalkButton.setText(newMessage);
                    audioWalkService.onStop();
                    ProgramViewModel.setAudioWalkStopped(true);
                }
            }
        });
    }

        public void onStart(){
            Log.d(TAG, "binding to service");
            super.onStart();
            Intent intent = new Intent(SettingActivity.this,MusicService.class);
            bindService(intent,connection, Context.BIND_AUTO_CREATE);
            audioWalkStopped = ProgramViewModel.getAudioWalkStopped();
            if(!audioWalkStopped){
                Intent intentStart = new Intent(SettingActivity.this, GPSService.class);
                intentStart.putExtra("Key Message", "STARTED");
                bindService(intentStart,connectionGPS, Context.BIND_AUTO_CREATE);
            }
        }

        public void onStop(){

            Log.d(TAG, "unbinding from service");
            super.onStop();
            unbindService(connection);
            if (!audioWalkStopped) {
                unbindService(connectionGPS);
            }
        }

        //Gets the connection with the musicService
        private ServiceConnection connection = new ServiceConnection() {
            @Override
            public void onServiceConnected(ComponentName name, IBinder service) {
                Log.d(TAG, " Music Service connected");
                MusicService.LocalBinder binder = (MusicService.LocalBinder) service;
                musicService = binder.getService();
            }

            @Override
            public void onServiceDisconnected(ComponentName name) {
                Log.d(TAG, "unbound from music service");
            }
        };

    //Gets the connection with the GPS Service
    private ServiceConnection connectionGPS = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            Log.d(TAG, "GPS onService connected");
            GPSService.LocalBinder binder = (GPSService.LocalBinder) service;
            audioWalkService = binder.getService();
        }
        @Override
        public void onServiceDisconnected(ComponentName name) {
            Log.d(TAG, "unbound from GPS service");
        }
    };

}