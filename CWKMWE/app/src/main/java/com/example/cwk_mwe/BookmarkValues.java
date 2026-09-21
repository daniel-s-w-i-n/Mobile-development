package com.example.cwk_mwe;



// a class storing a tuple of data made up of a name and a timestamp
public class BookmarkValues {
    private int time;
    private String name;

    public BookmarkValues(int timestamp, String nameOfBookmark) {
        setTime(timestamp);
        setName(nameOfBookmark);
    }

    public int getTime(){
        return time;
    }

    private void setTime(int timestamp){
        time = timestamp;
    }

    public String getName(){
        return name;
    }

    private void setName(String nameOfBookmark){
        name = nameOfBookmark;
    }
}
