// compress/app/src/main/java/android/compress/HomeActivity.java
package android.compress;

import android.compress.R;
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

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
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

        // Thiết lập dữ liệu giả lập
        setupMockData();
    }

    // Thiết lập dữ liệu giả lập cho cả hai RecyclerView
    private void setupMockData() {
        // Dữ liệu giả lập cho file đã tải lên
        List<FileItem> uploadedFiles = new ArrayList<>();
        uploadedFiles.add(new FileItem("Design Guide", R.drawable.ic_image_placeholder));
        uploadedFiles.add(new FileItem("Project Logo", R.drawable.ic_image_placeholder));
        uploadedFiles.add(new FileItem("Meeting Notes", R.drawable.ic_image_placeholder));
        uploadedFiles.add(new FileItem("Images", R.drawable.ic_image_placeholder));

        // Dữ liệu giả lập cho file đã nén
        List<FileItem> compressedFiles = new ArrayList<>();
        compressedFiles.add(new FileItem("Archive_2024", R.drawable.ic_image_placeholder));
        compressedFiles.add(new FileItem("Documents", R.drawable.ic_image_placeholder));
        compressedFiles.add(new FileItem("Photos", R.drawable.ic_image_placeholder));

        // Thiết lập adapter cho RecyclerView
        FileAdapter uploadedAdapter = new FileAdapter(uploadedFiles);
        FileAdapter compressedAdapter = new FileAdapter(compressedFiles);

        // Thiết lập LayoutManager cho RecyclerView - Horizontal
        recyclerViewUploaded.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        recyclerViewCompressed.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));

        // Gán adapter
        recyclerViewUploaded.setAdapter(uploadedAdapter);
        recyclerViewCompressed.setAdapter(compressedAdapter);
    }

    // Lớp chứa thông tin về file
    private static class FileItem {
        private String name;
        private int thumbnailRes;

        public FileItem(String name, int thumbnailRes) {
            this.name = name;
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
            
            // Điều chỉnh kích thước cho item hiển thị ngang
            ViewGroup.LayoutParams layoutParams = view.getLayoutParams();
            layoutParams.width = dpToPx(140); // Kích thước cố định 140dp thay vì dùng mtrl_card_spacing
            layoutParams.height = dpToPx(140); // Kích thước cố định 140dp
            view.setLayoutParams(layoutParams);
            
            return new FileViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull FileViewHolder holder, int position) {
            FileItem fileItem = fileItems.get(position);
            holder.textFileName.setText(fileItem.name);
            holder.imageFileThumbnail.setImageResource(fileItem.thumbnailRes);

            // Ẩn phần thông tin file vì không cần thiết trong hiển thị ngang
            if (holder.textFileInfo != null) {
                holder.textFileInfo.setVisibility(View.GONE);
            }

            // Xử lý sự kiện khi nhấn vào item
            holder.itemView.setOnClickListener(v -> {
                Intent intent = new Intent(HomeActivity.this, DetailActivity.class);
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

    // Phương thức chuyển đổi dp sang pixel
    private int dpToPx(int dp) {
        float density = getResources().getDisplayMetrics().density;
        return Math.round(dp * density);
    }
}