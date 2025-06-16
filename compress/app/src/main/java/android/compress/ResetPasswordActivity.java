package android.compress; // Thay 'com.example.yourappname' bằng package name của bạn

import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;

import java.util.Objects;

public class ResetPasswordActivity extends AppCompatActivity {

    private TextInputEditText newPasswordEditText, confirmPasswordEditText;
    private MaterialButton updatePasswordButton;

    private String phone; // Để nhận số điện thoại từ màn hình OTP

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reset_password);

        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }

        // Nhận dữ liệu từ OtpActivity
        phone = getIntent().getStringExtra("phone");

        // Ánh xạ view
        newPasswordEditText = findViewById(R.id.edit_text_new_password);
        confirmPasswordEditText = findViewById(R.id.edit_text_confirm_password);
        updatePasswordButton = findViewById(R.id.button_update_password);

        updatePasswordButton.setOnClickListener(v -> {
            String newPassword = Objects.requireNonNull(newPasswordEditText.getText()).toString().trim();
            String confirmPassword = Objects.requireNonNull(confirmPasswordEditText.getText()).toString().trim();

            if (newPassword.isEmpty() || confirmPassword.isEmpty()) {
                Toast.makeText(this, "Vui lòng nhập đầy đủ mật khẩu", Toast.LENGTH_SHORT).show();
                return;
            }

            if (!newPassword.equals(confirmPassword)) {
                Toast.makeText(this, getString(R.string.passwords_do_not_match), Toast.LENGTH_SHORT).show();
                return;
            }

            // TODO: Xử lý cập nhật mật khẩu mới vào Firebase
            Toast.makeText(this, "Đang cập nhật mật khẩu...", Toast.LENGTH_SHORT).show();

            // Sau khi cập nhật thành công
            Toast.makeText(this, getString(R.string.password_updated_success), Toast.LENGTH_LONG).show();

            // Chuyển về màn hình đăng nhập, xóa các màn hình cũ
            Intent intent = new Intent(ResetPasswordActivity.this, LoginActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
            finish();
        });
    }
}
