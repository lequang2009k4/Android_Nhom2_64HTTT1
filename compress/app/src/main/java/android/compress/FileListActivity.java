package android.compress;

import android.app.Activity;
import android.compress.utils.SearchHelper;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.content.Intent;
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
    
    // Thiết lập giao diện dựa theo loại file
    private void setupUI(int fileType) {
        List<FileItem> fileItems = new ArrayList<>();
        
        if (fileType == TYPE_UPLOADED) {
            // Sử dụng "< Đã tải lên" để chỉ rõ có icon mũi tên quay lại
            fileCategoryTitle.setText("Đã tải lên");
            fileSubtitle.setText("Tổng hợp file đã tải lên");
            
            // TODO: Thay thế bằng dữ liệu thật từ Firebase hoặc nguồn dữ liệu khác
            fileItems.add(new FileItem("Design Guide.pdf", "5MB • 15/04/2024", R.drawable.ic_image_placeholder));
            fileItems.add(new FileItem("Project Logo.png", "2MB • 14/04/2024", R.drawable.ic_image_placeholder));
            fileItems.add(new FileItem("Meeting Notes.docx", "1MB • 13/04/2024", R.drawable.ic_image_placeholder));
            fileItems.add(new FileItem("Product Images.zip", "15MB • 10/04/2024", R.drawable.ic_image_placeholder));
            
        } else if (fileType == TYPE_COMPRESSED) {
            // Sử dụng "< Đã nén" để chỉ rõ có icon mũi tên quay lại
            fileCategoryTitle.setText("Đã nén");
            fileSubtitle.setText("Tổng hợp file đã nén");
            
            // TODO: Thay thế bằng dữ liệu thật từ Firebase hoặc nguồn dữ liệu khác
            fileItems.add(new FileItem("Archive_2024.zip", "20MB • 15/04/2024", R.drawable.ic_image_placeholder));
            fileItems.add(new FileItem("Documents.rar", "8MB • 12/04/2024", R.drawable.ic_image_placeholder));
            fileItems.add(new FileItem("Photos_compressed.zip", "12MB • 08/04/2024", R.drawable.ic_image_placeholder));
        }
        
        FileAdapter adapter = new FileAdapter(fileItems);
        recyclerViewFiles.setAdapter(adapter);
    }
    
    // Khởi tạo activity với loại file
    public static void start(Activity activity, int fileType) {
        Intent intent = new Intent(activity, FileListActivity.class);
        intent.putExtra(EXTRA_FILE_TYPE, fileType);
        activity.startActivity(intent);
    }
    
    // Lớp chứa thông tin về file
    private static class FileItem {
        private String name;
        private String info;
        private int thumbnailRes;
        
        public FileItem(String name, String info, int thumbnailRes) {
            this.name = name;
            this.info = info;
            this.thumbnailRes = thumbnailRes;
        }
    }
    
    // Adapter cho RecyclerView
    private class FileAdapter extends RecyclerView.Adapter<FileAdapter.FileViewHolder> {
        private List<FileItem> fileItems;
        
        public FileAdapter(List<FileItem> fileItems) {
            this.fileItems = fileItems;
        }
        
        @NonNull
        @Override
        public FileViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_file, parent, false);
            return new FileViewHolder(view);
        }
        
        @Override
        public void onBindViewHolder(@NonNull FileViewHolder holder, int position) {
            FileItem fileItem = fileItems.get(position);
            holder.textFileName.setText(fileItem.name);
            holder.textFileInfo.setText(fileItem.info);
            holder.imageFileThumbnail.setImageResource(fileItem.thumbnailRes);
            
            // Xử lý sự kiện khi nhấn vào item
            holder.itemView.setOnClickListener(v -> {
                Intent intent = new Intent(FileListActivity.this, DetailActivity.class);
                intent.putExtra("file_name", fileItem.name);
                startActivity(intent);
            });
        }
        
        @Override
        public int getItemCount() {
            return fileItems.size();
        }
        
        class FileViewHolder extends RecyclerView.ViewHolder {
            ImageView imageFileThumbnail;
            TextView textFileName;
            TextView textFileInfo;
            
            public FileViewHolder(@NonNull View itemView) {
                super(itemView);
                imageFileThumbnail = itemView.findViewById(R.id.image_file_thumbnail);
                textFileName = itemView.findViewById(R.id.text_file_name);
                textFileInfo = itemView.findViewById(R.id.text_file_info);
            }
        }
    }
}
