package com.psk.shangxiazhi.history

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.psk.shangxiazhi.R
import com.psk.shangxiazhi.customui.Bg
import com.psk.shangxiazhi.customui.BoxButton
import com.psk.shangxiazhi.customui.Title
import com.psk.shangxiazhi.data.model.OrderInfo
import java.text.SimpleDateFormat

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
    showTime: String = "",
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
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(80.dp)
                        .background(MaterialTheme.colorScheme.onSurfaceVariant),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Spacer(modifier = Modifier.width(40.dp))
                    BoxButton(
                        modifier = Modifier
                            .width(100.dp)
                            .fillMaxHeight(),
                        onClick = {}
                    ) { isPressed, isFocused ->
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(
                                    if (isPressed || isFocused) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant
                                ),
                            contentAlignment = Alignment.Center,
                        ) {
                            Icon(
                                modifier = Modifier
                                    .width(40.dp)
                                    .height(40.dp),
                                contentDescription = null,
                                painter = painterResource(id = R.drawable.ic_left_arrow),
                                tint = MaterialTheme.colorScheme.surface
                            )
                        }
                    }
                    Text(
                        modifier = Modifier.weight(1f),
                        text = showTime,
                        textAlign = TextAlign.Center,
                        color = MaterialTheme.colorScheme.onPrimary,
                        style = MaterialTheme.typography.headlineSmall,
                    )
                    BoxButton(
                        modifier = Modifier
                            .width(100.dp)
                            .fillMaxHeight(),
                        onClick = {}
                    ) { isPressed, isFocused ->
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(
                                    if (isPressed || isFocused) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant
                                ),
                            contentAlignment = Alignment.Center,
                        ) {
                            Icon(
                                modifier = Modifier
                                    .width(40.dp)
                                    .height(40.dp),
                                contentDescription = null,
                                painter = painterResource(id = R.drawable.ic_right_arrow),
                                tint = MaterialTheme.colorScheme.surface
                            )
                        }
                    }
                    Spacer(modifier = Modifier.width(40.dp))
                }
                List(orderInfoList)
            }
        }
    }

}

@Composable
private fun List(orderInfoList: List<OrderInfo>? = null) {
    if (orderInfoList.isNullOrEmpty()) {
        return
    }
    val sdf by remember {
        mutableStateOf(SimpleDateFormat("MM月dd日"))
    }
    val sdf1 by remember {
        mutableStateOf(SimpleDateFormat("HH:mm:ss"))
    }
    LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        for (orderInfo in orderInfoList) {
            item {
                Column {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 30.dp, vertical = 30.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                    ) {
                        Text(
                            text = sdf.format(orderInfo.createTime),
                            textAlign = TextAlign.Center,
                            style = MaterialTheme.typography.headlineSmall,
                        )
                        Text(
                            text = sdf1.format(orderInfo.createTime),
                            textAlign = TextAlign.Center,
                            style = MaterialTheme.typography.headlineSmall,
                        )
                    }
                    Divider()
                }
            }
        }
    }
}
