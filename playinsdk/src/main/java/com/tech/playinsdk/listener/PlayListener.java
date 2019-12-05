package com.tech.playinsdk.listener;

public interface PlayListener {

    void onPlaystart();
    void onPlayFinish();
    void onPlayError(Exception ex);
    void onPlayTime(int count);
    void onPlayInstall(String url);
    void onPlayClose();
    void onPlayForceTime();         // 强制试玩
}
