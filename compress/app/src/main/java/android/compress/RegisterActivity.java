package android.compress; // Thay đổi thành tên gói của bạn

import androidx.appcompat.app.AppCompatActivity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import com.google.android.material.textfield.TextInputEditText;

public class RegisterActivity extends AppCompatActivity {

    private EditText usernameEditText;
    private EditText phoneEditText;
    private TextInputEditText passwordEditText;
    private Button registerButton;
    private TextView loginTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        usernameEditText = findViewById(R.id.usernameRegisterEditText);
        phoneEditText = findViewById(R.id.phoneRegisterEditText);
        passwordEditText = findViewById(R.id.passwordRegisterEditText);
        registerButton = findViewById(R.id.registerButton);
        loginTextView = findViewById(R.id.loginTextView);

        registerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String username = usernameEditText.getText().toString().trim();
                String phone = phoneEditText.getText().toString().trim();
                String password = passwordEditText.getText().toString().trim();

                if (username.isEmpty() || phone.isEmpty() || password.isEmpty()) {
                    Toast.makeText(RegisterActivity.this, "Vui lòng điền đầy đủ thông tin.", Toast.LENGTH_SHORT).show();
                } else {
                    // Đây là nơi bạn sẽ thực hiện logic đăng ký thực tế
                    // Ví dụ: Gửi dữ liệu đăng ký và yêu cầu OTP đến server
                    Toast.makeText(RegisterActivity.this, "Đang đăng ký và gửi OTP đến SĐT: " + phone, Toast.LENGTH_LONG).show();

                    // Chuyển sang OtpVerificationActivity để nhập OTP
                    Intent intent = new Intent(RegisterActivity.this, OtpVerificationActivity.class);
                    // Bạn có thể gửi dữ liệu như số điện thoại qua Intent nếu cần
                    intent.putExtra("phone_number", phone);
                    startActivity(intent);
                    // finish(); // Có thể không finish nếu bạn muốn người dùng có thể quay lại
                }
            }
        });

        loginTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(RegisterActivity.this, LoginActivity.class);
                startActivity(intent);
                finish();
            }
        });
    }
}