package android.compress; // Thay đổi thành tên gói của bạn

import androidx.appcompat.app.AppCompatActivity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

public class LoginActivity extends AppCompatActivity {

    private EditText usernameEditText;
    private TextInputEditText passwordEditText;
    private CheckBox rememberMeCheckBox;
    private TextView forgotPasswordTextView;
    private Button loginButton;
    private TextView registerTextView; // Đảm bảo đã khai báo

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        usernameEditText = findViewById(R.id.usernameEditText);
        passwordEditText = findViewById(R.id.passwordEditText);
        rememberMeCheckBox = findViewById(R.id.rememberMeCheckBox);
        forgotPasswordTextView = findViewById(R.id.forgotPasswordTextView);
        loginButton = findViewById(R.id.loginButton);
        registerTextView = findViewById(R.id.registerTextView); // Ánh xạ

        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String username = usernameEditText.getText().toString().trim();
                String password = passwordEditText.getText().toString().trim();

                if (username.isEmpty() || password.isEmpty()) {
                    Toast.makeText(LoginActivity.this, "Vui lòng nhập đầy đủ tên đăng nhập và mật khẩu.", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(LoginActivity.this, "Đăng nhập với Tên: " + username + " và Mật khẩu: " + password, Toast.LENGTH_LONG).show();
                    if (rememberMeCheckBox.isChecked()) {
                        Toast.makeText(LoginActivity.this, "Bạn đã chọn 'Nhớ thông tin đăng nhập'", Toast.LENGTH_SHORT).show();
                    }
                    // Sau khi đăng nhập thành công, có thể chuyển sang Activity khác
                    // Intent intent = new Intent(LoginActivity.this, HomeActivity.class);
                    // startActivity(intent);
                    // finish();
                }
            }
        });

        forgotPasswordTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(LoginActivity.this, "Bạn đã nhấn 'Quên mật khẩu'", Toast.LENGTH_SHORT).show();
            }
        });

        // Đã thay đổi: Chuyển sang RegisterActivity khi nhấn "Đăng ký"
        registerTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(LoginActivity.this, RegisterActivity.class);
                startActivity(intent);
                // Bạn có thể không cần finish() ở đây nếu muốn người dùng có thể quay lại màn hình đăng nhập bằng nút back
            }
        });
    }
}