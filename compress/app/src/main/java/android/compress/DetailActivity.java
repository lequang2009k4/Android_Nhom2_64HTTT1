package android.compress; // Đảm bảo package này khớp với namespace của bạn

import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.OpenableColumns;
import android.widget.ImageView;
import android.widget.TextView;

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

    // handle get file size for shot image
    private static class BitmapSizeTask extends android.os.AsyncTask<Bitmap, Void, String> {
        private final WeakReference<TextView> tvFileSizeRef;

        BitmapSizeTask(TextView tvFileSize) {
            this.tvFileSizeRef = new WeakReference<>(tvFileSize);
        }

        @Override
        protected String doInBackground(Bitmap... bitmaps) {
            ByteArrayOutputStream stream = new ByteArrayOutputStream();
            bitmaps[0].compress(Bitmap.CompressFormat.JPEG, 100, stream);
            int sizeInKB = stream.toByteArray().length / 1024;
            return sizeInKB + " KB";
        }

        @Override
        protected void onPostExecute(String result) {
            TextView tvFileSize = tvFileSizeRef.get();
            if (tvFileSize != null) {
                tvFileSize.setText(result);
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
            Bitmap bitmap = intent.getParcelableExtra("image_bitmap");
            new BitmapSizeTask(tvFileSize).execute(bitmap);
            imageView.setImageBitmap(bitmap);
            fileName = "Ảnh chụp";
            uploadDate = getIntent().getStringExtra("upload_date");
            if (uploadDate != null) {
                tvUploadDate.setText(uploadDate);
            }
        }
        tvFileName.setText(fileName);
        tvFileSize.setText(fileSize);
        tvUploadDate.setText(uploadDate);
    }
}