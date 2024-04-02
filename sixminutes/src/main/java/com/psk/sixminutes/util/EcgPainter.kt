package com.psk.sixminutes.util

import android.graphics.Color
import android.graphics.DashPathEffect
import android.graphics.Paint
import com.psk.ecg.effect.CirclePathEffect
import com.psk.ecg.painter.BgPainter
import com.psk.ecg.painter.DynamicDataPainter
import com.psk.ecg.painter.IBgPainter
import com.psk.ecg.painter.IDataPainter

fun createBgPainter(): IBgPainter = BgPainter(
    Paint().apply {
        color = Color.parseColor("#00a7ff")
        strokeWidth = 2f
        isAntiAlias = true
        alpha = 120
    },
    Paint().apply {
        color = Color.parseColor("#00a7ff")
        strokeWidth = 1f
        isAntiAlias = true
        pathEffect = DashPathEffect(floatArrayOf(1f, 1f), 0f)
        alpha = 90
    },
    null,
    Paint().apply {
        textSize = 18f
        color = Color.RED
    }
)

fun createDynamicDataPainter(): IDataPainter = DynamicDataPainter(
    CirclePathEffect(),
    Paint().apply {
        color = Color.parseColor("#44C71E")
        strokeWidth = 3f
        style = Paint.Style.STROKE
        isAntiAlias = true
    }
)