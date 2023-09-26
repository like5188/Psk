package com.psk.shangxiazhi.util

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.graphics.BitmapFactory
import android.os.Build
import androidx.annotation.DrawableRes
import androidx.core.app.NotificationCompat
import com.like.common.util.createNotificationChannel
import com.psk.shangxiazhi.R

/**
 * @param id    The identifier for this notification as per NotificationManager.notify(int, Notification); must not be 0.
 */
fun Service.setForeground(
    contentTitle: String,
    contentText: String,
    id: Int = this.hashCode(),
    channelName: String = this::class.java.simpleName,
    channelId: String = "${channelName}_id",
    @DrawableRes smallIcon: Int = R.drawable.ic_launcher_foreground,
    @DrawableRes largeIcon: Int = R.drawable.ic_launcher_foreground,
) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        createNotificationChannel(NotificationChannel(channelId, channelName, NotificationManager.IMPORTANCE_LOW))
    }
    startForeground(
        id,
        NotificationCompat.Builder(this, channelId).setSmallIcon(smallIcon).setLargeIcon(BitmapFactory.decodeResource(resources, largeIcon))
            .setContentTitle(contentTitle).setContentText(contentText).build()
    )
}