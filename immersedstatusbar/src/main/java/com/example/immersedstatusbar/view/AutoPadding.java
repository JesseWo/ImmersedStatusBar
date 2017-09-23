package com.example.immersedstatusbar.view;

import android.content.Context;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.os.Build;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import com.example.immersedstatusbar.R;
import static com.example.immersedstatusbar.core.StatusBarUtil.getStatusBarHeight;

/**
 * Created by Jessewo on 16/7/9.
 */

public class AutoPadding {

    private static final String TAG = "AutoPadding";

    public static void init(Context context, AttributeSet attrs, View root) {
        if (isWindowTranslucentStatus(context)) {
            if (needAutoPadding(context, attrs)) {
                int height = getStatusBarHeight(context);
                root.setPadding(root.getPaddingRight(), height + root.getPaddingTop(),
                        root.getPaddingLeft(), root.getPaddingBottom());
            }
        }
    }

    private static boolean isWindowTranslucentStatus(Context context) {
        Resources res = context.getResources();
        int resId = res.getIdentifier("windowTranslucentStatus", "boolean", "android");
        if (resId > 0) {
            Log.d(TAG, "isWindowTranslucentStatus:" + res.getBoolean(resId));
        }
        return true;
    }

    private static boolean needAutoPadding(Context context, AttributeSet attrs) {
        boolean enable = false;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            TypedArray a = context.obtainStyledAttributes(attrs,
                    R.styleable.AutoPaddingLayout, 0, 0);
            enable = a.getBoolean(R.styleable.AutoPaddingLayout_auto_padding, false);
            a.recycle();
        }
        return enable;
    }
}
