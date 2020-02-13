package com.tech.playinsdk.decoder;

import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioRecord;
import android.media.AudioTrack;

import com.tech.playinsdk.util.PlayLog;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

public class AudioDecoder implements Runnable {

    private BlockingQueue<byte[]> audioQueue = new LinkedBlockingQueue<>(10);

    private AudioTrack audioTrack;
    private Thread thread;
    private boolean loopFlag;
    private boolean initCodec;

    private FFmpegAAC fFmpegAAC;
    private MediaAac mediaAac;

    public void sendAudioData(byte[] buf) {
        if (initCodec && null != audioQueue) {
            audioQueue.offer(buf);
        }
    }

    public void initAudioTrack(int sampleRateInHz, int channelConfig, int audioFormat) {
        // TODO 测试数据
        sampleRateInHz = 44100;
        channelConfig = AudioFormat.CHANNEL_CONFIGURATION_STEREO;
        audioFormat = AudioFormat.ENCODING_PCM_16BIT;

        try {

            int bufferSize = AudioRecord.getMinBufferSize(sampleRateInHz, channelConfig, audioFormat);
            this.audioTrack = new AudioTrack(AudioManager.STREAM_MUSIC, sampleRateInHz,
                    channelConfig, audioFormat, bufferSize, AudioTrack.MODE_STREAM);
            this.audioTrack.play();


//            mediaAac = new MediaAac(new MediaAac.DecoderListener() {
//                @Override
//                public void pcmData(byte[] buf, int offset, int length) {
//                    if (null != buf) {
//                        AudioDecoder.this.audioTrack.write(buf, 0, buf.length);
//                    }
//                }
//            });

            fFmpegAAC = new FFmpegAAC();
            fFmpegAAC.aacInit(sampleRateInHz, 2, 6400);


            initCodec = true;
        } catch (Exception ex) {
            ex.printStackTrace();
            initCodec = false;
        }
    }

    public synchronized void start() {
        loopFlag = true;
        audioQueue.clear();
        thread = new Thread(this);
        thread.start();
    }

    public synchronized void stop() {
        audioQueue.clear();
        loopFlag = false;
        initCodec = false;
        if (this.audioTrack != null) {
            audioTrack.stop();
            audioTrack.release();
            audioTrack = null;
        }
        if (null != thread) {
            thread.interrupt();
        }
        if (null != fFmpegAAC) {
            fFmpegAAC.aacClose();
        }
    }

    @Override
    public void run() {
        while (loopFlag) {
            try {
                byte[] buf = audioQueue.take();
                if (initCodec) {

                    // PCM
//                    this.audioTrack.write(buf, 0, buf.length);

                    // MediaCodec
//                    if (null != mediaAac) {
//                        mediaAac.decodeAAc(buf, 0, buf.length);
//                    }

                    // ffmpeg
                    byte[] pcmBuf = fFmpegAAC.aacDecoding(buf, 0, buf.length);
                    if (null != pcmBuf) {
                        this.audioTrack.write(pcmBuf, 0, pcmBuf.length);
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

}
