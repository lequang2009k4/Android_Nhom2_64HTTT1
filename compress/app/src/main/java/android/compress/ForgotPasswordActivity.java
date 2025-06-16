package android.compress;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.compress.models.User;
import android.content.Intent;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import com.google.firebase.FirebaseException;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthOptions;
import com.google.firebase.auth.PhoneAuthProvider;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.concurrent.TimeUnit;

public class ForgotPasswordActivity extends AppCompatActivity {

    private static final String TAG = "ForgotPasswordActivity";

    private EditText phoneForgotPasswordEditText;
    private TextInputEditText newPasswordEditText; // Changed to TextInputEditText
    private EditText otpCodeEditText;
    private Button sendOtpButton;
    private Button resetPasswordButton; // Nút Đặt lại mật khẩu
    private TextView goBackToLoginTextView;
    private TextView otpTimerTextView;

    private FirebaseAuth mAuth;
    private DatabaseReference mDatabase; // Cần để tìm email ảo từ SĐT

    private String verificationId;
    private PhoneAuthProvider.ForceResendingToken resendToken;
    private CountDownTimer otpResetTimer; // Hẹn giờ cho OTP reset
    private long timeLeftInMillis = 60000; // 60 giây

    private String userEmailForReset = null; // Email ảo của user cần reset

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forgot_password);

        mAuth = FirebaseAuth.getInstance();
        mDatabase = FirebaseDatabase.getInstance().getReference(); // Khởi tạo Realtime Database

        phoneForgotPasswordEditText = findViewById(R.id.phoneForgotPasswordEditText);
        newPasswordEditText = findViewById(R.id.newPasswordEditText);
        otpCodeEditText = findViewById(R.id.otpCodeEditText);
        sendOtpButton = findViewById(R.id.sendOtpButton);
        resetPasswordButton = findViewById(R.id.getPasswordButton); // ID của nút "Đặt lại mật khẩu"
        goBackToLoginTextView = findViewById(R.id.goBackToLoginTextView);
        otpTimerTextView = findViewById(R.id.otpTimerTextView);

        // Ban đầu ẩn các trường OTP và mật khẩu mới
        newPasswordEditText.setVisibility(View.GONE);
        findViewById(R.id.newPasswordInputLayout).setVisibility(View.GONE); // Ẩn TextInputLayout
        otpCodeEditText.setVisibility(View.GONE);
        resetPasswordButton.setVisibility(View.GONE);
        otpTimerTextView.setVisibility(View.GONE);

        sendOtpButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String phoneNumber = phoneForgotPasswordEditText.getText().toString().trim();
                if (phoneNumber.isEmpty()) {
                    Toast.makeText(ForgotPasswordActivity.this, "Vui lòng nhập số điện thoại.", Toast.LENGTH_SHORT).show();
                    return;
                }
                if (!phoneNumber.startsWith("+")) {
                    phoneNumber = "+84" + phoneNumber; // Thêm tiền tố quốc gia nếu chưa có
                }

                // Bước 1: Tìm email ảo của người dùng từ số điện thoại trong Realtime Database
                findUserEmailByPhoneNumberAndSendOtp(phoneNumber);
            }
        });

        resetPasswordButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String newPassword = newPasswordEditText.getText().toString().trim();
                String otpCode = otpCodeEditText.getText().toString().trim();

                if (newPassword.isEmpty() || otpCode.isEmpty()) {
                    Toast.makeText(ForgotPasswordActivity.this, "Vui lòng điền đầy đủ thông tin.", Toast.LENGTH_SHORT).show();
                    return;
                }
                if (newPassword.length() < 6) {
                    Toast.makeText(ForgotPasswordActivity.this, "Mật khẩu mới phải có ít nhất 6 ký tự.", Toast.LENGTH_SHORT).show();
                    return;
                }
                if (verificationId != null) {
                    PhoneAuthCredential credential = PhoneAuthProvider.getCredential(verificationId, otpCode);
                    // Bước 3: Xác minh OTP và đặt lại mật khẩu
                    verifyOtpAndResetPassword(credential, newPassword);
                } else {
                    Toast.makeText(ForgotPasswordActivity.this, "Vui lòng gửi mã OTP trước.", Toast.LENGTH_SHORT).show();
                }
            }
        });

        goBackToLoginTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(ForgotPasswordActivity.this, LoginActivity.class);
                startActivity(intent);
                finish();
            }
        });
    }

    private void findUserEmailByPhoneNumberAndSendOtp(String phoneNumber) {
        Query phoneQuery = mDatabase.child("users").orderByChild("phoneNumber").equalTo(phoneNumber);
        phoneQuery.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    for (DataSnapshot userSnapshot : snapshot.getChildren()) {
                        User user = userSnapshot.getValue(User.class);
                        if (user != null && user.email != null) {
                            userEmailForReset = user.email; // Lưu email ảo của người dùng
                            break;
                        }
                    }
                    if (userEmailForReset != null) {
                        // Bước 2: Gửi OTP đến số điện thoại đã tìm thấy
                        sendVerificationCodeForReset(phoneNumber);
                    } else {
                        Toast.makeText(ForgotPasswordActivity.this, "Không tìm thấy tài khoản liên kết với số điện thoại này.", Toast.LENGTH_LONG).show();
                    }
                } else {
                    Toast.makeText(ForgotPasswordActivity.this, "Số điện thoại không tồn tại trong hệ thống.", Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(ForgotPasswordActivity.this, "Lỗi tìm kiếm số điện thoại: " + error.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }

    private void sendVerificationCodeForReset(String phoneNumber) {
        PhoneAuthOptions options =
                PhoneAuthOptions.newBuilder(mAuth)
                        .setPhoneNumber(phoneNumber)
                        .setTimeout(60L, TimeUnit.SECONDS)
                        .setActivity(this)
                        .setCallbacks(mPhoneCallbacksForReset)
                        .build();
        PhoneAuthProvider.verifyPhoneNumber(options);

        // Bắt đầu đếm ngược
        if (otpResetTimer != null) {
            otpResetTimer.cancel();
        }
        timeLeftInMillis = 60000;
        startResetOtpTimer();

        // Hiển thị các trường nhập OTP và mật khẩu mới
        newPasswordEditText.setVisibility(View.VISIBLE);
        findViewById(R.id.newPasswordInputLayout).setVisibility(View.VISIBLE); // Hiển thị TextInputLayout
        otpCodeEditText.setVisibility(View.VISIBLE);
        resetPasswordButton.setVisibility(View.VISIBLE);
        sendOtpButton.setVisibility(View.GONE); // Ẩn nút gửi OTP ban đầu
        otpTimerTextView.setVisibility(View.VISIBLE); // Hiển thị hẹn giờ

        Toast.makeText(this, "Mã OTP đã được gửi đến " + phoneNumber, Toast.LENGTH_LONG).show();
    }

    private PhoneAuthProvider.OnVerificationStateChangedCallbacks mPhoneCallbacksForReset =
            new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {

                @Override
                public void onVerificationCompleted(@NonNull PhoneAuthCredential credential) {
                    Log.d(TAG, "onVerificationCompleted for reset: " + credential);
                    // Tự động xác minh, tiến hành đặt lại mật khẩu
                    verifyOtpAndResetPassword(credential, newPasswordEditText.getText().toString().trim());
                }

                @Override
                public void onVerificationFailed(@NonNull FirebaseException e) {
                    Log.w(TAG, "onVerificationFailed for reset", e);
                    Toast.makeText(ForgotPasswordActivity.this, "Xác minh SĐT thất bại: " + e.getMessage(), Toast.LENGTH_LONG).show();
                    sendOtpButton.setVisibility(View.VISIBLE); // Hiển thị lại nút gửi OTP
                    newPasswordEditText.setVisibility(View.GONE);
                    findViewById(R.id.newPasswordInputLayout).setVisibility(View.GONE);
                    otpCodeEditText.setVisibility(View.GONE);
                    resetPasswordButton.setVisibility(View.GONE);
                    otpTimerTextView.setVisibility(View.GONE);
                    if (otpResetTimer != null) otpResetTimer.cancel();
                }

                @Override
                public void onCodeSent(@NonNull String verificationCode,
                                       @NonNull PhoneAuthProvider.ForceResendingToken token) {
                    Log.d(TAG, "onCodeSent for reset: " + verificationCode);
                    verificationId = verificationCode;
                    resendToken = token;
                    Toast.makeText(ForgotPasswordActivity.this, "Mã OTP đã được gửi.", Toast.LENGTH_SHORT).show();
                }
            };

    private void verifyOtpAndResetPassword(PhoneAuthCredential credential, String newPassword) {
        if (userEmailForReset == null) {
            Toast.makeText(this, "Không tìm thấy email tài khoản để đặt lại mật khẩu.", Toast.LENGTH_SHORT).show();
            return;
        }

        // Bước 3a: Đăng nhập lại bằng Phone Credential để có FirebaseUser hiện tại
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        // Xác thực OTP thành công, giờ có thể đặt lại mật khẩu
                        FirebaseUser user = mAuth.getCurrentUser();
                        if (user != null) {
                            // Cập nhật mật khẩu cho tài khoản Email/Password đã liên kết
                            user.updatePassword(newPassword)
                                    .addOnCompleteListener(passwordUpdateTask -> {
                                        if (passwordUpdateTask.isSuccessful()) {
                                            Toast.makeText(ForgotPasswordActivity.this, "Đặt lại mật khẩu thành công!", Toast.LENGTH_LONG).show();
                                            // Chuyển về màn hình đăng nhập
                                            Intent intent = new Intent(ForgotPasswordActivity.this, LoginActivity.class);
                                            startActivity(intent);
                                            finish();
                                        } else {
                                            Toast.makeText(ForgotPasswordActivity.this, "Lỗi đặt lại mật khẩu: " + passwordUpdateTask.getException().getMessage(), Toast.LENGTH_LONG).show();
                                            Log.e(TAG, "Password update failed: " + passwordUpdateTask.getException().getMessage());
                                        }
                                    });
                        }
                    } else {
                        Toast.makeText(ForgotPasswordActivity.this, "Xác minh OTP thất bại: " + task.getException().getMessage(), Toast.LENGTH_LONG).show();
                        Log.e(TAG, "OTP verification failed for reset: " + task.getException().getMessage());
                    }
                });
    }

    private void startResetOtpTimer() {
        otpResetTimer = new CountDownTimer(timeLeftInMillis, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                timeLeftInMillis = millisUntilFinished;
                long seconds = millisUntilFinished / 1000;
                otpTimerTextView.setText("Gửi lại sau " + seconds + "s");
                sendOtpButton.setEnabled(false); // Vô hiệu hóa nút gửi OTP
            }

            @Override
            public void onFinish() {
                sendOtpButton.setEnabled(true);
                otpTimerTextView.setText("Gửi lại OTP");
            }
        }.start();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (otpResetTimer != null) {
            otpResetTimer.cancel();
        }
    }
}