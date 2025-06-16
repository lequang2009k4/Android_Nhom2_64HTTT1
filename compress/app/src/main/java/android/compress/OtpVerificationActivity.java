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
import com.google.firebase.FirebaseException;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.EmailAuthProvider; // Import cho Email/Password Auth
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException; // Xử lý lỗi trùng email
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthOptions;
import com.google.firebase.auth.PhoneAuthProvider;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.concurrent.TimeUnit;

public class OtpVerificationActivity extends AppCompatActivity {

    private static final String TAG = "OtpVerificationActivity";

    private EditText otpEditText;
    private Button resendOtpButton;
    private TextView resendTimerTextView;
    private Button verifyOtpButton;
    private TextView changePhoneTextView;
    private TextView goBackTextView;

    private CountDownTimer otpResendTimer;
    private long timeLeftInMillis = 90000; // 90 giây
    private boolean timerRunning;

    private FirebaseAuth mAuth;
    private DatabaseReference mDatabase;
    private String verificationId; // ID từ Firebase khi gửi OTP
    private PhoneAuthProvider.ForceResendingToken resendToken; // Token để gửi lại OTP

    // Thông tin người dùng nhận từ RegisterActivity để tạo tài khoản
    private String username;
    private String password;
    private String phoneNumber;
    private String role;
    private String firebaseEmail; // Email ảo nội bộ

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_otp_verification);

        mAuth = FirebaseAuth.getInstance();
        mDatabase = FirebaseDatabase.getInstance().getReference(); // Khởi tạo Realtime Database

        otpEditText = findViewById(R.id.otpEditText);
        resendOtpButton = findViewById(R.id.resendOtpButton);
        resendTimerTextView = findViewById(R.id.resendTimerTextView);
        verifyOtpButton = findViewById(R.id.verifyOtpButton);
        changePhoneTextView = findViewById(R.id.changePhoneTextView);
        goBackTextView = findViewById(R.id.goBackTextView);

        // Lấy thông tin người dùng từ Intent gửi từ RegisterActivity
        Intent intent = getIntent();
        if (intent != null) {
            username = intent.getStringExtra("username");
            password = intent.getStringExtra("password");
            phoneNumber = intent.getStringExtra("phone_number");
            role = intent.getStringExtra("role");
            firebaseEmail = intent.getStringExtra("firebase_email");

            // Kiểm tra tính hợp lệ của dữ liệu nhận được
            if (username == null || password == null || phoneNumber == null || role == null || firebaseEmail == null) {
                Toast.makeText(this, "Thiếu thông tin đăng ký. Vui lòng thử lại.", Toast.LENGTH_LONG).show();
                finish(); // Đóng Activity nếu thiếu thông tin
                return;
            }
        } else {
            Toast.makeText(this, "Không có thông tin đăng ký được cung cấp.", Toast.LENGTH_LONG).show();
            finish();
            return;
        }

        // Tự động gửi mã OTP khi vào màn hình này
        sendVerificationCode(phoneNumber);

        resendOtpButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (phoneNumber != null && resendToken != null) {
                    sendVerificationCode(phoneNumber);
                    Toast.makeText(OtpVerificationActivity.this, "Đang gửi lại mã OTP...", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(OtpVerificationActivity.this, "Không thể gửi lại mã OTP. Vui lòng thử lại.", Toast.LENGTH_SHORT).show();
                }
            }
        });

        verifyOtpButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String otp = otpEditText.getText().toString().trim();

                if (otp.isEmpty() || otp.length() < 6) {
                    Toast.makeText(OtpVerificationActivity.this, "Vui lòng nhập mã OTP hợp lệ (6 chữ số).", Toast.LENGTH_SHORT).show();
                    return;
                }

                if (verificationId != null) {
                    PhoneAuthCredential credential = PhoneAuthProvider.getCredential(verificationId, otp);
                    // Dùng credential này để tạo/đăng nhập tài khoản Firebase Auth
                    // và sau đó liên kết email/password
                    createAccountWithPhoneAuthAndLinkEmail(credential);
                } else {
                    Toast.makeText(OtpVerificationActivity.this, "Không có mã xác minh. Vui lòng gửi lại OTP.", Toast.LENGTH_LONG).show();
                }
            }
        });

        changePhoneTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Quay lại màn hình đăng ký để thay đổi thông tin (đặc biệt là số điện thoại)
                Intent intent = new Intent(OtpVerificationActivity.this, RegisterActivity.class);
                startActivity(intent);
                finish(); // Đóng Activity này
            }
        });

        goBackTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed(); // Quay lại màn hình trước đó
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
                resendOtpButton.setEnabled(false); // Vô hiệu hóa nút trong khi đếm ngược
                timerRunning = true;
            }

            @Override
            public void onFinish() {
                resendOtpButton.setEnabled(true);
                resendOtpButton.setText("GỬI LẠI MÃ OTP");
                resendTimerTextView.setText(""); // Xóa thông báo đếm ngược
                timerRunning = false;
            }
        }.start();
    }

    private void sendVerificationCode(String phoneNumber) {
        // Firebase Phone Auth yêu cầu số điện thoại có tiền tố quốc gia (ví dụ: +84XXXXXXXXX)
        // Đảm bảo phoneNumber đã có định dạng này (đã được xử lý ở RegisterActivity)
        PhoneAuthOptions options =
                PhoneAuthOptions.newBuilder(mAuth)
                        .setPhoneNumber(phoneNumber)       // Số điện thoại để xác minh
                        .setTimeout(90L, TimeUnit.SECONDS) // Thời gian chờ OTP
                        .setActivity(this)                 // Activity hiện tại
                        .setCallbacks(mCallbacks)          // Callbacks để xử lý các sự kiện
                        .setForceResendingToken(resendToken) // Dùng token để gửi lại (null cho lần đầu)
                        .build();
        PhoneAuthProvider.verifyPhoneNumber(options);

        // Bắt đầu hoặc khởi động lại bộ đếm thời gian
        if (otpResendTimer != null) {
            otpResendTimer.cancel();
        }
        timeLeftInMillis = 90000; // Reset thời gian
        startOtpResendTimer();
        Toast.makeText(this, "Mã OTP đã được gửi đến " + phoneNumber, Toast.LENGTH_LONG).show();
    }

    private PhoneAuthProvider.OnVerificationStateChangedCallbacks mCallbacks =
            new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {

                @Override
                public void onVerificationCompleted(@NonNull PhoneAuthCredential credential) {
                    // Tự động xác minh mã OTP (thường xảy ra trên thiết bị có Dịch vụ Google Play)
                    Log.d(TAG, "onVerificationCompleted:" + credential);
                    createAccountWithPhoneAuthAndLinkEmail(credential); // Tiến hành tạo tài khoản và liên kết
                }

                @Override
                public void onVerificationFailed(@NonNull FirebaseException e) {
                    // Xử lý lỗi xác minh (ví dụ: số điện thoại không hợp lệ, giới hạn SMS...)
                    Log.w(TAG, "onVerificationFailed", e);
                    Toast.makeText(OtpVerificationActivity.this, "Xác minh thất bại: " + e.getMessage(), Toast.LENGTH_LONG).show();
                    resendOtpButton.setEnabled(true);
                    resendOtpButton.setText("GỬI LẠI MÃ OTP");
                    if (otpResendTimer != null) {
                        otpResendTimer.cancel();
                        resendTimerTextView.setText("Thử lại hoặc quay lại.");
                    }
                    timerRunning = false;
                }

                @Override
                public void onCodeSent(@NonNull String verificationCode,
                                       @NonNull PhoneAuthProvider.ForceResendingToken token) {
                    // Mã OTP đã được gửi đến số điện thoại, lưu ID xác minh và token
                    Log.d(TAG, "onCodeSent:" + verificationCode);
                    verificationId = verificationCode;
                    resendToken = token;
                    Toast.makeText(OtpVerificationActivity.this, "Mã OTP đã được gửi.", Toast.LENGTH_SHORT).show();
                }
            };

    private void createAccountWithPhoneAuthAndLinkEmail(PhoneAuthCredential phoneCredential) {
        // Bước 1: Đăng nhập (hoặc tạo tài khoản mới nếu chưa có) bằng Phone Credential
        mAuth.signInWithCredential(phoneCredential)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        // Đăng nhập/tạo tài khoản bằng SĐT thành công
                        FirebaseUser user = mAuth.getCurrentUser();
                        Log.d(TAG, "Signed in with phone successfully. User UID: " + user.getUid());

                        // Bước 2: Liên kết Email/Password Credential với tài khoản vừa tạo/đăng nhập
                        AuthCredential emailCredential = EmailAuthProvider.getCredential(firebaseEmail, password);

                        user.linkWithCredential(emailCredential)
                                .addOnCompleteListener(linkTask -> {
                                    if (linkTask.isSuccessful()) {
                                        Log.d(TAG, "Linked email/password successfully.");
                                        // Bước 3: Lưu thông tin người dùng vào Realtime Database
                                        writeNewUserToDatabase(user.getUid(), firebaseEmail, phoneNumber, username, role);

                                        Toast.makeText(OtpVerificationActivity.this, "Đăng ký thành công!", Toast.LENGTH_LONG).show();
                                        // Chuyển sang màn hình chính của ứng dụng sau khi đăng ký hoàn tất
                                        // Intent intent = new Intent(OtpVerificationActivity.this, HomeActivity.class);
                                        // startActivity(intent);
                                        // finish(); // Đóng Activity này
                                    } else {
                                        Log.e(TAG, "Failed to link email/password: " + linkTask.getException().getMessage());
                                        // Xử lý lỗi liên kết, ví dụ: email đã được sử dụng bởi tài khoản Firebase Auth khác
                                        if (linkTask.getException() instanceof FirebaseAuthInvalidCredentialsException) {
                                            Toast.makeText(OtpVerificationActivity.this, "Email này đã được sử dụng bởi một tài khoản khác.", Toast.LENGTH_LONG).show();
                                        } else {
                                            Toast.makeText(OtpVerificationActivity.this, "Lỗi liên kết email/mật khẩu: " + linkTask.getException().getMessage(), Toast.LENGTH_LONG).show();
                                        }
                                        // Tùy chọn: Nếu đây là đăng ký mới và liên kết thất bại,
                                        // bạn có thể xóa tài khoản Phone Auth vừa tạo để tránh tài khoản rác
                                        user.delete().addOnCompleteListener(deleteTask -> {
                                            if(deleteTask.isSuccessful()) Log.d(TAG, "Phone Auth user deleted due to email link failure.");
                                            else Log.e(TAG, "Failed to delete phone auth user: " + deleteTask.getException().getMessage());
                                        });
                                        // Chuyển về màn hình đăng ký hoặc đăng nhập
                                        Intent intent = new Intent(OtpVerificationActivity.this, RegisterActivity.class);
                                        startActivity(intent);
                                        finish();
                                    }
                                });
                    } else {
                        // Đăng nhập/tạo tài khoản bằng SĐT thất bại
                        Log.e(TAG, "Phone Auth sign-in failed: " + task.getException().getMessage());
                        Toast.makeText(OtpVerificationActivity.this, "Xác thực SĐT thất bại: " + task.getException().getMessage(), Toast.LENGTH_LONG).show();
                        // Chuyển về màn hình đăng ký hoặc đăng nhập
                        Intent intent = new Intent(OtpVerificationActivity.this, RegisterActivity.class);
                        startActivity(intent);
                        finish();
                    }
                });
    }

    // Phương thức để lưu thông tin người dùng vào Realtime Database
    private void writeNewUserToDatabase(String uid, String email, String phone, String username, String role) {
        User newUser = new User(uid, email, phone, username, username, role); // Đặt tên hiển thị mặc định là username
        mDatabase.child("users").child(uid).setValue(newUser)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(OtpVerificationActivity.this, "Lưu dữ liệu người dùng vào DB thành công.", Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(OtpVerificationActivity.this, "Lỗi khi lưu dữ liệu vào DB: " + e.getMessage(), Toast.LENGTH_LONG).show();
                    Log.e(TAG, "Failed to write user data to DB: " + e.getMessage());
                });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (otpResendTimer != null) {
            otpResendTimer.cancel();
        }
    }
}