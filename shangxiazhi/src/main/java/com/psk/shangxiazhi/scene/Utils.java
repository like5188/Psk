package com.psk.shangxiazhi.scene;

import android.app.ActivityManager;
import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;

import java.util.List;
import java.util.function.Consumer;

public class Utils {

    public static void getRunning3rdApp(Context context) {
        PackageManager localPackageManager = context.getPackageManager();
        List localList = localPackageManager.getInstalledPackages(0);
        for (int i = 0; i < localList.size(); i++) {
            PackageInfo localPackageInfo1 = (PackageInfo) localList.get(i);
            String str1 = localPackageInfo1.packageName.split(":")[0];
            if (((ApplicationInfo.FLAG_SYSTEM & localPackageInfo1.applicationInfo.flags) == 0)
                    && ((ApplicationInfo.FLAG_UPDATED_SYSTEM_APP & localPackageInfo1.applicationInfo.flags) == 0)
                    && ((ApplicationInfo.FLAG_STOPPED & localPackageInfo1.applicationInfo.flags) == 0)) {

                System.out.println(str1);
            }
        }
    }
}
