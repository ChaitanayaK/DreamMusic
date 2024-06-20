package com.example.dreammusic;

import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class SongsListActivity extends AppCompatActivity {
    private SongAdapter adapter;
    private List<Song> songs;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_songs_list);

        db = FirebaseFirestore.getInstance();

        ImageButton imageButton = findViewById(R.id.imageButton);
        ImageView imageView = findViewById(R.id.imageView3);
        TextView songName = findViewById(R.id.textView);
        ConstraintLayout constraintLayout = findViewById(R.id.constraint_id);
        ConstraintLayout clickableLayout = findViewById(R.id.clickable_layout);

        if (MainActivity.mediaPlayer.isPlaying()) {
            imageButton.setImageResource(android.R.drawable.ic_media_pause);
        } else{
            imageButton.setImageResource(android.R.drawable.ic_media_play);
        }

        if (MainActivity.staticImageUrl.isEmpty() && MainActivity.staticSongName.isEmpty()){
            constraintLayout.setVisibility(View.INVISIBLE);
        }
        else {
            constraintLayout.setVisibility(View.VISIBLE);
            Picasso.get().load(MainActivity.staticImageUrl).resize(40, 40).centerCrop().into(imageView);
            songName.setText(MainActivity.staticSongName);
        }

        clickableLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                MainActivity.slideActive = true;
                Intent intent = new Intent(SongsListActivity.this, MusicPlayerActivity.class);
                finish();
                startActivity(intent);
                overridePendingTransition(R.anim.slide_up, 0);
            }
        });

        imageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (MainActivity.mediaPlayer.isPlaying()){
                    imageButton.setImageResource(android.R.drawable.ic_media_play);
                    MainActivity.mediaPlayer.pause();
                }
                else{
                    imageButton.setImageResource(android.R.drawable.ic_media_pause);
                    MainActivity.mediaPlayer.start();
                }
            }
        });

        ListView songsListView = findViewById(R.id.songsListView);
        songs = new ArrayList<>();
        adapter = new SongAdapter(this, R.layout.list_item_song, songs);
        songsListView.setAdapter(adapter);

        retrieveSongs();
    }

    private void retrieveSongs() {
        db.collection("songs")
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            int index = 0;
                            boolean found = false;
                            for (QueryDocumentSnapshot document : task.getResult()) {

                                String songName = (String) document.get("songName");
                                String songImgUrl = (String) document.get("songImgUrl");

                                if (songName != null && songImgUrl != null) {
                                    songs.add(new Song(songName, songImgUrl));
                                    if (MainActivity.qrScanned && songName.equals(MainActivity.qrScannedString)){
                                        found = true;
                                        MainActivity.qrScanned = false;
                                        MainActivity.staticSongName = songName;
                                        MainActivity.staticImageUrl = songImgUrl;
                                        MainActivity.staticSongPosition = index;
                                    }
                                    index++;
                                }
                            }
                            adapter.notifyDataSetChanged();

                            if (found){
                                finish();
                                startActivity(new Intent(SongsListActivity.this, MusicPlayerActivity.class));
                            }
                        }
                    }
                });
    }

}
