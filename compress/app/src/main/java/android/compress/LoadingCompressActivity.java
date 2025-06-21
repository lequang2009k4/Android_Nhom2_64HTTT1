package android.compress;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;

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

    // Bitmap
    private class CompressImageTask extends AsyncTask<Object, Void, Bitmap> {
        @Override
        protected Bitmap doInBackground(Object... params) {
            try {
                Bitmap bitmap;
                if (params[0] instanceof String) {
                    // Handle URI
                    Uri imageUri = Uri.parse((String) params[0]);
                    InputStream inputStream = getContentResolver().openInputStream(imageUri);
                    bitmap = BitmapFactory.decodeStream(inputStream);
                } else if (params[0] instanceof Bitmap) {
                    // Handle Bitmap
                    bitmap = (Bitmap) params[0];
                } else {
                    return null;
                }
                ByteArrayOutputStream stream = new ByteArrayOutputStream();
                bitmap.compress(Bitmap.CompressFormat.JPEG, 100, stream);
                byte[] inputData = stream.toByteArray();
                byte[] compressedData = compressImage(inputData, 60);
                return BitmapFactory.decodeStream(new ByteArrayInputStream(compressedData));
            } catch (Exception e) {
                return null;
            }
        }

        @Override
        protected void onPostExecute(Bitmap bitmap) {
            if (bitmap != null) {
                Toast.makeText(LoadingCompressActivity.this, "Compression done", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(LoadingCompressActivity.this, "Error compressing image", Toast.LENGTH_SHORT).show();
                finish();
            }
        }
    }
}