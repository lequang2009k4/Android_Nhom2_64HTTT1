package android.compress;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

public class ForgotPasswordActivity extends AppCompatActivity {

    private EditText phoneEditText;
    private Button getPasswordButton;
    private TextView goBackToLoginTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forgot_password);

        // Ánh xạ các View
        phoneEditText = findViewById(R.id.phoneForgotPasswordEditText);
        getPasswordButton = findViewById(R.id.getPasswordButton);
        goBackToLoginTextView = findViewById(R.id.goBackToLoginTextView);

        // Xử lý sự kiện cho nút "Lấy lại mật khẩu"
        getPasswordButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String phoneNumber = phoneEditText.getText().toString().trim();

                if (phoneNumber.isEmpty()) {
                    Toast.makeText(ForgotPasswordActivity.this, "Vui lòng nhập số điện thoại.", Toast.LENGTH_SHORT).show();
                } else {
                    // Đây là nơi bạn sẽ thực hiện logic lấy lại mật khẩu thực tế.
                    // Thông thường sẽ gửi OTP đến số điện thoại này, sau đó chuyển sang màn hình nhập OTP.
                    Toast.makeText(ForgotPasswordActivity.this, "Đang xử lý lấy lại mật khẩu cho SĐT: " + phoneNumber, Toast.LENGTH_LONG).show();

                    // Ví dụ: Sau khi gửi yêu cầu thành công, chuyển sang màn hình OTP để xác nhận
                    // Intent intent = new Intent(ForgotPasswordActivity.this, OtpVerificationActivity.class);
                    // intent.putExtra("phone_number", phoneNumber); // Truyền số điện thoại
                    // startActivity(intent);
                    // finish(); // Tùy chọn: đóng màn hình này
                }
            }
        });

        // Xử lý sự kiện cho "Đã nhớ ra mật khẩu? Quay lại"
        goBackToLoginTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(ForgotPasswordActivity.this, LoginActivity.class);
                startActivity(intent);
                finish(); // Đóng ForgotPasswordActivity để quay lại LoginActivity
            }
        });
    }
}