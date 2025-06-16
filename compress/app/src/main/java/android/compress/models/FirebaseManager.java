package android.compress.models; // Thay bằng package name của bạn

import android.app.Activity;
import android.content.Context;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.google.firebase.FirebaseException;
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
 *
 * CẢNH BÁO BẢO MẬT:
 * Việc tự xử lý mật khẩu và đăng nhập trong Realtime Database tiềm ẩn nhiều rủi ro.
 * Trong thực tế, hãy ưu tiên sử dụng Firebase Authentication để có độ bảo mật cao nhất.
 */
public class FirebaseManager {

    private static final DatabaseReference dbRef = FirebaseDatabase.getInstance().getReference("users");
    private static final FirebaseAuth mAuth = FirebaseAuth.getInstance();

    // Interface để xử lý các callback từ Firebase
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

    /**
     * Bước 1 của Đăng ký: Kiểm tra username và SĐT, sau đó gửi OTP.
     */
    public static void sendRegistrationOtp(Activity activity, String username, String phone, VerificationCallback callback) {
        // Kiểm tra xem username đã tồn tại chưa
        Query usernameQuery = dbRef.orderByChild("username").equalTo(username);
        usernameQuery.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    callback.onFailure("Tên đăng nhập đã tồn tại.");
                    return;
                }

                // Nếu username chưa tồn tại, kiểm tra số điện thoại
                Query phoneQuery = dbRef.orderByChild("phone").equalTo(phone);
                phoneQuery.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot phoneSnapshot) {
                        if (phoneSnapshot.exists()) {
                            callback.onFailure("Số điện thoại đã được sử dụng.");
                            return;
                        }

                        // Nếu cả hai đều hợp lệ, gửi OTP
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
     * Bước 2 của Đăng ký: Xác thực OTP và tạo người dùng trong Realtime DB.
     */
    public static void verifyOtpAndRegisterUser(String verificationId, String otpCode, User user, SimpleCallback callback) {
        try {
            PhoneAuthCredential credential = PhoneAuthProvider.getCredential(verificationId, otpCode);
            // Không cần signIn với Firebase Auth, chỉ cần credential hợp lệ là được
            if (credential != null) {
                // Mã hóa mật khẩu trước khi lưu
                String hashedPassword = BCrypt.hashpw(user.getPassword(), BCrypt.gensalt());
                user.setPassword(hashedPassword);
                user.setRole("user"); // Gán quyền mặc định

                // Lưu vào Realtime Database
                String userId = dbRef.push().getKey();
                if (userId != null) {
                    dbRef.child(userId).setValue(user)
                            .addOnSuccessListener(aVoid -> callback.onSuccess("Đăng ký thành công!"))
                            .addOnFailureListener(e -> callback.onFailure("Lỗi khi lưu dữ liệu: " + e.getMessage()));
                } else {
                    callback.onFailure("Không thể tạo ID người dùng.");
                }
            }
        } catch (Exception e) {
            callback.onFailure("Mã OTP không hợp lệ hoặc đã hết hạn.");
        }
    }


    // =================================================================================
    // 2. ĐĂNG NHẬP (LOGIC TỰ XÂY DỰNG)
    // =================================================================================

    public static void loginWithUsername(String username, String rawPassword, AuthCallback callback) {
        Query query = dbRef.orderByChild("username").equalTo(username).limitToFirst(1);
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (!snapshot.exists()) {
                    callback.onFailure("Tên đăng nhập hoặc mật khẩu không đúng.");
                    return;
                }

                // Lấy thông tin người dùng
                for (DataSnapshot userSnapshot : snapshot.getChildren()) {
                    User user = userSnapshot.getValue(User.class);
                    if (user != null) {
                        // So sánh mật khẩu đã hash
                        if (BCrypt.checkpw(rawPassword, user.getPassword())) {
                            callback.onSuccess(user); // Đăng nhập thành công
                        } else {
                            callback.onFailure("Tên đăng nhập hoặc mật khẩu không đúng.");
                        }
                        return; // Chỉ xử lý người dùng đầu tiên tìm thấy
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

    /**
     * Gửi OTP để xác minh quyền sở hữu số điện thoại khi quên mật khẩu.
     */
    public static void sendPasswordResetOtp(Activity activity, String phone, VerificationCallback callback) {
        // Kiểm tra xem số điện thoại có tồn tại trong hệ thống không
        Query phoneQuery = dbRef.orderByChild("phone").equalTo(phone);
        phoneQuery.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (!snapshot.exists()) {
                    callback.onFailure("Số điện thoại này chưa được đăng ký.");
                    return;
                }
                // Nếu SĐT tồn tại, gửi mã OTP
                sendOtp(activity, phone, callback);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                callback.onFailure(error.getMessage());
            }
        });
    }

    /**
     * *** HÀM MỚI ***
     * Chỉ xác thực OTP mà không làm gì thêm. Dùng cho luồng quên mật khẩu.
     */
    public static void verifyOtp(String verificationId, String otpCode, SimpleCallback callback) {
        try {
            PhoneAuthCredential credential = PhoneAuthProvider.getCredential(verificationId, otpCode);
            // Nếu credential hợp lệ (không ném ra exception), coi như thành công.
            if (credential != null) {
                callback.onSuccess("Xác thực OTP thành công!");
            } else {
                callback.onFailure("Không thể tạo credential.");
            }
        } catch (Exception e) {
            callback.onFailure("Mã OTP không hợp lệ hoặc đã hết hạn.");
        }
    }

    /**
     * Cập nhật mật khẩu mới cho người dùng dựa trên số điện thoại.
     */
    public static void updatePassword(String phone, String newRawPassword, SimpleCallback callback) {
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

    /**
     * Hàm chung để gửi OTP qua Firebase Phone Authentication.
     */
    private static void sendOtp(Activity activity, String phone, VerificationCallback callback) {
        // Cần đảm bảo SĐT có mã quốc gia (+84)
        if (!phone.startsWith("+")) {
            // Giả sử SĐT Việt Nam, thêm +84 nếu thiếu
            if (phone.startsWith("0")) {
                phone = "+84" + phone.substring(1);
            } else {
                phone = "+84" + phone;
            }
        }

        final String finalPhone = phone;
        PhoneAuthOptions options =
                PhoneAuthOptions.newBuilder(mAuth)
                        .setPhoneNumber(finalPhone)       // Số điện thoại để gửi OTP
                        .setTimeout(90L, TimeUnit.SECONDS) // Thời gian chờ
                        .setActivity(activity)                 // Activity để xử lý callback
                        .setCallbacks(new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
                            @Override
                            public void onVerificationCompleted(@NonNull PhoneAuthCredential credential) {
                                // Tự động xác thực (hiếm gặp)
                            }

                            @Override
                            public void onVerificationFailed(@NonNull FirebaseException e) {
                                callback.onFailure("Lỗi gửi OTP: " + e.getMessage());
                            }

                            @Override
                            public void onCodeSent(@NonNull String verificationId,
                                                   @NonNull PhoneAuthProvider.ForceResendingToken token) {
                                // Mã đã được gửi, trả về verificationId để sử dụng ở bước sau
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

        public User() {
            // Cần constructor rỗng cho Firebase
        }

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
