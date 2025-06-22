// app/src/main/cpp/imagecompressor.cpp

#include <jni.h>
#include <vector>
#include <cstring>

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
    
    // TODO: Thêm logic nén ảnh thực sự ở đây
    // Hiện tại chỉ trả về dữ liệu gốc, nhưng bạn có thể thêm:
    // - JPEG compression với libjpeg
    // - PNG compression với libpng
    // - WebP compression với libwebp
    
    // Tạo byte array mới để trả về
    jbyteArray result = env->NewByteArray(len);
    if (result) {
        env->SetByteArrayRegion(result, 0, len, buffer.data());
    }
    
    return result;
}