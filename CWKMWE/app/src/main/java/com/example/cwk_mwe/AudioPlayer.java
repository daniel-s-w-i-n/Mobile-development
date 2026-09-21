package com.example.cwk_mwe;

/**
 * Created by pszat on 14/07/25
 */

import android.media.AudioAttributes;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.util.Log;
import java.io.IOException;

public class AudioPlayer {

    protected MediaPlayer mediaPlayer;
    protected AudioPlayerState state;
    protected String filePath;

    public enum AudioPlayerState {
        ERROR,
        PLAYING,
        PAUSED,
        STOPPED
    }

    public AudioPlayer() {
        this.state = AudioPlayerState.STOPPED;
    }

    public AudioPlayerState getState() {
        return this.state;
    }

    public void load(String filePath, float speed) {
        this.filePath = filePath;
        mediaPlayer = new MediaPlayer();

        AudioAttributes audioAttributes = new AudioAttributes.Builder()
                .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                .setUsage(AudioAttributes.USAGE_MEDIA)
                .build();
        mediaPlayer.setAudioAttributes(audioAttributes);

        try {
            mediaPlayer.setDataSource(filePath);
            mediaPlayer.setPlaybackParams(mediaPlayer.getPlaybackParams().setSpeed(speed));
            mediaPlayer.prepare();
        } catch (IOException e) {
            Log.e("AudioPlayer", e.toString());
            e.printStackTrace();
            this.state = AudioPlayerState.ERROR;
            return;
        } catch (IllegalArgumentException e) {
            Log.e("AudioPlayer", e.toString());
            e.printStackTrace();
            this.state = AudioPlayerState.ERROR;
            return;
        }

        this.state = AudioPlayerState.PLAYING;
        mediaPlayer.start();
    }

    public String getFilePath() {
        return this.filePath;
    }

    public int getProgress() {
        if (mediaPlayer != null) {
            if (this.state == AudioPlayerState.PAUSED || this.state == AudioPlayerState.PLAYING)
                return mediaPlayer.getCurrentPosition();
        }
        return 0;
    }

    public void play() {
        if (this.state == AudioPlayerState.PAUSED) {
            mediaPlayer.start();
            this.state = AudioPlayerState.PLAYING;
        }
    }

    public void pause() {
        if (this.state == AudioPlayerState.PLAYING) {
            mediaPlayer.pause();
            this.state = AudioPlayerState.PAUSED;
        }
    }

    public void stop() {
        if (mediaPlayer != null) {
            if (mediaPlayer.isPlaying())
                mediaPlayer.stop();
            this.state = AudioPlayerState.STOPPED;
            mediaPlayer.reset();
            mediaPlayer.release();
            mediaPlayer = null;
        }
    }

    public void setPlaybackSpeed(float speed) {
        if (mediaPlayer != null) {
            this.state = AudioPlayerState.PAUSED;
            mediaPlayer.setPlaybackParams(mediaPlayer.getPlaybackParams().setSpeed(speed));
            this.state = AudioPlayerState.PLAYING;
        }
    }

    public void skipTo(int milliseconds) {
        if (mediaPlayer != null && (this.state == AudioPlayerState.PLAYING || this.state == AudioPlayerState.PAUSED)) {
            this.state = AudioPlayerState.PAUSED;
            mediaPlayer.seekTo(milliseconds);
            this.state = AudioPlayerState.PLAYING;
        }
    }
}
