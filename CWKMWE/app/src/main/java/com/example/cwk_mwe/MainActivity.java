package com.example.cwk_mwe;


import android.Manifest;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.lifecycle.Observer;

import java.io.File;
import java.util.ArrayList;


public class MainActivity extends AppCompatActivity {

    ActivityResultLauncher<Intent> resultLauncher;

    private MusicService musicService;
    public static final String BACKGROUND_COLOUR = null;
    private static final int MY_PERMISSIONS_REQUEST_READ_MEDIA_AUDIO = 1;

    private int backgroundColour;
    private float playbackSpeed;

    private final String TAG = "MusicPlayer Activity";

    static File music = new File("/storage/self/primary/Music/");
    static File[] tracks = music.listFiles();

    private static ArrayList<Integer> notMusic = new ArrayList<>();

    ListView listViewSongs;
    ListView listViewBookmarks;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_track_list);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        Button settingsButton = findViewById(R.id.settingButton);

        settingsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view){
                Intent intentSettings = new Intent(MainActivity.this,SettingActivity.class);
                startActivity(intentSettings);
            }
        });

        //gets files in directory ending in mp3
        assert tracks != null;
        notMusic.clear();
        for (int i = 0; i< tracks.length; i++) {
            if (!(tracks[i].getName()).endsWith(".mp3")) {
                notMusic.add(i);
            }
        }

        checkAndRequestPermissions();

        String[] values = this.getMusic();

        //displays the tracks in a listView
        listViewSongs = findViewById(R.id.songList);
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout
                .simple_list_item_1, android.R.id.text1, values);
        listViewSongs.setAdapter(adapter);
        listViewSongs.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long
                    id) {
                String itemValue = (String) listViewSongs.getItemAtPosition(position);
                Log.d(TAG, String.valueOf(ProgramViewModel.getAudioWalkStopped()));
                if (ProgramViewModel.getAudioWalkStopped()) {
                    musicService.loadAudio(itemValue, position, playbackSpeed, values.length - 1);
                }
                Intent intent = new Intent(MainActivity.this, MusicPlayer.class);
                intent.putExtra("Key Message", itemValue);
                resultLauncher.launch(intent);
            }
        });
        resultLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == RESULT_OK && result.getData() != null)
                    {
                        String returnedMessage = result.getData().getStringExtra("Return Message");
                        assert returnedMessage != null;
                        Log.d(TAG,returnedMessage);

                    }
                });

        //goes through the bookmarks and puts them in a list to use in the listView
        ArrayList<BookmarkValues> listOfBookmarks = ProgramViewModel.getBookmark().getValue();
        if (listOfBookmarks != null) {

            String[] bookmarkNames = new String[listOfBookmarks.toArray().length];
            for (int i = 0; i<listOfBookmarks.toArray().length;i++){
                bookmarkNames[i] = listOfBookmarks.get(i).getName();
            }

            //displays the bookmark names in a listView
            listViewBookmarks = findViewById(R.id.bookmarkList);
            ArrayAdapter<String> adapterBookmarks = new ArrayAdapter<String>(this, android.R.layout
                    .simple_list_item_1, android.R.id.text1, bookmarkNames);
            listViewBookmarks.setAdapter(adapterBookmarks);
            listViewBookmarks.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long
                        id) {
                    String itemValue = (String) listViewBookmarks.getItemAtPosition(position);
                    if (ProgramViewModel.getAudioWalkStopped()) {
                        musicService.loadAudio(itemValue, position, playbackSpeed, bookmarkNames.length - 1);
                        musicService.setTimestamp(listOfBookmarks.get(position).getTime());
                    }
                    Intent intent = new Intent(MainActivity.this, MusicPlayer.class);
                    intent.putExtra("Key Message", itemValue);
                    resultLauncher.launch(intent);
                }
            });
        }

        resultLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == RESULT_OK && result.getData() != null)
                    {
                        String returnedMessage = result.getData().getStringExtra("Return Message");
                        assert returnedMessage != null;
                        Log.d(TAG,returnedMessage);
                    }
                });


        if(savedInstanceState != null){
            backgroundColour = (savedInstanceState.getInt(BACKGROUND_COLOUR));
        }
        findViewById(R.id.main).setBackgroundColor(backgroundColour);


        ProgramViewModel.getBackgroundColour().observe(this, new Observer<Integer>() {
            @Override
            public void onChanged(Integer colour) {
                findViewById(R.id.main).setBackgroundColor(colour);
            }
        });
        ProgramViewModel.getPlaybackSpeed().observe(this, new Observer<Float>() {
            @Override
            public void onChanged(Float newPlaybackspeed) {
                playbackSpeed = newPlaybackspeed;
            }
        });

    }


    //for requesting permission
    private void checkAndRequestPermissions() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_MEDIA_AUDIO) != PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.READ_MEDIA_AUDIO)) {
                new AlertDialog.Builder(this)
                        .setTitle("Permission needed")
                        .setMessage("This permission is needed to access the music files on your device.")
                        .setPositiveButton("OK", (dialog, which) -> ActivityCompat.requestPermissions(MainActivity.this,
                                new String[]{Manifest.permission.READ_MEDIA_AUDIO},
                                MY_PERMISSIONS_REQUEST_READ_MEDIA_AUDIO))
                        .create()
                        .show();
            } else {
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.READ_MEDIA_AUDIO},
                        MY_PERMISSIONS_REQUEST_READ_MEDIA_AUDIO);
            }
        } else {
            // Permission already granted, Log output
            loadAudio();

        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == MY_PERMISSIONS_REQUEST_READ_MEDIA_AUDIO) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permission was granted, load the audio
                loadAudio();
            } else {
                Toast.makeText(this, "Permission DENIED", Toast.LENGTH_SHORT).show(); // Permission denied, show a toast
            }
        }
    }

    private void loadAudio(){
        Log.d(TAG, "music loaded");
    }


    public void onStart(){
        Log.d(TAG, "binding to service");
        super.onStart();
        Intent intent = new Intent(MainActivity.this,MusicService.class);
        startService(intent);
        bindService(intent,connection,  Context.BIND_AUTO_CREATE);

    }

    public void onStop(){
        Log.d(TAG, "unbinding to service");
        super.onStop();
        unbindService(connection);
    }

    //uses the binder to create a connection to the service
    private ServiceConnection connection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            Log.d(TAG, "onService connected");
            MusicService.LocalBinder binder = (MusicService.LocalBinder) service;
            musicService = binder.getService();
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            Log.d(TAG, "unbound from service");
        }
    };

    //Removes non music tracks from the tracks and returns the music in a list
    public String[] getMusic() {
        ArrayList<String> musicTemp = new ArrayList<>();
        for(int i = 0; i< tracks.length - notMusic.toArray().length; i++){
            if( !notMusic.contains(i)){
                musicTemp.add(tracks[i].getName());
            }
        }

        String[] musicList = new String[musicTemp.toArray().length];
        for(int i = 0; i< musicTemp.toArray().length; i++){
            musicList[i] = musicTemp.get(i);
        }
        return musicList;
    }
}

