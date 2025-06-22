package android.compress;

import android.content.Intent;
import android.graphics.Bitmap;
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

    private native byte[] compressImage(byte[] imageData, int quality);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_loading_compress);

        String imageUriStr = getIntent().getStringExtra("image_uri");
        Bitmap bitmap = getIntent().getParcelableExtra("image_bitmap");

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
                Bitmap bitmap;
                if (params[0] instanceof String) {
                    Uri imageUri = Uri.parse((String) params[0]);
                    InputStream inputStream = getContentResolver().openInputStream(imageUri);
                    bitmap = BitmapFactory.decodeStream(inputStream);
                } else if (params[0] instanceof Bitmap) {
                    bitmap = (Bitmap) params[0];
                } else {
                    return null;
                }

                // Nén ảnh bằng native (JNI)
                ByteArrayOutputStream stream = new ByteArrayOutputStream();
                // Chuyển đổi Bitmap sang byte array
                bitmap.compress(Bitmap.CompressFormat.JPEG, 100, stream);
                byte[] inputData = stream.toByteArray();
                byte[] compressedData = compressImage(inputData, 60); // 60 là chất lượng nén

                // Lưu file nén ra cache
                File file = new File(getCacheDir(), "compressed.jpg");
                try (FileOutputStream out = new FileOutputStream(file)) {
                    out.write(compressedData);
                }
                return file;
            } catch (Exception e) {
                return null;
            }
        }

        @Override
        protected void onPostExecute(File file) {
            if (file != null && file.exists()) {
                String newFileName = System.currentTimeMillis() + "_compressed";
                String uploadDate = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss", Locale.getDefault()).format(new Date());

                Intent intent = new Intent(LoadingCompressActivity.this, ResultCompressActivity.class);
                intent.putExtra("file_path", file.getAbsolutePath());
                intent.putExtra("file_name", newFileName);
                intent.putExtra("file_size", file.length() / 1024 + "kb");
                intent.putExtra("compression_date", uploadDate);

//                new android.os.Handler().postDelayed(() -> {
                    startActivity(intent);
                    finish();
//                }, 1000);
            } else {
                Toast.makeText(LoadingCompressActivity.this, "Error compressing image", Toast.LENGTH_SHORT).show();
                finish();
            }
        }
    }
}