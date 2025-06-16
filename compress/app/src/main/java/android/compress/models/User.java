package android.compress.models; // Đảm bảo tên gói trùng với dự án của bạn

public class User {
    public String uid;
    public String email; // Đây sẽ là email ảo nội bộ (username@yourappdomain.com)
    public String phoneNumber;
    public String username; // Tên người dùng thực tế mà user nhập
    public String name;     // Tên hiển thị thêm (có thể lấy từ username)
    public String role;     // Vai trò của người dùng (ví dụ: "user", "admin")

    // Constructor rỗng cần thiết cho Firebase Realtime Database
    public User() {
    }

    public User(String uid, String email, String phoneNumber, String username, String name, String role) {
        this.uid = uid;
        this.email = email;
        this.phoneNumber = phoneNumber;
        this.username = username;
        this.name = name;
        this.role = role;
    }

    // Bạn có thể thêm getters/setters nếu cần thiết cho việc xử lý dữ liệu cụ thể
    // Ví dụ:
    public String getUid() { return uid; }
    public String getEmail() { return email; }
    public String getPhoneNumber() { return phoneNumber; }
    public String getUsername() { return username; }
    public String getName() { return name; }
    public String getRole() { return role; }

    public void setUid(String uid) { this.uid = uid; }
    public void setEmail(String email) { this.email = email; }
    public void setPhoneNumber(String phoneNumber) { this.phoneNumber = phoneNumber; }
    public void setUsername(String username) { this.username = username; }
    public void setName(String name) { this.name = name; }
    public void setRole(String role) { this.role = role; }
}