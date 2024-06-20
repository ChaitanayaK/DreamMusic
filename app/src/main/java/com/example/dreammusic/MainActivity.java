package com.example.dreammusic;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Shader;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextPaint;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.VideoView;
import androidx.activity.result.ActivityResultLauncher;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.database.FirebaseDatabase;
import com.journeyapps.barcodescanner.ScanContract;
import com.journeyapps.barcodescanner.ScanOptions;
import java.util.HashMap;

public class MainActivity extends AppCompatActivity {

    private final int MY_GALLERY_REQUEST_CODE = 44;
    public static MediaPlayer mediaPlayer;
    public static String staticSongName = "";
    public static String staticImageUrl = "", qrScannedString = "";
    public static int staticSongPosition = 0;
    public static int repeatState = 0;
    public static int shuffleState = 0;
    public static boolean slideActive = false, qrScanned = false;
    private VideoView videoView;
    SignInButton btSignIn;
    FirebaseAuth auth;
    int RC_SIGN_IN = 20;
    FirebaseDatabase database;
    GoogleSignInClient mgoogleSignInClient;
    private ImageButton scanBtn, settingBtn;
    GoogleSignInClient googleSignInClient;

    ActivityResultLauncher<ScanOptions> cameraLauncher = registerForActivityResult(new ScanContract(), result -> {
        if (result.getContents() != null) {
            qrScannedString = result.getContents();
            qrScanned = true;
            startActivity(new Intent(MainActivity.this, SongsListActivity.class));
        }
    });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        FirebaseApp.initializeApp(this);

        auth = FirebaseAuth.getInstance();
        database = FirebaseDatabase.getInstance();

        FirebaseUser currentUser = auth.getCurrentUser();
        if (currentUser == null) {
            GoogleSignInOptions googleSignInOptions = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                    .requestIdToken("753511650832-9kr22fmoac3esddtum6pg59cer1buh2j.apps.googleusercontent.com")
                    .requestEmail()
                    .build();

            googleSignInClient = GoogleSignIn.getClient(MainActivity.this, googleSignInOptions);

            Intent intent = googleSignInClient.getSignInIntent();
            // Start activity for result
            startActivityForResult(intent, 100);
        }

        else {
            Toast.makeText(this, currentUser.getDisplayName() + " is signed in...", Toast.LENGTH_SHORT).show();
        }

        MainActivity.mediaPlayer = new MediaPlayer();

        scanBtn = findViewById(R.id.scan_btn_id);
        settingBtn = findViewById(R.id.settings_btn_id);

        scanBtn.setVisibility(View.INVISIBLE);
        settingBtn.setVisibility(View.INVISIBLE);

        ImageButton usefulBtn = findViewById(R.id.usefull);

        videoView = findViewById(R.id.video_view);

//        Uri videoUri = Uri.parse("android.resource://" + getPackageName() + "/" + R.raw.rotating_lines);
//        videoView.setVideoURI(videoUri);
//        videoView.start();
//        videoView.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
//            @Override
//            public void onPrepared(MediaPlayer mp) {
//                mp.setLooping(false);
//            }
//        });

        scanBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showScanOptionsDialog();
            }
        });

        ImageButton artistPageBtn = findViewById(R.id.artist_page_btn_id);

        ImageButton playerPageBtn = findViewById(R.id.player_page_btn_id);

        TextView logo_title = findViewById(R.id.app_title_id);


        TextPaint paint = logo_title.getPaint();
        float width = paint.measureText((String) logo_title.getText());

        Shader textShader = new LinearGradient(0 ,0 , width, logo_title.getTextSize(),
                new int[] {
                        Color.parseColor("#ffbe0b"),
                        Color.parseColor("#ffbe0b"),
                       // Color.parseColor("#fb5607"),
                        Color.parseColor("#ff006e"),
                        Color.parseColor("#3a86ff")
                }, null, Shader.TileMode.CLAMP);

        logo_title.getPaint().setShader(textShader);

        logo_title.setText("Dream\n\tMusic");
        ObjectAnimator fadeInText = ObjectAnimator.ofFloat(logo_title, "alpha", 0f, 1f);
        fadeInText.setDuration(2000);

        ObjectAnimator changePos = ObjectAnimator.ofFloat(logo_title, "translationY", -450f);
        changePos.setDuration(1000);

        AnimatorSet animatorSet = new AnimatorSet();
        animatorSet.playSequentially(fadeInText, changePos);

        animatorSet.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);
                artistPageBtn.setVisibility(View.VISIBLE);
                playerPageBtn.setVisibility(View.VISIBLE);
                settingBtn.setVisibility(View.VISIBLE);
                scanBtn.setVisibility(View.VISIBLE);
                usefulBtn.setVisibility(View.INVISIBLE);
                ObjectAnimator fadeInArtist = ObjectAnimator.ofFloat(artistPageBtn, "alpha", 0f, 1f);
                fadeInArtist.setDuration(1000);
                fadeInArtist.start();

                ObjectAnimator fadeInPlayer = ObjectAnimator.ofFloat(playerPageBtn, "alpha", 0f, 1f);
                fadeInPlayer.setDuration(1000);
                fadeInPlayer.start();

                ObjectAnimator fadeInSetting = ObjectAnimator.ofFloat(settingBtn, "alpha", 0f, 1f);
                fadeInSetting.setDuration(1000);
                fadeInSetting.start();

                ObjectAnimator fadeInQr = ObjectAnimator.ofFloat(scanBtn, "alpha", 0f, 1f);
                fadeInQr.setDuration(1000);
                fadeInQr.start();

                artistPageBtn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent intent = new Intent(MainActivity.this, GroupPlayActivity.class);
                        startActivity(intent);
                    }
                });

                playerPageBtn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent intent = new Intent(MainActivity.this, SongsListActivity.class);
                        startActivity(intent);
                    }
                });

                settingBtn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        finish();
                        startActivity(new Intent(MainActivity.this, SettingsActivity.class));
                    }
                });
            }
        });
        animatorSet.start();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        // Check condition
        if (requestCode == 100) {
            // When request code is equal to 100 initialize task
            Task<GoogleSignInAccount> signInAccountTask = GoogleSignIn.getSignedInAccountFromIntent(data);
            // check condition
            if (signInAccountTask.isSuccessful()) {
                // When google sign in successful initialize string
                String s = "Google sign in successful";
                // Display Toast
                displayToast(s);
                // Initialize sign in account
                try {
                    // Initialize sign in account
                    GoogleSignInAccount googleSignInAccount = signInAccountTask.getResult(ApiException.class);
                    // Check condition
                    if (googleSignInAccount != null) {
                        // When sign in account is not equal to null initialize auth credential
                        AuthCredential authCredential = GoogleAuthProvider.getCredential(googleSignInAccount.getIdToken(), null);
                        // Check credential
                        auth.signInWithCredential(authCredential).addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                            @Override
                            public void onComplete(@NonNull Task<AuthResult> task) {
                                // Check condition
                                if (task.isSuccessful()) {
                                    // When task is successful redirect to profile activity display Toast
                                    displayToast("Firebase authentication successful");
                                } else {
                                    // When task is unsuccessful display Toast
                                    displayToast("Authentication Failed :" + task.getException().getMessage());
                                }
                            }
                        });
                    }
                } catch (ApiException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private void displayToast(String s) {
        Toast.makeText(getApplicationContext(), s, Toast.LENGTH_SHORT).show();
    }

    private void showScanOptionsDialog() {
        scanCode();
    }

    private void scanCode() {
        ScanOptions options = new ScanOptions();
        options.setPrompt("Volume up to flash on");
        options.setBeepEnabled(true);
        options.setOrientationLocked(true);
        options.setCaptureActivity(CaptureAct.class);
        cameraLauncher.launch(options);
    }
}