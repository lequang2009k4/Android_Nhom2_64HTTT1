package android.compress; // Đảm bảo package này khớp với namespace của bạn

import android.compress.models.StorageManager;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

public class ResultCompressActivity extends AppCompatActivity {

    private TextView tvNewFileName;
    private TextView tvNewUploadDate;
    private TextView tvNewFileSize;
    private TextView tvReductionInfo;
    private TextView tvPsnrValue;
    private Button btnComplete;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_result_compress);

        // Khởi tạo các View
        tvReductionInfo = findViewById(R.id.tv_reduction_info);
        tvPsnrValue = findViewById(R.id.tv_psnr_value);
        tvNewFileName = findViewById(R.id.tv_new_file_name);
        tvNewUploadDate = findViewById(R.id.tv_new_upload_date);
        tvNewFileSize = findViewById(R.id.tv_new_file_size);
        btnComplete = findViewById(R.id.btn_complete);
        ImageView imageViewCompressed = findViewById(R.id.imageViewCompressed);

        String filePath = getIntent().getStringExtra("file_path");
        if (filePath != null) {
            Bitmap bitmap = BitmapFactory.decodeFile(filePath);
            imageViewCompressed.setImageBitmap(bitmap);
        }

        // Bạn có thể nhận dữ liệu từ Intent ở đây và cập nhật UI
        // Ví dụ:
        Intent intent = getIntent();
        if (intent != null) {
            // In onCreate, after getting intent extras
            String fileName = intent.getStringExtra("file_name");
            String fileSize = intent.getStringExtra("file_size");
            String compressionDate = intent.getStringExtra("compression_date");

            tvNewFileName.setText(fileName);
            tvNewFileSize.setText(fileSize);
            tvNewUploadDate.setText(compressionDate);
        }

        // Xử lý sự kiện khi nhấn nút "Hoàn tất"
        btnComplete.setOnClickListener(v -> {
            // Làm mới cache để hiển thị ảnh mới khi về màn hình chính
            StorageManager.refreshCache();
            
            // Chuyển về màn hình chính hoặc một màn hình khác
//             Ví dụ: finish(); // Đóng Activity hiện tại
//             Hoặc: Intent homeIntent = new Intent(ResultCompressActivity.this, MainActivity.class);
            Intent intentNext = new Intent(ResultCompressActivity.this, HomeActivity.class);
            startActivity(intentNext);
        });
    }
}