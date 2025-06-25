package android.compress; // Đảm bảo package này khớp với namespace của bạn

import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.OpenableColumns;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.io.ByteArrayOutputStream;
import java.lang.ref.WeakReference;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import android.compress.models.StorageManager;

public class DetailActivity extends AppCompatActivity {

    private TextView tvFileName;
    private TextView tvUploadDate;
    private TextView tvFileSize;
    private Bitmap bitmap;

    // Add this inner class to your DetailActivity

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);

        ImageView imageView = findViewById(R.id.imageView); // Đặt id đúng với layout
        TextView tvFileName = findViewById(R.id.tv_file_name);
        TextView tvFileSize = findViewById(R.id.tv_file_size);
        TextView tvUploadDate = findViewById(R.id.tv_upload_date);
        Button btnCompress = findViewById(R.id.btn_compress_now);


        Intent intent = getIntent();
        String imageUriStr = intent.getStringExtra("image_uri");
        String fileName = intent.getStringExtra("file_name");
        String fileSize = intent.getStringExtra("file_size");
        String uploadDate = intent.getStringExtra("upload_date");
        bitmap = intent.getParcelableExtra("image_bitmap");

        if (imageUriStr != null) {
            imageView.setImageURI(Uri.parse(imageUriStr));
        } else if (intent.hasExtra("image_bitmap")) {
            imageView.setImageBitmap(bitmap);
        }
        tvFileName.setText(fileName);
        tvFileSize.setText(fileSize);
        tvUploadDate.setText(uploadDate);

        btnCompress.setOnClickListener(v -> {
            FirebaseStorage storage = FirebaseStorage.getInstance();
            StorageReference storageRef = storage.getReference();
            final String fileNameToUpload = "compressed/" + fileName;

            Intent intentNext = new Intent(DetailActivity.this, LoadingCompressActivity.class);
            if (imageUriStr != null) {
                intentNext.putExtra("image_uri", imageUriStr);
            } else if (bitmap != null) {
                intentNext.putExtra("image_bitmap", bitmap);
            }
            intentNext.putExtra("file_name", fileNameToUpload.split("/")[1].split("\\.")[0] + "_compressed.jpg"); // Chỉ lấy tên file
            startActivity(intentNext);
        });
    }

    @Override
    protected void onDestroy() {
        // Hủy tất cả tác vụ đang chạy khi Activity bị hủy
        StorageManager.cancelAllTasks();
        super.onDestroy();
    }
}