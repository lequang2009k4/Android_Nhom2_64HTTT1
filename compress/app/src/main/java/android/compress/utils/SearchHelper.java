package android.compress.utils;

import android.app.Activity;
import android.compress.SearchActivity;
import android.compress.models.UserManager;
import android.content.Intent;
import android.provider.SearchRecentSuggestions;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.ImageView;

import androidx.appcompat.widget.SearchView;

import android.compress.providers.SearchSuggestionProvider;

/**
 * Helper class để thiết lập chức năng tìm kiếm chung cho tất cả các Activity
 */
public class SearchHelper {
    
    /**
     * Thiết lập chức năng tìm kiếm cho một view chứa search bar
     * 
     * @param activity Activity hiện tại
     * @param searchBarViewId ID của view chứa search bar (thường là include với layout_id)
     */
    public static void setupSearchBar(Activity activity, int searchBarViewId) {
        if (activity == null) return;
        
        View searchBarView = activity.findViewById(searchBarViewId);
        if (searchBarView != null) {
            // Thiết lập avatar người dùng
            setupUserAvatar(activity, searchBarView);
            
            // Lấy SearchView
            SearchView searchView = searchBarView.findViewById(
                    activity.getResources().getIdentifier("search_view", "id", activity.getPackageName()));
            
            if (searchView != null) {
                // Thiết lập sự kiện khi người dùng submit query
                searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
                    @Override
                    public boolean onQueryTextSubmit(String query) {
                        if (!query.trim().isEmpty()) {
                            // Lưu query vào lịch sử tìm kiếm
                            SearchRecentSuggestions suggestions = new SearchRecentSuggestions(activity,
                                    SearchSuggestionProvider.AUTHORITY, SearchSuggestionProvider.MODE);
                            suggestions.saveRecentQuery(query, null);
                            
                            // Mở trang tìm kiếm
                            openSearchActivity(activity, query);
                        }
                        return true;
                    }
                    
                    @Override
                    public boolean onQueryTextChange(String newText) {
                        return false;
                    }
                });
            }
        }
    }
    
    /**
     * Thiết lập avatar người dùng trong search bar
     * 
     * @param activity Activity hiện tại
     * @param searchBarView View chứa search bar
     */
    private static void setupUserAvatar(Activity activity, View searchBarView) {
        if (activity == null || searchBarView == null) return;
        
        // Tìm ImageView avatar
        ImageView avatarImageView = searchBarView.findViewById(
                activity.getResources().getIdentifier("image_view_avatar", "id", activity.getPackageName()));
        
        if (avatarImageView != null) {
            // Lấy username hiện tại từ UserManager
            String username = UserManager.getCurrentUsername(activity);
            
            // Tạo và hiển thị avatar từ chữ cái đầu tiên của username
            AvatarUtils.TextDrawable avatar = AvatarUtils.createLetterAvatar(username);
            avatarImageView.setImageDrawable(avatar);
        }
    }
    
    /**
     * Mở SearchActivity với từ khóa tìm kiếm (nếu có)
     * 
     * @param activity Activity hiện tại
     * @param query Từ khóa tìm kiếm (có thể rỗng)
     */
    private static void openSearchActivity(Activity activity, String query) {
        Intent intent = new Intent(activity, SearchActivity.class);
        intent.putExtra("search_query", query);
        
        // Đảm bảo tạo một instance mới của SearchActivity
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        
        activity.startActivity(intent);
    }
} 