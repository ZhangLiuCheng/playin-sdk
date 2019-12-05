package com.tech.playinsdk.demo;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.View;
import android.widget.Toast;

import com.tech.playinsdk.PlayInView;
import com.tech.playinsdk.listener.PlayListener;
import com.tech.playinsdk.util.PlayLog;

public class PlayActivity extends AppCompatActivity implements PlayListener {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_play);
        getSupportActionBar().hide();
        playGame();
    }

    private void playGame() {
        PlayInView playView = findViewById(R.id.playView);
        playView.play(Constants.ADID, 10,this);
        playView.setShowView(true);
//        playView.setAudioOpen(true);
//        playView.setAutoRotate(false);
    }

    @Override
    public void onPlaystart() {
        hideLoading();
    }

    @Override
    public void onPlayFinish() {
        showDialog("Play finish");
    }

    @Override
    public void onPlayClose() {
        finish();
    }

    @Override
    public void onPlayError(Exception ex) {
        hideLoading();
        showDialog(ex.getMessage());
    }

    @Override
    public void onPlayTime(int count) {

    }

    @Override
    public void onPlayInstall(String url) {
        downloadApp(url);
    }

    @Override
    public void onPlayForceTime() {
        PlayLog.e("强制试玩时间结束");
    }

    private void hideLoading() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                findViewById(R.id.loading).setVisibility(View.GONE);
            }
        });
    }

    private void showDialog(String message) {
        if (isFinishing()) return;
        AlertDialog dialog = new AlertDialog.Builder(this)
                .setMessage(message + ", click confirm to return")
                .setPositiveButton("confirm", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        finish();
                    }
                }).create();
        dialog.setCancelable(false);
        dialog.show();
    }

    private void downloadApp(String url) {
        if (TextUtils.isEmpty(url) || "null".equals(url)) {
            Toast.makeText(this, "There is no googlePlay download url", Toast.LENGTH_SHORT).show();
            return;
        }
        try {
            Uri uri = Uri.parse(url);
            Intent intent = new Intent(Intent.ACTION_VIEW, uri);
            startActivity(intent);
            finish();
        } catch (Exception ex) {
            ex.printStackTrace();
            PlayLog.e("download app error：" + ex);
        }
    }
}
