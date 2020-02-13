package com.tech.playinsdk.decoder;

public class FFmpegAAC {

    static {
        System.loadLibrary("playin");
    }

    public native int aacInit(int sampleRate, int channel, int bit);
    public native byte[] aacDecoding(byte[] aacBuf, int offset, int length);
    public native void aacClose();
}
