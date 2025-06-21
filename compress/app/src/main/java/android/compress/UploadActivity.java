package android.compress;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

public class UploadActivity extends AppCompatActivity {

    private static final int REQUEST_CODE_PICK_IMAGE = 1001;
    private static final int REQUEST_CODE_TAKE_PHOTO = 1002;
    private static final int REQUEST_CODE_CAMERA_PERMISSION = 2001;
    private Button btnConfirm;

    private ImageView imagePlaceholder;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_upload);

        imagePlaceholder = findViewById(R.id.image_placeholder);
        btnConfirm = findViewById(R.id.btn_confirm);
        btnConfirm.setEnabled(false); // Disable by default

        btnConfirm.setOnClickListener(v -> {
            Intent intent = new Intent(UploadActivity.this, DetailActivity.class);
            startActivity(intent);
        });

        Button btnUpload = findViewById(R.id.btn_upload);
        Button btnTakePhoto = findViewById(R.id.btn_take_photo);

        btnUpload.setOnClickListener(v -> openGallery());
        btnTakePhoto.setOnClickListener(v -> checkCameraPermissionAndOpenCamera());
    }

    private void openGallery() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        intent.setType("image/*");
        startActivityForResult(intent, REQUEST_CODE_PICK_IMAGE);
    }

    private void checkCameraPermissionAndOpenCamera() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA}, REQUEST_CODE_CAMERA_PERMISSION);
        } else {
            openCamera();
        }
    }

    private void openCamera() {
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        startActivityForResult(intent, REQUEST_CODE_TAKE_PHOTO);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_CODE_CAMERA_PERMISSION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                openCamera();
            } else {
                Toast.makeText(this, "Camera permission denied", Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == REQUEST_CODE_PICK_IMAGE && data != null) {
                Uri selectedImageUri = data.getData();
                if (selectedImageUri != null) {
                    imagePlaceholder.setImageURI(selectedImageUri);
                    Toast.makeText(this, "Upload Success", Toast.LENGTH_SHORT).show();
                    btnConfirm.setEnabled(true);
                } else {
                    Toast.makeText(this, "Không lấy được ảnh", Toast.LENGTH_SHORT).show();
                }
            } else if (requestCode == REQUEST_CODE_TAKE_PHOTO && data != null) {
                Bitmap photo = (Bitmap) data.getExtras().get("data");
                if (photo != null) {
                    imagePlaceholder.setImageBitmap(photo);
                    Toast.makeText(this, "Shot Success", Toast.LENGTH_SHORT).show();
                    btnConfirm.setEnabled(true);
                } else {
                    Toast.makeText(this, "Không lấy được ảnh chụp", Toast.LENGTH_SHORT).show();
                }
            }
        }
    }
}