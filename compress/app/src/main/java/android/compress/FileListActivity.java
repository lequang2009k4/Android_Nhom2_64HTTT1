package android.compress;

import android.app.Activity;
import android.compress.models.StorageManager;
import android.compress.utils.SearchHelper;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.content.Intent;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.GridLayoutManager;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.ImageView;

import java.util.ArrayList;
import java.util.List;

public class FileListActivity extends Activity {
    
    private static final String EXTRA_FILE_TYPE = "file_type";
    public static final int TYPE_UPLOADED = 1;
    public static final int TYPE_COMPRESSED = 2;
    
    private TextView fileCategoryTitle;
    private TextView fileSubtitle;
    private RecyclerView recyclerViewFiles;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_file_list);
        
        // Khởi tạo StorageManager
        StorageManager.init(getApplicationContext());
        
        // Khởi tạo các view
        fileCategoryTitle = findViewById(R.id.file_category_title);
        fileSubtitle = findViewById(R.id.file_subtitle);
        recyclerViewFiles = findViewById(R.id.recycler_view_files);
        ImageButton buttonBack = findViewById(R.id.button_back);
        Button buttonNewCompress = findViewById(R.id.button_new_compress);
        
        // Thiết lập thanh tìm kiếm - Chỉ cần một dòng này
        SearchHelper.setupSearchBar(this, R.id.file_list_search_bar);
        
        // Xử lý sự kiện nút quay lại
        buttonBack.setOnClickListener(v -> finish());
        
        // Xử lý sự kiện nút nén mới
        buttonNewCompress.setOnClickListener(v -> {
            Intent intent = new Intent(FileListActivity.this, UploadActivity.class);
            startActivity(intent);
        });
        
        // Thiết lập RecyclerView
        recyclerViewFiles.setLayoutManager(new GridLayoutManager(this, 2));
        
        // Lấy dữ liệu từ intent
        int fileType = getIntent().getIntExtra(EXTRA_FILE_TYPE, TYPE_UPLOADED);
        setupUI(fileType);
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Tải lại dữ liệu khi activity được mở lại
        int fileType = getIntent().getIntExtra(EXTRA_FILE_TYPE, TYPE_UPLOADED);
        setupUI(fileType);
    }
    
    @Override
    protected void onDestroy() {
        // Hủy tất cả tác vụ đang chạy khi Activity bị hủy
        StorageManager.cancelAllTasks();
        super.onDestroy();
    }
    
    // Thiết lập giao diện dựa theo loại file
    private void setupUI(int fileType) {
        if (fileType == TYPE_UPLOADED) {
            // Sử dụng "< Đã tải lên" để chỉ rõ có icon mũi tên quay lại
            fileCategoryTitle.setText("Đã tải lên");
            fileSubtitle.setText("Tổng hợp ảnh đã tải lên");
            
            // Load dữ liệu thực từ Firebase Storage
            StorageManager.getUploadedImages(new StorageManager.StorageCallback() {
                @Override
                public void onSuccess(List<StorageManager.ImageItem> imageItems) {
                    ImageAdapter adapter = new ImageAdapter(imageItems);
                    recyclerViewFiles.setAdapter(adapter);
                    
                    if (imageItems.isEmpty()) {
                        Toast.makeText(FileListActivity.this, "Không có ảnh nào đã tải lên", Toast.LENGTH_SHORT).show();
                    }
                }
                
                @Override
                public void onFailure(Exception e) {
                    Toast.makeText(FileListActivity.this, "Lỗi tải dữ liệu: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
            
        } else if (fileType == TYPE_COMPRESSED) {
            // Sử dụng "< Đã nén" để chỉ rõ có icon mũi tên quay lại
            fileCategoryTitle.setText("Đã nén");
            fileSubtitle.setText("Tổng hợp ảnh đã nén");
            
            // Load dữ liệu thực từ Firebase Storage
            StorageManager.getCompressedImages(new StorageManager.StorageCallback() {
                @Override
                public void onSuccess(List<StorageManager.ImageItem> imageItems) {
                    ImageAdapter adapter = new ImageAdapter(imageItems);
                    recyclerViewFiles.setAdapter(adapter);
                    
                    if (imageItems.isEmpty()) {
                        Toast.makeText(FileListActivity.this, "Không có ảnh nào đã nén", Toast.LENGTH_SHORT).show();
                    }
                }
                
                @Override
                public void onFailure(Exception e) {
                    Toast.makeText(FileListActivity.this, "Lỗi tải dữ liệu: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
        }
    }
    
    // Khởi tạo activity với loại file
    public static void start(Activity activity, int fileType) {
        Intent intent = new Intent(activity, FileListActivity.class);
        intent.putExtra(EXTRA_FILE_TYPE, fileType);
        activity.startActivity(intent);
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
            holder.itemView.setOnClickListener(v -> {
                StorageManager.openDetailActivity(FileListActivity.this, imageItem);
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
}