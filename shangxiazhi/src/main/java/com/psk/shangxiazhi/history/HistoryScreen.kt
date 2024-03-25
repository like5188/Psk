package com.psk.shangxiazhi.history

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.psk.shangxiazhi.customui.Bg
import com.psk.shangxiazhi.customui.Title
import com.psk.shangxiazhi.data.model.OrderInfo

@Preview(widthDp = 1920, heightDp = 1080)
@Composable
private fun HistoryScreenPreview() {
    HistoryScreen(
        "2022年02月",
        listOf(
            OrderInfo(0, System.currentTimeMillis() - 1000, 0),
            OrderInfo(1, System.currentTimeMillis() - 2000, 1),
            OrderInfo(2, System.currentTimeMillis() - 3000, 2),
        )
    )
}

/**
 * 设置界面
 */
@Composable
fun HistoryScreen(
    showTime: String? = null,
    orderInfoList: List<OrderInfo>? = null,
) {
    Bg(contentAlignment = Alignment.Center) {
        Surface(
            shape = RoundedCornerShape(10.dp),
            shadowElevation = 10.dp,
            modifier = Modifier
                .width(1000.dp)
                .fillMaxHeight()
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Title("选择训练时间")
            }

        }
    }

}
