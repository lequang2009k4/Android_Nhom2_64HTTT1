// compress/app/src/main/java/android/compress/HomeActivity.java
package android.compress;

import android.compress.R;
import android.compress.models.StorageManager;
import android.compress.utils.SearchHelper;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.core.graphics.Insets;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import java.util.ArrayList;
import java.util.List;

public class HomeActivity extends AppCompatActivity {

    private RecyclerView recyclerViewUploaded;
    private RecyclerView recyclerViewCompressed;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_home);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Khởi tạo RecyclerView
        recyclerViewUploaded = findViewById(R.id.recycler_view_uploaded);
        recyclerViewCompressed = findViewById(R.id.recycler_view_compressed);

        // Thiết lập thanh tìm kiếm - Chỉ cần một dòng này thay vì cả một phương thức
        SearchHelper.setupSearchBar(this, R.id.home_search_bar);

        // Nút nén mới
        Button newCompressButton = findViewById(R.id.button_new_compress);
        newCompressButton.setOnClickListener(v -> {
            Intent intent = new Intent(HomeActivity.this, UploadActivity.class);
            startActivity(intent);
        });

        // Nút xem tất cả các file đã tải lên
        ImageButton viewAllUploaded = findViewById(R.id.button_view_all_uploaded);
        viewAllUploaded.setOnClickListener(v -> {
            FileListActivity.start(HomeActivity.this, FileListActivity.TYPE_UPLOADED);
        });

        // Nút xem tất cả các file đã nén
        ImageButton viewAllCompressed = findViewById(R.id.button_view_all_compressed);
        viewAllCompressed.setOnClickListener(v -> {
            FileListActivity.start(HomeActivity.this, FileListActivity.TYPE_COMPRESSED);
        });

        // Thiết lập RecyclerView với LayoutManager - Horizontal
        recyclerViewUploaded.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        recyclerViewCompressed.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));

        // Load dữ liệu thật
        loadData();
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Tải lại dữ liệu mỗi khi activity được mở lại
        loadData();
        
        // Reset SearchView mỗi khi quay về Home
        View searchBarView = findViewById(R.id.home_search_bar);
        if (searchBarView != null) {
            SearchView searchView = searchBarView.findViewById(R.id.search_view);
            if (searchView != null) {
                searchView.setQuery("", false);
                searchView.clearFocus();
            }
        }
    }
    
    @Override
    protected void onDestroy() {
        // Hủy tất cả tác vụ đang chạy khi Activity bị hủy
        StorageManager.cancelAllTasks();
        super.onDestroy();
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);
        
        // Tìm và reset SearchView
        View searchBarView = findViewById(R.id.home_search_bar);
        if (searchBarView != null) {
            androidx.appcompat.widget.SearchView searchView = searchBarView.findViewById(R.id.search_view);
            if (searchView != null) {
                searchView.setQuery("", false);
                searchView.clearFocus();
            }
        }
    }

    // Load dữ liệu thật từ Firebase
    private void loadData() {
        // Load dữ liệu đã upload
        StorageManager.getUploadedImages(new StorageManager.StorageCallback() {
            @Override
            public void onSuccess(List<StorageManager.ImageItem> imageItems) {
                ImageAdapter uploadedAdapter = new ImageAdapter(imageItems);
                recyclerViewUploaded.setAdapter(uploadedAdapter);

                if (imageItems.isEmpty()) {
                    // Có thể hiển thị thông báo "Không có ảnh"
                    Toast.makeText(HomeActivity.this, "Không có ảnh nào đã tải lên", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Exception e) {
                Toast.makeText(HomeActivity.this, "Lỗi tải dữ liệu: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });

        // Load dữ liệu đã nén
        StorageManager.getCompressedImages(new StorageManager.StorageCallback() {
            @Override
            public void onSuccess(List<StorageManager.ImageItem> imageItems) {
                ImageAdapter compressedAdapter = new ImageAdapter(imageItems);
                recyclerViewCompressed.setAdapter(compressedAdapter);

                if (imageItems.isEmpty()) {
                    // Có thể hiển thị thông báo "Không có ảnh"
                    Toast.makeText(HomeActivity.this, "Không có ảnh nào đã nén", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Exception e) {
                Toast.makeText(HomeActivity.this, "Lỗi tải dữ liệu: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    // Adapter cho RecyclerView với dữ liệu thật
    private class ImageAdapter extends RecyclerView.Adapter<ImageAdapter.ImageViewHolder> {
        private List<StorageManager.ImageItem> imageItems;

        public ImageAdapter(List<StorageManager.ImageItem> imageItems) {
            this.imageItems = imageItems;
        }

        @NonNull
        @Override
        public ImageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_file, parent, false);
            
            // Điều chỉnh kích thước cho item hiển thị ngang
            ViewGroup.LayoutParams layoutParams = view.getLayoutParams();
            layoutParams.width = dpToPx(140); // Kích thước cố định 140dp
            layoutParams.height = dpToPx(140); // Kích thước cố định 140dp
            view.setLayoutParams(layoutParams);
            
            return new ImageViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull ImageViewHolder holder, int position) {
            StorageManager.ImageItem imageItem = imageItems.get(position);
            holder.textFileName.setText(imageItem.getName());
            
            // Load thumbnail từ Storage
            StorageManager.loadImageThumbnail(imageItem, holder.imageFileThumbnail);

            // Ẩn phần thông tin file vì không cần thiết trong hiển thị ngang
            if (holder.textFileInfo != null) {
                holder.textFileInfo.setVisibility(View.GONE);
            }

            // Xử lý sự kiện khi nhấn vào item
            holder.itemView.setOnClickListener(v -> {
                StorageManager.openDetailActivity(HomeActivity.this, imageItem);
            });
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

    // Phương thức chuyển đổi dp sang pixel
    private int dpToPx(int dp) {
        float density = getResources().getDisplayMetrics().density;
        return Math.round(dp * density);
    }
}