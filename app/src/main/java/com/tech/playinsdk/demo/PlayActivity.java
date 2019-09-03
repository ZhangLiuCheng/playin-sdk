package com.tech.playinsdk.demo;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

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
        playView.play(Constants.ADID, 3, this);
    }

    @Override
    public void onPlaystart() {
        hideLoading();
    }

    @Override
    public void onPlayClose() {
        finish();
    }

    @Override
    public void onPlayError(Exception ex) {
        PlayLog.e("onPlayError " + ex);
    }

    private void hideLoading() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                findViewById(R.id.loading).setVisibility(View.GONE);
            }
        });
    }
}
