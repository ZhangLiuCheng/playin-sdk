package com.tech.playinsdk.util;

public class Analyze {

    private static class AnalyzeClassInstance {
        private static final Analyze instance = new Analyze();
    }

    private Analyze() {}

    public static Analyze getInstance() {
        return AnalyzeClassInstance.instance;
    }

    public void reset() {
        sendTotal = 0;
        sendFail = 0;
        recvVideoTotal = 0;
        recvVideoFail = 0;
    }

    public void report(String token) {
        PlayLog.e("sendTotal: " + sendTotal);
        PlayLog.e("sendFail: " + sendFail);
        PlayLog.e("recvVideoTotal: " + recvVideoTotal);
        PlayLog.e("recvVideoFail: " + recvVideoFail);
    }

    private long sendTotal = 0;
    private long sendFail = 0;

    public void sendResult(boolean reslut) {
        sendTotal++;
        if (!reslut) sendFail++;
    }

    private long recvVideoTotal = 0;
    private long recvVideoFail = 0;
    public void receiveVideoResult(boolean reslut) {
        recvVideoTotal++;
        if (!reslut) recvVideoFail++;
    }
}
