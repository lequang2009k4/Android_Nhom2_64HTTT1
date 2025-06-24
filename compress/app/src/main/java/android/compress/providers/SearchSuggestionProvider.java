package android.compress.providers;

import android.content.SearchRecentSuggestionsProvider;

/**
 * Provider để quản lý lịch sử tìm kiếm gần đây
 */
public class SearchSuggestionProvider extends SearchRecentSuggestionsProvider {
    
    public static final String AUTHORITY = "android.compress.providers.SearchSuggestionProvider";
    public static final int MODE = DATABASE_MODE_QUERIES;
    
    public SearchSuggestionProvider() {
        setupSuggestions(AUTHORITY, MODE);
    }
} 