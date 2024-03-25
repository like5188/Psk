package com.psk.shangxiazhi.setting

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.psk.shangxiazhi.R
import com.psk.shangxiazhi.customui.Bg
import com.psk.shangxiazhi.customui.Title

@Preview(widthDp = 1920, heightDp = 1080)
@Composable
private fun SettingScreenPreview() {
    SettingScreen("123")
}

/**
 * 设置界面
 */
@Composable
fun SettingScreen(
    version: String = "",
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
                Title("设置")
                Row(
                    modifier = Modifier
                        .clickable {}
                        .padding(horizontal = 15.dp, vertical = 30.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        modifier = Modifier
                            .width(40.dp)
                            .height(40.dp),
                        contentDescription = null,
                        painter = painterResource(id = R.drawable.ic_version),
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = "当前版本：",
                        style = MaterialTheme.typography.headlineSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    Text(
                        modifier = Modifier.fillMaxWidth(),
                        text = version,
                        textAlign = TextAlign.End,
                        style = MaterialTheme.typography.headlineSmall,
                    )
                }
            }

        }
    }

}
