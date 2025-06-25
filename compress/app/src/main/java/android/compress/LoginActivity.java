package android.compress;

import android.app.ProgressDialog;
import android.compress.models.FirebaseManager;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.CheckBox;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;

public class LoginActivity extends AppCompatActivity {

    private TextInputEditText usernameEditText, passwordEditText;
    private ProgressDialog progressDialog;

    // Các biến cho tính năng "Ghi nhớ đăng nhập"
    private CheckBox rememberMeCheckBox;
    private SharedPreferences loginPreferences;
    private static final String PREFS_NAME = "login_prefs";
    private static final String KEY_USERNAME = "username";
    private static final String KEY_PASSWORD = "password";
    private static final String KEY_REMEMBER = "remember";

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
        rememberMeCheckBox = findViewById(R.id.checkbox_remember_me);

        // Khởi tạo ProgressDialog
        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Đang đăng nhập...");
        progressDialog.setCancelable(false);

        // Tải thông tin đăng nhập đã lưu (nếu có)
        loginPreferences = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        loadCredentials();

        // Thiết lập sự kiện
        loginButton.setOnClickListener(v -> handleLogin());
        findViewById(R.id.text_register).setOnClickListener(v -> startActivity(new Intent(this, RegisterActivity.class)));
        findViewById(R.id.text_forgot_password).setOnClickListener(v -> startActivity(new Intent(this, ForgotPasswordActivity.class)));
    }

    /**
     * Tải thông tin đăng nhập từ SharedPreferences.
     */
    private void loadCredentials() {
        boolean shouldRemember = loginPreferences.getBoolean(KEY_REMEMBER, false);
        if (shouldRemember) {
            usernameEditText.setText(loginPreferences.getString(KEY_USERNAME, ""));
            passwordEditText.setText(loginPreferences.getString(KEY_PASSWORD, ""));
            rememberMeCheckBox.setChecked(true);
        }
    }

    /**
     * Lưu hoặc xóa thông tin đăng nhập dựa vào CheckBox.
     */
    private void manageCredentials() {
        SharedPreferences.Editor editor = loginPreferences.edit();
        if (rememberMeCheckBox.isChecked()) {
            editor.putString(KEY_USERNAME, usernameEditText.getText().toString().trim());
            editor.putString(KEY_PASSWORD, passwordEditText.getText().toString().trim());
            editor.putBoolean(KEY_REMEMBER, true);
        } else {
            editor.clear();
        }
        editor.apply();
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

        // Quản lý việc lưu/xóa thông tin đăng nhập
        manageCredentials();

        // Gọi phương thức đăng nhập từ FirebaseManager
        FirebaseManager.loginWithUsername(username, password, new FirebaseManager.AuthCallback() {
            @Override
            public void onSuccess(FirebaseManager.User user) {
                progressDialog.dismiss();
                Toast.makeText(LoginActivity.this, "Đăng nhập thành công!", Toast.LENGTH_SHORT).show();

                // Điều hướng dựa trên vai trò
                if ("admin".equals(user.getRole())) {
                    startActivity(new Intent(LoginActivity.this, AdminDashboardActivity.class));
                } else {
                    // TODO: Chuyển đến màn hình chính cho user thường
                     startActivity(new Intent(LoginActivity.this, HomeActivity.class));
                    Toast.makeText(LoginActivity.this, "Chào mừng user " + user.getUsername(), Toast.LENGTH_SHORT).show();
                }
                finish(); // Đóng màn hình đăng nhập
            }

            @Override
            public void onFailure(String message) {
                progressDialog.dismiss();
                Toast.makeText(LoginActivity.this, message, Toast.LENGTH_LONG).show();
            }
        });
    }
}
