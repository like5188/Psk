package com.psk.shangxiazhi.util

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.graphics.BitmapFactory
import android.os.Build
import androidx.annotation.DrawableRes
import androidx.core.app.NotificationCompat
import com.like.common.util.createNotificationChannel

/**
 * @param id    The identifier for this notification as per NotificationManager.notify(int, Notification); must not be 0.
 */
fun Service.setForeground(
    id: Int,
    channelId: String,
    channelName: String,
    contentTitle: String,
    contentText: String,
    @DrawableRes smallIcon: Int,
    @DrawableRes largeIcon: Int,
) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        createNotificationChannel(NotificationChannel(channelId, channelName, NotificationManager.IMPORTANCE_HIGH))
    }
    startForeground(
        id,
        NotificationCompat.Builder(this, channelId)
            .setSmallIcon(smallIcon)
            .setLargeIcon(BitmapFactory.decodeResource(resources, largeIcon))
            .setContentTitle(contentTitle)
            .setContentText(contentText)
            .build()
    )
}