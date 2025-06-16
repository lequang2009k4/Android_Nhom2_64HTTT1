package android.compress; // Thay 'com.example.yourappname' bằng package name của bạn

import android.app.ProgressDialog;
import android.compress.models.FirebaseManager;
import android.content.Intent;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;

import java.util.Locale;
import java.util.Objects;

public class OtpActivity extends AppCompatActivity {

    private TextInputEditText otpEditText;
    private MaterialButton resendButton;
    private TextView countdownTextView;
    private CountDownTimer countDownTimer;
    private ProgressDialog progressDialog;

    // Các biến để nhận dữ liệu từ Intent
    private String flowType, verificationId, username, phone, password;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_otp);
        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }

        // Lấy dữ liệu từ Activity trước
        Intent intent = getIntent();
        flowType = intent.getStringExtra("flow_type");
        verificationId = intent.getStringExtra("verificationId");
        phone = intent.getStringExtra("phone");
        if ("register".equals(flowType)) {
            username = intent.getStringExtra("username");
            password = intent.getStringExtra("password");
        }

        // Ánh xạ View
        otpEditText = findViewById(R.id.edit_text_otp);
        resendButton = findViewById(R.id.button_resend_otp);
        countdownTextView = findViewById(R.id.text_countdown);
        MaterialButton confirmButton = findViewById(R.id.button_confirm);

        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Đang xác thực...");
        progressDialog.setCancelable(false);

        // Thiết lập sự kiện
        confirmButton.setOnClickListener(v -> handleConfirmOtp());
        findViewById(R.id.text_go_back).setOnClickListener(v -> finish());

        startCountdown();
    }

    /**
     * Xử lý việc xác nhận OTP dựa trên luồng (đăng ký hoặc quên mật khẩu).
     */
    private void handleConfirmOtp() {
        String otp = Objects.requireNonNull(otpEditText.getText()).toString().trim();
        if (otp.length() < 6) {
            Toast.makeText(this, "Vui lòng nhập đủ 6 số OTP", Toast.LENGTH_SHORT).show();
            return;
        }

        progressDialog.show();

        if ("register".equals(flowType)) {
            // Luồng đăng ký
            FirebaseManager.User user = new FirebaseManager.User(username, phone, password);
            FirebaseManager.verifyOtpAndRegisterUser(verificationId, otp, user, new FirebaseManager.SimpleCallback() {
                @Override
                public void onSuccess(String message) {
                    progressDialog.dismiss();
                    Toast.makeText(OtpActivity.this, message, Toast.LENGTH_LONG).show();
                    // Chuyển về màn hình đăng nhập sau khi đăng ký thành công
                    Intent intent = new Intent(OtpActivity.this, LoginActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(intent);
                    finish();
                }

                @Override
                public void onFailure(String message) {
                    progressDialog.dismiss();
                    Toast.makeText(OtpActivity.this, message, Toast.LENGTH_LONG).show();
                }
            });
        } else if ("forgot_password".equals(flowType)) {
            // Luồng quên mật khẩu
            FirebaseManager.verifyOtp(verificationId, otp, new FirebaseManager.SimpleCallback() {
                @Override
                public void onSuccess(String message) {
                    progressDialog.dismiss();
                    Toast.makeText(OtpActivity.this, message, Toast.LENGTH_SHORT).show();
                    // Chuyển sang màn hình tạo mật khẩu mới
                    Intent intent = new Intent(OtpActivity.this, ResetPasswordActivity.class);
                    intent.putExtra("phone", phone);
                    startActivity(intent);
                    finish();
                }

                @Override
                public void onFailure(String message) {
                    progressDialog.dismiss();
                    Toast.makeText(OtpActivity.this, message, Toast.LENGTH_LONG).show();
                }
            });
        }
    }

    /**
     * Bắt đầu hoặc khởi động lại bộ đếm ngược 90 giây.
     */
    private void startCountdown() {
        resendButton.setEnabled(false);
        countdownTextView.setVisibility(View.VISIBLE);

        if (countDownTimer != null) {
            countDownTimer.cancel();
        }

        countDownTimer = new CountDownTimer(90000, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                long seconds = millisUntilFinished / 1000;
                countdownTextView.setText(String.format(Locale.getDefault(), "Vui lòng chờ %d giây để gửi lại", seconds));
            }

            @Override
            public void onFinish() {
                resendButton.setEnabled(true);
                countdownTextView.setVisibility(View.GONE);
            }
        }.start();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Hủy timer để tránh rò rỉ bộ nhớ
        if (countDownTimer != null) {
            countDownTimer.cancel();
        }
    }
}
