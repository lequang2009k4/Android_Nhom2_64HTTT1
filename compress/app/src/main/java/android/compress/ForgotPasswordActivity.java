package android.compress; // Thay 'com.example.yourappname' bằng package name của bạn

import android.app.ProgressDialog;
import android.compress.models.FirebaseManager;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;

public class ForgotPasswordActivity extends AppCompatActivity {

    private TextInputEditText phoneEditText;
    private ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forgot_password);
        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }

        // Ánh xạ View
        phoneEditText = findViewById(R.id.edit_text_forgot_phone);
        MaterialButton recoverButton = findViewById(R.id.button_recover_password);

        // Khởi tạo ProgressDialog
        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Đang gửi yêu cầu...");
        progressDialog.setCancelable(false);

        // Thiết lập sự kiện
        recoverButton.setOnClickListener(v -> handlePasswordReset());
        findViewById(R.id.text_go_back_login).setOnClickListener(v -> finish());
    }

    /**
     * Xử lý logic khi người dùng nhấn nút Lấy lại mật khẩu.
     */
    private void handlePasswordReset() {
        String phone = phoneEditText.getText().toString().trim();
        if (phone.isEmpty()) {
            Toast.makeText(this, "Vui lòng nhập số điện thoại", Toast.LENGTH_SHORT).show();
            return;
        }

        progressDialog.show();

        // Gọi phương thức gửi OTP từ FirebaseManager
        FirebaseManager.sendPasswordResetOtp(this, phone, new FirebaseManager.VerificationCallback() {
            @Override
            public void onVerified(String verificationId) {
                progressDialog.dismiss();
                Toast.makeText(ForgotPasswordActivity.this, "Mã OTP đã được gửi.", Toast.LENGTH_SHORT).show();

                // Chuyển sang màn hình OTP với các thông tin cần thiết
                Intent intent = new Intent(ForgotPasswordActivity.this, OtpActivity.class);
                intent.putExtra("flow_type", "forgot_password");
                intent.putExtra("verificationId", verificationId);
                intent.putExtra("phone", phone);
                startActivity(intent);
            }

            @Override
            public void onFailure(String message) {
                progressDialog.dismiss();
                Toast.makeText(ForgotPasswordActivity.this, message, Toast.LENGTH_LONG).show();
            }
        });
    }
}
