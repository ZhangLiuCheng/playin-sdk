package com.tech.playinsdk.util;

import android.util.Log;
import android.view.MotionEvent;

import com.tech.playinsdk.model.entity.PlayInfo;

import org.json.JSONException;
import org.json.JSONObject;

public class TouchUtil {

    private static class GameEvent {
        int action;
        int pointerCount;
        MotionEvent.PointerProperties[] properties;
        MotionEvent.PointerCoords[] coords;
    }

    public static String processTouchEvent(MotionEvent event, int width, int height, PlayInfo playInfo) {
        int pointerCount = event.getPointerCount();
        MotionEvent.PointerProperties[] pps = new MotionEvent.PointerProperties[pointerCount];
        MotionEvent.PointerCoords[] pcs = new MotionEvent.PointerCoords[pointerCount];
        for (int i = 0; i < pointerCount; i ++) {
            MotionEvent.PointerProperties pp = new MotionEvent.PointerProperties();
            event.getPointerProperties(i, pp);
            pps[i] = pp;
            MotionEvent.PointerCoords pc = new MotionEvent.PointerCoords();
            event.getPointerCoords(i, pc);
            pcs[i] = pc;
        }
        GameEvent gameEvent = new GameEvent();
        int action = event.getAction();
        gameEvent.action = action;
        gameEvent.pointerCount = pointerCount;
        gameEvent.properties = pps;
        gameEvent.coords = pcs;
        String conStr = convertGameEvent(gameEvent, width, height, playInfo);
        return conStr;
    }

    private static String convertGameEvent(GameEvent gameEvent, int width, int height, PlayInfo playInfo) {
        // 安卓触摸 0-down,2-move,1-up
        // 目标触摸 0-down,1-move,2-up
        try {
            int action = gameEvent.action;
            if (action == 1) {
                action = 2;
            } else if (action == 2) {
                action = 1;
            }
            JSONObject obj1 = new JSONObject();
            for (int i = 0; i < gameEvent.pointerCount; i++) {
                float rateWidth;
                float rateHeight;
                // ios 横屏需转换坐标
                if (playInfo.getOrientation() == 1 && playInfo.getOsType() == 1) {
                    float x = height - gameEvent.coords[i].y;
                    float y = gameEvent.coords[i].x;
                    rateWidth = x / height;
                    rateHeight = y / width;
                } else {
                    float x = gameEvent.coords[i].x;
                    float y = gameEvent.coords[i].y;
                    rateWidth = x / width;
                    rateHeight = y / height;
                }
                String control = rateWidth + "_" + rateHeight + "_" + action + "_0_0";
                obj1.put("" + gameEvent.properties[i].id, control);
            }
            Log.e("TAG", "原始数据 ====>  " + obj1.toString());

            JSONObject obj = new JSONObject();
            for (int i = 0; i < gameEvent.pointerCount; i++) {
                float rateWidth;
                float rateHeight;
                // ios 横屏需转换坐标
                if (playInfo.getOrientation() == 1 && playInfo.getOsType() == 1) {
                    float x = height - gameEvent.coords[i].y;
                    float y = gameEvent.coords[i].x;
                    rateWidth = x / height;
                    rateHeight = y / width;
                } else {
                    float x = gameEvent.coords[i].x;
                    float y = gameEvent.coords[i].y;
                    rateWidth = x / width;
                    rateHeight = y / height;
                }
                int tmpAction = action;
                // down
                if (action == 5) {
                    if (i == 0) {
                        tmpAction = 0;
                    } else {
                        tmpAction = 1;
                    }
                } else if (action == 261) {
                    if (i == 1) {
                        tmpAction = 0;
                    } else {
                        tmpAction = 1;
                    }
                } else if (action == 517) {
                    if (i == 2) {
                        tmpAction = 0;
                    } else {
                        tmpAction = 1;
                    }
                } else if (action == 773) {
                    if (i == 3) {
                        tmpAction = 0;
                    } else {
                        tmpAction = 1;
                    }
                } else if (action == 1029) {
                    if (i == 4) {
                        tmpAction = 0;
                    } else {
                        tmpAction = 1;
                    }
                }

                // up
                if (action == 6) {
                    if (i == 0) {
                        tmpAction = 2;
                    } else {
                        tmpAction = 1;
                    }
                } else if (action == 262) {
                    if (i == 1) {
                        tmpAction = 2;
                    } else {
                        tmpAction = 1;
                    }
                } else if (action == 518) {
                    if (i == 2) {
                        tmpAction = 2;
                    } else {
                        tmpAction = 1;
                    }
                } else if (action == 774) {
                    if (i == 3) {
                        tmpAction = 2;
                    } else {
                        tmpAction = 1;
                    }
                } else if (action == 1030) {
                    if (i == 4) {
                        tmpAction = 2;
                    } else {
                        tmpAction = 1;
                    }
                }
                String control = rateWidth + "_" + rateHeight + "_" + tmpAction + "_0_0";
                obj.put("" + gameEvent.properties[i].id, control);
            }
            Log.e("TAG", "修改数据 ----->  " + obj.toString());

            return obj.toString();
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return null;
    }
}
