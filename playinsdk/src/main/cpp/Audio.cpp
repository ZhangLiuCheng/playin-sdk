//
// Created by zhangliucheng on 2019-08-21.
//
//

#include "PILog.h"
#include "Aac.h"
#include <jni.h>

Aac *aac = NULL;

extern "C"
JNIEXPORT jint JNICALL
Java_com_tech_playinsdk_decoder_FFmpegAAC_aacInit(JNIEnv *env, jobject thiz, jint sample_rate,
                                                  jint channel, jint bit) {
    if (NULL == aac) {
        aac = new Aac();
    }
    return aac->init(env, thiz, sample_rate, channel, bit);
}

extern "C"
JNIEXPORT jbyteArray JNICALL
Java_com_tech_playinsdk_decoder_FFmpegAAC_aacDecoding(JNIEnv *env, jobject thiz, jbyteArray aac_buf,
                                                      jint offset, jint length) {
    if (NULL != aac) {
        return aac->decoding(env, thiz, aac_buf, offset, length);
    }
    return NULL;
}

extern "C"
JNIEXPORT void JNICALL
Java_com_tech_playinsdk_decoder_FFmpegAAC_aacClose(JNIEnv *env, jobject thiz) {
    if (NULL != aac) {
        aac->close(env, thiz);
    }
}