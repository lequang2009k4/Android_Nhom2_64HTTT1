package android.compress; // Thay 'com.example.yourappname' bằng package name của bạn

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
    private MaterialButton confirmButton, resendButton;
    private TextView countdownTextView, goBackTextView;
    private CountDownTimer countDownTimer;

    // Biến để lưu thông tin từ Intent
    private String username, phone, password;
    private String flowType; // Sẽ là "register" hoặc "forgot_password"

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_otp);

        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }

        // Lấy dữ liệu được gửi từ Activity trước
        Intent intent = getIntent();
        phone = intent.getStringExtra("phone");
        flowType = intent.getStringExtra("flow_type");

        // Nếu là luồng đăng ký thì mới lấy username và password
        if ("register".equals(flowType)) {
            username = intent.getStringExtra("username");
            password = intent.getStringExtra("password");
        }

        // Ánh xạ các view
        otpEditText = findViewById(R.id.edit_text_otp);
        confirmButton = findViewById(R.id.button_confirm);
        resendButton = findViewById(R.id.button_resend_otp);
        countdownTextView = findViewById(R.id.text_countdown);
        goBackTextView = findViewById(R.id.text_go_back);

        setupListeners();
        startCountdown();
    }

    private void setupListeners() {
        confirmButton.setOnClickListener(v -> {
            String otp = Objects.requireNonNull(otpEditText.getText()).toString().trim();
            if (otp.length() < 6) {
                Toast.makeText(this, "Vui lòng nhập đủ 6 số OTP", Toast.LENGTH_SHORT).show();
            } else {
                // Xử lý logic dựa trên luồng (`flowType`)
                if ("register".equals(flowType)) {
                    verifyOtpForRegistration(otp);
                } else if ("forgot_password".equals(flowType)) {
                    verifyOtpForPasswordReset(otp);
                }
            }
        });

        resendButton.setOnClickListener(v -> {
            // TODO: Xử lý gửi lại OTP với Firebase
            Toast.makeText(this, "Đang gửi lại mã OTP...", Toast.LENGTH_SHORT).show();
            startCountdown();
        });

        goBackTextView.setOnClickListener(v -> {
            // Quay lại màn hình trước đó
            finish();
        });
    }

    /**
     * Xác thực OTP cho luồng đăng ký tài khoản mới.
     * @param otp Mã OTP người dùng nhập.
     */
    private void verifyOtpForRegistration(String otp) {
        // TODO: Xử lý xác thực OTP và tạo tài khoản mới với Firebase.
        Toast.makeText(this, "Đang xác thực OTP để Đăng ký...", Toast.LENGTH_SHORT).show();
        // Nếu thành công -> Chuyển đến màn hình chính của ứng dụng.
    }

    /**
     * Xác thực OTP cho luồng lấy lại mật khẩu.
     * @param otp Mã OTP người dùng nhập.
     */
    private void verifyOtpForPasswordReset(String otp) {
        // TODO: Xử lý xác thực OTP với Firebase.
        Toast.makeText(this, "Xác thực OTP thành công!", Toast.LENGTH_SHORT).show();

        // Nếu OTP đúng, chuyển sang màn hình tạo mật khẩu mới.
        Intent intent = new Intent(OtpActivity.this, ResetPasswordActivity.class);
        intent.putExtra("phone", phone); // Gửi số điện thoại sang để xử lý bước cuối
        startActivity(intent);
        finish(); // Đóng màn hình OTP sau khi hoàn tất.
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
        // Rất quan trọng: Hủy timer để tránh rò rỉ bộ nhớ khi Activity bị hủy.
        if (countDownTimer != null) {
            countDownTimer.cancel();
        }
    }
}
