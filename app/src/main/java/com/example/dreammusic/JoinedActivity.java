package com.example.dreammusic;

import android.content.DialogInterface;
import android.content.Intent;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.SeekBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.squareup.picasso.Picasso;

import java.io.IOException;

public class JoinedActivity extends AppCompatActivity {

    private Song song;
    TextView textView;
    ImageView imageView;
    ImageButton playBtn, pauseBtn;
    FirebaseDatabase database;
    FirebaseStorage storage = FirebaseStorage.getInstance();
    StorageReference storageRef = storage.getReference();
    SeekBar seekBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_joined);

        ProgressBar progressBar = findViewById(R.id.progressBar);
        progressBar.setVisibility(View.VISIBLE);

        textView = findViewById(R.id.song_name_textView_id);
        imageView = findViewById(R.id.imageView);
        playBtn = findViewById(R.id.playBtn_id);
        pauseBtn = findViewById(R.id.pauseBtn_id);

        playBtn.setVisibility(View.INVISIBLE);
        pauseBtn.setVisibility(View.VISIBLE);

        seekBar = findViewById(R.id.seekBar);
        seekBar.setEnabled(false);

        database = FirebaseDatabase.getInstance();

        DatabaseReference reference = database.getReference(GroupPlayActivity.uniqueId);
        reference.child("song").child("name").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                progressBar.setVisibility(View.VISIBLE);
                String songName = snapshot.getValue(String.class);
                textView.setText(songName);

                MainActivity.mediaPlayer.stop();
                MainActivity.mediaPlayer.reset();
                MainActivity.mediaPlayer = new MediaPlayer();

                StorageReference songRef = storageRef.child("Songs/" + songName + ".mp3");

                MainActivity.mediaPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                    @Override
                    public void onPrepared(MediaPlayer mp) {
                        playBtn.setVisibility(View.VISIBLE);
                        pauseBtn.setVisibility(View.INVISIBLE);
                        progressBar.setVisibility(View.GONE);

                        seekBar.setMax(mp.getDuration());

                        new Thread(new Runnable() {
                            @Override
                            public void run() {
                                int totalDuration = mp.getDuration();
                                int currentPosition = 0;

                                while (currentPosition < totalDuration) {
                                    try {
                                        Thread.sleep(200);
                                        currentPosition = mp.getCurrentPosition();
                                        seekBar.setProgress(currentPosition);
                                    } catch (InterruptedException e) {
                                        e.printStackTrace();
                                    }
                                }
                            }
                        }).start();
                    }
                });
                songRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                    @Override
                    public void onSuccess(Uri uri) {
                        try {
                            MainActivity.mediaPlayer.setDataSource(uri.toString());
                            MainActivity.mediaPlayer.prepareAsync();
//                    mediaPlayer.start();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        MainActivity.mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                            @Override
                            public void onCompletion(MediaPlayer mediaPlayer) {
                                mediaPlayer.reset();
                            }
                        });
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        // Handle any errors
                    }
                });
            }

            @Override
            public void onCancelled(DatabaseError error) {
                // Handle the error
            }
        });

        reference.child("song").child("imageUrl").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                String imageUrl = snapshot.getValue(String.class);

                Picasso.get().load(imageUrl).resize(300, 300).centerCrop().into(imageView);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        reference.child("timeStamp").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                Integer changePos = snapshot.getValue(Integer.class);

                MainActivity.mediaPlayer.seekTo(changePos);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        reference.child("play").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                Boolean playStatus = snapshot.getValue(Boolean.class);

                if (Boolean.TRUE.equals(playStatus)){
                    playBtn.setVisibility(View.INVISIBLE);
                    pauseBtn.setVisibility(View.VISIBLE);

                    MainActivity.mediaPlayer.start();
                }
                else {
                    playBtn.setVisibility(View.VISIBLE);
                    pauseBtn.setVisibility(View.INVISIBLE);
                    if (MainActivity.mediaPlayer.isPlaying()){
                        MainActivity.mediaPlayer.pause();
                    }
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    @Override
    public void onBackPressed() {
//        super.onBackPressed();
        AlertDialog.Builder builder = new AlertDialog.Builder(this);

        builder.setMessage("Do you really want to leave this page?")
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        // Finish the current activity if "Yes" is clicked
                        finish();
                    }
                })
                .setNegativeButton("No", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        // Dismiss the dialog if "No" is clicked
                        dialog.dismiss();
                    }
                });

        AlertDialog dialog = builder.create();
        dialog.show();
    }

}
