package android.compress; // Thay 'com.example.yourappname' bằng package name của bạn

import android.app.ProgressDialog;
import android.compress.models.FirebaseManager;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;

import java.util.Objects;

public class ResetPasswordActivity extends AppCompatActivity {

    private TextInputEditText newPasswordEditText, confirmPasswordEditText;
    private ProgressDialog progressDialog;
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
        MaterialButton updatePasswordButton = findViewById(R.id.button_update_password);

        // Khởi tạo ProgressDialog
        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Đang cập nhật...");
        progressDialog.setCancelable(false);

        // Thiết lập sự kiện
        updatePasswordButton.setOnClickListener(v -> handleUpdatePassword());
    }

    /**
     * Xử lý logic khi người dùng nhấn nút Xác nhận để đổi mật khẩu.
     */
    private void handleUpdatePassword() {
        String newPassword = Objects.requireNonNull(newPasswordEditText.getText()).toString().trim();
        String confirmPassword = Objects.requireNonNull(confirmPasswordEditText.getText()).toString().trim();

        if (newPassword.isEmpty() || confirmPassword.isEmpty()) {
            Toast.makeText(this, "Vui lòng nhập đầy đủ mật khẩu", Toast.LENGTH_SHORT).show();
            return;
        }

        if (newPassword.length() < 6) {
            Toast.makeText(this, "Mật khẩu phải có ít nhất 6 ký tự.", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!newPassword.equals(confirmPassword)) {
            Toast.makeText(this, getString(R.string.passwords_do_not_match), Toast.LENGTH_SHORT).show();
            return;
        }

        progressDialog.show();

        // Gọi phương thức cập nhật mật khẩu từ FirebaseManager
        FirebaseManager.updatePassword(phone, newPassword, new FirebaseManager.SimpleCallback() {
            @Override
            public void onSuccess(String message) {
                progressDialog.dismiss();
                Toast.makeText(ResetPasswordActivity.this, message, Toast.LENGTH_LONG).show();

                // Chuyển về màn hình đăng nhập, xóa các màn hình cũ trên stack
                Intent intent = new Intent(ResetPasswordActivity.this, LoginActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
                finish();
            }

            @Override
            public void onFailure(String message) {
                progressDialog.dismiss();
                Toast.makeText(ResetPasswordActivity.this, message, Toast.LENGTH_LONG).show();
            }
        });
    }
}
