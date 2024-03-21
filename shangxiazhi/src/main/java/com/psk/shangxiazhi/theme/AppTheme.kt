package com.psk.shangxiazhi.theme

import android.content.res.Configuration
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MaterialTheme.shapes
import androidx.compose.material3.MaterialTheme.typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Density

@Composable
fun AppTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    //判断竖屏，计算Density的方式会有变化
    val isPortrait = LocalConfiguration.current.orientation == Configuration.ORIENTATION_PORTRAIT

    //字体缩放暂时不变，有兴趣的玩家可以自己尝试
    val fontScale = LocalDensity.current.fontScale

    val displayMetrics = LocalContext.current.resources.displayMetrics
    val widthPixels = displayMetrics.widthPixels
    val heightPixels = displayMetrics.heightPixels

    val designWidth = 1920f //设计图宽度（一倍、像素）
    val designHeight = 1080f //设计图高度（一倍、像素）

    //除以设计图宽度或高度，计算出Density
    val density = if (isPortrait) {
        widthPixels / designWidth
    } else {
        widthPixels / designWidth
    }
    val colorScheme = if (darkTheme) {
        darkColorScheme()
    } else {
        lightColorScheme()
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = typography,
        shapes = shapes,
        content = {
            CompositionLocalProvider( //屏幕适配
                values = arrayOf(
                    LocalDensity provides Density(
                        density = density,
                        fontScale = fontScale
                    )
                ),
                content = content
            )
        }
    )
}