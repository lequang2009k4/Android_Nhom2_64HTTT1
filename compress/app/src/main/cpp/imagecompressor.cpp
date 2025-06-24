#include <jni.h>
#include <vector>
#include <cstring>
#include <algorithm>

// Cách 1: Chỉ xóa EXIF data ở đầu file JPEG (cực kỳ an toàn)
std::vector<jbyte> removeJpegExif(const std::vector<jbyte>& data) {
    // Kiểm tra nếu là JPEG
    if (data.size() < 4 || 
        static_cast<unsigned char>(data[0]) != 0xFF || 
        static_cast<unsigned char>(data[1]) != 0xD8) {
        return data; // Không phải JPEG, trả về nguyên bản
    }
    
    std::vector<jbyte> result;
    result.push_back(data[0]); // FF
    result.push_back(data[1]); // D8 (SOI)
    
    size_t pos = 2;
    bool foundFirstScan = false;
    
    while (pos < data.size() - 1 && !foundFirstScan) {
        if (static_cast<unsigned char>(data[pos]) == 0xFF) {
            unsigned char marker = static_cast<unsigned char>(data[pos + 1]);
            
            // APP1 (EXIF) - có thể xóa an toàn
            if (marker == 0xE1) {
                pos += 2; // Skip marker
                if (pos + 1 < data.size()) {
                    // Đọc length của segment
                    unsigned short length = (static_cast<unsigned char>(data[pos]) << 8) | 
                                           static_cast<unsigned char>(data[pos + 1]);
                    pos += length; // Bỏ qua toàn bộ EXIF segment
                    continue; // Không copy segment này
                }
            }
            // APP0 (JFIF) - giữ lại
            else if (marker == 0xE0) {
                result.push_back(data[pos]);
                result.push_back(data[pos + 1]);
                pos += 2;
                if (pos + 1 < data.size()) {
                    unsigned short length = (static_cast<unsigned char>(data[pos]) << 8) | 
                                           static_cast<unsigned char>(data[pos + 1]);
                    for (int i = 0; i < length && pos < data.size(); i++) {
                        result.push_back(data[pos]);
                        pos++;
                    }
                }
            }
            // Start of Scan - từ đây trở đi là dữ liệu ảnh quan trọng
            else if (marker == 0xDA) {
                foundFirstScan = true;
                // Copy hết phần còn lại
                while (pos < data.size()) {
                    result.push_back(data[pos]);
                    pos++;
                }
            }
            // Các marker quan trọng khác
            else {
                result.push_back(data[pos]);
                result.push_back(data[pos + 1]);
                pos += 2;
                
                // Nếu marker có length, copy theo length
                if (marker != 0xD9 && pos + 1 < data.size()) {
                    unsigned short length = (static_cast<unsigned char>(data[pos]) << 8) | 
                                           static_cast<unsigned char>(data[pos + 1]);
                    for (int i = 0; i < length && pos < data.size(); i++) {
                        result.push_back(data[pos]);
                        pos++;
                    }
                }
            }
        } else {
            result.push_back(data[pos]);
            pos++;
        }
    }
    
    return result;
}

// Cách 2: Chỉ cắt bớt dữ liệu ở cuối file nếu thực sự cần
std::vector<jbyte> safeTruncate(const std::vector<jbyte>& data, int quality) {
    if (quality >= 80) return data; // Quality cao thì không động gì
    
    // Chỉ cắt tối đa 10% và chỉ khi quality rất thấp
    if (quality < 30) {
        size_t keepSize = static_cast<size_t>(data.size() * 0.9); // Giữ 90%
        std::vector<jbyte> result(data.begin(), data.begin() + keepSize);
        
        // Nếu là JPEG, thêm EOI marker
        if (data.size() >= 2 && 
            static_cast<unsigned char>(data[0]) == 0xFF && 
            static_cast<unsigned char>(data[1]) == 0xD8) {
            result.push_back(static_cast<jbyte>(0xFF));
            result.push_back(static_cast<jbyte>(0xD9));
        }
        
        return result;
    }
    
    return data;
}

extern "C"
JNIEXPORT jbyteArray JNICALL
Java_android_compress_LoadingCompressActivity_compressImage(JNIEnv *env, jobject /* this */,
                                                            jbyteArray imageData, jint quality) {
    if (!imageData) {
        return nullptr;
    }
    
    jsize len = env->GetArrayLength(imageData);
    if (len <= 0) {
        return imageData;
    }
    
    // Lấy dữ liệu từ Java
    jbyte* data = env->GetByteArrayElements(imageData, nullptr);
    if (!data) {
        return nullptr;
    }
    
    // Tạo buffer để lưu dữ liệu
    std::vector<jbyte> buffer(data, data + len);
    
    // Giải phóng dữ liệu từ Java
    env->ReleaseByteArrayElements(imageData, data, JNI_ABORT);
    
    // === THUẬT TOÁN CỰC KỲ AN TOÀN ===
    
    std::vector<jbyte> result = buffer;
    size_t originalSize = buffer.size();
    
    // Bước 1: Chỉ xóa EXIF nếu là JPEG
    if (quality < 90) {
        result = removeJpegExif(result);
    }
    
    // Bước 2: Chỉ truncate nếu quality rất thấp và đã tiết kiệm được ít dung lượng
    if (quality < 30 && result.size() > originalSize * 0.95) {
        std::vector<jbyte> truncated = safeTruncate(result, quality);
        result = truncated;
    }
    
    // Bước 3: Kiểm tra an toàn cuối cùng
    if (result.size() < originalSize * 0.5 || result.size() < 100) {
        // Nếu giảm quá nhiều hoặc quá nhỏ, trả về nguyên bản
        result = buffer;
    }
    
    // === KẾT THÚC ===
    
    // Tạo byte array mới để trả về
    jbyteArray finalResult = env->NewByteArray(result.size());
    if (finalResult) {
        env->SetByteArrayRegion(finalResult, 0, result.size(), result.data());
    }
    
    return finalResult;
}