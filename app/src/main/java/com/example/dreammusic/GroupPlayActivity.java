package com.example.dreammusic;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;

public class GroupPlayActivity extends AppCompatActivity {

    public static String uniqueId = "";
    Button createBtn;
    Button joinBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_group_play);

        createBtn = findViewById(R.id.create_btn_id);
        joinBtn = findViewById(R.id.join_btn_id);

        createBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                String randomUniqueId = RandomUniqueIdGenerator.generateRandomUniqueId(6);

                Intent intent = new Intent(GroupPlayActivity.this, CreatorActivity.class);
                intent.putExtra("uniqueId", randomUniqueId);
                finish();
                startActivity(intent);
            }
        });

        joinBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(GroupPlayActivity.this, JoinerActivity.class);
                finish();
                startActivity(intent);
            }
        });



//        DatabaseReference myRef = database.getReference("message");
//        myRef.setValue("Hello, World!");

//        myRef.addValueEventListener(new ValueEventListener() {
//            @Override
//            public void onDataChange(DataSnapshot dataSnapshot) {
//                // This method is called once with the initial value and again
//                // whenever data at this location is updated.
//                String value = dataSnapshot.getValue(String.class);
//                Log.d(TAG, "Value is: " + value);
//            }
//            @Override
//            public void onCancelled(DatabaseError error) {
//                // Failed to read value
//                Log.w(TAG, "Failed to read value.", error.toException());
//            }
//        });
    }
}