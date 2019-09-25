package com.tech.playinsdk.util;

import android.content.Context;
import android.graphics.PixelFormat;
import android.media.AudioFormat;
import android.os.Handler;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import com.tech.playinsdk.connect.PlaySocket;
import com.tech.playinsdk.decoder.AudioDecoder;
import com.tech.playinsdk.decoder.FFmpegDecoder;
import com.tech.playinsdk.decoder.VideoDecoder;
import com.tech.playinsdk.http.HttpException;
import com.tech.playinsdk.model.entity.PlayInfo;

import org.json.JSONException;
import org.json.JSONObject;


public class GameView extends SurfaceView implements SurfaceHolder.Callback, VideoDecoder.DecoderListener {

    public interface GameListener {
        void onGameStart();
        void onGameError(Exception ex);
    }

    private final Handler handler = new Handler();
    private final StringBuilder controlBuilder = new StringBuilder();

    private VideoDecoder videodecoder;
    private AudioDecoder audioDecoder;

    private GameListener playListener;
    private PlayInfo playInfo;
    private PlaySocket playSocket;
    private int visibility;

    public GameView(Context context) {
        super(context);
        init();
    }

    public GameView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    private void init() {
        SurfaceHolder surfaceHolder = getHolder();
        surfaceHolder.setFormat(PixelFormat.TRANSLUCENT);
        surfaceHolder.addCallback(this);
    }

    public void startConnect(PlayInfo playInfo, GameListener listener) {
        this.playInfo = playInfo;
        this.playListener = listener;
        playSocket = new MyPlaySocket(playInfo.getServerIp(), playInfo.getServerPort());
        playSocket.connect();

        audioDecoder = new AudioDecoder();
        audioDecoder.start();

        int width = playInfo.getDeviceWidth();
        int height = playInfo.getDeviceHeight();

        if (playInfo.getOsType() == 2 && playInfo.getOrientation() == 1) {
            // android 设备端 并且是 横屏
            width = playInfo.getDeviceHeight();
            height = playInfo.getDeviceWidth();
        }

        videodecoder = new FFmpegDecoder(width, height);
//        videodecoder = new MediaDecoder(playInfo.getDeviceWidth(), playInfo.getDeviceHeight());
        videodecoder.setDecoderListener(this);
        videodecoder.start();
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        this.playListener = null;
        if (null != playSocket) {
            playSocket.disConnect();
        }
        if (null != audioDecoder) {
            audioDecoder.stop();
        }
        if (null != videodecoder) {
            videodecoder.stop();
        }
        handler.removeCallbacksAndMessages(null);
    }

    @Override
    protected void onWindowVisibilityChanged(int visibility) {
        super.onWindowVisibilityChanged(visibility);
        this.visibility = visibility;
        if (null != videodecoder) {
            if (visibility == 0) {
                videodecoder.resume();
            } else {
                videodecoder.pause();
            }
        }
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        if (null != videodecoder) {
            videodecoder.setDisplayHolder(holder);
        }
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        float rateWidth = event.getX() / getWidth();
        float rateHeight = event.getY() / getHeight();

        int action = event.getAction();  // 0 down, 1 up, 2 move
        // 目标触摸 0-down,1-move,2-up
        if (action == 1) {
            action = 2;
        } else if (action == 2) {
            action = 1;
        }
        controlBuilder.delete(0, controlBuilder.length());
        controlBuilder.append(rateWidth).append("_")
                .append(rateHeight).append("_")
                .append(action)
                .append("_0_0");
        sendControl(event.getPointerCount(), controlBuilder.toString());
        return true;
    }

    private void sendControl(int finger, String control) {
        if (null != playSocket && playSocket.isConnected()) {
            try {
                JSONObject obj = new JSONObject();
                obj.put("" + finger, control);
                playSocket.sendControl(obj.toString());
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }


    private void sendUserContect() {
        try {
            JSONObject obj = new JSONObject();
            obj.put("token", playInfo.getToken());
            obj.put("device_name", android.os.Build.BRAND);
            obj.put("os_type", Constants.OS_TYPE);
            obj.put("coder", "annex-b");  // 安卓annex-b, 苹果avcc
            playSocket.sendText(obj.toString());
        } catch (Exception ex) {
            PlayLog.e("sendUserContect  exception :" + ex);
        }
    }

    private void sendMessageToAndroid() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    Thread.sleep(2000);
                    playSocket.sendStream(Constants.PacketType.STREAM, Constants.StreamType.ANDROID_VIDEO_START, "");

                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

    private class MyPlaySocket extends PlaySocket {

        public MyPlaySocket(String ip, int port) {
            super(ip, port);
        }

        @Override
        public void onOpen() {
            PlayLog.v("MyPlaySocket --> onMessage  onOpen " + playInfo.getOsType());
            sendUserContect();
//            if (playInfo.getOsType() == 2) {
                sendMessageToAndroid();
//            }
        }

        @Override
        public void onMessage(String msg) {
            PlayLog.v("MyPlaySocket --> onMessage  msg " + msg);
            try {
                JSONObject object = new JSONObject(msg);
                if (0 != object.optInt("code")) {
                    invokeGameError(new HttpException(-1, object.optString("error")));
                }

                // ios 默认音频参数
                int sampleRateInHz = 22050;
                int channelConfig = AudioFormat.CHANNEL_CONFIGURATION_MONO;
                int audioFormat = AudioFormat.ENCODING_PCM_16BIT;
                if (playInfo.getOsType() == 2) {
                    // android 默认音频参数
                    sampleRateInHz = 24000;
                    channelConfig = 3;
                    audioFormat = 2;
                }
                JSONObject streamInfo = object.optJSONObject("stream_info");
                if (null != streamInfo) {
                    sampleRateInHz = streamInfo.optInt("sample_rate");
                    channelConfig = streamInfo.optInt("channels");
                    int bps = streamInfo.optInt("bits_per_sample");
                    if (bps == 16) audioFormat = AudioFormat.ENCODING_PCM_16BIT;
                    else if (bps == 8) audioFormat = AudioFormat.ENCODING_PCM_8BIT;
                }
                audioDecoder.initAudioTrack(sampleRateInHz, channelConfig, audioFormat);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        @Override
        public void onMessage(int streamType, byte[] buf) {
//            PlayLog.e("onMessage  " + streamType + " ====  " + buf.length);
            if (GameView.this.visibility == 0) {
                if (streamType == Constants.StreamType.H264) {
                    if (null != videodecoder) videodecoder.sendVideoData(buf);
                } else if (streamType == Constants.StreamType.PCM) {
                    if (null != audioDecoder) audioDecoder.sendAudioData(buf);
                }
            }
        }

        @Override
        public void onError(Exception ex) {
            PlayLog.v("MyPlaySocket --> onError  " + ex);
            invokeGameError(ex);
        }
    }

    @Override
    public void decoderSuccess() {
        invokeGameStart();
    }

    private void invokeGameStart() {
        handler.post(new Runnable() {
            @Override
            public void run() {
                if (null != playListener) {
                    playListener.onGameStart();
                }
            }
        });
    }

    private void invokeGameError(final Exception ex) {
        handler.post(new Runnable() {
            @Override
            public void run() {
                if (null != playListener) {
                    playListener.onGameError(ex);
                }
            }
        });
    }
}
