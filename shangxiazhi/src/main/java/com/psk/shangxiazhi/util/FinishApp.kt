package com.psk.shangxiazhi.util

import android.app.Activity
import android.content.Context
import android.widget.Toast

class FinishApp {
    companion object {
        private const val INTERVAL: Long = 1500
        var mLastTime: Long = 0
    }

    fun execute(context: Context) {
        when {
            /**
             * [INTERVAL] 间隔以内点击两次返回键退出程序
             */
            (System.currentTimeMillis() - mLastTime) > INTERVAL -> {
                Toast.makeText(context, "再按一次退出程序", Toast.LENGTH_SHORT).show()
                mLastTime = System.currentTimeMillis()
            }

            else -> {
                (context as? Activity)?.finish()
            }
        }
    }
}