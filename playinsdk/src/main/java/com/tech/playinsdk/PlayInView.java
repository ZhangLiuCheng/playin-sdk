package com.tech.playinsdk;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

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

    private int playDuration;
    private int countDuration;

    private GameView gameView;
    private View playingView;
    private View finishView;

    private boolean isDetachedFromWindow;

    public PlayInView(Context context) {
        super(context);
        initGameView();
    }

    public PlayInView(Context context, AttributeSet attrs) {
        super(context, attrs);
        initGameView();
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        isDetachedFromWindow = true;
        getHandler().removeCallbacksAndMessages(null);
        reportPlayEnd();
    }

    /**
     * playGame
     *
     * @param adid
     * @param playDuration
     * @param listener
     */
    public void play(String adid, int playDuration, final PlayListener listener) {
        this.playDuration = playDuration;
        this.playListener = listener;
        this.requestPlayInfo(adid);
    }

    private void initGameView() {
        LayoutInflater.from(getContext()).inflate(R.layout.playin_view, this);
        gameView = findViewById(R.id.gameview);
        playingView = findViewById(R.id.playingView);
        finishView = findViewById(R.id.finishView);
        finishView.setOnClickListener(this);
    }

    @Override
    public void onGameStart() {
        this.playListener.onPlaystart();
        getHandler().post(new Runnable() {
            @Override
            public void run() {
                startCount();
            }
        });
    }

    @Override
    public void onGameError(Exception ex) {
        this.playListener.onPlayError(ex);
        getHandler().post(new Runnable() {
            @Override
            public void run() {
                showError();
            }
        });
    }

    @Override
    public void onClick(View v) {
        String tag = (String) v.getTag();
        if ("close".equals(tag)) {
            playListener.onPlayClose();
        } else if ("install".equals(tag)) {
            goDownload();
        }

        if (v.getId() == R.id.finishView) {
            finishView.setVisibility(GONE);
        }
    }

    private void requestPlayInfo(String adid) {
        PlayInSdk.getInstance().userActions(adid, new HttpListener<PlayInfo>() {
            @Override
            public void success(PlayInfo result) {
                if (isDetachedFromWindow) return;
                initData(result);
                playInfo = result;
                countDuration = Math.min(playDuration, result.getDuration());
                gameView.startConnect(playInfo, PlayInView.this);
            }

            @Override
            public void failure(final HttpException userActionExc) {
                playListener.onPlayError(userActionExc);
            }
        });
    }

    private void initData(PlayInfo playInfo) {
        final ImageView appIcon = findViewById(R.id.appIcon);
        HttpHelper.obtian().getHttpBitmap("https://timgsa.baidu.com/timg?image&quality=80&size=b9999_10000&sec=1567520583167&di=63d09991b1d38d15bc100be6f6b81b1d&imgtype=0&src=http%3A%2F%2Fpic39.nipic.com%2F20140321%2F18063302_210604412116_2.jpg", new HttpListener<Bitmap>() {
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

    private void goDownload() {
        reportDownload();
        String downloadUrl = playInfo.getGoogleplayUrl();
        if (TextUtils.isEmpty(downloadUrl) || "null".equals(downloadUrl)) {
            Toast.makeText(getContext(), "There is no googlePlay download url", Toast.LENGTH_SHORT).show();
            return;
        }
        try {
            Uri uri = Uri.parse(downloadUrl);
            Intent intent = new Intent(Intent.ACTION_VIEW, uri);
            getContext().startActivity(intent);
        } catch (Exception ex) {
            ex.printStackTrace();
            PlayLog.e("download app error：" + ex);
        }
    }

    private void startCount() {
        final TextView countView = findViewById(R.id.countDownTv);
        countView.setText(countDuration + "");
        getHandler().postDelayed(new Runnable() {
            @Override
            public void run() {
                countView.setText(countDuration + "");
                countDuration--;
                if (countDuration >= 0) {
                    getHandler().postDelayed(this, 1000);
                } else {
                    showPlayFinish();
                    reportPlayEnd();
                }
            }
        }, 1000);
    }

    private void showPlayFinish() {
        playingView.setVisibility(GONE);
        finishView.setVisibility(VISIBLE);
    }

    private void showError() {
        playingView.setVisibility(GONE);
        finishView.setVisibility(VISIBLE);
    }

    private void reportDownload() {
        if (null != playInfo && !TextUtils.isEmpty(playInfo.getToken())) {
            PlayInSdk.getInstance().report(playInfo.getToken(), Constants.Report.DOWN_LOAD);
        }
    }

    private void reportPlayEnd() {
        PlayLog.e("reportPlayEnd");
        if (/*leftTime <= 1 && */null != playInfo && !TextUtils.isEmpty(playInfo.getToken())) {
            PlayInSdk.getInstance().report(playInfo.getToken(), Constants.Report.END_PLAY);
        }
    }

    /*
    // No device, show installation
    private void showInstallWithoutMobile(Bitmap coverBitmap) {
        final Context context = getContext();
        DisplayMetrics dm = getResources().getDisplayMetrics();

        final RelativeLayout rLayout = new RelativeLayout(context);
        rLayout.setBackgroundColor(Color.parseColor("#88000000"));
        this.addView(rLayout);
        ImageView bg = new ImageView(context);
        bg.setImageBitmap(coverBitmap);
        bg.setBackgroundColor(Color.red(255));
        bg.setScaleType(ImageView.ScaleType.CENTER_CROP);
        rLayout.addView(bg, LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
        addCloseView(rLayout);

        // fuzzy background
        RelativeLayout botLayout = new RelativeLayout(context);
//        botLayout.setBackgroundDrawable(new BitmapDrawable(context.getResources(),
//                BitmapUtil.rsBlur(context, coverBitmap, 25)));
        RelativeLayout.LayoutParams botParams = new RelativeLayout.LayoutParams(
                LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        int margin = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 260, dm);
        botParams.setMargins(0, margin, 0 , 0);
        botParams.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
        rLayout.addView(botLayout, botParams);


        final LinearLayout fcLayout = new LinearLayout(context);
        fcLayout.setOrientation(LinearLayout.VERTICAL);
        RelativeLayout.LayoutParams fcParams = new RelativeLayout.LayoutParams(
                LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
        fcParams.addRule(RelativeLayout.CENTER_IN_PARENT);
        fcLayout.setGravity(Gravity.CENTER);
        botLayout.addView(fcLayout, fcParams);

        // app Icon
        final ImageView icon = new ImageView(context);
        icon.setScaleType(ImageView.ScaleType.FIT_XY);
        int size = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 100, dm);
        LinearLayout.LayoutParams iconParams = new LinearLayout.LayoutParams(size, size);
        fcLayout.addView(icon, iconParams);

        // app name
        TextView name = new TextView(getContext());
        name.setTextSize(TypedValue.COMPLEX_UNIT_SP, 20);
        name.setTextColor(Color.parseColor("#FFFFFF"));
        name.setTypeface(name.getTypeface(), Typeface.BOLD_ITALIC);
        name.setGravity(Gravity.CENTER);
//        name.setText(this.appName);
        LinearLayout.LayoutParams nameParams = new LinearLayout.LayoutParams(LayoutParams.WRAP_CONTENT,
                LayoutParams.WRAP_CONTENT);
        int marginTop = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 30, dm);
        nameParams.setMargins(0, marginTop, 0, 0);
        fcLayout.addView(name, nameParams);

        // install button
        addIntallBtn(fcLayout);
//
//        HttpHelper.obtian().getHttpBitmap(this.appIcon, new HttpListener<Bitmap>() {
//            @Override
//            public void success(Bitmap result) {
//                icon.setImageBitmap(result);
//            }
//
//            @Override
//            public void failure(HttpException e) {
//                PlayLog.e("获取APP Icon图片失败:  " + e);
//            }
//        });

    }

    // logo and countdown - playing
    private void addPlayingInfo() {
        Context context = getContext();
        RelativeLayout rLayout = new RelativeLayout(context);
        playingView = rLayout;

        addView(rLayout, LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);

        addLogoView(rLayout);
        countView = addCountdownView(rLayout);

        // landscape
        if (playInfo.getOrientation() == 1) {
            countView.setRotation(90);
        }
    }

    // playend
    private void addPlayFinish() {
        Context context = getContext();
        RelativeLayout rLayout = new RelativeLayout(context);
        rLayout.setVisibility(GONE);        // 默认隐藏
        finishView = rLayout;

        rLayout.setClickable(true);
        rLayout.setBackgroundColor(Color.parseColor("#88000000"));
        addView(rLayout, LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
        // close
        addCloseView(rLayout);

        // download and continue to play the button
        LinearLayout fucLayout = new LinearLayout(context);
        fucLayout.setOrientation(LinearLayout.VERTICAL);
        RelativeLayout.LayoutParams fucParams = new RelativeLayout.LayoutParams(LayoutParams.WRAP_CONTENT,
                LayoutParams.WRAP_CONTENT);
        fucParams.addRule(RelativeLayout.CENTER_IN_PARENT);
        rLayout.addView(fucLayout, fucParams);

        addInstall(fucLayout);
        continueView = addContinue(fucLayout);

        // landscape
        if (playInfo.getOrientation() == 1) {
            fucLayout.setRotation(90);
        }
    }

    // added logo - playing
    private View addLogoView(RelativeLayout rLayout) {
        DisplayMetrics dm = getResources().getDisplayMetrics();
        TextView logo = new TextView(getContext());
        logo.setTextSize(TypedValue.COMPLEX_UNIT_SP, 14);
        logo.setText("PlayIn Ads");
        logo.setTypeface(logo.getTypeface(), Typeface.BOLD_ITALIC);
        logo.setTextColor(Color.parseColor("#FFFFFF"));
        RelativeLayout.LayoutParams logoParams = new RelativeLayout.LayoutParams(LayoutParams.WRAP_CONTENT,
                LayoutParams.WRAP_CONTENT);

        // landscape
        if (playInfo.getOrientation() == 1) {
            logoParams.addRule(RelativeLayout.CENTER_VERTICAL);
            logo.setRotation(90);
        } else {
            logoParams.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
            logoParams.addRule(RelativeLayout.CENTER_HORIZONTAL);
        }
        int marginBottom = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 15, dm);
        logoParams.setMargins(0, 0, 0, marginBottom);
        rLayout.addView(logo, logoParams);

        return logo;
    }

    // added countdown - playing
    private TextView addCountdownView(RelativeLayout rLayout) {
        DisplayMetrics dm = getResources().getDisplayMetrics();
        TextView countView = new TextView(getContext());
        countView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 14);
        countView.setTextColor(Color.parseColor("#FFFFFF"));
        countView.setGravity(Gravity.CENTER);
        countView.setText(leftDuration + "");
        countView.setBackgroundResource(ResHelper.getResDraw(getContext(), "playin_btn_close"));
        int size = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 30, dm);
        RelativeLayout.LayoutParams countParams = new RelativeLayout.LayoutParams(size, size);
        countParams.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
        int margin = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 10, dm);
        countParams.setMargins(0, margin, margin, 0);
        rLayout.addView(countView, countParams);
        return countView;
    }

    // add close button  -- gameover
    private View addCloseView(RelativeLayout rLayout) {
        DisplayMetrics dm = getResources().getDisplayMetrics();
        ImageView close = new ImageView(getContext());
        close.setTag("close");
        close.setOnClickListener(this);
        close.setImageResource(ResHelper.getResDraw(getContext(), "playin_close"));
        close.setBackgroundResource(ResHelper.getResDraw(getContext(), "playin_btn_close"));
        int paddding = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 7, dm);
        close.setPadding(paddding, paddding, paddding, paddding);
        int size = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 30, dm);
        RelativeLayout.LayoutParams countParams = new RelativeLayout.LayoutParams(size, size);
        countParams.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
        int margin = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 10, dm);
        countParams.setMargins(0, margin, margin, 0);
        rLayout.addView(close, countParams);
        return close;
    }

    // install - gameover
    private View addInstall(LinearLayout layout) {
        DisplayMetrics dm = getResources().getDisplayMetrics();
        int height = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 55, dm);
        TextView installBtn = new TextView(getContext());
        installBtn.setTag("install");
        installBtn.setOnClickListener(this);
        installBtn.setTextColor(Color.parseColor("#FFFFFF"));
        installBtn.setTextSize(TypedValue.COMPLEX_UNIT_SP, 18);
        installBtn.setGravity(Gravity.CENTER);
        installBtn.setBackgroundResource(ResHelper.getResDraw(getContext(), "playin_btn_install"));
        installBtn.setText(playInfo.getInstallText());
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams((int)(dm.widthPixels / 1.8), height);
        layout.addView(installBtn, params);
        return installBtn;
    }

    // Go on - gameover
    private View addContinue(LinearLayout layout) {
        DisplayMetrics dm = getResources().getDisplayMetrics();
        int height = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 55, dm);
        TextView continueBtn = new TextView(getContext());
        continueBtn.setTag("continue");
        continueBtn.setOnClickListener(this);
        continueBtn.setTextColor(Color.parseColor("#FFFFFF"));
        continueBtn.setTextSize(TypedValue.COMPLEX_UNIT_SP, 18);
        continueBtn.setGravity(Gravity.CENTER);
        continueBtn.setBackgroundResource(ResHelper.getResDraw(getContext(), "playin_btn_install"));
        continueBtn.setText(playInfo.getContinueText());
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams((int)(dm.widthPixels / 1.8), height);
        int marginTop = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 50, dm);
        params.setMargins(0, marginTop, 0, 0);
        layout.addView(continueBtn, params);
        return continueBtn;
    }


    // Installation - no equipment available
    private View addIntallBtn(LinearLayout layout) {
        DisplayMetrics dm = getResources().getDisplayMetrics();
        int height = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 55, dm);
        TextView continueBtn = new TextView(getContext());
        continueBtn.setTag("install");
        continueBtn.setOnClickListener(this);
        continueBtn.setTextColor(Color.parseColor("#FFFFFF"));
        continueBtn.setTextSize(TypedValue.COMPLEX_UNIT_SP, 18);
        continueBtn.setGravity(Gravity.CENTER);
        continueBtn.setBackgroundResource(ResHelper.getResDraw(getContext(), "playin_btn_install"));
        continueBtn.setText("Install");
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(height * 3, height);
        int marginTop = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 30, dm);
        params.setMargins(0, marginTop, 0, 0);
        layout.addView(continueBtn, params);
        return continueBtn;
    }
    */
}
