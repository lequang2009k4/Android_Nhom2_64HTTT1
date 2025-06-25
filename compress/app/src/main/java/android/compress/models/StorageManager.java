package android.compress.models;

import android.compress.R;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Handler;
import android.os.Looper;
import android.widget.ImageView;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.Task;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.ListResult;
import com.google.firebase.storage.StorageReference;

import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Locale;
import java.util.concurrent.atomic.AtomicInteger;

public class StorageManager {

    private static final FirebaseStorage storage = FirebaseStorage.getInstance();
    private static final String UPLOADED_PATH = "uploaded/";
    private static final String COMPRESSED_PATH = "compressed/";
    
    // Thời gian cache hợp lệ (milliseconds) - 5 phút
    private static final long CACHE_VALID_DURATION = 5 * 60 * 1000;
    
    // Cache cho danh sách ảnh
    private static List<ImageItem> uploadedImagesCache = null;
    private static List<ImageItem> compressedImagesCache = null;
    
    // Thời điểm cập nhật cache gần nhất
    private static long uploadedCacheTimestamp = 0;
    private static long compressedCacheTimestamp = 0;
    
    // Handler cho main thread
    private static final Handler mainHandler = new Handler(Looper.getMainLooper());
    
    // Quản lý task đang chạy
    private static final TaskManager taskManager = new TaskManager();

    public interface StorageCallback {
        void onSuccess(List<ImageItem> imageItems);
        void onFailure(Exception e);
    }
    
    public interface CompressionCallback {
        void onSuccess(Bitmap compressedBitmap, int sizeReduction);
        void onFailure(Exception e);
    }
    
    /**
     * Kiểm tra xem có đang ở main thread không
     */
    private static boolean isMainThread() {
        return Looper.myLooper() == Looper.getMainLooper();
    }
    
    /**
     * Đảm bảo runnable được chạy trên main thread
     */
    private static void runOnUiThread(Runnable runnable) {
        if (isMainThread()) {
            runnable.run();
        } else {
            mainHandler.post(runnable);
        }
    }
    
    /**
     * Lớp quản lý các task đang chạy
     */
    private static class TaskManager {
        private final Map<String, Task<?>> activeTasks = new HashMap<>();
        private final AtomicInteger taskIdGenerator = new AtomicInteger(0);
        
        /**
         * Đăng ký một task mới và trả về ID
         */
        public String registerTask(Task<?> task) {
            String taskId = String.valueOf(taskIdGenerator.incrementAndGet());
            synchronized (activeTasks) {
                activeTasks.put(taskId, task);
                
                // Tự động xóa task khi hoàn thành hoặc thất bại
                task.addOnCompleteListener(t -> {
                    synchronized (activeTasks) {
                        activeTasks.remove(taskId);
                    }
                });
            }
            return taskId;
        }
        
        /**
         * Hủy tất cả task đang chạy
         * Lưu ý: Chỉ xóa khỏi danh sách theo dõi
         * vì Firebase Task không hỗ trợ hủy trực tiếp
         */
        public void cancelAllTasks() {
            synchronized (activeTasks) {
                activeTasks.clear();
            }
        }
    }

    /**
     * Lấy danh sách ảnh đã tải lên từ Firebase Storage
     */
    public static void getUploadedImages(StorageCallback callback) {
        // Kiểm tra cache trước
        if (isCacheValid(uploadedImagesCache, uploadedCacheTimestamp)) {
            runOnUiThread(() -> callback.onSuccess(uploadedImagesCache));
            return;
        }
        
        StorageReference uploadedRef = storage.getReference().child(UPLOADED_PATH);
        getImagesFromPath(uploadedRef, new StorageCallback() {
            @Override
            public void onSuccess(List<ImageItem> imageItems) {
                // Lưu kết quả vào cache
                uploadedImagesCache = new ArrayList<>(imageItems);
                uploadedCacheTimestamp = System.currentTimeMillis();
                runOnUiThread(() -> callback.onSuccess(imageItems));
            }
            
            @Override
            public void onFailure(Exception e) {
                runOnUiThread(() -> callback.onFailure(e));
            }
        });
    }

    /**
     * Lấy danh sách ảnh đã nén từ Firebase Storage
     */
    public static void getCompressedImages(StorageCallback callback) {
        // Kiểm tra cache trước
        if (isCacheValid(compressedImagesCache, compressedCacheTimestamp)) {
            runOnUiThread(() -> callback.onSuccess(compressedImagesCache));
            return;
        }
        
        StorageReference compressedRef = storage.getReference().child(COMPRESSED_PATH);
        getImagesFromPath(compressedRef, new StorageCallback() {
            @Override
            public void onSuccess(List<ImageItem> imageItems) {
                // Lưu kết quả vào cache
                compressedImagesCache = new ArrayList<>(imageItems);
                compressedCacheTimestamp = System.currentTimeMillis();
                runOnUiThread(() -> callback.onSuccess(imageItems));
            }
            
            @Override
            public void onFailure(Exception e) {
                runOnUiThread(() -> callback.onFailure(e));
            }
        });
    }
    
    /**
     * Kiểm tra xem cache có hợp lệ không
     */
    private static boolean isCacheValid(List<ImageItem> cache, long timestamp) {
        if (cache == null || cache.isEmpty()) {
            return false;
        }
        
        long currentTime = System.currentTimeMillis();
        long cacheAge = currentTime - timestamp;
        
        return cacheAge < CACHE_VALID_DURATION;
    }
    
    /**
     * Xóa cache để buộc tải lại dữ liệu từ Firebase
     */
    public static void clearCache() {
        uploadedImagesCache = null;
        compressedImagesCache = null;
        uploadedCacheTimestamp = 0;
        compressedCacheTimestamp = 0;
    }
    
    /**
     * Cập nhật cache sau khi thêm/xóa ảnh
     * Gọi phương thức này sau khi tải lên hoặc xóa ảnh
     */
    public static void refreshCache() {
        clearCache();
    }
    
    /**
     * Hủy tất cả các tác vụ đang chạy
     * Gọi phương thức này trong onDestroy() của Activity
     */
    public static void cancelAllTasks() {
        taskManager.cancelAllTasks();
    }

    /**
     * Phương thức chung để lấy danh sách ảnh từ một đường dẫn
     */
    private static void getImagesFromPath(StorageReference folderRef, StorageCallback callback) {
        Task<ListResult> listTask = folderRef.listAll()
                .addOnSuccessListener(listResult -> {
                    List<ImageItem> imageItems = new ArrayList<>();
                    if (listResult.getItems().isEmpty()) {
                        runOnUiThread(() -> callback.onSuccess(imageItems));
                        return;
                    }

                    final int[] completedCount = {0};
                    for (StorageReference item : listResult.getItems()) {
                        item.getMetadata().addOnSuccessListener(storageMetadata -> {
                            String name = item.getName();
                            String contentType = storageMetadata.getContentType();
                            
                            // Chỉ xử lý file ảnh
                            if (contentType != null && contentType.startsWith("image/")) {
                                long size = storageMetadata.getSizeBytes();
                                long creationTime = storageMetadata.getCreationTimeMillis();
                                
                                // Tạo đối tượng ImageItem mới
                                ImageItem imageItem = new ImageItem(name, item.getPath(),
                                        formatSize(size), formatDate(creationTime), contentType, size, item);
                                imageItems.add(imageItem);
                            }
                            
                            completedCount[0]++;
                            if (completedCount[0] == listResult.getItems().size()) {
                                runOnUiThread(() -> callback.onSuccess(imageItems));
                            }
                        }).addOnFailureListener(e -> {
                            completedCount[0]++;
                            if (completedCount[0] == listResult.getItems().size()) {
                                runOnUiThread(() -> callback.onSuccess(imageItems));
                            }
                        });
                    }
                })
                .addOnFailureListener(e -> runOnUiThread(() -> callback.onFailure(e)));
        
        // Đăng ký task để có thể quản lý
        taskManager.registerTask(listTask);
    }

    /**
     * Tải thumbnail của ảnh với cách xử lý an toàn
     * Trả về taskId để có thể quản lý tác vụ nếu cần
     */
    public static String loadImageThumbnail(ImageItem imageItem, ImageView imageView) {
        try {
            StorageReference imageRef = imageItem.getStorageRef();
            
            // Bước 1: Tính toán kích thước mục tiêu cho thumbnail
            int initialTargetSize = Math.max(imageView.getWidth(), imageView.getHeight());
            final int targetSize = initialTargetSize <= 0 ? 300 : initialTargetSize; // Giá trị mặc định
            
            final int maxSize = 500 * 1024; // 500KB maximum cho thumbnail
            
            // Tải dữ liệu ảnh
            Task<byte[]> downloadTask = imageRef.getBytes(maxSize)
                .addOnSuccessListener(bytes -> {
                    try {
                        // Tạo bitmap với kích thước phù hợp
                        Bitmap bitmap = decodeSampledBitmapFromByteArray(bytes, targetSize, targetSize);
                        if (bitmap != null) {
                            runOnUiThread(() -> imageView.setImageBitmap(bitmap));
                        } else {
                            runOnUiThread(() -> imageView.setImageResource(R.drawable.ic_image_placeholder));
                        }
                    } catch (OutOfMemoryError e) {
                        // Xử lý khi bị OOM
                        runOnUiThread(() -> imageView.setImageResource(R.drawable.ic_image_placeholder));
                    }
                })
                .addOnFailureListener(e -> runOnUiThread(() -> 
                    imageView.setImageResource(R.drawable.ic_image_placeholder)));
            
            // Đăng ký task để có thể quản lý
            return taskManager.registerTask(downloadTask);
        } catch (Exception e) {
            runOnUiThread(() -> imageView.setImageResource(R.drawable.ic_image_placeholder));
            return null;
        }
    }
    
    /**
     * Decode bitmap từ mảng byte với kích thước được thu nhỏ phù hợp
     */
    private static Bitmap decodeSampledBitmapFromByteArray(byte[] data, int reqWidth, int reqHeight) {
        // Bước 1: Tính inSampleSize
        final BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeByteArray(data, 0, data.length, options);
        
        options.inSampleSize = calculateInSampleSize(options, reqWidth, reqHeight);
        
        // Bước 2: Decode với inSampleSize đã tính
        options.inJustDecodeBounds = false;
        options.inPreferredConfig = Bitmap.Config.RGB_565; // Tiết kiệm bộ nhớ
        
        try {
            return BitmapFactory.decodeByteArray(data, 0, data.length, options);
        } catch (OutOfMemoryError e) {
            // Nếu vẫn OOM, tăng inSampleSize và thử lại
            options.inSampleSize *= 2;
            return BitmapFactory.decodeByteArray(data, 0, data.length, options);
        }
    }
    
    /**
     * Tính toán inSampleSize để giảm kích thước bitmap
     */
    private static int calculateInSampleSize(BitmapFactory.Options options, int reqWidth, int reqHeight) {
        final int height = options.outHeight;
        final int width = options.outWidth;
        int inSampleSize = 1;
        
        if (height > reqHeight || width > reqWidth) {
            final int halfHeight = height / 2;
            final int halfWidth = width / 2;
            
            while ((halfHeight / inSampleSize) >= reqHeight && (halfWidth / inSampleSize) >= reqWidth) {
                inSampleSize *= 2;
            }
        }
        
        return inSampleSize;
    }

    /**
     * Format kích thước file
     */
    private static String formatSize(long sizeBytes) {
        if (sizeBytes < 1024) {
            return sizeBytes + " B";
        } else if (sizeBytes < 1024 * 1024) {
            return String.format(Locale.getDefault(), "%.2f KB", sizeBytes / 1024.0);
        } else {
            return String.format(Locale.getDefault(), "%.2f MB", sizeBytes / (1024.0 * 1024.0));
        }
    }

    /**
     * Format thời gian tạo
     */
    private static String formatDate(long creationTimeMillis) {
        java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("dd/MM/yyyy HH:mm", java.util.Locale.getDefault());
        return sdf.format(new java.util.Date(creationTimeMillis));
    }

    /**
     * Lớp đại diện cho một ảnh
     */
    public static class ImageItem {
        private final String name;
        private final String path;
        private final String size;
        private final String date;
        private final String mimeType;
        private final long sizeBytes;
        private final StorageReference storageRef;

        public ImageItem(String name, String path, String size, String date, String mimeType, 
                       long sizeBytes, StorageReference storageRef) {
            this.name = name;
            this.path = path;
            this.size = size;
            this.date = date;
            this.mimeType = mimeType;
            this.sizeBytes = sizeBytes;
            this.storageRef = storageRef;
        }

        public String getName() {
            return name;
        }

        public String getPath() {
            return path;
        }

        public String getSize() {
            return size;
        }

        public String getDate() {
            return date;
        }

        public String getMimeType() {
            return mimeType;
        }

        public StorageReference getStorageRef() {
            return storageRef;
        }
        
        public long getSizeBytes() {
            return sizeBytes;
        }
        
        public String getInfo() {
            return size + " • " + date;
        }
    }

    /**
     * Tải ảnh đầy đủ từ Firebase Storage và mở DetailActivity
     * Phương thức này giúp đồng nhất luồng 2 với luồng 1
     */
    public static void openDetailActivity(Context context, ImageItem imageItem) {
        try {
            // Tạo ProgressDialog
            android.app.ProgressDialog progressDialog = new android.app.ProgressDialog(context);
            progressDialog.setMessage("Đang tải ảnh...");
            progressDialog.setCancelable(false);
            progressDialog.show();
            
            // Tải bytes từ Firebase 
            final long ONE_MEGABYTE = 1024 * 1024 * 10; // 10MB max
            imageItem.getStorageRef().getBytes(ONE_MEGABYTE)
                .addOnSuccessListener(bytes -> {
                    try {
                        // Lưu vào cache directory để ContentResolver có thể đọc
                        java.io.File cachePath = new java.io.File(context.getCacheDir(), "images");
                        cachePath.mkdirs();
                        java.io.File imageFile = new java.io.File(cachePath, imageItem.getName());
                        
                        java.io.FileOutputStream stream = new java.io.FileOutputStream(imageFile);
                        stream.write(bytes);
                        stream.close();
                        
                        // Tạo content Uri từ file đã lưu
                        android.net.Uri contentUri = androidx.core.content.FileProvider.getUriForFile(
                            context, 
                            context.getPackageName() + ".fileprovider",
                            imageFile);
                        
                        // Đóng dialog
                        if (progressDialog != null && progressDialog.isShowing()) {
                            progressDialog.dismiss();
                        }
                        
                        // Chuẩn bị intent với ContentUri
                        Intent intent = new Intent(context, android.compress.DetailActivity.class);
                        intent.putExtra("file_name", imageItem.getName());
                        intent.putExtra("file_size", imageItem.getSize());
                        intent.putExtra("upload_date", imageItem.getDate());
                        intent.putExtra("image_uri", contentUri.toString());
                        
                        // Cấp quyền đọc URI
                        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                        
                        context.startActivity(intent);
                    } catch (Exception e) {
                        // Fallback nếu lưu file lỗi
                        if (progressDialog != null && progressDialog.isShowing()) {
                            progressDialog.dismiss();
                        }
                        
                        Intent intent = new Intent(context, android.compress.DetailActivity.class);
                        intent.putExtra("file_name", imageItem.getName());
                        intent.putExtra("file_size", imageItem.getSize());
                        intent.putExtra("upload_date", imageItem.getDate());
                        context.startActivity(intent);
                    }
                })
                .addOnFailureListener(e -> {
                    // Fallback nếu tải lỗi
                    if (progressDialog != null && progressDialog.isShowing()) {
                        progressDialog.dismiss();
                    }
                    
                    Intent intent = new Intent(context, android.compress.DetailActivity.class);
                    intent.putExtra("file_name", imageItem.getName());
                    intent.putExtra("file_size", imageItem.getSize());
                    intent.putExtra("upload_date", imageItem.getDate());
                    context.startActivity(intent);
                });
        } catch (Exception e) {
            // Fallback nếu có lỗi
            Intent intent = new Intent(context, android.compress.DetailActivity.class);
            intent.putExtra("file_name", imageItem.getName());
            intent.putExtra("file_size", imageItem.getSize());
            intent.putExtra("upload_date", imageItem.getDate());
            context.startActivity(intent);
        }
    }
}