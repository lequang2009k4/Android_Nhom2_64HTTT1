#include <jni.h>
#include <vector>

extern "C"
JNIEXPORT jbyteArray JNICALL
Java_android_compress_LoadingCompressActivity_compressImage(JNIEnv *env, jobject /* this */,
                                                   jbyteArray imageData, jint /* quality */) {
    jsize len = env->GetArrayLength(imageData);
    std::vector<uint8_t> input(len);
    env->GetByteArrayRegion(imageData, 0, len, reinterpret_cast<jbyte *>(input.data()));

    jbyteArray result = env->NewByteArray(len);
    env->SetByteArrayRegion(result, 0, len, reinterpret_cast<const jbyte *>(input.data()));
    return result;
}