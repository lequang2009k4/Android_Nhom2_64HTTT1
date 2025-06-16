package android.compress; // Thay 'com.example.yourappname' bằng package name của bạn

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;

public class RegisterActivity extends AppCompatActivity {

    private TextInputEditText usernameEditText, phoneEditText, passwordEditText;
    private MaterialButton registerButton;
    private TextView loginLinkTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        // Ẩn Action Bar
        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }

        // Ánh xạ các view
        usernameEditText = findViewById(R.id.edit_text_register_username);
        phoneEditText = findViewById(R.id.edit_text_phone);
        passwordEditText = findViewById(R.id.edit_text_register_password);
        registerButton = findViewById(R.id.button_register);
        loginLinkTextView = findViewById(R.id.text_login_link);

        // Sự kiện click cho nút Đăng ký
        registerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String username = usernameEditText.getText().toString().trim();
                String phone = phoneEditText.getText().toString().trim();
                String password = passwordEditText.getText().toString().trim();

                if (username.isEmpty() || phone.isEmpty() || password.isEmpty()) {
                    Toast.makeText(RegisterActivity.this, "Vui lòng nhập đầy đủ thông tin", Toast.LENGTH_SHORT).show();
                } else {
                    // TODO: Xử lý logic đăng ký với Firebase tại đây
                    // Bước 1: Gửi OTP đến số điện thoại
                    Toast.makeText(RegisterActivity.this, "Đang xử lý đăng ký...", Toast.LENGTH_SHORT).show();
                }
            }
        });

        // Sự kiện click cho link "Đăng nhập"
        loginLinkTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Quay lại màn hình Đăng nhập
                finish(); // Đóng Activity hiện tại
            }
        });
    }
}
