package com.tech.playinsdk.decoder;

import android.media.AudioFormat;
import android.media.MediaCodec;
import android.media.MediaCodecInfo;
import android.media.MediaFormat;

import java.nio.ByteBuffer;

/**
 * aac解码pcm.
 */
public class MediaAac {

    public static final String MIME_TYPE = "audio/mp4a-latm";
    public static final int CHANNEL_CONFIG = AudioFormat.CHANNEL_CONFIGURATION_STEREO;
    public static final int AUDIO_FORMAT = AudioFormat.ENCODING_PCM_16BIT;
    public static final int SAMPLE_RATE = 44100;
    public static final int BIT_RATE = 64000;
    public static final int MAX_INPUT_SIZE = 655360;
    public static final int CHANNEL_COUNT = 2;

    public interface DecoderListener {
        void pcmData(byte[] buf, int offset, int length);
    }


    private DecoderListener listener;
    private MediaCodec mAudioDecoder;

    public MediaAac(DecoderListener listener) {
        this.listener = listener;
        init();
    }

    private void init() {
        try {
            mAudioDecoder = MediaCodec.createDecoderByType(MIME_TYPE);
            MediaFormat format = new MediaFormat();
            format.setString(MediaFormat.KEY_MIME, MIME_TYPE);
            format.setInteger(MediaFormat.KEY_CHANNEL_COUNT, CHANNEL_COUNT);
            format.setInteger(MediaFormat.KEY_SAMPLE_RATE, SAMPLE_RATE);
            format.setInteger(MediaFormat.KEY_BIT_RATE, BIT_RATE);
            format.setInteger(MediaFormat.KEY_MAX_INPUT_SIZE, MAX_INPUT_SIZE);
            format.setInteger(MediaFormat.KEY_IS_ADTS, 0);
            format.setInteger(MediaFormat.KEY_AAC_PROFILE, MediaCodecInfo.CodecProfileLevel.AACObjectLC);

            //ByteBuffer key（暂时不了解该参数的含义，但必须设置）
//            byte[] data = new byte[]{(byte) 0x11, (byte) 0x90};
//            byte[] data = new byte[]{(byte) 0x12, (byte) 0x8};
//            ByteBuffer csd_0 = ByteBuffer.wrap(data);
//            format.setByteBuffer("csd-0", csd_0);

            mAudioDecoder.configure(format, null, null, 0);
            mAudioDecoder.start();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void decodeAAc(byte[] buf, int offset, int length) {
        ByteBuffer[] codecInputBuffers = mAudioDecoder.getInputBuffers();
        ByteBuffer[] codecOutputBuffers = mAudioDecoder.getOutputBuffers();
        long kTimeOutUs = 0;
        try {
            int inputBufIndex = mAudioDecoder.dequeueInputBuffer(kTimeOutUs);
            if (inputBufIndex >= 0) {
                ByteBuffer dstBuf = codecInputBuffers[inputBufIndex];
                dstBuf.clear();
                dstBuf.put(buf, offset, length);
                mAudioDecoder.queueInputBuffer(inputBufIndex, 0, length, 0, 0);
            }
            MediaCodec.BufferInfo info = new MediaCodec.BufferInfo();
            int outputBufferIndex = mAudioDecoder.dequeueOutputBuffer(info, kTimeOutUs);
            ByteBuffer outputBuffer;
            while (outputBufferIndex >= 0) {
                outputBuffer = codecOutputBuffers[outputBufferIndex];
                byte[] outData = new byte[info.size];
                outputBuffer.get(outData);
                outputBuffer.clear();
                this.listener.pcmData(outData, 0, info.size);
                mAudioDecoder.releaseOutputBuffer(outputBufferIndex, false);
                outputBufferIndex = mAudioDecoder.dequeueOutputBuffer(info, kTimeOutUs);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
