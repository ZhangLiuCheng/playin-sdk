package com.tech.playinsdk;

import android.app.Activity;
import android.content.Context;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.AdapterView;
import android.widget.CompoundButton;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.ToggleButton;

import com.tech.playinsdk.http.HttpException;
import com.tech.playinsdk.http.HttpHelper;
import com.tech.playinsdk.listener.HttpListener;
import com.tech.playinsdk.listener.PlayListener;
import com.tech.playinsdk.model.entity.PlayInfo;
import com.tech.playinsdk.util.Constants;
import com.tech.playinsdk.util.GameView;
import com.tech.playinsdk.util.PlayLog;

public class PlayInView extends FrameLayout implements View.OnClickListener, GameView.GameListener {

    private PlayInfo playInfo;
    private PlayListener playListener;

    private View infoDialog;
    private TextView videoTimeTv;
    private TextView totalTimeTv;

    private int videoTime;
    private int totalTime;
    private boolean autoRotate, audioOpen, showView;

    private boolean isDetached, isPause, isFinish, isDownload;

    public PlayInView(Context context) {
        super(context);
    }

    public PlayInView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        isDetached = true;
        getHandler().removeCallbacksAndMessages(null);
        reportPlayEnd();
    }

    @Override
    protected void onWindowVisibilityChanged(int visibility) {
        super.onWindowVisibilityChanged(visibility);
        if (visibility == 0) {
            if (isPause) {
                getHandler().postDelayed(videoTimeRunnable, 1000);  // 返回前台
            }
            isPause = false;
        } else if (visibility == 4) {
            if (!isPause) {
                getHandler().removeCallbacks(videoTimeRunnable);               // 进入后台
            }
            isPause = true;
        }
    }

    @Override
    protected void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        getHandler().postDelayed(new Runnable() {
            @Override
            public void run() {
                adapterGameSize();
            }
        }, 200);
    }

    /**
     * playGame
     * @param adid
     * @param playDuration
     * @param listener
     */
    public void play(String adid, int playDuration, PlayListener listener) {
        this.play(adid, playDuration, true, true, false, listener);
    }

    public void play(String adid, int playDuration, boolean audioOpen, boolean autoRotate,
                     boolean showView, PlayListener listener) {
        this.videoTime = playDuration;
        this.audioOpen = audioOpen;
        this.autoRotate = autoRotate;
        this.showView = showView;
        this.playListener = listener;
        this.requestPlayInfo(adid);
    }

    public void setAudioOpen(boolean audioOpen) {
        this.audioOpen = audioOpen;
        initVoiceView();
    }

    public void setAutoRotate(boolean rotate) {
        this.autoRotate = rotate;
        setScreenOrientation();
    }

    public void setShowView(boolean show) {
        this.showView = show;
        View infoView = findViewById(R.id.infoView);
        if (null != infoView) infoView.setVisibility(this.showView ? VISIBLE : GONE);
    }

    @Override
    public void onGameStart() {
        playListener.onPlaystart();
        countVideoTime();
        countTotalTime();
    }

    @Override
    public void onGameError(final Exception ex) {
        if (!isFinish && !isDownload) {
            playListener.onPlayError(ex);
        }
        try {
            getHandler().removeCallbacksAndMessages(null);
            reportPlayEnd();
            showPlayFinish();
            totalTimeTv.setVisibility(GONE);
            videoTimeTv.setText("Skip Ads");
            videoTimeTv.setOnClickListener(PlayInView.this);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onClick(View v) {
        int cId = v.getId();
        if (cId == R.id.videoTimeTv) {
            playListener.onPlayClose();
        } else if (cId == R.id.downloadTv) {
            goDownload();
        } else if (cId == R.id.infoDialog) {
            hidMenuInfo();
        } else if (cId == R.id.menuLayout) {
            showMenuInfo();
        }
    }

    private void requestPlayInfo(String adid) {
        PlayInSdk.getInstance().userActions(adid, new HttpListener<PlayInfo>() {
            @Override
            public void success(PlayInfo result) {
                if (isDetached) return;

                // TODO 测试数据
//                result.setOsType(2);
//                result.setOrientation(1);

                playInfo = result;
                LayoutInflater.from(getContext()).inflate(R.layout.playin_view, PlayInView.this);
                initView(result);
                initData(result);
                connectPlayIn(result);
            }

            @Override
            public void failure(final HttpException userActionExc) {
                playListener.onPlayError(userActionExc);
            }
        });
    }

    private void initView(PlayInfo playInfo) {
        findViewById(R.id.infoView).setVisibility(this.showView ? VISIBLE : GONE);

        infoDialog = findViewById(R.id.infoDialog);
        infoDialog.setOnClickListener(this);
        videoTimeTv = findViewById(R.id.videoTimeTv);
        totalTimeTv = findViewById(R.id.totlalTimeTv);

        findViewById(R.id.downloadTv).setOnClickListener(this);
        findViewById(R.id.menuLayout).setOnClickListener(this);

        Animation anim = AnimationUtils.loadAnimation(getContext(), R.anim.playin_menu);
        ImageView menuIv = findViewById(R.id.menuIv);
        menuIv.startAnimation(anim);

        initVoiceView();
        initqualityView();
    }

    // 加载声音控制
    private void initVoiceView() {
        if (null == playInfo) return;
        final GameView gameView = findViewById(R.id.gameview);
        gameView.setAudioState(audioOpen);

        ToggleButton voiceTb = findViewById(R.id.audioTb);
        voiceTb.setVisibility(View.VISIBLE);
        voiceTb.setChecked(audioOpen);
        voiceTb.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                PlayInView.this.audioOpen = isChecked;
                gameView.setAudioState(isChecked);
            }
        });
    }

    private void initqualityView() {
        final GameView gameView = findViewById(R.id.gameview);
        Spinner spinner = findViewById(R.id.qualitySp);
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                gameView.sendVideoQuality(position + 1);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });
    }

    private void initData(PlayInfo playInfo) {
        TextView appNameTv = findViewById(R.id.appName);
        TextView appAudienceTv = findViewById(R.id.appAudience);
        TextView appRateTv = findViewById(R.id.appRate);
        TextView commentTv = findViewById(R.id.commentTv);
        TextView adInfoTv = findViewById(R.id.adInfoTv);
        appNameTv.setText(playInfo.getAppName());
        appAudienceTv.setText(String.valueOf(playInfo.getAudience()));
        appRateTv.setText(String.valueOf(playInfo.getAppRate()));
        commentTv.setText(String.valueOf(playInfo.getCommentsCount()));
        adInfoTv.setText(playInfo.getCopywriting());

        final ImageView appIcon = findViewById(R.id.appIcon);
        HttpHelper.obtian().getHttpBitmap(playInfo.getAppIcon(), new HttpListener<Bitmap>() {
            @Override
            public void success(Bitmap result) {
                appIcon.setImageBitmap(result);
            }

            @Override
            public void failure(HttpException e) {
                PlayLog.e("获取APP Icon图片失败:  " + e);
            }
        });
    }

    private void connectPlayIn(PlayInfo playInfo) {
        videoTime = Math.min(videoTime, playInfo.getDuration());
        totalTime = playInfo.getDuration();
        GameView gameView = findViewById(R.id.gameview);
        gameView.startConnect(playInfo, PlayInView.this);

        setScreenOrientation();
        adapterGameSize();
    }

    private void setScreenOrientation() {
        if (playInfo == null || !autoRotate) return;
        try {
            Activity curActivity = (Activity) getContext();
            if (playInfo.getOrientation() == 0) {
                // 竖屏
                curActivity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
            } else {
                // 横屏
                curActivity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private void adapterGameSize() {
        if (playInfo == null || isFinish) return;
        GameView gameView = findViewById(R.id.gameview);
        if (getWidth() != 0 && getHeight() != 0) {
            int srcWidth = playInfo.getDeviceWidth();
            int srcHeight = playInfo.getDeviceHeight();
            // 横屏
            if (playInfo.getOrientation() == 1) {
                srcWidth = playInfo.getDeviceHeight();
                srcHeight = playInfo.getDeviceWidth();
            }
            int destWidth;
            int destHeight;
            float scaleWidth = getWidth() * 1.0f / srcWidth;
            float scaleHeight = getHeight() * 1.0f / srcHeight;
            if (scaleWidth < scaleHeight) {
                destWidth = getWidth();
                destHeight = (int) (getWidth() * 1.0f * srcHeight / srcWidth);
            } else {
                destWidth = (int) (getHeight() * 1.0f * srcWidth / srcHeight);
                destHeight = getHeight();
            }
            RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) gameView.getLayoutParams();
            params.width = destWidth;
            params.height = destHeight;
            gameView.setLayoutParams(params);
        }
    }

    private void goDownload() {
        reportDownload();
        String downloadUrl = playInfo.getGoogleplayUrl();
        playListener.onPlayInstall(downloadUrl);
    }

    // 激励视频倒计时
    private void countVideoTime() {
        if (videoTime <= 0) {
            videoTimeTv.setText("Skip Ads");
            videoTimeTv.setOnClickListener(this);
            return;
        }
        videoTime--;
        videoTimeTv.setText("Skip Ads ( " + videoTime + " )");
        getHandler().postDelayed(videoTimeRunnable, 1000);
    }

    private Runnable videoTimeRunnable = new Runnable() {
        @Override
        public void run() {
            if (null == videoTimeTv) return;
            videoTime--;
            videoTimeTv.setText("Skip Ads ( " + videoTime + " )");
            if (videoTime > 0) {
                getHandler().postDelayed(this, 1000);
            } else {
                videoTimeTv.setText("Skip Ads");
                videoTimeTv.setOnClickListener(PlayInView.this);
                findViewById(R.id.menuLayout).setVisibility(VISIBLE);
                playListener.onPlayForceTime();
            }
        }
    };

    // 总试玩时长倒计时
    private void countTotalTime() {
        totalTime--;
        totalTimeTv.setText(totalTime + "s | ");
        getHandler().postDelayed(new Runnable() {
            @Override
            public void run() {
                totalTime--;
                totalTimeTv.setText(totalTime + "s | ");
                if (totalTime > 0) {
                    getHandler().postDelayed(this, 1000);
                } else {
                    totalTimeTv.setVisibility(GONE);
                    playListener.onPlayFinish();
                    reportPlayEnd();
                    showPlayFinish();
                }
            }
        }, 1000);
    }

    private void showPlayFinish() {
        findViewById(R.id.menuLayout).setVisibility(GONE);          // 隐藏menu
        infoDialog.setVisibility(VISIBLE);                          // 显示下载弹窗
        // 时间到不让关闭弹窗
        if (isFinish) {
            infoDialog.setOnClickListener(null);
            findViewById(R.id.continueTv).setVisibility(INVISIBLE);
        }
    }

    private void showMenuInfo() {
        if (findViewById(R.id.menuLayout).getVisibility() == GONE) return;
        findViewById(R.id.menuLayout).setVisibility(GONE);          // 隐藏menu
        Animation anim = AnimationUtils.loadAnimation(getContext(), R.anim.playin_portrait_in);
        infoDialog.setVisibility(VISIBLE);                          // 显示下载弹窗
        infoDialog.startAnimation(anim);
    }

    private void hidMenuInfo() {
        if (infoDialog.getVisibility() == GONE) return;
        infoDialog.setVisibility(GONE);
        Animation anim = AnimationUtils.loadAnimation(getContext(), R.anim.playin_portrait_out);
        anim.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {}

            @Override
            public void onAnimationEnd(Animation animation) {
                findViewById(R.id.menuLayout).setVisibility(VISIBLE);
            }
            @Override
            public void onAnimationRepeat(Animation animation) {}
        });
        infoDialog.startAnimation(anim);
    }


    private void reportDownload() {
        isDownload = true;
        if (null != playInfo && !TextUtils.isEmpty(playInfo.getToken())) {
            PlayInSdk.getInstance().report(playInfo.getToken(), Constants.Report.DOWN_LOAD);
        }
    }

    private void reportPlayEnd() {
        isFinish = true;
        if (null != playInfo && !TextUtils.isEmpty(playInfo.getToken())) {
            PlayInSdk.getInstance().report(playInfo.getToken(), Constants.Report.END_PLAY);
        }
    }
}
