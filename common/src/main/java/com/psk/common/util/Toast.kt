package com.psk.common.util

import android.content.Context
import android.os.Handler
import android.os.Looper
import android.widget.Toast
import androidx.annotation.StringRes

private val handler = Handler(Looper.getMainLooper())

data class ToastEvent(
    @StringRes val res: Int = -1,
    val text: String? = null,
    val throwable: Throwable? = null
)

fun Context.showToast(toastEvent: ToastEvent) {
    var msg = try {
        this.getString(toastEvent.res)
    } catch (e: Exception) {
        null
    }
    if (msg.isNullOrEmpty()) {
        msg = toastEvent.text
    }
    if (msg.isNullOrEmpty()) {
        msg = toastEvent.throwable?.message ?: ""
    }
    this.showToast(msg)
}

fun Context.showToast(throwable: Throwable?) {
    showToast(throwable?.message ?: "unknown error")
}

fun Context.showToast(msg: String?) {
    if (Looper.getMainLooper() === Looper.myLooper()) {
        Toast.makeText(this, msg ?: "", Toast.LENGTH_SHORT).show()
    } else {
        handler.post { Toast.makeText(this, msg ?: "", Toast.LENGTH_SHORT).show() }
    }
}
