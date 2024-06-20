package com.example.dreammusic;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import com.google.zxing.qrcode.QRCodeReader;
import com.google.zxing.Result;

//import com.journeyapps.barcodescanner.QRcode.QRCodeDecoder;
import com.journeyapps.barcodescanner.ScanContract;
import com.journeyapps.barcodescanner.ScanOptions;

import java.io.InputStream;

public class QrScannerActivity extends AppCompatActivity {
    Button btn_scan;

    // ActivityResultLauncher for gallery image selection
//    ActivityResultLauncher<Intent> galleryLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
//        if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
//            Intent data = result.getData();
//            // Scan the QR code in the selected image
//            InputStream inputStream = getContentResolver().openInputStream(data.getData());
//            QRCodeDecoder decoder = new QRCodeDecoder();
//            String decodedText = decoder.decode(inputStream);
//
//            // Handle the decoded text here
//            if (decodedText != null) {
//                AlertDialog.Builder builder = new AlertDialog.Builder(QrScannerActivity.this);
//                builder.setTitle("Result");
//                builder.setMessage(decodedText);
//                builder.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
//                    @Override
//                    public void onClick(DialogInterface dialogInterface, int i) {
//                        dialogInterface.dismiss();
//                    }
//                }).show();
//            }
//        }
//    });

    // ActivityResultLauncher for camera-based QR code scanning
    ActivityResultLauncher<ScanOptions> cameraLauncher = registerForActivityResult(new ScanContract(), result -> {
        if (result.getContents() != null) {
            AlertDialog.Builder builder = new AlertDialog.Builder(QrScannerActivity.this);
            builder.setTitle("Result");
            builder.setMessage(result.getContents());
            builder.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    dialogInterface.dismiss();
                }
            }).show();
        }
    });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_qr_scanner);
        btn_scan = findViewById(R.id.btn_scan);
        btn_scan.setOnClickListener(v -> {
            showScanOptionsDialog();
        });
    }

    private void showScanOptionsDialog() {
        // Create a dialog to choose between camera and gallery
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Select Scanning Option");
        builder.setItems(new CharSequence[]{"Gallery", "Camera"}, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (which == 0) {
                    // Launch the gallery for image selection
//                    Intent galleryIntent = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
//                    galleryLauncher.launch(galleryIntent);
                } else if (which == 1) {
                    // Launch the camera-based QR code scanning
                    scanCode();
                }
            }
        });
        builder.show();
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