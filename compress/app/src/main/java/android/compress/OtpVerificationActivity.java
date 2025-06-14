package android.compress; // Thay đổi thành tên gói của bạn

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

public class OtpVerificationActivity extends AppCompatActivity {

    private EditText otpEditText;
    private Button resendOtpButton;
    private TextView resendTimerTextView;
    private Button verifyOtpButton;
    private TextView changePhoneTextView;
    private TextView goBackTextView;

    private CountDownTimer otpResendTimer;
    private long timeLeftInMillis = 90000; // 90 seconds

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_otp_verification);

        // Ánh xạ các View
        otpEditText = findViewById(R.id.otpEditText);
        resendOtpButton = findViewById(R.id.resendOtpButton);
        resendTimerTextView = findViewById(R.id.resendTimerTextView);
        verifyOtpButton = findViewById(R.id.verifyOtpButton);
        changePhoneTextView = findViewById(R.id.changePhoneTextView);
        goBackTextView = findViewById(R.id.goBackTextView);

        // Bắt đầu đếm ngược OTP khi Activity được tạo
        startOtpResendTimer();

        // Xử lý sự kiện cho nút "Gửi lại mã OTP"
        resendOtpButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Tắt nút gửi lại và bắt đầu lại đếm ngược
                resendOtpButton.setEnabled(false);
                resendOtpButton.setText("Đang gửi lại...");
                // Thực hiện logic gửi lại OTP (gọi API, etc.)
                Toast.makeText(OtpVerificationActivity.this, "Đang gửi lại mã OTP...", Toast.LENGTH_SHORT).show();

                // Đặt lại bộ đếm thời gian
                timeLeftInMillis = 90000; // Reset 90 giây
                if (otpResendTimer != null) {
                    otpResendTimer.cancel();
                }
                startOtpResendTimer();
            }
        });

        // Xử lý sự kiện cho nút "Xác nhận"
        verifyOtpButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String otp = otpEditText.getText().toString().trim();

                if (otp.isEmpty() || otp.length() < 6) {
                    Toast.makeText(OtpVerificationActivity.this, "Vui lòng nhập mã OTP hợp lệ (6 chữ số).", Toast.LENGTH_SHORT).show();
                } else {
                    // Logic xác thực OTP thực tế (gọi API, kiểm tra OTP)
                    Toast.makeText(OtpVerificationActivity.this, "Đang xác nhận mã OTP: " + otp, Toast.LENGTH_LONG).show();

                    // Ví dụ: Nếu OTP đúng, chuyển người dùng đến màn hình chính
                    // Intent intent = new Intent(OtpVerificationActivity.this, HomeActivity.class);
                    // startActivity(intent);
                    // finish(); // Đóng Activity này
                }
            }
        });

        // Xử lý sự kiện cho "Thay đổi số điện thoại" (quay lại màn hình đăng ký)
        changePhoneTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(OtpVerificationActivity.this, RegisterActivity.class);
                startActivity(intent);
                finish(); // Đóng OtpVerificationActivity
            }
        });

        // Xử lý sự kiện cho "Quay lại" (quay lại màn hình đăng ký)
        goBackTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed(); // Sử dụng phương thức back của hệ thống
            }
        });
    }

    private void startOtpResendTimer() {
        otpResendTimer = new CountDownTimer(timeLeftInMillis, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                timeLeftInMillis = millisUntilFinished;
                long seconds = millisUntilFinished / 1000;
                resendTimerTextView.setText("Vui lòng chờ " + seconds + " giây để gửi lại");
            }

            @Override
            public void onFinish() {
                resendOtpButton.setEnabled(true);
                resendOtpButton.setText("GỬI LẠI MÃ OTP");
                resendTimerTextView.setText(""); // Xóa thông báo đếm ngược
            }
        }.start();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (otpResendTimer != null) {
            otpResendTimer.cancel(); // Đảm bảo hủy timer khi Activity bị hủy
        }
    }
}