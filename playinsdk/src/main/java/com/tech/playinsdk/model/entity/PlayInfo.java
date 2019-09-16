package com.tech.playinsdk.model.entity;

import org.json.JSONObject;

public class PlayInfo {

    private String token;
    private String serverIp;
    private int serverPort;
    private int duration;
    private int orientation;
    private int closeX;
    private int closeY;
    private String appstoreUrl;
    private String googleplayUrl;
    private String playnowOffset;
    private String continueText;
    private String installText;
    private String webviewUrl;
    private String webviewDevUrl;
    private int deviceWidth;
    private int deviceHeight;
    private int osType;

    private String audience;
    private String appName;
    private String appIcon;
    private int appRate;
    private int commentsCount;
    private int downloadsCount;
    private String copywriting;

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public String getServerIp() {
        return serverIp;
    }

    public void setServerIp(String serverIp) {
        this.serverIp = serverIp;
    }

    public int getServerPort() {
        return serverPort;
    }

    public void setServerPort(int serverPort) {
        this.serverPort = serverPort;
    }

    public int getDuration() {
        return duration;
    }

    public void setDuration(int duration) {
        this.duration = duration;
    }

    public int getOrientation() {
        return orientation;
    }

    public void setOrientation(int orientation) {
        this.orientation = orientation;
    }

    public float getCloseX() {
        return closeX;
    }

    public void setCloseX(int closeX) {
        this.closeX = closeX;
    }

    public float getCloseY() {
        return closeY;
    }

    public void setCloseY(int closeY) {
        this.closeY = closeY;
    }

    public String getAppstoreUrl() {
        return appstoreUrl;
    }

    public void setAppstoreUrl(String appstoreUrl) {
        this.appstoreUrl = appstoreUrl;
    }

    public String getGoogleplayUrl() {
        return googleplayUrl;
    }

    public void setGoogleplayUrl(String googleplayUrl) {
        this.googleplayUrl = googleplayUrl;
    }

    public String getPlaynowOffset() {
        return playnowOffset;
    }

    public void setPlaynowOffset(String playnowOffset) {
        this.playnowOffset = playnowOffset;
    }

    public String getContinueText() {
        return continueText;
    }

    public void setContinueText(String continueText) {
        this.continueText = continueText;
    }

    public String getInstallText() {
        return installText;
    }

    public void setInstallText(String installText) {
        this.installText = installText;
    }

    public String getWebviewUrl() {
        return webviewUrl;
    }

    public void setWebviewUrl(String webviewUrl) {
        this.webviewUrl = webviewUrl;
    }

    public String getWebviewDevUrl() {
        return webviewDevUrl;
    }

    public void setWebviewDevUrl(String webviewDevUrl) {
        this.webviewDevUrl = webviewDevUrl;
    }

    public int getDeviceWidth() {
        return deviceWidth;
    }

    public void setDeviceWidth(int deviceWidth) {
        this.deviceWidth = deviceWidth;
    }

    public int getDeviceHeight() {
        return deviceHeight;
    }

    public void setDeviceHeight(int deviceHeight) {
        this.deviceHeight = deviceHeight;
    }

    public int getOsType() {
        return osType;
    }

    public void setOsType(int osType) {
        this.osType = osType;
    }


    public String getAudience() {
        return audience;
    }

    public void setAudience(String audience) {
        this.audience = audience;
    }

    public String getAppName() {
        return appName;
    }

    public void setAppName(String appName) {
        this.appName = appName;
    }

    public String getAppIcon() {
        return appIcon;
    }

    public void setAppIcon(String appIcon) {
        this.appIcon = appIcon;
    }

    public int getAppRate() {
        return appRate;
    }

    public void setAppRate(int appRate) {
        this.appRate = appRate;
    }

    public int getCommentsCount() {
        return commentsCount;
    }

    public void setCommentsCount(int commentsCount) {
        this.commentsCount = commentsCount;
    }

    public int getDownloadsCount() {
        return downloadsCount;
    }

    public void setDownloadsCount(int downloadsCount) {
        this.downloadsCount = downloadsCount;
    }

    public String getCopywriting() {
        return copywriting;
    }

    public void setCopywriting(String copywriting) {
        this.copywriting = copywriting;
    }

    public static PlayInfo jsonToPlayInfo(JSONObject obj) {
        PlayInfo playInfo = new PlayInfo();
        playInfo.setServerIp(obj.optString("stream_server_ip"));
        playInfo.setServerPort(obj.optInt("stream_server_port"));
        playInfo.setToken(obj.optString("token"));
        playInfo.setDuration(obj.optInt("duration"));
        playInfo.setCloseX(obj.optInt("close_x"));
        playInfo.setCloseX(obj.optInt("close_y"));
        playInfo.setInstallText(obj.optString("install_text"));
        playInfo.setContinueText(obj.optString("continue_text"));
        playInfo.setAppstoreUrl(obj.optString("appstoreUrl"));
        playInfo.setGoogleplayUrl(obj.optString("googleplay_url"));
        playInfo.setOrientation(obj.optInt("orientation"));
        playInfo.setDeviceWidth(obj.optInt("device_width"));
        playInfo.setDeviceHeight(obj.optInt("device_height"));
        playInfo.setPlaynowOffset(obj.optString("playnow_offset"));

        playInfo.setAudience(obj.optString("audience"));
        playInfo.setAppName(obj.optString("app_name"));
        playInfo.setAppIcon(obj.optString("icon_url"));
        playInfo.setAppRate(obj.optInt("app_rate"));
        playInfo.setCommentsCount(obj.optInt("comments_count"));
        playInfo.setDownloadsCount(obj.optInt("downloads"));
        playInfo.setCopywriting(obj.optString("copywriting"));
        playInfo.setOsType(obj.optInt("os_type"));

        return playInfo;
    }
}
