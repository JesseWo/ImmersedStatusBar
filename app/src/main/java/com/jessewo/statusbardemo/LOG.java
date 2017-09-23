package com.jessewo.statusbardemo;

import android.text.TextUtils;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by Jessewo on 16/1/25.
 */
public class LOG {

    private static final String TAG = "statusBar";
    private static final boolean debug = BuildConfig.DEBUG;
    /**
     * It is used for json pretty println
     */
    private static final int JSON_INDENT = 2;

    public static void d(String tag, String info) {
        if (debug) {
            Log.d(TAG, tag + " " + info);
        }
    }

    public static void d(String info) {
        d(getClassName(), info);
    }

    public static void d(String format, Object... args) {
        d(String.format(format, args));
    }

    public static void e(String tag, String info) {
        Log.e(TAG, tag + " " + info);
    }

    public static void e(String info) {
        e(getClassName(), info);
    }

    public static void e(String tag, Throwable e) {
        Log.e(TAG, tag, e);
    }

    public static void e(String tag, String string, Throwable e) {
        Log.e(TAG, tag + " " + string, e);
    }

    public static void i(String tag, String info) {
        Log.i(TAG, tag + " " + info);
    }

    public static void i(String info) {
        i(getClassName(), info);
    }

    public static void i(String tag, String string, Throwable e) {
        Log.i(TAG, tag + " " + string, e);
    }

    private static String getClassName() {
        String result;
        // 这里的数组的index2是根据你工具类的层级做不同的定义，这里仅仅是关键代码
        StackTraceElement thisMethodStack = (new Exception()).getStackTrace()[2];
        result = thisMethodStack.getClassName();
        int lastIndex = result.lastIndexOf(".");
        result = result.substring(lastIndex + 1, result.length());
        return result;
    }

    public static void json(String tag, String json) {
        tag = getClassName() + ": " + tag;
        if (TextUtils.isEmpty(json)) {
            println(tag, "");
            return;
        }
        try {
            json = json.trim();
            if (json.startsWith("{")) {
                JSONObject jsonObject = new JSONObject(json);
                String message = jsonObject.toString(JSON_INDENT);
                println(tag, message);
                return;
            }
            if (json.startsWith("[")) {
                JSONArray jsonArray = new JSONArray(json);
                String message = jsonArray.toString(JSON_INDENT);
                println(tag, message);
                return;
            }
            println(tag, json);
        } catch (JSONException e) {
            println(tag, json);
        }
    }

    private static void println(String tag, String message) {
        String[] lines = message.split(System.getProperty("line.separator"));
        for (String line : lines) {
            d(tag, line);
        }
    }
}
