package android.compress; // Thay 'com.example.yourappname' bằng package name của bạn

import android.app.ProgressDialog;
import android.compress.models.FirebaseManager;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;

public class LoginActivity extends AppCompatActivity {

    private TextInputEditText usernameEditText, passwordEditText;
    private ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }

        // Ánh xạ View
        usernameEditText = findViewById(R.id.edit_text_username);
        passwordEditText = findViewById(R.id.edit_text_password);
        MaterialButton loginButton = findViewById(R.id.button_login);

        // Khởi tạo ProgressDialog
        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Đang đăng nhập...");
        progressDialog.setCancelable(false);

        // Thiết lập sự kiện
        loginButton.setOnClickListener(v -> handleLogin());
        findViewById(R.id.text_register).setOnClickListener(v -> startActivity(new Intent(this, RegisterActivity.class)));
        findViewById(R.id.text_forgot_password).setOnClickListener(v -> startActivity(new Intent(this, ForgotPasswordActivity.class)));
    }

    /**
     * Xử lý logic khi người dùng nhấn nút Đăng nhập.
     */
    private void handleLogin() {
        String username = usernameEditText.getText().toString().trim();
        String password = passwordEditText.getText().toString().trim();

        if (username.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Vui lòng nhập đầy đủ thông tin", Toast.LENGTH_SHORT).show();
            return;
        }

        progressDialog.show();

        // Gọi phương thức đăng nhập từ FirebaseManager
        FirebaseManager.loginWithUsername(username, password, new FirebaseManager.AuthCallback() {
            @Override
            public void onSuccess(FirebaseManager.User user) {
                progressDialog.dismiss();
                Toast.makeText(LoginActivity.this, "Đăng nhập thành công! Role: " + user.getRole(), Toast.LENGTH_LONG).show();

                // TODO: Dựa vào user.getRole() để chuyển đến màn hình tương ứng (User/Admin)
//                 Ví dụ:
                 if ("admin".equals(user.getRole())) {
//                     startActivity(new Intent(LoginActivity.this, AdminDashboardActivity.class));
                 } else {
                     startActivity(new Intent(LoginActivity.this, HomeActivity.class));
                 }
                 finish();
            }

            @Override
            public void onFailure(String message) {
                progressDialog.dismiss();
                Toast.makeText(LoginActivity.this, message, Toast.LENGTH_LONG).show();
            }
        });
    }
}
