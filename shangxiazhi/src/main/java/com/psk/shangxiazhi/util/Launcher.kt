package com.psk.shangxiazhi.util

import android.content.ComponentName
import android.content.Context
import android.content.Intent

/**
 * 启动指定的 app
 * @param packageName           app 包名
 * @param launcherClassName     app 启动页的全类名
 */
fun Context.startApp(packageName: String, launcherClassName: String) {
    if (packageName.isEmpty() || launcherClassName.isEmpty()) {
        return
    }
    Intent().apply {
        component = ComponentName(packageName, launcherClassName)
        action = Intent.ACTION_MAIN
        addCategory(Intent.CATEGORY_LAUNCHER)
        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        startActivity(this)
    }
}