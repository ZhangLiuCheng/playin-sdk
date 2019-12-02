package com.tech.playinsdk.listener;

public interface PlayListener {

    void onPlaystart();
    void onPlayFinish();
    void onPlayClose();
    void onPlayError(Exception ex);
    void onPlayInstall(String url);
    void onPlayForceTime();         // 强制试玩
}
