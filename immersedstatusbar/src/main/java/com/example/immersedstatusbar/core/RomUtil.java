package com.example.immersedstatusbar.core;

import android.os.Build;
import android.text.TextUtils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

/**
 * Created by Jessewo on 2017/7/4.
 * 判断当前Rom类型
 * MIUI , Flyme, Color os, Android M
 */

class RomUtil {

    class SupportedRom {
        public static final int MIUI = 1;
        public static final int FLYME = 2;
        public static final int ANDROID_NATIVE = 3;
        public static final int NA = 4;
    }

    public static boolean isLightStatusBarAvailable() {
        return isMIUIV6OrAbove() || isFlymeV4OrAbove() || isAndroidMOrAbove();
    }

    public static int getLightStatusBarAvailableRomType() {
        //MIUI V6及以上
        if (isMIUIV6OrAbove()) {
            return SupportedRom.MIUI;
        }
        //Flyme 4.0及以上
        if (isFlymeV4OrAbove()) {
            return SupportedRom.FLYME;
        }
        //Android 6.0及以上
        if (isAndroidMOrAbove()) {
            return SupportedRom.ANDROID_NATIVE;
        }

        return SupportedRom.NA;
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

    /**
     * Android 4.4及其以上和6.0以下的OPPO机型
     *
     * @return
     */
    private static boolean isColorOsBetweenK_M() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT
                && Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            String brand = Build.BRAND;
            if (!TextUtils.isEmpty(brand) && brand.contains("OPPO"))
                return true;
            String manufacturer = Build.MANUFACTURER;
            if (!TextUtils.isEmpty(manufacturer) && manufacturer.contains("OPPO"))
                return true;
            String fingerprint = Build.FINGERPRINT;
            if (!TextUtils.isEmpty(fingerprint) && fingerprint.contains("OPPO"))
                return true;
            String colorOsVersionNum = getSystemProperty("ro.build.version.opporom");
            if (!TextUtils.isEmpty(colorOsVersionNum))
                return true;
            String colorOsVersion = getSystemProperty("ro.rom.different.version");
            if (!TextUtils.isEmpty(colorOsVersion) && colorOsVersion.contains("ColorOS"))
                return true;
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
