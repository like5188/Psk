package com.psk.shangxiazhi.login

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.psk.shangxiazhi.customui.Bg
import com.psk.shangxiazhi.customui.Title

@Preview(widthDp = 1920, heightDp = 1080)
@Composable
private fun LoginScreenPreview() {
    LoginScreen("123")
}

/**
 * 登录界面
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginScreen(
    serialNumber: String = "",
    code: String = "",
    onCodeChange: (String) -> Unit = {},
    onActivation: () -> Unit = {}
) {
    Bg(contentAlignment = Alignment.Center) {
        Surface(
            shape = RoundedCornerShape(10.dp),
            shadowElevation = 10.dp,
            modifier = Modifier
                .width(600.dp)
                .height(560.dp)
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Title("软件激活")
                Spacer(modifier = Modifier.height(50.dp))
                Row(modifier = Modifier.padding(horizontal = 30.dp), verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "序列号：",
                        style = MaterialTheme.typography.headlineSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    Text(
                        modifier = Modifier.fillMaxWidth(),
                        text = serialNumber,
                        style = MaterialTheme.typography.headlineSmall,
                    )
                }
                Spacer(modifier = Modifier.height(50.dp))
                Row(modifier = Modifier.padding(horizontal = 30.dp), verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "激活码：",
                        style = MaterialTheme.typography.headlineSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    TextField(
                        modifier = Modifier.fillMaxWidth(),
                        value = code,
                        onValueChange = onCodeChange,
                        textStyle = MaterialTheme.typography.headlineSmall
                    )
                }
                Spacer(modifier = Modifier.height(100.dp))
                Button(
                    modifier = Modifier
                        .width(500.dp)
                        .height(60.dp),
                    onClick = onActivation
                ) {
                    Text(text = "激活", fontSize = 20.sp)
                }
            }

        }
    }

}
