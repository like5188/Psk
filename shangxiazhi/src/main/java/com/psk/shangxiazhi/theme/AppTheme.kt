package com.psk.shangxiazhi.theme

import android.content.res.Configuration
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material.MaterialTheme
import androidx.compose.material.darkColors
import androidx.compose.material.lightColors
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Density

private val DarkColorPalette = darkColors(
    primary = Purple200,
    primaryVariant = Purple700,
    secondary = Teal200
)

private val LightColorPalette = lightColors(
    primary = Purple500,
    primaryVariant = Purple700,
    secondary = Teal200

    /* Other default colors to override
    background = Color.White,
    surface = Color.White,
    onPrimary = Color.White,
    onSecondary = Color.Black,
    onBackground = Color.Black,
    onSurface = Color.Black,
    */
)

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
    val colors = if (darkTheme) {
        DarkColorPalette
    } else {
        LightColorPalette
    }

    MaterialTheme(
        colors = colors,
        typography = Typography,
        shapes = Shapes,
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