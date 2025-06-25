package android.compress.models;

import android.content.Context;
import android.content.SharedPreferences;

import androidx.annotation.NonNull;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

/**
 * Quản lý thông tin người dùng và không gian lưu trữ
 */
public class UserManager {
    private static final String PREF_NAME = "UserPrefs";
    private static final String KEY_USER_ID = "current_user_id";
    private static final String KEY_USERNAME = "current_username";
    
    /**
     * Lưu thông tin người dùng vào SharedPreferences
     */
    public static void saveUserInfo(Context context, String userId, String username) {
        SharedPreferences prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString(KEY_USER_ID, userId);
        editor.putString(KEY_USERNAME, username);
        editor.apply();
    }
    
    /**
     * Lấy ID người dùng hiện tại
     */
    public static String getCurrentUserId(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        return prefs.getString(KEY_USER_ID, null);
    }
    
    /**
     * Lấy tên người dùng hiện tại
     */
    public static String getCurrentUsername(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        return prefs.getString(KEY_USERNAME, null);
    }
    
    /**
     * Xóa thông tin người dùng khi đăng xuất
     */
    public static void clearUserInfo(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        prefs.edit().clear().apply();
    }
} 