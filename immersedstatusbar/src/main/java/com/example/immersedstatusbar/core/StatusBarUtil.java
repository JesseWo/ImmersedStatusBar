package com.example.immersedstatusbar.core;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.os.Build;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;

import com.example.immersedstatusbar.R;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

/**
 * Created by Jessewo on 2017/7/4.
 * 兼容方案<br/>
 * 1. 对于支持[状态栏透明 + 状态栏文字图标黑白切换]的机型(Android 6.0及以上,MIUI v6及以上, Flyme 4.0及以上), 全透明状态栏 + 黑色文字图标
 * 2. 对于仅支持[状态栏透明]的机型(Android 4.4及其以上), 采用: 半透明状态栏 + 白色文字图标
 * 3. 对于不支持的机型(Android 4.4以下), 采用默认状态: 黑色状态栏 + 白色文字图标
 */

public class StatusBarUtil {

    private static int sHeight = -1;

    /**
     * Activity SetContentView 之后执行才生效<br></>
     * 底层版本为Android6.0.1的MIUI 7.1系统不支持View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR,所以优先对MIUI进行判断
     *
     * @param activity
     * @param dark     true 透明底-黑字, false 透明底-白字
     */
    public static void setLightStatusBar(Activity activity, boolean dark) {
        if (activity == null)
            return;
        //状态栏透明
        transparencyStatusBar(activity);
        //状态栏 文字图标 颜色
        switch (RomUtil.getLightStatusBarAvailableRomType()) {
            case MIUI:
                setMIUILightStatusBar(activity, dark);
                break;
            case FLYME:
                setFlymeLightStatusBar(activity, dark);
                break;
            case ANDROID_NATIVE:
                setAndroidNativeLightStatusBar(activity, dark);
                break;
            case NA:
                int colorResId = dark ? R.color.status_translucent_color : R.color.transparent;
                setStatusBarColor(activity, colorResId);
                break;
        }
    }

    /**
     * MIUI v6及其以上ROM 状态栏背景透明, 切换图标字体颜色
     *
     * @param activity
     * @param darkmode
     * @return
     */
    private static boolean setMIUILightStatusBar(Activity activity, boolean darkmode) {
        Window window = activity.getWindow();
        Class<? extends Window> clazz = window.getClass();
        try {
            Class layoutParams = Class.forName("android.view.MiuiWindowManager$LayoutParams");
            int tranceFlag;
            Field field = layoutParams.getField("EXTRA_FLAG_STATUS_BAR_TRANSPARENT");
            tranceFlag = field.getInt(layoutParams);
            int darkModeFlag;
            field = layoutParams.getField("EXTRA_FLAG_STATUS_BAR_DARK_MODE");
            darkModeFlag = field.getInt(layoutParams);

            Method extraFlagField = clazz.getMethod("setExtraFlags", int.class, int.class);
            if (darkmode) {
                //状态栏透明-黑色字体
                extraFlagField.invoke(window, tranceFlag | darkModeFlag, tranceFlag | darkModeFlag);
            } else {
                //状态栏透明-白色字体
                extraFlagField.invoke(window, tranceFlag, tranceFlag | darkModeFlag);
            }
            /*//只需要状态栏透明
            extraFlagField.invoke(window, tranceFlag, tranceFlag);
            //default:状态栏不透明,白色字体
            extraFlagField.invoke(window, 0, darkModeFlag);*/
            return true;
        } catch (Exception e) {
            //ignore
        }

        return false;
    }

    /**
     * Flyme 4及其以上
     *
     * @param activity
     * @param dark
     * @return
     */

    private static boolean setFlymeLightStatusBar(Activity activity, boolean dark) {
        //状态栏字体图标颜色
        FlymeStatusbarUtil.setStatusBarDarkIcon(activity, dark);
        return true;
    }

    /**
     * Android 6.0 原生
     *
     * @param activity
     * @param dark
     */
    private static void setAndroidNativeLightStatusBar(Activity activity, boolean dark) {
        //状态栏字体图标颜色
        View decor = activity.getWindow().getDecorView();
        if (dark) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                decor.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR //浅色状态栏(字体图标白色)
                        | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN //contentView 全屏(置于statusbar之下)
                        | View.SYSTEM_UI_FLAG_LAYOUT_STABLE);
            }
        } else {
            // We want to change tint color to white again.
            // You can also record the flags in advance so that you can turn UI back completely if
            // you have set other flags before, such as translucent or full screen.
            decor.setSystemUiVisibility(View.SYSTEM_UI_FLAG_VISIBLE);
        }
    }

    /**
     * 设置状态栏为全透明
     * 通过设置theme的方式无法达到全透明效果
     *
     * @param activity
     */
    @TargetApi(19)
    private static void transparencyStatusBar(Activity activity) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            //5.0及其以上
            Window window = activity.getWindow();
            window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            window.getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                    | View.SYSTEM_UI_FLAG_LAYOUT_STABLE);
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.setStatusBarColor(Color.TRANSPARENT);
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            //4.4及其以上
            activity.getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        }
    }

    /**
     * 修改状态栏颜色，支持4.4以上版本
     *
     * @param activity
     * @param colorId
     */
    private static void setStatusBarColor(Activity activity, int colorId) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            activity.getWindow().setStatusBarColor(activity.getResources().getColor(colorId));
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            //使用SystemBarTint库使4.4版本状态栏变色，需要先将状态栏设置为透明
            SystemBarTintManager tintManager = new SystemBarTintManager(activity);
            tintManager.setStatusBarTintEnabled(true);
            tintManager.setStatusBarTintResource(colorId);
        }
    }

    public static int getStatusBarHeight(Context context) {
        if (sHeight == -1) {
            //获取status_bar_height资源的ID
            int resourceId = context.getResources().getIdentifier("status_bar_height", "dimen", "android");
            if (resourceId > 0) {
                //根据资源ID获取响应的尺寸值
                sHeight = context.getResources().getDimensionPixelSize(resourceId);
            }
        }
        return sHeight;
    }

}
