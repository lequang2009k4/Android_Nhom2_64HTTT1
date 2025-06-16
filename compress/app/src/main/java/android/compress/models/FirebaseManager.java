package android.compress.models; // Thay bằng package name của bạn

import android.app.Activity;
import android.content.Context;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.FirebaseException;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthOptions;
import com.google.firebase.auth.PhoneAuthProvider;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import org.mindrot.jbcrypt.BCrypt;

import java.util.concurrent.TimeUnit;

/**
 * Lớp quản lý tất cả các tương tác với Firebase.
 */
public class FirebaseManager {

    private static final DatabaseReference dbRef = FirebaseDatabase.getInstance().getReference("users");
    private static final FirebaseAuth mAuth = FirebaseAuth.getInstance();

    public interface AuthCallback {
        void onSuccess(User user);
        void onFailure(String message);
    }

    public interface VerificationCallback {
        void onVerified(String verificationId);
        void onFailure(String message);
    }

    public interface SimpleCallback {
        void onSuccess(String message);
        void onFailure(String message);
    }

    // =================================================================================
    // 1. ĐĂNG KÝ
    // =================================================================================
    public static void sendRegistrationOtp(Activity activity, String username, String phone, VerificationCallback callback) {
        // ... (Giữ nguyên logic kiểm tra)
        Query usernameQuery = dbRef.orderByChild("username").equalTo(username);
        usernameQuery.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    callback.onFailure("Tên đăng nhập đã tồn tại.");
                    return;
                }
                Query phoneQuery = dbRef.orderByChild("phone").equalTo(phone);
                phoneQuery.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot phoneSnapshot) {
                        if (phoneSnapshot.exists()) {
                            callback.onFailure("Số điện thoại đã được sử dụng.");
                            return;
                        }
                        sendOtp(activity, phone, callback);
                    }
                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        callback.onFailure(error.getMessage());
                    }
                });
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                callback.onFailure(error.getMessage());
            }
        });
    }

    /**
     * *** ĐÃ SỬA LỖI LOGIC ***
     * Bước 2 của Đăng ký: Xác thực OTP và tạo người dùng.
     */
    public static void verifyOtpAndRegisterUser(String verificationId, String otpCode, User user, SimpleCallback callback) {
        PhoneAuthCredential credential = PhoneAuthProvider.getCredential(verificationId, otpCode);
        // Dùng signInWithCredential để XÁC THỰC OTP VỚI SERVER
        mAuth.signInWithCredential(credential)
                .addOnSuccessListener(authResult -> {
                    // OTP ĐÚNG -> Tiếp tục lưu người dùng
                    String hashedPassword = BCrypt.hashpw(user.getPassword(), BCrypt.gensalt());
                    user.setPassword(hashedPassword);
                    user.setRole("user");

                    String userId = dbRef.push().getKey();
                    if (userId != null) {
                        dbRef.child(userId).setValue(user)
                                .addOnSuccessListener(aVoid -> {
                                    // Đăng xuất người dùng khỏi Firebase Auth ngay lập tức
                                    mAuth.signOut();
                                    callback.onSuccess("Đăng ký thành công!");
                                })
                                .addOnFailureListener(e -> callback.onFailure("Lỗi khi lưu dữ liệu: " + e.getMessage()));
                    } else {
                        callback.onFailure("Không thể tạo ID người dùng.");
                    }
                })
                .addOnFailureListener(e -> {
                    // OTP SAI
                    callback.onFailure("Mã OTP không hợp lệ.");
                });
    }

    // =================================================================================
    // 2. ĐĂNG NHẬP
    // =================================================================================
    public static void loginWithUsername(String username, String rawPassword, AuthCallback callback) {
        // ... (Giữ nguyên logic)
        Query query = dbRef.orderByChild("username").equalTo(username).limitToFirst(1);
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (!snapshot.exists()) {
                    callback.onFailure("Tên đăng nhập hoặc mật khẩu không đúng.");
                    return;
                }
                for (DataSnapshot userSnapshot : snapshot.getChildren()) {
                    User user = userSnapshot.getValue(User.class);
                    if (user != null) {
                        if (BCrypt.checkpw(rawPassword, user.getPassword())) {
                            callback.onSuccess(user);
                        } else {
                            callback.onFailure("Tên đăng nhập hoặc mật khẩu không đúng.");
                        }
                        return;
                    }
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                callback.onFailure(error.getMessage());
            }
        });
    }

    // =================================================================================
    // 3. QUÊN MẬT KHẨU
    // =================================================================================
    public static void sendPasswordResetOtp(Activity activity, String phone, VerificationCallback callback) {
        // ... (Giữ nguyên logic)
        Query phoneQuery = dbRef.orderByChild("phone").equalTo(phone);
        phoneQuery.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (!snapshot.exists()) {
                    callback.onFailure("Số điện thoại này chưa được đăng ký.");
                    return;
                }
                sendOtp(activity, phone, callback);
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                callback.onFailure(error.getMessage());
            }
        });
    }

    /**
     * *** ĐÃ SỬA LỖI LOGIC ***
     * Chỉ xác thực OTP. Dùng cho luồng quên mật khẩu.
     */
    public static void verifyOtp(String verificationId, String otpCode, SimpleCallback callback) {
        PhoneAuthCredential credential = PhoneAuthProvider.getCredential(verificationId, otpCode);
        // Dùng signInWithCredential để XÁC THỰC OTP VỚI SERVER
        mAuth.signInWithCredential(credential)
                .addOnSuccessListener(authResult -> {
                    // OTP ĐÚNG
                    // Đăng xuất ngay lập tức vì không cần giữ phiên đăng nhập
                    mAuth.signOut();
                    callback.onSuccess("Xác thực OTP thành công!");
                })
                .addOnFailureListener(e -> {
                    // OTP SAI
                    callback.onFailure("Mã OTP không hợp lệ.");
                });
    }

    public static void updatePassword(String phone, String newRawPassword, SimpleCallback callback) {
        // ... (Giữ nguyên logic)
        Query query = dbRef.orderByChild("phone").equalTo(phone).limitToFirst(1);
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (!snapshot.exists()) {
                    callback.onFailure("Không tìm thấy người dùng với số điện thoại này.");
                    return;
                }
                for(DataSnapshot userSnapshot : snapshot.getChildren()){
                    String userId = userSnapshot.getKey();
                    String newHashedPassword = BCrypt.hashpw(newRawPassword, BCrypt.gensalt());
                    dbRef.child(userId).child("password").setValue(newHashedPassword)
                            .addOnSuccessListener(aVoid -> callback.onSuccess("Cập nhật mật khẩu thành công!"))
                            .addOnFailureListener(e -> callback.onFailure("Lỗi khi cập nhật mật khẩu."));
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                callback.onFailure(error.getMessage());
            }
        });
    }

    // =================================================================================
    // HÀM TIỆN ÍCH CHUNG
    // =================================================================================
    private static void sendOtp(Activity activity, String phone, VerificationCallback callback) {
        // ... (Giữ nguyên logic)
        if (!phone.startsWith("+")) {
            if (phone.startsWith("0")) {
                phone = "+84" + phone.substring(1);
            } else {
                phone = "+84" + phone;
            }
        }
        final String finalPhone = phone;
        PhoneAuthOptions options =
                PhoneAuthOptions.newBuilder(mAuth)
                        .setPhoneNumber(finalPhone)
                        .setTimeout(90L, TimeUnit.SECONDS)
                        .setActivity(activity)
                        .setCallbacks(new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
                            @Override
                            public void onVerificationCompleted(@NonNull PhoneAuthCredential credential) {}
                            @Override
                            public void onVerificationFailed(@NonNull FirebaseException e) {
                                callback.onFailure("Lỗi gửi OTP: " + e.getMessage());
                            }
                            @Override
                            public void onCodeSent(@NonNull String verificationId, @NonNull PhoneAuthProvider.ForceResendingToken token) {
                                callback.onVerified(verificationId);
                            }
                        })
                        .build();
        PhoneAuthProvider.verifyPhoneNumber(options);
    }

    // =================================================================================
    // LỚP MODEL
    // =================================================================================
    public static class User {
        private String username;
        private String phone;
        private String password;
        private String role;
        public User() {}
        public User(String username, String phone, String password) {
            this.username = username;
            this.phone = phone;
            this.password = password;
        }
        public String getUsername() { return username; }
        public String getPhone() { return phone; }
        public String getPassword() { return password; }
        public String getRole() { return role; }
        public void setPassword(String password) { this.password = password; }
        public void setRole(String role) { this.role = role; }
    }
}
