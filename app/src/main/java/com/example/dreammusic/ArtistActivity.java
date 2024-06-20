package com.example.dreammusic;

import static android.content.ContentValues.TAG;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;

import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class ArtistActivity extends AppCompatActivity {

    private static final int PICK_FILE_REQUEST_CODE = 123;
    private FirebaseStorage storage;
    private StorageReference storageReference;
    private FirebaseFirestore db;
    private ProgressBar progressBar;
    private TextView progressText;
    private EditText songNameTxt;
    private EditText songImgTxt ;
    private String songName, songImgUrl;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_artist);

        // Initialize Firebase Storage
        storage = FirebaseStorage.getInstance();
        storageReference = storage.getReference();
        db = FirebaseFirestore.getInstance();

        Button uploadButton = findViewById(R.id.uploadButton);
        progressBar = findViewById(R.id.progressBar);
        progressText = findViewById(R.id.progressText);

        songNameTxt = findViewById(R.id.songName_txt_id);
        songImgTxt = findViewById(R.id.img_link_txt);

        uploadButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                songName = songNameTxt.getText().toString();
                songImgUrl = songImgTxt.getText().toString();

                if (songName.isEmpty() || songImgUrl.isEmpty()) {
                    Toast.makeText(ArtistActivity.this, "Please fill the complete details!", Toast.LENGTH_SHORT).show();
                    return;
                }

                if (!songImgUrl.toLowerCase().endsWith(".png") &&
                        !songImgUrl.toLowerCase().endsWith(".jpg") &&
                        !songImgUrl.toLowerCase().endsWith(".jpeg")) {
                    songImgTxt.setText("");
                    Toast.makeText(ArtistActivity.this, "Invalid image URL format!", Toast.LENGTH_SHORT).show();
                    return; // Return early to prevent further execution
                }

                Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                intent.setType("audio/*"); // Only allow audio files
                startActivityForResult(intent, PICK_FILE_REQUEST_CODE);

                Map<String, Object> song = new HashMap<>();
                song.put("songName", songName);
                song.put("songImgUrl", songImgUrl);


                db.collection("songs")
                        .add(song)
                        .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                            @Override
                            public void onSuccess(DocumentReference documentReference) {
                                Log.d(TAG, "DocumentSnapshot added with ID: " + documentReference.getId());
                            }
                        })
                        .addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Log.w(TAG, "Error adding document", e);
                            }
                        });

                songNameTxt.setText("");
                songImgTxt.setText("");
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PICK_FILE_REQUEST_CODE && resultCode == RESULT_OK) {
            if (data != null && data.getData() != null) {
                Uri fileUri = data.getData();
                String fileName = UUID.randomUUID().toString(); // Generate a unique filename

                StorageReference audioRef = storageReference.child("Songs/" + songName + ".mp3");

                UploadTask uploadTask = audioRef.putFile(fileUri);

                uploadTask.addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        // File uploaded successfully
                        progressBar.setVisibility(View.GONE);
                        progressText.setVisibility(View.GONE);

                        Toast.makeText(ArtistActivity.this, "Upload successful!", Toast.LENGTH_SHORT).show();
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        // Handle unsuccessful uploads
                        progressBar.setVisibility(View.GONE);
                        progressText.setVisibility(View.GONE);

                        Toast.makeText(ArtistActivity.this, "Upload failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                }).addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onProgress(@NonNull UploadTask.TaskSnapshot taskSnapshot) {
                        // Handle progress of the upload (if needed)
                        double progress = (100.0 * taskSnapshot.getBytesTransferred() / taskSnapshot.getTotalByteCount());
                        progressBar.setProgress((int) progress);
                        progressText.setText((int) progress + "%");
                    }
                });

                // Show progress bar and text
                progressBar.setVisibility(View.VISIBLE);
                progressText.setVisibility(View.VISIBLE);
            }
        }
    }
}
