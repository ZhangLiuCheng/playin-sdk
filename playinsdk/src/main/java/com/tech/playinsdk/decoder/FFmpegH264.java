package com.tech.playinsdk.decoder;

import android.view.Surface;

import com.tech.playinsdk.util.Analyze;

public class FFmpegH264 extends VideoDecoder {

    static {
        System.loadLibrary("playin");
    }

    public native int videoInit(int width, int height, int rotate, Surface surface);
    public native int videoDecoding(byte[] data);
    public native void videoClose();
    public native void videoUpdateRotate(int rotate);

    public long ffmpegHandle;            // Don't delete, native holds the pointer to the object

    private boolean init;

    public FFmpegH264(int videoWidth, int videoHeight, int videoRotate) {
        super(videoWidth, videoHeight, videoRotate);
    }

    @Override
    protected boolean initDecoder(Surface surface) {
        boolean result = videoInit(videoWidth, videoHeight, videoRotate, surface) >= 0;
        init = result;
        return result;
    }

    protected void onFrame(byte[] buf, int offset, int length) {
        int value = buf[4] & 0x0f;
        if (!tryCodecSuccess(value)) {
            return;
        }
        if (init) {
            long start = System.currentTimeMillis();
            videoDecoding(buf);
            long end = System.currentTimeMillis();
            int duration = (int) (end - start);
            Analyze.getInstance().videoDecoder(duration);
        }
    }

    @Override
    protected void releaseDecoder() {
        if (init) {
            videoClose();
        }
        init = false;
    }

    @Override
    public void updateRotate(int videoRotate) {
        this.videoRotate = videoRotate;
        if (init) {
            videoUpdateRotate(videoRotate);
        }
    }
}
