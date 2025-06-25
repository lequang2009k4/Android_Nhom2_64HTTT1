package android.compress;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
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

import android.compress.models.StorageManager;

import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.io.ByteArrayOutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class UploadActivity extends AppCompatActivity {

    private static final int REQUEST_CODE_PICK_IMAGE = 1001;
    private static final int REQUEST_CODE_TAKE_PHOTO = 1002;
    private static final int REQUEST_CODE_CAMERA_PERMISSION = 2001;
    private Button btnConfirm;
    private Uri selectedImageUri;
    private Bitmap photoBitmap;

    private ImageView imagePlaceholder;

    //    @Override
    // reset
    //    protected void onResume() {
    //        super.onResume();
    //        selectedImageUri = null;
    //        photoBitmap = null;
    //        imagePlaceholder.setImageResource(R.drawable.ic_image_placeholder); // Use your placeholder image
    //        btnConfirm.setEnabled(false);
    //    }

    private boolean isImageFileAllowed(String fileName) {
        if (fileName == null) return false;
        String lower = fileName.toLowerCase();
        return lower.endsWith(".jpg");
    }

    private String getFileExtension(Uri uri) {
        String extension = null;
        if (uri != null) {
            String type = getContentResolver().getType(uri);
            if (type != null) {
                extension = android.webkit.MimeTypeMap.getSingleton()
                        .getExtensionFromMimeType(type);
            } else {
                String path = uri.getPath();
                if (path != null) {
                    int dot = path.lastIndexOf(".");
                    if (dot != -1) {
                        extension = path.substring(dot + 1);
                    }
                }
            }
        }
        return extension != null ? extension : "jpg"; // Default to .jpg if no extension found
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_upload);

        imagePlaceholder = findViewById(R.id.image_placeholder);
        btnConfirm = findViewById(R.id.btn_confirm);
        btnConfirm.setEnabled(false); // Disable by default

        // In UploadActivity.java, inside btnConfirm.setOnClickListener
        btnConfirm.setOnClickListener(v -> {
            String ext = getFileExtension(selectedImageUri);
            String uploadDate = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss", Locale.getDefault()).format(new Date());
            String fileName = "uploaded/" + System.currentTimeMillis() + "." + ext;

            if (!isImageFileAllowed(fileName)) {
                Toast.makeText(this, "Only JPG are allowed", Toast.LENGTH_SHORT).show();
                return;
            }

            if (selectedImageUri != null) {
                StorageReference storageRef = FirebaseStorage.getInstance().getReference();
                StorageReference fileRef = storageRef.child(fileName);
                fileRef.putFile(selectedImageUri)
                        .addOnSuccessListener(taskSnapshot -> fileRef.getDownloadUrl().addOnSuccessListener(uri -> {
                            // Get file name and size from content resolver
//                            String displayName = "Unknown";
                            String fileSize = "Unknown";
                            try (Cursor cursor = getContentResolver().query(selectedImageUri, null, null, null, null)) {
                                if (cursor != null && cursor.moveToFirst()) {
                                    int sizeIndex = cursor.getColumnIndex(MediaStore.Images.Media.SIZE);
                                    if (sizeIndex != -1) {
                                        long sizeBytes = cursor.getLong(sizeIndex);
                                        if (sizeBytes >= 1024 * 1024) {
                                            fileSize = String.format(Locale.getDefault(), "%.2f MB", sizeBytes / (1024.0 * 1024.0));
                                        } else {
                                            fileSize = String.format(Locale.getDefault(), "%.2f KB", sizeBytes / 1024.0);
                                        }
                                    }
                                }
                            }
                            // Làm mới cache sau khi tải lên
                            StorageManager.refreshCache();

                            Intent intent = new Intent(UploadActivity.this, DetailActivity.class);
                            intent.putExtra("image_uri", selectedImageUri.toString());
                            intent.putExtra("file_name", fileName.split("/")[1]); // Get just the file name
                            intent.putExtra("file_size", fileSize);
                            intent.putExtra("upload_date", uploadDate);
                            startActivity(intent);
                        }))
                        .addOnFailureListener(e -> {
                            Toast.makeText(this, "Upload failed", Toast.LENGTH_SHORT).show();
                        });
            } else if (photoBitmap != null) {
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                photoBitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
                byte[] data = baos.toByteArray();
                StorageReference storageRef = FirebaseStorage.getInstance().getReference();
                StorageReference fileRef = storageRef.child(fileName);
                fileRef.putBytes(data)
                        .addOnSuccessListener(taskSnapshot -> fileRef.getDownloadUrl().addOnSuccessListener(uri -> {
                            // Estimate file size
                            String fileSize;
                            if (data.length >= 1024 * 1024) {
                                fileSize = String.format(Locale.getDefault(), "%.2f MB", data.length / (1024.0 * 1024.0));
                            } else {
                                fileSize = String.format(Locale.getDefault(), "%.2f KB", data.length / 1024.0);
                            }

                            // Làm mới cache sau khi tải lên
                            StorageManager.refreshCache();

                            Intent intent = new Intent(UploadActivity.this, DetailActivity.class);
                            intent.putExtra("image_bitmap", photoBitmap);
                            intent.putExtra("file_name", fileName.split("/")[1]); // Get just the file name
                            intent.putExtra("file_size", fileSize);
                            intent.putExtra("upload_date", uploadDate);
                            startActivity(intent);
                        }))
                        .addOnFailureListener(e -> {
                            Toast.makeText(this, "Upload failed", Toast.LENGTH_SHORT).show();
                        });
            }
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
                selectedImageUri = data.getData();
                if (selectedImageUri != null) {
                    imagePlaceholder.setImageURI(selectedImageUri);
                    btnConfirm.setEnabled(true);
                    photoBitmap = null;
                }
            } else if (requestCode == REQUEST_CODE_TAKE_PHOTO && data != null) {
                photoBitmap = (Bitmap) data.getExtras().get("data");
                if (photoBitmap != null) {
                    imagePlaceholder.setImageBitmap(photoBitmap);
                    btnConfirm.setEnabled(true);
                    selectedImageUri = null;
                }
            }
        }
    }
}