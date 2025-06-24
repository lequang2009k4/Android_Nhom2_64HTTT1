package android.compress;

import android.app.SearchManager;
import android.compress.models.StorageManager;
import android.compress.providers.SearchSuggestionProvider;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.provider.SearchRecentSuggestions;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CursorAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.tabs.TabLayout;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class SearchActivity extends AppCompatActivity {

    private RecyclerView recyclerViewSearchResults;
    private TextView textSearchResults;
    private LinearLayout layoutNoResults;
    private TabLayout tabLayoutFilter;
    private List<StorageManager.ImageItem> allImages;
    private List<StorageManager.ImageItem> uploadedImages;
    private List<StorageManager.ImageItem> compressedImages;
    private ImageAdapter adapter;
    private Set<String> loadedImagePaths;
    private TextView loadingTextView;
    private int currentTabPosition = 0;
    private SearchView searchView;
    private SearchRecentSuggestions searchSuggestions;
    
    // Độ trễ debounce cho tìm kiếm (300ms)
    private static final long SEARCH_DEBOUNCE_DELAY = 300;
    // Handler cho việc debounce
    private final Handler searchHandler = new Handler(Looper.getMainLooper());
    // Runnable để thực hiện tìm kiếm
    private Runnable searchRunnable;
    
    // Loại tab
    private static final int TAB_ALL = 0;
    private static final int TAB_UPLOADED = 1;
    private static final int TAB_COMPRESSED = 2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);
        
        // Khởi tạo provider cho lịch sử tìm kiếm
        searchSuggestions = new SearchRecentSuggestions(this,
                SearchSuggestionProvider.AUTHORITY, SearchSuggestionProvider.MODE);
        
        // Thiết lập UI elements
        setupUI();
        initViews();
        setupRecyclerView();
        setupSearchView();
        setupTabs();

        // Khởi tạo danh sách và adapter
        allImages = new ArrayList<>();
        uploadedImages = new ArrayList<>();
        compressedImages = new ArrayList<>();
        loadedImagePaths = new HashSet<>();
        adapter = new ImageAdapter(new ArrayList<>());
        recyclerViewSearchResults.setAdapter(adapter);
        
        // Load dữ liệu từ Firebase
        String searchQuery = getIntent().getStringExtra("search_query");
        
        // Kiểm tra nếu Intent có action SEARCH
        if (Intent.ACTION_SEARCH.equals(getIntent().getAction())) {
            searchQuery = getIntent().getStringExtra(SearchManager.QUERY);
            // Lưu query vào lịch sử tìm kiếm
            saveQueryToHistory(searchQuery);
        }
        
        loadData(searchQuery);
    }
    
    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);
        
        // Xử lý intent search mới
        if (Intent.ACTION_SEARCH.equals(intent.getAction())) {
            String query = intent.getStringExtra(SearchManager.QUERY);
            // Lưu query vào lịch sử
            saveQueryToHistory(query);
                
            // Đặt query vào SearchView
            if (searchView != null) {
                searchView.setQuery(query, false);
            }
                
            // Thực hiện tìm kiếm
            performSearch(query);
        }
    }
    
    @Override
    protected void onDestroy() {
        // Hủy tất cả tác vụ đang chạy khi Activity bị hủy
        StorageManager.cancelAllTasks();
        
        // Hủy tác vụ tìm kiếm đang chờ (nếu có)
        if (searchHandler != null && searchRunnable != null) {
            searchHandler.removeCallbacks(searchRunnable);
        }
        
        super.onDestroy();
    }

    private void initViews() {
        recyclerViewSearchResults = findViewById(R.id.recycler_view_search_results);
        textSearchResults = findViewById(R.id.text_search_results);
        layoutNoResults = findViewById(R.id.layout_no_results);
        tabLayoutFilter = findViewById(R.id.tab_layout_filter);
        
        // Lấy SearchView từ layout
        View searchBarView = findViewById(R.id.search_bar);
        searchView = searchBarView.findViewById(R.id.search_view);
        
        // Lấy TextView từ layout_no_results để hiển thị trạng thái
        if (layoutNoResults.getChildCount() > 1 && layoutNoResults.getChildAt(1) instanceof TextView) {
            loadingTextView = (TextView) layoutNoResults.getChildAt(1);
        }
    }
    
    private void setupRecyclerView() {
        recyclerViewSearchResults.setLayoutManager(new GridLayoutManager(this, 2));
    }
    
    private void setupTabs() {
        // Thiết lập các tab
        tabLayoutFilter.addTab(tabLayoutFilter.newTab().setText(getString(R.string.tab_all)));
        tabLayoutFilter.addTab(tabLayoutFilter.newTab().setText(getString(R.string.tab_uploaded)));
        tabLayoutFilter.addTab(tabLayoutFilter.newTab().setText(getString(R.string.tab_compressed)));
        
        // Xử lý sự kiện khi chọn tab
        tabLayoutFilter.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                currentTabPosition = tab.getPosition();
                filterByTab();
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {
                // Không cần xử lý
            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {
                // Không cần xử lý
            }
        });
    }
    
    private void setupSearchView() {
        // Cấu hình SearchView
        SearchManager searchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);
        searchView.setSearchableInfo(searchManager.getSearchableInfo(getComponentName()));
        searchView.setIconifiedByDefault(false);
        searchView.setQueryHint(getString(R.string.search_hint));
        
        // Đặt giá trị tìm kiếm từ intent (nếu có)
        String searchQuery = getIntent().getStringExtra("search_query");
        if (searchQuery != null && !searchQuery.isEmpty()) {
            searchView.setQuery(searchQuery, false);
        }
        
        // Thiết lập sự kiện cho SearchView
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                // Lưu query vào lịch sử tìm kiếm
                saveQueryToHistory(query);
                // Hủy tìm kiếm đang chờ (nếu có)
                searchHandler.removeCallbacks(searchRunnable);
                performSearch(query);
                searchView.clearFocus();
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                // Hủy tìm kiếm đang chờ (nếu có)
                if (searchRunnable != null) {
                    searchHandler.removeCallbacks(searchRunnable);
                }
                
                // Tạo một tác vụ tìm kiếm mới và đặt delay
                searchRunnable = () -> performSearch(newText);
                
                // Thực hiện tìm kiếm sau khoảng thời gian SEARCH_DEBOUNCE_DELAY
                searchHandler.postDelayed(searchRunnable, SEARCH_DEBOUNCE_DELAY);
                return true;
            }
        });
        
        // Xử lý sự kiện khi người dùng chọn gợi ý tìm kiếm
        searchView.setOnSuggestionListener(new SearchView.OnSuggestionListener() {
            @Override
            public boolean onSuggestionSelect(int position) {
                return false;
            }

            @Override
            public boolean onSuggestionClick(int position) {
                // Xử lý khi người dùng nhấp vào gợi ý
                Cursor cursor = (Cursor) searchView.getSuggestionsAdapter().getItem(position);
                int indexColumnText = cursor.getColumnIndex(SearchManager.SUGGEST_COLUMN_TEXT_1);
                if (indexColumnText != -1) {
                    String suggestion = cursor.getString(indexColumnText);
                    searchView.setQuery(suggestion, true);
                    return true;
                }
                return false;
            }
        });
    }
    
    private void setupUI() {
        // Xử lý sự kiện quay về trang Home khi nhấn logo
        findViewById(R.id.logo_home).setOnClickListener(view -> {
            // Xóa text tìm kiếm trước khi quay về Home
            if (searchView != null) {
                searchView.setQuery("", false);
                searchView.clearFocus();
            }
            
            Intent intent = new Intent(SearchActivity.this, HomeActivity.class);
            // Đảm bảo tạo một instance mới của HomeActivity và xóa stack
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);
            finish();
        });
        
        // Xử lý sự kiện nút nén mới
        Button buttonNewCompress = findViewById(R.id.button_new_compress);
        buttonNewCompress.setOnClickListener(v -> {
            Intent intent = new Intent(SearchActivity.this, UploadActivity.class);
            startActivity(intent);
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Tải lại dữ liệu mỗi khi activity được mở lại
        String searchQuery = searchView != null ? searchView.getQuery().toString() : "";
        loadData(searchQuery);
    }
    
    // Xóa lịch sử tìm kiếm
    public void clearSearchHistory() {
        searchSuggestions.clearHistory();
        Toast.makeText(this, getString(R.string.search_history_cleared), Toast.LENGTH_SHORT).show();
    }
    
    // Load dữ liệu từ Firebase
    private void loadData(String initialSearchQuery) {
        showLoading(true);
        
        // Reset danh sách ảnh và paths
        allImages.clear();
        uploadedImages.clear();
        compressedImages.clear();
        loadedImagePaths.clear();
        
        loadImagesFromBothSources(initialSearchQuery);
    }
    
    // Load tất cả ảnh từ cả hai nguồn (đã tải lên và đã nén)
    private void loadImagesFromBothSources(String searchQuery) {
        // Đếm số lượng tác vụ đã hoàn thành
        final int[] completedTasks = {0};
        final int totalTasks = 2; // Uploaded và Compressed
        
        // Load ảnh đã tải lên
        StorageManager.getUploadedImages(new StorageManager.StorageCallback() {
            @Override
            public void onSuccess(List<StorageManager.ImageItem> images) {
                uploadedImages.addAll(images);
                addUniqueImages(images);
                checkAllTasksCompleted(++completedTasks[0], totalTasks, searchQuery);
            }
            
            @Override
            public void onFailure(Exception e) {
                runOnUiThread(() -> Toast.makeText(SearchActivity.this, 
                        "Lỗi tải ảnh đã tải lên: " + e.getMessage(), Toast.LENGTH_SHORT).show());
                checkAllTasksCompleted(++completedTasks[0], totalTasks, searchQuery);
            }
        });
        
        // Load ảnh đã nén
        StorageManager.getCompressedImages(new StorageManager.StorageCallback() {
            @Override
            public void onSuccess(List<StorageManager.ImageItem> images) {
                compressedImages.addAll(images);
                addUniqueImages(images);
                checkAllTasksCompleted(++completedTasks[0], totalTasks, searchQuery);
            }
            
            @Override
            public void onFailure(Exception e) {
                runOnUiThread(() -> Toast.makeText(SearchActivity.this, 
                        "Lỗi tải ảnh đã nén: " + e.getMessage(), Toast.LENGTH_SHORT).show());
                checkAllTasksCompleted(++completedTasks[0], totalTasks, searchQuery);
            }
        });
    }
    
    // Kiểm tra xem tất cả các tác vụ đã hoàn thành chưa
    private void checkAllTasksCompleted(int completed, int total, String searchQuery) {
        if (completed >= total) {
            showLoading(false);
            runOnUiThread(() -> performSearch(searchQuery));
        }
    }
    
    // Thêm ảnh vào danh sách mà không bị trùng lặp - sử dụng HashSet để tối ưu hóa
    private void addUniqueImages(List<StorageManager.ImageItem> newImages) {
        for (StorageManager.ImageItem image : newImages) {
            if (!loadedImagePaths.contains(image.getPath())) {
                allImages.add(image);
                loadedImagePaths.add(image.getPath());
            }
        }
    }
    
    // Hiển thị hoặc ẩn trạng thái đang tải
    private void showLoading(boolean isLoading) {
        if (isLoading) {
            layoutNoResults.setVisibility(View.VISIBLE);
            recyclerViewSearchResults.setVisibility(View.GONE);
            if (loadingTextView != null) {
                loadingTextView.setText(getString(R.string.loading_data));
            }
        } else {
            // Ẩn trạng thái loading nhưng không hiện kết quả tìm kiếm ngay
            // (sẽ được hiển thị bởi updateResultsVisibility dựa vào kết quả tìm kiếm)
            if (loadingTextView != null) {
                loadingTextView.setText(getString(R.string.no_results));
            }
        }
    }
    
    // Thực hiện tìm kiếm và cập nhật UI
    private void performSearch(String query) {
        String normalizedQuery = query != null ? query.trim().toLowerCase() : "";
        
        // Lọc các danh sách dựa trên query
        List<StorageManager.ImageItem> filteredAll = filterImagesByQuery(allImages, normalizedQuery);
        List<StorageManager.ImageItem> filteredUploaded = filterImagesByQuery(uploadedImages, normalizedQuery);
        List<StorageManager.ImageItem> filteredCompressed = filterImagesByQuery(compressedImages, normalizedQuery);
        
        // Cập nhật tiêu đề tìm kiếm
        if (normalizedQuery.isEmpty()) {
            textSearchResults.setText(getString(R.string.all_images));
        } else {
            // Hiển thị kết quả tìm kiếm với số lượng tìm được
            String result = String.format("Kết quả tìm kiếm: %s (%d)", 
                    query, filteredAll.size());
            textSearchResults.setText(result);
        }
        
        // Lọc theo tab hiện tại
        filterByTabWithData(filteredAll, filteredUploaded, filteredCompressed);
    }
    
    // Lọc danh sách ảnh dựa trên từ khóa tìm kiếm
    private List<StorageManager.ImageItem> filterImagesByQuery(List<StorageManager.ImageItem> images, String query) {
        if (query.isEmpty()) {
            return new ArrayList<>(images);
        }
        
        List<StorageManager.ImageItem> results = new ArrayList<>();
        for (StorageManager.ImageItem image : images) {
            if (image.getName().toLowerCase().contains(query)) {
                results.add(image);
            }
        }
        return results;
    }
    
    // Lọc dữ liệu theo tab đã chọn
    private void filterByTab() {
        String query = searchView.getQuery().toString().trim().toLowerCase();
        performSearch(query);
    }
    
    // Lọc dữ liệu theo tab với dữ liệu đã lọc theo query
    private void filterByTabWithData(List<StorageManager.ImageItem> filteredAll, 
                                    List<StorageManager.ImageItem> filteredUploaded,
                                    List<StorageManager.ImageItem> filteredCompressed) {
        List<StorageManager.ImageItem> resultsToShow;
        int resultCount;
        
        // Chọn danh sách kết quả dựa vào tab hiện tại
        switch (currentTabPosition) {
            case TAB_UPLOADED:
                resultsToShow = filteredUploaded;
                resultCount = filteredUploaded.size();
                break;
            case TAB_COMPRESSED:
                resultsToShow = filteredCompressed;
                resultCount = filteredCompressed.size();
                break;
            case TAB_ALL:
            default:
                resultsToShow = filteredAll;
                resultCount = filteredAll.size();
                break;
        }
        
        // Cập nhật tiêu đề tìm kiếm nếu có từ khóa tìm kiếm
        String query = searchView.getQuery().toString().trim();
        if (!query.isEmpty()) {
            String result = String.format("Kết quả tìm kiếm: %s (%d)", query, resultCount);
            textSearchResults.setText(result);
        }
        
        // Cập nhật adapter
        adapter.updateData(resultsToShow);
        
        // Hiển thị kết quả hoặc thông báo không có kết quả
        updateResultsVisibility(resultsToShow.isEmpty());
    }
    
    // Cập nhật trạng thái hiển thị kết quả tìm kiếm
    private void updateResultsVisibility(boolean isEmpty) {
        if (isEmpty) {
            layoutNoResults.setVisibility(View.VISIBLE);
            recyclerViewSearchResults.setVisibility(View.GONE);
            if (loadingTextView != null) {
                loadingTextView.setText(getString(R.string.no_results));
            }
        } else {
            layoutNoResults.setVisibility(View.GONE);
            recyclerViewSearchResults.setVisibility(View.VISIBLE);
        }
    }
    
    // Tạo menu với tùy chọn xóa lịch sử tìm kiếm
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_search, menu);
        return true;
    }
    
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.action_search) {
            return true;
        } else if (item.getItemId() == R.id.action_clear_history) {
            clearSearchHistory();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
    
    // Adapter cho RecyclerView với dữ liệu thật
    private class ImageAdapter extends RecyclerView.Adapter<ImageAdapter.ImageViewHolder> {
        private List<StorageManager.ImageItem> imageItems;
        
        public ImageAdapter(List<StorageManager.ImageItem> imageItems) {
            this.imageItems = imageItems;
        }
        
        public void updateData(List<StorageManager.ImageItem> newData) {
            this.imageItems = newData;
            notifyDataSetChanged();
        }
        
        @NonNull
        @Override
        public ImageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_file, parent, false);
            return new ImageViewHolder(view);
        }
        
        @Override
        public void onBindViewHolder(@NonNull ImageViewHolder holder, int position) {
            StorageManager.ImageItem imageItem = imageItems.get(position);
            holder.textFileName.setText(imageItem.getName());
            holder.textFileInfo.setText(imageItem.getInfo());
            
            // Load thumbnail từ Storage
            StorageManager.loadImageThumbnail(imageItem, holder.imageFileThumbnail);
            
            // Xử lý sự kiện khi nhấn vào item
            holder.itemView.setOnClickListener(v -> openImageDetail(imageItem));
        }
        
        @Override
        public int getItemCount() {
            return imageItems.size();
        }
        
        class ImageViewHolder extends RecyclerView.ViewHolder {
            ImageView imageFileThumbnail;
            TextView textFileName;
            TextView textFileInfo;
            
            public ImageViewHolder(@NonNull View itemView) {
                super(itemView);
                imageFileThumbnail = itemView.findViewById(R.id.image_file_thumbnail);
                textFileName = itemView.findViewById(R.id.text_file_name);
                textFileInfo = itemView.findViewById(R.id.text_file_info);
            }
        }
    }
    
    // Mở màn hình chi tiết ảnh
    private void openImageDetail(StorageManager.ImageItem imageItem) {
        StorageManager.openDetailActivity(this, imageItem);
    }

    // Lưu query vào lịch sử tìm kiếm
    private void saveQueryToHistory(String query) {
        if (query != null && !query.isEmpty()) {
            searchSuggestions.saveRecentQuery(query, null);
        }
    }
}