package android.compress.utils;

import android.app.Activity;
import android.compress.SearchActivity;
import android.content.Intent;
import android.view.View;
import android.view.inputmethod.EditorInfo;

import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

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
            // Lấy TextInputEditText và TextInputLayout
            TextInputEditText editTextSearch = searchBarView.findViewById(
                    activity.getResources().getIdentifier("edit_text_search", "id", activity.getPackageName()));
            TextInputLayout textInputLayoutSearch = searchBarView.findViewById(
                    activity.getResources().getIdentifier("text_input_layout_search", "id", activity.getPackageName()));
            
            // Không tự động chuyển đến trang tìm kiếm khi nhấp vào EditText
            // Thay vào đó, chỉ thiết lập sự kiện IME_ACTION_SEARCH (khi nhấn nút tìm kiếm trên bàn phím)
            if (editTextSearch != null) {
                editTextSearch.setOnEditorActionListener((v, actionId, event) -> {
                    if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                        String query = editTextSearch.getText() != null 
                                ? editTextSearch.getText().toString() : "";
                        if (!query.trim().isEmpty()) {
                            openSearchActivity(activity, query);
                        }
                        return true;
                    }
                    return false;
                });
            }
            
            // Xử lý sự kiện khi nhấn vào icon tìm kiếm
            if (textInputLayoutSearch != null) {
                textInputLayoutSearch.setEndIconOnClickListener(v -> {
                    String query = editTextSearch != null && editTextSearch.getText() != null 
                            ? editTextSearch.getText().toString() : "";
                    if (!query.trim().isEmpty()) {
                        openSearchActivity(activity, query);
                    }
                });
            }
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
        activity.startActivity(intent);
    }
} 