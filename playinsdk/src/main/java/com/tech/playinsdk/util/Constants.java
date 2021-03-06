package com.tech.playinsdk.util;

public class Constants {

    private static final String HOST_ONLINE = "https://playinads.com";
    private static final String HOST_OFFLINE = "https://playin.live";

    public static final String OS_TYPE = "2";
    public static final String VERSION = "1.0.0";

    public static boolean TEST = false;

    public static String getHost() {
        if (TEST) {
            return HOST_OFFLINE;
        }
        return HOST_ONLINE;
    }

    public static String getConfigHost() {
        if (TEST) {
            return HOST_OFFLINE + "/config?device_name=test";
        }
        return HOST_ONLINE + "/config";
    }


    public final static class Report {
        public final static String DOWN_LOAD = "AppStore";
        public final static String END_PLAY = "endplay";
    }


    public final static class PacketType {
        public final static byte CONTROL = 1;
        public final static byte STREAM = 2;
    }

    public final static class StreamType {
        public final static byte TOUCH = 0;
        public final static byte H264 = 1;
        public final static byte AAC = 2;
        public final static byte PCM = 3;
        public final static byte PARAMS = 4;
        public final static byte ANDROID_VIDEO_START = 6;
    }
}
