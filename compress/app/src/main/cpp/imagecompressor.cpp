// app/src/main/cpp/imagecompressor.cpp

#include <jni.h>

extern "C"
JNIEXPORT jbyteArray JNICALL
Java_android_compress_LoadingCompressActivity_compressImage(JNIEnv *env, jobject /* this */,
                                                            jbyteArray imageData, jint /* quality */) {
    // Return the original image data without any modification
    if (!imageData) {
        return nullptr;
    }
    jsize len = env->GetArrayLength(imageData);
    if (len <= 0) {
        return imageData;
    }
    return imageData;
}