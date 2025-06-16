package android.compress;


import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.compress.models.User;
import android.content.Intent;
import android.os.Bundle;
import android.os.CountDownTimer; // Thêm import này
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout; // Thêm import này

import com.google.firebase.FirebaseException;
import com.google.firebase.auth.AuthResult;
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

public class LoginActivity extends AppCompatActivity {

    private static final String TAG = "LoginActivity";

    // Các thành phần cho Username/Password Login
    private EditText usernameEditText;
    private TextInputEditText passwordEditText;
    private CheckBox rememberMeCheckBox;
    private TextView forgotPasswordTextView;
    private Button loginButton;
    private TextView registerTextView;
    private LinearLayout usernamePasswordLayout; // Layout bao quanh username/pass

    // Các thành phần cho Phone Auth Login
    private LinearLayout phoneAuthLayout;
    private EditText phoneAuthEditText;
    private EditText otpAuthEditText;
    private Button sendOtpLoginButton;
    private Button verifyOtpLoginButton;
    private TextView switchAuthMethodTextView; // TextView để chuyển đổi phương thức

    private FirebaseAuth mAuth;
    private DatabaseReference mDatabase;

    // Biến cho Phone Auth
    private String verificationId;
    private PhoneAuthProvider.ForceResendingToken resendToken;
    private CountDownTimer otpLoginTimer; // Hẹn giờ cho OTP login
    private long timeLeftInMillis = 60000; // 60 giây

    private static final String FIREBASE_EMAIL_DOMAIN = "@yourappdomain.com"; // Tên miền ảo

    private boolean isUsernamePasswordMode = true; // Biến trạng thái để chuyển đổi giao diện

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        mAuth = FirebaseAuth.getInstance();
        mDatabase = FirebaseDatabase.getInstance().getReference(); // Khởi tạo Realtime Database

        // Ánh xạ các View cho Username/Password Login
        usernamePasswordLayout = findViewById(R.id.passwordEditText);
        usernameEditText = findViewById(R.id.usernameEditText);
        passwordEditText = findViewById(R.id.passwordEditText);
        rememberMeCheckBox = findViewById(R.id.rememberMeCheckBox);
        forgotPasswordTextView = findViewById(R.id.forgotPasswordTextView);
        loginButton = findViewById(R.id.loginButton);
        registerTextView = findViewById(R.id.registerTextView);

        // Ánh xạ các View cho Phone Auth Login
//        phoneAuthLayout = findViewById(R.id.phoneAuthLayout);
//        phoneAuthEditText = findViewById(R.id.phoneAuthEditText);
//        otpAuthEditText = findViewById(R.id.otpAuthEditText);
//        sendOtpLoginButton = findViewById(R.id.sendOtpLoginButton);
//        verifyOtpLoginButton = findViewById(R.id.verifyOtpLoginButton);
//        switchAuthMethodTextView = findViewById(R.id.switchAuthMethodTextView);

        // Thiết lập trạng thái ban đầu của giao diện
        updateUIMode();

        // OnClickListener cho nút Đăng nhập (Username/Password)
        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isUsernamePasswordMode) { // Chỉ xử lý khi đang ở chế độ Username/Password
                    String username = usernameEditText.getText().toString().trim();
                    String password = passwordEditText.getText().toString().trim();

                    if (username.isEmpty() || password.isEmpty()) {
                        Toast.makeText(LoginActivity.this, "Vui lòng nhập đầy đủ tên đăng nhập và mật khẩu.", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    findEmailByUsernameAndLogin(username, password); // Bắt đầu luồng đăng nhập Username/Password
                }
            }
        });

        // OnClickListener cho nút Gửi OTP (Phone Auth Login)
        sendOtpLoginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String phoneNumber = phoneAuthEditText.getText().toString().trim();
                if (phoneNumber.isEmpty()) {
                    Toast.makeText(LoginActivity.this, "Vui lòng nhập số điện thoại.", Toast.LENGTH_SHORT).show();
                    return;
                }
                if (!phoneNumber.startsWith("+")) {
                    phoneNumber = "+84" + phoneNumber; // Thêm tiền tố quốc gia nếu chưa có
                }
                sendVerificationCodeForLogin(phoneNumber); // Bắt đầu luồng gửi OTP cho đăng nhập
            }
        });

        // OnClickListener cho nút Xác minh OTP (Phone Auth Login)
        verifyOtpLoginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String otp = otpAuthEditText.getText().toString().trim();
                if (otp.isEmpty() || otp.length() < 6) {
                    Toast.makeText(LoginActivity.this, "Vui lòng nhập mã OTP hợp lệ.", Toast.LENGTH_SHORT).show();
                    return;
                }
                if (verificationId != null) {
                    PhoneAuthCredential credential = PhoneAuthProvider.getCredential(verificationId, otp);
                    signInWithPhoneAuthCredential(credential); // Hoàn tất đăng nhập Phone Auth
                } else {
                    Toast.makeText(LoginActivity.this, "Chưa gửi mã OTP. Vui lòng gửi lại.", Toast.LENGTH_SHORT).show();
                }
            }
        });

        // OnClickListener cho chuyển đổi phương thức xác thực
        switchAuthMethodTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                isUsernamePasswordMode = !isUsernamePasswordMode;
                updateUIMode(); // Cập nhật giao diện
            }
        });

        forgotPasswordTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(LoginActivity.this, ForgotPasswordActivity.class);
                startActivity(intent);
            }
        });

        registerTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(LoginActivity.this, RegisterActivity.class);
                startActivity(intent);
            }
        });
    }

    // Phương thức để cập nhật trạng thái hiển thị của giao diện
    private void updateUIMode() {
        if (isUsernamePasswordMode) {
            usernamePasswordLayout.setVisibility(View.VISIBLE);
            phoneAuthLayout.setVisibility(View.GONE);
            switchAuthMethodTextView.setText("Đăng nhập bằng số điện thoại");
        } else {
            usernamePasswordLayout.setVisibility(View.GONE);
            phoneAuthLayout.setVisibility(View.VISIBLE);
            switchAuthMethodTextView.setText("Đăng nhập bằng tên đăng nhập");
            // Khi chuyển sang Phone Auth, ẩn các trường OTP và nút xác minh ban đầu
            otpAuthEditText.setVisibility(View.GONE);
            verifyOtpLoginButton.setVisibility(View.GONE);
            sendOtpLoginButton.setEnabled(true); // Đảm bảo nút gửi OTP có thể nhấn lại
            if (otpLoginTimer != null) {
                otpLoginTimer.cancel(); // Hủy timer nếu đang chạy
            }
            otpAuthEditText.setText(""); // Xóa OTP cũ
        }
    }

    // --- Logic Đăng nhập bằng Username/Password ---
    private void findEmailByUsernameAndLogin(String username, String password) {
        Query usernameQuery = mDatabase.child("users").orderByChild("username").equalTo(username);
        usernameQuery.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    String firebaseEmail = null;
                    for (DataSnapshot userSnapshot : snapshot.getChildren()) {
                        User user = userSnapshot.getValue(User.class);
                        if (user != null && user.email != null) {
                            firebaseEmail = user.email; // Lấy email ảo nội bộ từ DB
                            break;
                        }
                    }

                    if (firebaseEmail != null) {
                        mAuth.signInWithEmailAndPassword(firebaseEmail, password)
                                .addOnCompleteListener(LoginActivity.this, task -> {
                                    if (task.isSuccessful()) {
                                        FirebaseUser user = mAuth.getCurrentUser();
                                        Toast.makeText(LoginActivity.this, "Đăng nhập thành công!", Toast.LENGTH_SHORT).show();
                                        // Chuyển sang màn hình chính của ứng dụng
                                        // Intent intent = new Intent(LoginActivity.this, HomeActivity.class);
                                        // startActivity(intent);
                                        // finish();
                                    } else {
                                        // Đăng nhập Firebase thất bại (ví dụ: sai mật khẩu cho email ảo)
                                        Toast.makeText(LoginActivity.this, "Sai tên đăng nhập hoặc mật khẩu.", Toast.LENGTH_LONG).show();
                                        Log.e(TAG, "Firebase login failed (Email/Pass): " + task.getException().getMessage());
                                    }
                                });
                    } else {
                        Toast.makeText(LoginActivity.this, "Không tìm thấy thông tin email cho tên đăng nhập này.", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    // Username không tồn tại trong database
                    Toast.makeText(LoginActivity.this, "Tên đăng nhập không tồn tại.", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(LoginActivity.this, "Lỗi khi tìm tên đăng nhập: " + error.getMessage(), Toast.LENGTH_LONG).show();
                Log.e(TAG, "Database query cancelled: " + error.getMessage());
            }
        });
    }

    // --- Logic Đăng nhập bằng Số điện thoại (Phone Auth) ---
    private void sendVerificationCodeForLogin(String phoneNumber) {
        PhoneAuthOptions options =
                PhoneAuthOptions.newBuilder(mAuth)
                        .setPhoneNumber(phoneNumber)
                        .setTimeout(60L, TimeUnit.SECONDS) // Thời gian chờ OTP
                        .setActivity(this)
                        .setCallbacks(mPhoneCallbacks) // Callbacks cho Phone Auth Login
                        .build();
        PhoneAuthProvider.verifyPhoneNumber(options);

        // Bắt đầu đếm ngược
        if (otpLoginTimer != null) {
            otpLoginTimer.cancel();
        }
        timeLeftInMillis = 60000;
        startLoginOtpTimer();
        Toast.makeText(this, "Mã OTP đã được gửi đến " + phoneNumber, Toast.LENGTH_LONG).show();
    }

    private PhoneAuthProvider.OnVerificationStateChangedCallbacks mPhoneCallbacks =
            new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {

                @Override
                public void onVerificationCompleted(@NonNull PhoneAuthCredential credential) {
                    // Tự động xác minh mã OTP (thường xảy ra trên thiết bị có Dịch vụ Google Play)
                    Log.d(TAG, "onVerificationCompleted for Phone Auth login:" + credential);
                    signInWithPhoneAuthCredential(credential); // Hoàn tất đăng nhập Phone Auth
                }

                @Override
                public void onVerificationFailed(@NonNull FirebaseException e) {
                    // Xử lý lỗi xác minh (ví dụ: số điện thoại không hợp lệ, giới hạn SMS...)
                    Log.w(TAG, "onVerificationFailed for Phone Auth login", e);
                    Toast.makeText(LoginActivity.this, "Xác minh thất bại: " + e.getMessage(), Toast.LENGTH_LONG).show();
                    sendOtpLoginButton.setEnabled(true); // Cho phép gửi lại
                    if (otpLoginTimer != null) {
                        otpLoginTimer.cancel();
                        otpAuthEditText.setHint("Lỗi OTP. Thử lại.");
                    }
                }

                @Override
                public void onCodeSent(@NonNull String verificationCode,
                                       @NonNull PhoneAuthProvider.ForceResendingToken token) {
                    // Mã OTP đã được gửi đến số điện thoại, lưu ID xác minh và token
                    Log.d(TAG, "onCodeSent for Phone Auth login:" + verificationCode);
                    verificationId = verificationCode;
                    resendToken = token;
                    Toast.makeText(LoginActivity.this, "Mã OTP đã được gửi.", Toast.LENGTH_SHORT).show();
                    otpAuthEditText.setVisibility(View.VISIBLE); // Hiển thị trường OTP
                    verifyOtpLoginButton.setVisibility(View.VISIBLE); // Hiển thị nút xác minh
                }
            };

    private void signInWithPhoneAuthCredential(PhoneAuthCredential credential) {
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        FirebaseUser user = task.getResult().getUser();
                        Toast.makeText(LoginActivity.this, "Đăng nhập bằng SĐT thành công!", Toast.LENGTH_SHORT).show();
                        // Chuyển sang màn hình chính của ứng dụng
                        // Intent intent = new Intent(LoginActivity.this, HomeActivity.class);
                        // startActivity(intent);
                        // finish();
                    } else {
                        Log.e(TAG, "Phone Auth sign-in failed: " + task.getException().getMessage());
                        if (task.getException() instanceof FirebaseAuthInvalidCredentialsException) {
                            // Mã OTP không hợp lệ hoặc đã hết hạn
                            Toast.makeText(LoginActivity.this, "Mã OTP không đúng hoặc đã hết hạn.", Toast.LENGTH_LONG).show();
                        } else {
                            Toast.makeText(LoginActivity.this, "Đăng nhập SĐT thất bại: " + task.getException().getMessage(), Toast.LENGTH_LONG).show();
                        }
                    }
                });
    }

    private void startLoginOtpTimer() {
        otpLoginTimer = new CountDownTimer(timeLeftInMillis, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                timeLeftInMillis = millisUntilFinished;
                long seconds = millisUntilFinished / 1000;
                sendOtpLoginButton.setText("Gửi lại sau " + seconds + "s");
                sendOtpLoginButton.setEnabled(false);
            }

            @Override
            public void onFinish() {
                sendOtpLoginButton.setEnabled(true);
                sendOtpLoginButton.setText("GỬI LẠI OTP");
                otpAuthEditText.setHint("Nhập mã OTP");
            }
        }.start();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (otpLoginTimer != null) {
            otpLoginTimer.cancel();
        }
    }
}