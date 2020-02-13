//
// Created by zhangliucheng on 2019-08-21.
//
//
extern "C"{
#include "ffmpeg/include/libavcodec/jni.h"
}
#include "H264.h"
#include "PILog.h"

void saveH264(JNIEnv *env, jobject instance, H264 *h264) {
    jclass clazz = env->GetObjectClass(instance);
    jfieldID field = env->GetFieldID(clazz, "ffmpegHandle", "J");
    jlong handle = reinterpret_cast<jlong>(h264);
    env->SetLongField(instance, field, handle);
}

H264* getH264(JNIEnv *env, jobject instance) {
    jclass clazz = env->GetObjectClass(instance);
    jfieldID field = env->GetFieldID(clazz, "ffmpegHandle", "J");
    jlong handle = env->GetLongField(instance, field);
    H264 *h264 = reinterpret_cast<H264 *>(handle);
    return h264;
}

extern "C"
JNIEXPORT jint JNI_OnLoad(JavaVM *vm, void *res) {
    //java虚拟机环境传递给ffmpeg
//    av_jni_set_java_vm(vm, 0);
    return JNI_VERSION_1_4;
}

extern "C"
JNIEXPORT jint JNICALL
Java_com_tech_playinsdk_decoder_FFmpegH264_videoInit(JNIEnv *env, jobject instance, jint width,
                                                         jint height, jint rotate, jobject surface) {
    H264 *h264 = getH264(env, instance);
    if (NULL == h264) {
        h264 = new H264();
        saveH264(env, instance, h264);
        return h264->init(env, instance, width, height, rotate, surface);
    } else {
        h264->updateSurface(env, surface);
        return 1;
    }
}

extern "C"
JNIEXPORT jint JNICALL
Java_com_tech_playinsdk_decoder_FFmpegH264_videoDecoding(JNIEnv *env, jobject instance, jbyteArray data) {
    H264 *h264 = getH264(env, instance);
    if (NULL != h264) {
        return h264->decoding(env, instance, data);
    }
    return -1;
}

extern "C"
JNIEXPORT void JNICALL
Java_com_tech_playinsdk_decoder_FFmpegH264_videoClose(JNIEnv *env, jobject instance) {
    H264 *h264 = getH264(env, instance);
    if (NULL != h264) {
        h264->close();
        delete h264;
    }
}

extern "C"
JNIEXPORT void JNICALL
Java_com_tech_playinsdk_decoder_FFmpegH264_videoUpdateRotate(JNIEnv *env, jobject instance,
                                                                 jint rotate) {
    H264 *h264 = getH264(env, instance);
    if (NULL != h264) {
        h264->updateRotate(env, rotate);
    }
}