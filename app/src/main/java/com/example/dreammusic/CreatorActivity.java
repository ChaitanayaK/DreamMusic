package com.example.dreammusic;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.window.OnBackInvokedDispatcher;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CreatorActivity extends AppCompatActivity {

    public static DatabaseReference reference;
    public static FirebaseDatabase database;
    public static boolean groupPlay = false;
    TextView uniqueIdTxt;
    Button startBtn, endBtn;
    ImageButton shareBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_creator);

        Intent intent = getIntent();
        GroupPlayActivity.uniqueId = intent.getStringExtra("uniqueId");

        database = FirebaseDatabase.getInstance();

        uniqueIdTxt = findViewById(R.id.textView3);
        uniqueIdTxt.setText(GroupPlayActivity.uniqueId);

        reference = database.getReference(GroupPlayActivity.uniqueId);
        reference.child("song").setValue(null);
        reference.child("play").setValue(false);
        reference.child("timeStamp").setValue(0);

        startBtn = findViewById(R.id.start_btn_id);
        startBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                endBtn.setVisibility(View.VISIBLE);
                groupPlay = true;
                startActivity(new Intent(CreatorActivity.this, SongsListActivity.class));
            }
        });

        endBtn = findViewById(R.id.button2);
        endBtn.setVisibility(View.INVISIBLE);
        endBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                reference.removeValue();
                finish();
                groupPlay = false;
                startActivity(new Intent(CreatorActivity.this, GroupPlayActivity.class));
            }
        });

        shareBtn = findViewById(R.id.share_button_id);
        shareBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                shareText(GroupPlayActivity.uniqueId);
            }
        });
//        List<String> stringList = Arrays.asList("Item 1", "Item 2", "Item 3");
//        Map<String, Object> listMap = new HashMap<>();
//        for (int i = 0; i < stringList.size(); i++) {
//            listMap.put(String.valueOf(i), stringList.get(i));
//        }
//
//        FirebaseDatabase database = FirebaseDatabase.getInstance();
//
//        DatabaseReference listReference = database.getReference("myList");
//        listReference.setValue(listMap);
    }

    private void shareText(String textToShare) {
        Intent sendIntent = new Intent();
        sendIntent.setAction(Intent.ACTION_SEND);
        sendIntent.putExtra(Intent.EXTRA_TEXT, textToShare);
        sendIntent.setType("text/plain");

        // If you want to specify a list of apps to show in the chooser, you can do so by setting a package name.
        // Example: sendIntent.setPackage("com.whatsapp");

        Intent shareIntent = Intent.createChooser(sendIntent, null);
        startActivity(shareIntent);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        reference.removeValue();
        finish();
        groupPlay = false;
        startActivity(new Intent(CreatorActivity.this, GroupPlayActivity.class));
    }

//    @Override
//    protected void onDestroy() {
//        super.onDestroy();
//    }
}