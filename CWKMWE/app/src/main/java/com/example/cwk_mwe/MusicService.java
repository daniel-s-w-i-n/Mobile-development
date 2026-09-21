package com.example.cwk_mwe;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;

import android.os.Binder;
import android.os.Build;
import android.os.IBinder;
import android.util.Log;


import androidx.core.app.NotificationCompat;



import java.io.File;

public class MusicService extends Service {

    private static final String CHANNEL_ID = "MusicChannel";
    private static final int NOTIFICATION_ID = 1;
    private final String TAG = " MusicService";
    private boolean isPlaying = false;
    private boolean isStopped = false;
    private DownloadCallback callback; //for the time in the song

    private AudioPlayer audioPlayer;

    private int currentSong = 0;
    private static int songNumber = 0;

    private final IBinder binder = new LocalBinder();

    static File music = new File("/storage/self/primary/Music/");
    static File[] tracks = music.listFiles();

    @Override
    public void onCreate() {
        super.onCreate();
        Log.d(TAG, " Starting musicService");
        audioPlayer = new AudioPlayer();
        createNotificationChannel();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(TAG,"On start");
        if ("stopService".equals(intent.getAction())) {
            Log.d(TAG, "Told to stop");
            stopForeground(true);
            audioPlayer.stop();
            stopSelf();
            return START_NOT_STICKY;
        } else if (isPlaying) {
            Log.d(TAG, "Playing already");
            return START_NOT_STICKY;
        }
        Log.d(TAG, "add notification");

        //creates an intent to stop the service

        Intent intentStopService = new Intent(this, MusicService.class);
        intentStopService.setAction("stopService");
        PendingIntent intentEndService = PendingIntent.getService(this,0,intentStopService,PendingIntent.FLAG_IMMUTABLE);

        Notification notification = new NotificationCompat.Builder(this,CHANNEL_ID)
                .setContentTitle("Music Player")
                .setContentText("Playing "+ audioPlayer.getFilePath() + "\nClick to stop")
                .setSmallIcon(R.drawable.ic_launcher_background)
                .setContentIntent(intentEndService)
                .setAutoCancel(true)
                .build();
        startForeground(NOTIFICATION_ID,notification);

        //use callback for progress through song
        isStopped = false;
        new Thread(() -> {
            try{
                isPlaying = true;
                Log.d(TAG, "running thread");
                while(!isStopped) {
                    while (isPlaying) {
                        Thread.sleep(1000);
                        if (callback != null) {
                            callback.onDownloadProgress(audioPlayer.getProgress() / 1000);
                        }
                    }
                    Thread.sleep(1000);
                    Log.d(TAG, "song paused");
                }
                Log.d(TAG, "SONGS complete");
            }catch (InterruptedException e){
                e.printStackTrace();
            }
            Log.d(TAG, "ENDING SERVICE");
            isPlaying = false;
            audioPlayer.stop();
            stopSelf();
        }).start();


        isPlaying = true;
        Log.d(TAG, "start Foreground service");
        return START_NOT_STICKY;
    }

    private void createNotificationChannel(){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = "Music Service";
            String description = "Used for playing Music";
            int importance = NotificationManager.IMPORTANCE_LOW;
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, name, importance);
            channel.setDescription(description);
            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
    }

    public void playFromStart() {
        isPlaying = true;
        audioPlayer.stop();
        Log.d(TAG, "play From start");
        loadAudio(tracks[0].getName(),0,1,tracks.length - 2);
    }


    public class LocalBinder extends Binder {
        MusicService getService() {return MusicService.this;}
    }

    public interface DownloadCallback{ void onDownloadProgress(int progress);}

    public void setCallback(DownloadCallback callback){this.callback = callback;}

    @Override
    public IBinder onBind (Intent intent){
        // TODO: Return the communication channel to the service.
        return binder;
    }

    public void loadAudio(String song, int position, float speed, int numOfSongs) {
        audioPlayer.stop();
        currentSong = position;
        audioPlayer.load("/storage/self/primary/Music/" + song, speed);
        songNumber = numOfSongs;
    }

    private int getNumber() {
        if (currentSong > songNumber - 1) {
            currentSong = 0;
        } else {
            currentSong += 1;
        }
        return currentSong;
    }

    public int getTimestamp(){
        return audioPlayer.getProgress();
    }

    public void setTimestamp(int milliseconds){
        audioPlayer.skipTo(milliseconds);
    }

    public void setPlaybackSpeed(float speed){
        audioPlayer.setPlaybackSpeed(speed);
    }

    public String skipSong(float speed){
        audioPlayer.stop();
        int trackNumber = getNumber();
        audioPlayer.load("" + tracks[trackNumber] , speed);
        return (tracks[trackNumber].getName());
    }

    public void onPause(){
        audioPlayer.pause();
        isPlaying = false;
    }

    public void onPlay(){
        audioPlayer.play();
        isPlaying = true;
    }

    public void onDestroy(){
        super.onDestroy();
        stopForeground(true);
        Log.d(TAG, "destroyed service");
    }

    public void onStop(){
        isPlaying = false;
        isStopped = true;
    }

}