package com.psk.shangxiazhi.login

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.paint
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.psk.shangxiazhi.R

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
    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .fillMaxSize()
            .paint(painterResource(id = R.drawable.main_bg), contentScale = ContentScale.FillBounds)
            .padding(horizontal = 48.dp, vertical = 27.dp)
    ) {
        Surface(
            shape = RoundedCornerShape(10.dp),
            shadowElevation = 10.dp,
            modifier = Modifier
                .width(600.dp)
                .height(560.dp)
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Spacer(modifier = Modifier.height(20.dp))
                Text(
                    modifier = Modifier.fillMaxWidth(),
                    text = "软件激活",
                    style = MaterialTheme.typography.headlineLarge,
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(20.dp))
                Divider()
                Spacer(modifier = Modifier.height(50.dp))
                Row(modifier = Modifier.padding(horizontal = 15.dp), verticalAlignment = Alignment.CenterVertically) {
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
                Row(modifier = Modifier.padding(horizontal = 15.dp), verticalAlignment = Alignment.CenterVertically) {
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
