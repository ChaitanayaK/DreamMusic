package com.example.dreammusic;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

public class JoinerActivity extends AppCompatActivity {
    EditText addUniqueId;
    Button joinBtn;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_joiner);

        addUniqueId = findViewById(R.id.add_unique_id_edit_text);
        joinBtn = findViewById(R.id.final_join_button_id);

        joinBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                GroupPlayActivity.uniqueId = addUniqueId.getText().toString();
                startActivity(new Intent(JoinerActivity.this, JoinedActivity.class));
                finish();
            }
        });
    }
}