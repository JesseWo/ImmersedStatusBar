package com.example.immersedstatusbar.core;

import android.os.Build;
import android.text.TextUtils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import static com.example.immersedstatusbar.core.RomUtil.SupportedRom.ANDROID_NATIVE;
import static com.example.immersedstatusbar.core.RomUtil.SupportedRom.FLYME;
import static com.example.immersedstatusbar.core.RomUtil.SupportedRom.MIUI;
import static com.example.immersedstatusbar.core.RomUtil.SupportedRom.NA;

/**
 * Created by Jessewo on 2017/7/4.
 * 判断当前Rom类型
 * MIUI Flyme Android M
 */

class RomUtil {

    enum SupportedRom {
        MIUI,
        FLYME,
        ANDROID_NATIVE,
        NA
    }

    public static boolean isLightStatusBarAvailable() {
        return isMIUIV6OrAbove() || isFlymeV4OrAbove() || isAndroidMOrAbove();
    }

    static SupportedRom getLightStatusBarAvailableRomType() {
        //MIUI V6及以上
        if (isMIUIV6OrAbove()) {
            return MIUI;
        }
        //Flyme 4.0及以上
        if (isFlymeV4OrAbove()) {
            return FLYME;
        }
        //Android 6.0及以上
        if (isAndroidMOrAbove()) {
            return ANDROID_NATIVE;
        }

        return NA;
    }

    //Flyme V4的displayId格式为 [Flyme OS 4.x.x.xA]
    //Flyme V5的displayId格式为 [Flyme 5.x.x.x beta]
    private static boolean isFlymeV4OrAbove() {
        String displayId = Build.DISPLAY;
        if (!TextUtils.isEmpty(displayId) && displayId.contains("Flyme")) {
            String[] displayIdArray = displayId.split(" ");
            for (String temp : displayIdArray) {
                //版本号4以上，形如4.x.
                if (temp.matches("^[4-9]\\.(\\d+\\.)+\\S*")) {
                    return true;
                }
            }
        }
        return false;
    }

    //MIUI V6对应的versionCode是4
    //MIUI V7对应的versionCode是5
    private static boolean isMIUIV6OrAbove() {
        String miuiVersionCodeStr = getSystemProperty("ro.miui.ui.version.code");
        if (!TextUtils.isEmpty(miuiVersionCodeStr)) {
            try {
                int miuiVersionCode = Integer.parseInt(miuiVersionCodeStr);
                if (miuiVersionCode >= 4) {
                    return true;
                }
            } catch (Exception e) {
            }
        }
        return false;
    }

    //Android Api 23以上
    private static boolean isAndroidMOrAbove() {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.M;
    }

    private static String getSystemProperty(String propName) {
        String line;
        BufferedReader input = null;
        try {
            Process p = Runtime.getRuntime().exec("getprop " + propName);
            input = new BufferedReader(new InputStreamReader(p.getInputStream()), 1024);
            line = input.readLine();
            input.close();
        } catch (IOException ex) {
            return null;
        } finally {
            if (input != null) {
                try {
                    input.close();
                } catch (IOException e) {
                }
            }
        }
        return line;
    }
}
