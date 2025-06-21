package android.compress; // Đảm bảo package này khớp với namespace của bạn

import android.os.Bundle;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;

public class DetailActivity extends AppCompatActivity {

    private TextView tvFileName;
    private TextView tvUploadDate;
    private TextView tvFileSize;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);

        tvFileName = findViewById(R.id.tv_file_name);
        tvUploadDate = findViewById(R.id.tv_upload_date);
        tvFileSize = findViewById(R.id.tv_file_size);

        // Đặt dữ liệu mẫu (bạn sẽ nhận dữ liệu thực từ Intent hoặc các nguồn khác)
        // tvFileName.setText("ten_file_thuc.jpg");
        // tvUploadDate.setText("01/01/2026");
        // tvFileSize.setText("5mb");

        // Ví dụ xử lý nút "Nén ngay"
        // Button btnCompressNow = findViewById(R.id.btn_compress_now);
        // btnCompressNow.setOnClickListener(v -> {
        //     // Logic để nén file
        // });
    }
}