package android.compress;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.textfield.TextInputEditText;

import java.util.ArrayList;
import java.util.List;

public class SearchActivity extends AppCompatActivity {

    private RecyclerView recyclerViewSearchResults;
    private TextView textSearchResults;
    private LinearLayout layoutNoResults;
    private TextInputEditText searchEditText;
    private List<FileItem> allFiles;
    private FileAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);

        // Khởi tạo các view
        recyclerViewSearchResults = findViewById(R.id.recycler_view_search_results);
        textSearchResults = findViewById(R.id.text_search_results);
        layoutNoResults = findViewById(R.id.layout_no_results);
        
        // Thiết lập RecyclerView
        recyclerViewSearchResults.setLayoutManager(new GridLayoutManager(this, 2));

        // Xử lý intent từ các màn hình khác
        String searchQuery = getIntent().getStringExtra("search_query");
        
        // Lấy EditText từ search bar
        View searchBarView = findViewById(R.id.search_bar);
        searchEditText = searchBarView.findViewById(R.id.edit_text_search);
        
        // Đặt giá trị tìm kiếm từ intent (nếu có)
        if (searchQuery != null && !searchQuery.isEmpty()) {
            searchEditText.setText(searchQuery);
        }
        
        // Thiết lập sự kiện cho EditText
        searchEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable s) {
                performSearch(s.toString());
            }
        });

        // Xử lý sự kiện nút nén mới
        Button buttonNewCompress = findViewById(R.id.button_new_compress);
        buttonNewCompress.setOnClickListener(v -> {
            Intent intent = new Intent(SearchActivity.this, UploadActivity.class);
            startActivity(intent);
        });

        // Thiết lập dữ liệu giả lập
        setupMockData();
        
        // Thực hiện tìm kiếm ban đầu
        performSearch(searchQuery != null ? searchQuery : "");
    }

    // Thiết lập dữ liệu giả lập
    private void setupMockData() {
        allFiles = new ArrayList<>();
        
        // Thêm dữ liệu giả lập (kết hợp cả file đã tải lên và đã nén)
        allFiles.add(new FileItem("Design Guide.pdf", "5MB • 15/04/2024", R.drawable.ic_image_placeholder));
        allFiles.add(new FileItem("Project Logo.png", "2MB • 14/04/2024", R.drawable.ic_image_placeholder));
        allFiles.add(new FileItem("Meeting Notes.docx", "1MB • 13/04/2024", R.drawable.ic_image_placeholder));
        allFiles.add(new FileItem("Product Images.zip", "15MB • 10/04/2024", R.drawable.ic_image_placeholder));
        allFiles.add(new FileItem("Archive_2024.zip", "20MB • 15/04/2024", R.drawable.ic_image_placeholder));
        allFiles.add(new FileItem("Documents.rar", "8MB • 12/04/2024", R.drawable.ic_image_placeholder));
        allFiles.add(new FileItem("Photos_compressed.zip", "12MB • 08/04/2024", R.drawable.ic_image_placeholder));
        allFiles.add(new FileItem("Presentation.pptx", "7MB • 05/04/2024", R.drawable.ic_image_placeholder));
        
        // Khởi tạo adapter với danh sách rỗng ban đầu
        adapter = new FileAdapter(new ArrayList<>());
        recyclerViewSearchResults.setAdapter(adapter);
    }
    
    // Thực hiện tìm kiếm và cập nhật UI
    private void performSearch(String query) {
        List<FileItem> searchResults = new ArrayList<>();
        
        // Nếu query rỗng, hiển thị tất cả
        if (query == null || query.trim().isEmpty()) {
            searchResults.addAll(allFiles);
            textSearchResults.setText("Tất cả các file");
        } else {
            // Tìm kiếm các file phù hợp
            for (FileItem file : allFiles) {
                if (file.name.toLowerCase().contains(query.toLowerCase())) {
                    searchResults.add(file);
                }
            }
            textSearchResults.setText("Kết quả tìm kiếm cho '" + query + "'");
        }
        
        // Cập nhật adapter
        adapter.updateData(searchResults);
        
        // Hiển thị thông báo không có kết quả nếu cần
        if (searchResults.isEmpty()) {
            layoutNoResults.setVisibility(View.VISIBLE);
            recyclerViewSearchResults.setVisibility(View.GONE);
        } else {
            layoutNoResults.setVisibility(View.GONE);
            recyclerViewSearchResults.setVisibility(View.VISIBLE);
        }
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
        
        public void updateData(List<FileItem> newData) {
            this.fileItems = newData;
            notifyDataSetChanged();
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
                Intent intent = new Intent(SearchActivity.this, DetailActivity.class);
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