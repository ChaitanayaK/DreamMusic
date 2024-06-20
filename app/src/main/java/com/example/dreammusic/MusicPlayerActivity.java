package com.example.dreammusic;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.view.KeyEvent;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.journeyapps.barcodescanner.BarcodeEncoder;
import com.squareup.picasso.Picasso;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Random;

public class MusicPlayerActivity extends AppCompatActivity implements SeekBar.OnSeekBarChangeListener{
    FirebaseStorage storage = FirebaseStorage.getInstance();
    StorageReference storageRef = storage.getReference();
    private ImageButton pauseBtn, nextBtn, previousBtn;
    private ImageButton shareBtn, repeatBtn, shuffleBtn, pullDownBtn;
    TextView artistText, songNameText, totalTimeTxt, currentTimeTxt;
    SeekBar seekBar;
    Bitmap bitmap;
    ImageView qrImage;
    private boolean isMediaPlayerPrepared = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_music_player);

        pauseBtn = findViewById(R.id.pauseBtn_id);
        nextBtn = findViewById(R.id.next_button_id);
        previousBtn = findViewById(R.id.previous_btn_id);
        pullDownBtn = findViewById(R.id.pull_down_btn_id);
        shareBtn = findViewById(R.id.share_qr_btn_id);
        qrImage = findViewById(R.id.qr_code_imageview_id);
        repeatBtn = findViewById(R.id.repeat_btn_id);
        shuffleBtn = findViewById(R.id.shuffle_btn_id);
        currentTimeTxt = findViewById(R.id.current_time_text_id);
        totalTimeTxt = findViewById(R.id.total_time_txt_id);
        artistText = findViewById(R.id.artist_name_textView_id);
        artistText.setSelected(true);
        songNameText = findViewById(R.id.song_name_textView_id);
        songNameText.setSelected(true);
        seekBar = findViewById(R.id.seekBar);
        seekBar.setOnSeekBarChangeListener(this);
        seekBar.setEnabled(false);
        ProgressBar progressBar = findViewById(R.id.progressBar);

        if (MainActivity.mediaPlayer.isPlaying()) {
            pauseBtn.setImageResource(android.R.drawable.ic_media_pause);
        } else{
            pauseBtn.setImageResource(android.R.drawable.ic_media_play);
        }

        if (MainActivity.shuffleState == 0){
            shuffleBtn.setBackgroundResource(R.drawable.no_shuffle);
        }
        else if (MainActivity.shuffleState == 1){
            shuffleBtn.setBackgroundResource(R.drawable.yes_shuffle);
        }


        shuffleBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                MainActivity.shuffleState = (MainActivity.shuffleState + 1) % 2;
                if (MainActivity.shuffleState == 0){
                    shuffleBtn.setBackgroundResource(R.drawable.no_shuffle);
                }
                else if (MainActivity.shuffleState == 1){
                    shuffleBtn.setBackgroundResource(R.drawable.yes_shuffle);
                }
            }
        });

        repeatBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                MainActivity.repeatState = (MainActivity.repeatState + 1) % 2;

                if (MainActivity.repeatState == 0) {
                    repeatBtn.setBackgroundResource(R.drawable.repeat_loop);
                } else if (MainActivity.repeatState == 1) {
                    // Repeat this song
                    repeatBtn.setBackgroundResource(R.drawable.same_song_repeat);
                }
            }
        });

        pullDownBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (isMediaPlayerPrepared) {
                    MainActivity.slideActive = false;
                    Intent intent = new Intent(MusicPlayerActivity.this, SongsListActivity.class);
//        finish();
                    startActivity(intent);
                    overridePendingTransition(0, R.anim.slide_down);
                } else {
                    Toast.makeText(MusicPlayerActivity.this, "MediaPlayer is not ready yet", Toast.LENGTH_SHORT).show();
                }
            }
        });

        songNameText.setText(MainActivity.staticSongName);

        nextBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                MainActivity.slideActive = false;

                if (MainActivity.staticSongPosition >= SongAdapter.songs.size()-1){
                    MainActivity.staticSongPosition = 0;
                }
                else {
                    MainActivity.staticSongPosition++;
                }
                Song song = SongAdapter.songs.get(MainActivity.staticSongPosition);

                MainActivity.staticSongName = song.getName();
                MainActivity.staticImageUrl = song.getImageUrl();

                if (CreatorActivity.groupPlay){
                    CreatorActivity.reference = CreatorActivity.database.getReference(GroupPlayActivity.uniqueId);
                    CreatorActivity.reference.child("song").setValue(song);
                    CreatorActivity.reference.child("play").setValue(false);
                    CreatorActivity.reference.child("timeStamp").setValue(0);
                }
                // Start the activity
                try {
                    finish();
                    overridePendingTransition(0, 0);
                    startActivity(getIntent());
                    overridePendingTransition(0, 0);
                    if (MainActivity.mediaPlayer.isPlaying()){
                        MainActivity.mediaPlayer.stop();
                    }
//                    MainActivity.mediaPlayer.reset();
//                    startActivity(intent);
//                    overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
                }
                catch (Exception e){
                    e.printStackTrace();
                }
            }
        });

        previousBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
//                Intent intent = new Intent(MusicPlayerActivity.this, MusicPlayerActivity.class);
                MainActivity.slideActive = false;

                if (MainActivity.staticSongPosition <= 0) {
                    MainActivity.staticSongPosition = SongAdapter.songs.size() - 1;
                } else {
                    MainActivity.staticSongPosition--;
                }
                Song song = SongAdapter.songs.get(MainActivity.staticSongPosition);

                MainActivity.staticSongName = song.getName();
                MainActivity.staticImageUrl = song.getImageUrl();

                if (CreatorActivity.groupPlay){
                    CreatorActivity.reference = CreatorActivity.database.getReference(GroupPlayActivity.uniqueId);
                    CreatorActivity.reference.child("song").setValue(song);
                    CreatorActivity.reference.child("play").setValue(false);
                    CreatorActivity.reference.child("timeStamp").setValue(0);
                }
                // Start the activity
                try {
                    finish();
                    overridePendingTransition(0, 0);
                    startActivity(getIntent());
                    overridePendingTransition(0, 0);
                    if (MainActivity.mediaPlayer.isPlaying()){
                        MainActivity.mediaPlayer.stop();
                    }
//                    MainActivity.mediaPlayer.reset();
//                    startActivity(intent);
//                    overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
                }
                catch (Exception e){
                    e.printStackTrace();
                }
            }
        });

        ImageView imageView = findViewById(R.id.imageView); // Replace with your ImageView

        Picasso.get().load(MainActivity.staticImageUrl).resize(300, 300).centerCrop().into(imageView);

        pauseBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (MainActivity.mediaPlayer.isPlaying()){
                    MainActivity.mediaPlayer.pause();
                    pauseBtn.setImageResource(android.R.drawable.ic_media_play);

                    if (CreatorActivity.groupPlay){
                        CreatorActivity.reference = CreatorActivity.database.getReference(GroupPlayActivity.uniqueId);
                        CreatorActivity.reference.child("play").setValue(false);
                    }
                }
                else {
                    MainActivity.mediaPlayer.start();
                    pauseBtn.setImageResource(android.R.drawable.ic_media_pause);

                    if (CreatorActivity.groupPlay){
                        CreatorActivity.reference = CreatorActivity.database.getReference(GroupPlayActivity.uniqueId);
                        CreatorActivity.reference.child("play").setValue(true);
                    }
                }
            }
        });


        shareBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String text = MainActivity.staticSongName;
                MultiFormatWriter writer = new MultiFormatWriter();
                try {
                    BitMatrix matrix = writer.encode(text, BarcodeFormat.QR_CODE,400,400);
                    BarcodeEncoder encoder = new BarcodeEncoder();
                    bitmap = encoder.createBitmap(matrix);
                    qrImage.setImageBitmap(bitmap);

                    BitmapDrawable bitmapDrawable = (BitmapDrawable) qrImage.getDrawable();
                    Bitmap bitmap1 = bitmapDrawable.getBitmap();
                    shareImageAndText(bitmap1);

                } catch (WriterException e) {
                    throw new RuntimeException(e);
                }
            }
        });

        if (!MainActivity.slideActive){
            StorageReference songRef = storageRef.child("Songs/" + MainActivity.staticSongName + ".mp3");
            songRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                @Override
                public void onSuccess(Uri uri) {
                    // Create a MediaPlayer and set the data source to the download URL
                    if (MainActivity.mediaPlayer.isPlaying()){
                        MainActivity.mediaPlayer.stop();
                    }
                    MainActivity.mediaPlayer = new MediaPlayer();
                    try {
                        MainActivity.mediaPlayer.setDataSource(uri.toString());
                        MainActivity.mediaPlayer.prepareAsync();
                        progressBar.setVisibility(View.VISIBLE);

                        MainActivity.mediaPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                            @Override
                            public void onPrepared(MediaPlayer mp) {
                                isMediaPlayerPrepared = true;
                                progressBar.setVisibility(View.GONE);
                                seekBar.setEnabled(true);
                                seekBar.setMax(mp.getDuration());

                                int totalMinutes = (mp.getDuration() / 1000) / 60;
                                int totalSeconds = (mp.getDuration() / 1000) % 60;
                                String formattedDuration = String.format("%02d:%02d", totalMinutes, totalSeconds);
                                totalTimeTxt.setText(formattedDuration);

                                if (CreatorActivity.groupPlay){
                                    pauseBtn.setImageResource(android.R.drawable.ic_media_play);
                                }
                                else{
                                    mp.start();
                                }

                                if (MainActivity.mediaPlayer.isPlaying()) {
                                    pauseBtn.setImageResource(android.R.drawable.ic_media_pause);
                                } else{
                                    pauseBtn.setImageResource(android.R.drawable.ic_media_play);
                                }

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

                                Handler handler = new Handler();
                                Runnable updateText = new Runnable() {
                                    @Override
                                    public void run() {
                                        if (MainActivity.mediaPlayer != null && MainActivity.mediaPlayer.isPlaying()) {
                                            int currentPosition = MainActivity.mediaPlayer.getCurrentPosition();
                                            int minutes = (currentPosition / 1000) / 60;
                                            int seconds = (currentPosition / 1000) % 60;
                                            String formattedCurrentTime = String.format("%02d:%02d", minutes, seconds);

                                            currentTimeTxt.setText(formattedCurrentTime);
                                        }
                                        handler.postDelayed(this, 1000);
                                    }
                                };
                                handler.postDelayed(updateText, 1000);
                            }
                        });
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    MainActivity.mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                        @Override
                        public void onCompletion(MediaPlayer mediaPlayer) {

                            if (MainActivity.repeatState == 0){
                                if (MainActivity.shuffleState == 1){
                                    shuffleFunction();
                                } else{
                                    nextRepeat();
                                }
                            }
                            else if (MainActivity.repeatState == 1) {
                                mediaPlayer.seekTo(0);
                                mediaPlayer.start();
                            }
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

        else {
            try {
                int totalMinutes = (MainActivity.mediaPlayer.getDuration() / 1000) / 60;
                int totalSeconds = (MainActivity.mediaPlayer.getDuration() / 1000) % 60;
                String formattedDuration = String.format("%02d:%02d", totalMinutes, totalSeconds);
                totalTimeTxt.setText(formattedDuration);
                isMediaPlayerPrepared = true;
                seekBar.setEnabled(true);
                seekBar.setMax(MainActivity.mediaPlayer.getDuration());
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        int totalDuration = MainActivity.mediaPlayer.getDuration();
                        int currentPosition = 0;

                        while (currentPosition < totalDuration) {
                            try {
                                Thread.sleep(200);
                                currentPosition = MainActivity.mediaPlayer.getCurrentPosition();
                                seekBar.setProgress(currentPosition);
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                }).start();

                Handler handler = new Handler();
                Runnable updateText = new Runnable() {
                    @Override
                    public void run() {
                        if (MainActivity.mediaPlayer != null && MainActivity.mediaPlayer.isPlaying()) {
                            int currentPosition = MainActivity.mediaPlayer.getCurrentPosition();
                            int minutes = (currentPosition / 1000) / 60;
                            int seconds = (currentPosition / 1000) % 60;
                            String formattedCurrentTime = String.format("%02d:%02d", minutes, seconds);

                            currentTimeTxt.setText(formattedCurrentTime);
                        }
                        handler.postDelayed(this, 1000);
                    }
                };
                handler.postDelayed(updateText, 1000);

                MainActivity.mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                    @Override
                    public void onCompletion(MediaPlayer mediaPlayer) {

                        if (MainActivity.repeatState == 0){
                            nextRepeat();
                        }
                        else if (MainActivity.repeatState == 1) {
                            mediaPlayer.seekTo(0);
                            mediaPlayer.start();
                        }
                    }
                });
            }
            catch (Exception e){
                e.printStackTrace();
            }
        }
    }

    private void shuffleFunction() {
        Random random = new Random();
        int randomIndex;
        while (true){
            randomIndex = random.nextInt(SongAdapter.songs.size());
            if (randomIndex != MainActivity.staticSongPosition){
                break;
            }
        }
        MainActivity.staticSongPosition = randomIndex;

        Song song = SongAdapter.songs.get(MainActivity.staticSongPosition);

        MainActivity.staticSongName = song.getName();
        MainActivity.staticImageUrl = song.getImageUrl();

        if (CreatorActivity.groupPlay){
            CreatorActivity.reference = CreatorActivity.database.getReference(GroupPlayActivity.uniqueId);
            CreatorActivity.reference.child("song").setValue(song);
            CreatorActivity.reference.child("play").setValue(false);
            CreatorActivity.reference.child("timeStamp").setValue(0);
        }
        try {
            finish();
            overridePendingTransition(0, 0);
            startActivity(getIntent());
            overridePendingTransition(0, 0);
            if (MainActivity.mediaPlayer.isPlaying()){
                MainActivity.mediaPlayer.stop();
            }
        }
        catch (Exception e){
            e.printStackTrace();
        }
    }

    private void shareImageAndText(Bitmap bitmap1) {
        Uri uri = getImageToShare(bitmap1);

        String message = "Scan the QR using the DreamMusic App to play the song '"+MainActivity.staticSongName+"'";

        Intent intent = new Intent(Intent.ACTION_SEND);
        intent.putExtra(Intent.EXTRA_STREAM, uri);
        intent.putExtra(Intent.EXTRA_TEXT, message);
        intent.putExtra(Intent.EXTRA_SUBJECT, "Image Subject");
        intent.setType("image/*");
        startActivity(Intent.createChooser(intent, "Share via"));

    }

    private Uri getImageToShare(Bitmap bitmap1) {
        File folder = new File(getCacheDir(), "images");
        Uri uri = null;
        try {
            folder.mkdirs();
            File file = new File(folder, "image.jpg");
            FileOutputStream fileOutputStream = new FileOutputStream(file);

            bitmap1.compress(Bitmap.CompressFormat.JPEG, 90, fileOutputStream);
            fileOutputStream.flush();
            fileOutputStream.close();

            uri = FileProvider.getUriForFile(this, "com.example.dreammusic", file);

        } catch (Exception e) {
            e.printStackTrace();
        }
        return uri;
    }

    @Override
    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
        // Update the progress of the SeekBar
        if (fromUser) {
            MainActivity.mediaPlayer.seekTo(progress);
            if (CreatorActivity.groupPlay){
                CreatorActivity.reference = CreatorActivity.database.getReference(GroupPlayActivity.uniqueId);
                CreatorActivity.reference.child("timeStamp").setValue(progress);
            }
        }
    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {
        // Not needed for this implementation
        MainActivity.mediaPlayer.pause();
    }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {
        // Not needed for this implementation
        MainActivity.mediaPlayer.start();
    }

    @Override
    public void onBackPressed() {
        if (isMediaPlayerPrepared) {
            super.onBackPressed();
            MainActivity.slideActive = false;
            Intent intent = new Intent(this, SongsListActivity.class);
//        finish();
            startActivity(intent);
            overridePendingTransition(0, R.anim.slide_down);
        } else {
            Toast.makeText(this, "MediaPlayer is not ready yet", Toast.LENGTH_SHORT).show();
        }
    }

    private void nextRepeat(){
        if (MainActivity.staticSongPosition >= SongAdapter.songs.size()-1){
            MainActivity.staticSongPosition = 0;
        }
        else {
            MainActivity.staticSongPosition++;
            System.out.println("Song position " + MainActivity.staticSongPosition);
        }
        Song song = SongAdapter.songs.get(MainActivity.staticSongPosition);

        MainActivity.staticSongName = song.getName();
        MainActivity.staticImageUrl = song.getImageUrl();

        if (CreatorActivity.groupPlay){
            CreatorActivity.reference = CreatorActivity.database.getReference(GroupPlayActivity.uniqueId);
            CreatorActivity.reference.child("song").setValue(song);
            CreatorActivity.reference.child("play").setValue(false);
            CreatorActivity.reference.child("timeStamp").setValue(0);
        }
        // Start the activity
        try {
            finish();
            overridePendingTransition(0, 0);
            startActivity(getIntent());
            overridePendingTransition(0, 0);
            if (MainActivity.mediaPlayer.isPlaying()){
                MainActivity.mediaPlayer.stop();
            }
        }
        catch (Exception e){
            e.printStackTrace();
        }
    }



}