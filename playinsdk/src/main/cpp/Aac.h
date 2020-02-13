//
// Created by zhangliucheng on 2019-09-09.
//

#ifndef PLAYINDECODER_AAC_H
#define PLAYINDECODER_AAC_H

extern "C" {
#include "ffmpeg/include/libavcodec/avcodec.h"
#include "ffmpeg/include/libavformat/avformat.h"
#include "ffmpeg/include/libavutil/avutil.h"
#include "ffmpeg/include/libavutil/frame.h"
#include "ffmpeg/include/libavutil/imgutils.h"
#include "ffmpeg/include/libavutil/opt.h"
#include "ffmpeg/include/libswresample/swresample.h"
}

#include <jni.h>


class Aac {

public:
    Aac();

    ~Aac();

    int init(JNIEnv *, jobject thiz, jint, jint, jint);
    jbyteArray decoding(JNIEnv *, jobject, jbyteArray, jint, jint);
    void close(JNIEnv *, jobject);

private:
    AVCodecContext *aacCodecCtx = NULL;
    AVCodec *aacCodec;
    AVFrame *aacFrame;
    AVPacket aacPacket;
    SwrContext *au_convert_ctx;

};


#endif //PLAYINDECODER_AAC_H
