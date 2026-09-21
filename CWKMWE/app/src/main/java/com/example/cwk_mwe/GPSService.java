package com.example.cwk_mwe;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.location.LocationManager;
import android.os.Binder;
import android.os.Build;
import android.os.IBinder;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;

import java.util.Objects;


public class GPSService extends Service {

    private final String TAG = "GPS Service";
    private static final String CHANNEL_ID = "GPSChannel";

    private static final int NOTIFICATION_ID = 2;
    private boolean isPlaying = false;

    private final IBinder binder = new LocalBinder();

    MusicService musicService;

    private float playbackSpeed = 1;

    private boolean boundToService = false;

    private int cumulativeDistance = 0;

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return binder;
    }

    public void onCreate() {
        super.onCreate();
        createNotificationChannel();
        Log.d(TAG, " Starting GPSService");
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(TAG,"On start");
        if(intent != null && intent.hasExtra("Key Message")) {
            String message = intent.getStringExtra("Key Message");
            if(Objects.equals(message, "STOP")){
                Intent intentStopMusicService = new Intent(GPSService.this, MusicService.class);
                intentStopMusicService.putExtra("Key Message", "STOP");
                stopService(intent);
                musicService.onStop();
                boundToService = false;
                isPlaying = false;
                stopForeground(true);
                stopSelf();
                return START_NOT_STICKY;
            }
        }
        assert intent != null;
        if ("stopService".equals(intent.getAction())) {
            Log.d(TAG, "Told to stop");
            Intent intentStopMusicService = new Intent(GPSService.this, MusicService.class);
            intentStopMusicService.putExtra("Key Message", "STOP");
            stopService(intent);
            ProgramViewModel.setAudioWalkStopped(true);
            musicService.onStop();
            boundToService = false;
            isPlaying = false;
            stopForeground(true);
            stopSelf();
            return START_NOT_STICKY;
        } else if (isPlaying) {
            Log.d(TAG, "Playing already");
            return START_NOT_STICKY;
        }else{
            boundToService = true;
            //starts the musicService from the beginning
            Log.d(TAG , "binding to music service");
            Intent intentFormMusicService = new Intent(GPSService.this, MusicService.class);
            startService(intentFormMusicService);
            bindService(intentFormMusicService, connection, Context.BIND_AUTO_CREATE);

            playbackSpeed = 1;

            LocationManager locationManager =
                    (LocationManager) getSystemService(Context.LOCATION_SERVICE);
            MyLocationListener locationListener = new MyLocationListener();
            try {
                locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 5, 5, locationListener);
            } catch (SecurityException e) {
                Log.d(TAG , e.toString());
            }

            //Intent to relaunch the service with the intent of stopping it
            Intent intentStopService = new Intent(this, GPSService.class);
            intentStopService.setAction("stopService");
            PendingIntent intentEndService = PendingIntent.getService(this,0,intentStopService,PendingIntent.FLAG_IMMUTABLE);

            //Notification for GPS Service
            Notification notification = new NotificationCompat.Builder(this,CHANNEL_ID)
                    .setContentTitle("GPS Player")
                    .setContentText("Click to stop Service")
                    .setSmallIcon(R.drawable.ic_launcher_background)
                    .setContentIntent(intentEndService)
                    .setAutoCancel(true)
                    .build();
            startForeground(NOTIFICATION_ID,notification);


            new Thread(() -> {
                try {
                    isPlaying = true;
                    Log.d(TAG, "running");
                    while (isPlaying) {
                        Thread.sleep(5000);
                        //changes the song every 20 metres
                        cumulativeDistance += locationListener.getDistance();
                        if (cumulativeDistance >= 20 && boundToService) {
                            Log.d(TAG, "CHANGE SONG");
                            musicService.skipSong(playbackSpeed);
                            cumulativeDistance = 0;
                        }
                    }
                    Thread.sleep(1000);
                    Log.d(TAG, "ROUTE complete");
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                Log.d(TAG, "ENDING SERVICE");
                isPlaying = false;
                stopForeground(true);
                musicService.onStop();
                stopSelf();
            }).start();


            return START_NOT_STICKY;
        }
    }

    //creates the notification channel for the notification
    private void createNotificationChannel(){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = "GPS SERVICE";
            String description = "Used for doing an audio walk";
            int importance = NotificationManager.IMPORTANCE_LOW;
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, name, importance);
            channel.setDescription(description);
            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
    }

    public class LocalBinder extends Binder {
        GPSService getService() {return GPSService.this;}
    }



    public void onDestroy(){
        super.onDestroy();
        unbindService(connection);
        stopForeground(true);
    }

    public void onStop(){
        isPlaying = false;
    }

    private ServiceConnection connection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            Log.d(TAG, "onService connected");
            MusicService.LocalBinder binder = (MusicService.LocalBinder) service;
            musicService = binder.getService();
            boundToService = true;
            musicService.playFromStart();
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            Log.d(TAG, "unbound from service");
            boundToService = false;
        }
    };
}

