package android.compress;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class LoadingCompressActivity extends AppCompatActivity {

    static {
        System.loadLibrary("imagecompressor");
    }

    private String originalSize;

    private native byte[] compressImage(byte[] imageData, int quality);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_loading_compress);

        String imageUriStr = getIntent().getStringExtra("image_uri");
        Bitmap bitmap = getIntent().getParcelableExtra("image_bitmap");
        originalSize = getIntent().getStringExtra("original_size");

        Object input = imageUriStr != null ? imageUriStr : bitmap;
        if (input == null) {
            Toast.makeText(this, "No image data provided", Toast.LENGTH_SHORT).show();
            finish();
        } else {
            new CompressImageTask().execute(input);
        }
    }

    private class CompressImageTask extends AsyncTask<Object, Void, File> {
        @Override
        protected File doInBackground(Object... params) {
            try {
                byte[] inputData;

                if (params[0] instanceof String) {
                    // Đọc dữ liệu gốc từ file thay vì decode thành Bitmap
                    Uri imageUri = Uri.parse((String) params[0]);
                    InputStream inputStream = getContentResolver().openInputStream(imageUri);
                    ByteArrayOutputStream byteStream = new ByteArrayOutputStream();
                    byte[] buffer = new byte[4096];
                    int bytesRead;
                    while ((bytesRead = inputStream.read(buffer)) != -1) {
                        byteStream.write(buffer, 0, bytesRead);
                    }
                    inputData = byteStream.toByteArray();
                    inputStream.close();
                    byteStream.close();
                } else if (params[0] instanceof Bitmap) {
                    // Nếu là Bitmap từ camera, nén với chất lượng phù hợp
                    Bitmap bitmap = (Bitmap) params[0];
                    ByteArrayOutputStream stream = new ByteArrayOutputStream();
                    // Sử dụng chất lượng 90 thay vì 100 để có dữ liệu tốt cho C++ nén
                    // nhưng vẫn đảm bảo chất lượng cao
                    bitmap.compress(CompressFormat.JPEG, 100, stream);
                    inputData = stream.toByteArray();
                } else {
                    return null;
                }

                // Nén ảnh bằng native (JNI) với dữ liệu gốc
                byte[] compressedData = compressImage(inputData, 60);

                // Lưu file nén ra cache
                File file = new File(getCacheDir(), "compressed.jpg");
                try (FileOutputStream out = new FileOutputStream(file)) {
                    out.write(compressedData);
                }
                return file;
            } catch (Exception e) {
                e.printStackTrace();
                return null;
            }
        }

        @Override
        protected void onPostExecute(File file) {
            if (file != null && file.exists()) {
                String newFileName = getIntent().getStringExtra("file_name");
                String uploadDate = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss", Locale.getDefault()).format(new Date());

                // Upload to Firebase Storage
                com.google.firebase.storage.FirebaseStorage storage = com.google.firebase.storage.FirebaseStorage.getInstance();
                com.google.firebase.storage.StorageReference storageRef = storage.getReference();
                com.google.firebase.storage.StorageReference fileRef = storageRef.child("compressed/" + newFileName);

                fileRef.putFile(android.net.Uri.fromFile(file)).addOnSuccessListener(taskSnapshot -> {
                    // On upload success, go to ResultCompressActivity
                    long fileSizeBytes = file.length();
                    String fileSizeStr;
                    if (fileSizeBytes >= 1024 * 1024) {
                        fileSizeStr = String.format(Locale.getDefault(), "%.2f MB", fileSizeBytes / (1024.0 * 1024.0));
                    } else {
                        fileSizeStr = String.format(Locale.getDefault(), "%.2f KB", fileSizeBytes / 1024.0);
                    }
                    Intent intentNext = new Intent(LoadingCompressActivity.this, ResultCompressActivity.class);
                    intentNext.putExtra("file_path", file.getAbsolutePath());
                    intentNext.putExtra("file_name", newFileName);
                    intentNext.putExtra("original_size", originalSize);
                    intentNext.putExtra("file_size", fileSizeStr);
                    intentNext.putExtra("compression_date", uploadDate);
                    startActivity(intentNext);
                    finish();
                }).addOnFailureListener(e -> {
                    Toast.makeText(LoadingCompressActivity.this, "Upload failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    finish();
                });
            } else {
                Toast.makeText(LoadingCompressActivity.this, "Error compressing image", Toast.LENGTH_SHORT).show();
                finish();
            }
        }
    }
}