package com.psk.ecg.base

import android.content.Context
import android.util.AttributeSet
import android.util.Log
import android.view.SurfaceHolder
import android.view.SurfaceView
import com.psk.ecg.util.TAG

abstract class BaseSurfaceView(context: Context, attrs: AttributeSet?) : SurfaceView(context, attrs), SurfaceHolder.Callback {
    protected var isSurfaceCreated = false
        private set

    init {
        holder.addCallback(this)
    }

    /*
     下面的三个函数是 实现 SurfaceHolder.Callback 接口方法
     */
    // activity onPause时调用
    override fun surfaceDestroyed(holder: SurfaceHolder) {
        Log.w(TAG, "surfaceDestroyed")
        isSurfaceCreated = false
    }

    override fun surfaceChanged(holder: SurfaceHolder, format: Int, width: Int, height: Int) {
        Log.w(TAG, "surfaceChanged")
    }

    // activity onResume时调用
    override fun surfaceCreated(holder: SurfaceHolder) {
        Log.w(TAG, "surfaceCreated")
        isSurfaceCreated = true
    }
}
