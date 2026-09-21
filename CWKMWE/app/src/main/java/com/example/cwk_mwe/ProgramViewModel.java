package com.example.cwk_mwe;

import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import java.util.ArrayList;

public class ProgramViewModel extends ViewModel {

    private static final MutableLiveData<Integer> backgroundColour = new MutableLiveData<>();
    private static final MutableLiveData<Float> playbackSpeed = new MutableLiveData<>();

    private static Boolean AudioWalkStopped = true;

    public static final MutableLiveData<ArrayList<BookmarkValues>> bookmarks = new MutableLiveData<>();

    //adds the bookmark to the array or creates a new one to store the bookmarks
    public static void setBookmark(int time, String name){
        if (bookmarks.getValue() != null){
            ArrayList<BookmarkValues> newList = bookmarks.getValue();
            newList.add(new BookmarkValues(time,name));
            bookmarks.setValue(newList);
        }else{
            ArrayList<BookmarkValues> newList = new ArrayList<>();
            newList.add(new BookmarkValues(time,name));
            bookmarks.setValue(newList);
        }
    }

    public static MutableLiveData<ArrayList<BookmarkValues>> getBookmark(){
        return bookmarks;
    }
    public static MutableLiveData<Integer> getBackgroundColour() {
        return backgroundColour;
    }

    public static void setBackgroundColour(int colour){
        backgroundColour.setValue(colour);
    }

    public static MutableLiveData<Float> getPlaybackSpeed() {
        return playbackSpeed;
    }

    public static void setPlaybackSpeed(float speed){
        playbackSpeed.setValue(speed);
    }

    public static Boolean getAudioWalkStopped() {
        return AudioWalkStopped;
    }

    public static void setAudioWalkStopped(Boolean value){
        AudioWalkStopped = (value);
    }

}
