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

import java.io.ByteArrayOutputStream;
import java.lang.ref.WeakReference;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class DetailActivity extends AppCompatActivity {

    private TextView tvFileName;
    private TextView tvUploadDate;
    private TextView tvFileSize;
    private Bitmap bitmap;

    // Add this inner class to your DetailActivity
    private static class BitmapSizeTask extends android.os.AsyncTask<Bitmap, Void, String> {
        private final WeakReference<TextView> tvFileSizeRef;

        BitmapSizeTask(TextView tvFileSize) {
            this.tvFileSizeRef = new WeakReference<>(tvFileSize);
        }

        @Override
        protected String doInBackground(Bitmap... bitmaps) {
            // Tính kích thước ước tính dựa trên thông tin Bitmap mà không cần compress
            Bitmap bitmap = bitmaps[0];
            int width = bitmap.getWidth();
            int height = bitmap.getHeight();
            
            // Ước tính kích thước JPEG dựa trên số pixel và tỷ lệ nén thông thường
            // JPEG thường có tỷ lệ nén khoảng 1:10 đến 1:20 tùy thuộc vào độ phức tạp của ảnh
            long estimatedSize = (long) width * height * 3 / 15; // Ước tính với tỷ lệ nén 1:15
            
            // Chuyển đổi sang KB hoặc MB
            if (estimatedSize >= 1024 * 1024) {
                return String.format("%.1f MB", estimatedSize / (1024.0 * 1024.0));
            } else {
                return (estimatedSize / 1024) + " KB";
            }
        }

        @Override
        protected void onPostExecute(String fileSizeStr) {
            TextView tvFileSize = tvFileSizeRef.get();
            if (tvFileSize != null) {
                tvFileSize.setText(fileSizeStr);
            }
        }
    }

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
        // Lấy tên file và size
        String fileName = "Unknown";
        String fileSize = "Unknown";
        String uploadDate = "Unknown";


        if (imageUriStr != null) {
            Uri imageUri = Uri.parse(imageUriStr);
            imageView.setImageURI(imageUri);
            Cursor cursor = getContentResolver().query(imageUri, null, null, null, null);
            if (cursor != null && cursor.moveToFirst()) {
                int nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME);
                int sizeIndex = cursor.getColumnIndex(OpenableColumns.SIZE);
                if (nameIndex != -1) fileName = cursor.getString(nameIndex);
                if (sizeIndex != -1) fileSize = cursor.getLong(sizeIndex) / 1024 + " KB";
                // Lấy ngày upload
                cursor.close();
            }
            // Get upload date from Intent
            uploadDate = getIntent().getStringExtra("upload_date");
        } else if (intent.hasExtra("image_bitmap")) {
            bitmap = intent.getParcelableExtra("image_bitmap");
            imageView.setImageBitmap(bitmap);
            fileName = "Ảnh chụp";
            uploadDate = getIntent().getStringExtra("upload_date");
            if (uploadDate != null) {
                tvUploadDate.setText(uploadDate);
            }
            // Calculate size in background
            new BitmapSizeTask(tvFileSize).execute(bitmap);
        }
        tvFileName.setText(fileName);
        tvFileSize.setText(fileSize);
        tvUploadDate.setText(uploadDate);

        btnCompress.setOnClickListener(v -> {
            Intent intentNext = new Intent(DetailActivity.this, LoadingCompressActivity.class);
            if (imageUriStr != null) {
                intentNext.putExtra("image_uri", imageUriStr);
            } else if (bitmap != null) {
                intentNext.putExtra("image_bitmap", bitmap);
            }
            intentNext.putExtra("image_bitmap", bitmap);
            startActivity(intentNext);
        });
    }
}