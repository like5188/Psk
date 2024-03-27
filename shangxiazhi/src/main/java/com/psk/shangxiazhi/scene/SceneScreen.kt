package com.psk.shangxiazhi.scene

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.psk.shangxiazhi.R
import com.psk.shangxiazhi.customui.Bg
import com.psk.shangxiazhi.customui.ColumnButton
import com.twsz.twsystempre.TrainScene

@Preview(widthDp = 1920, heightDp = 1080)
@Composable
private fun SceneScreenPreview() {
    SceneScreen()
}

/**
 * 选择场景界面
 */
@Composable
fun SceneScreen(
    selectedScene: TrainScene? = null,
    onClick: (TrainScene) -> Unit = {}
) {
    Bg(contentAlignment = Alignment.Center) {
        Column {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center,
            ) {
                ColumnButton(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    onClick = { onClick(TrainScene.country) },
                ) { isPressed, isFocused ->
                    Image(
                        modifier = Modifier
                            .width(480.dp)
                            .height(340.dp),
                        painter = painterResource(
                            id = if (isPressed || isFocused || selectedScene == TrainScene.country) R.drawable.scene_choose_01 else R.drawable.scene_normal_01
                        ),
                        contentScale = ContentScale.FillBounds,
                        contentDescription = null
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = "丛林",
                        style = MaterialTheme.typography.displayMedium,
                        color = MaterialTheme.colorScheme.onPrimary,
                    )
                }
                Spacer(modifier = Modifier.width(40.dp))
                ColumnButton(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    onClick = { onClick(TrainScene.dust) },
                ) { isPressed, isFocused ->
                    Image(
                        modifier = Modifier
                            .width(480.dp)
                            .height(340.dp),
                        painter = painterResource(
                            id = if (isPressed || isFocused || selectedScene == TrainScene.dust) R.drawable.scene_choose_02 else R.drawable.scene_normal_02
                        ),
                        contentScale = ContentScale.FillBounds,
                        contentDescription = null
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = "沙漠",
                        style = MaterialTheme.typography.displayMedium,
                        color = MaterialTheme.colorScheme.onPrimary,
                    )
                }
            }
            Spacer(modifier = Modifier.height(40.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center,
            ) {
                ColumnButton(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    onClick = { onClick(TrainScene.lasa) },
                ) { isPressed, isFocused ->
                    Image(
                        modifier = Modifier
                            .width(480.dp)
                            .height(340.dp),
                        painter = painterResource(
                            id = if (isPressed || isFocused || selectedScene == TrainScene.lasa) R.drawable.scene_choose_03 else R.drawable.scene_normal_03
                        ),
                        contentScale = ContentScale.FillBounds,
                        contentDescription = null
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = "拉萨",
                        style = MaterialTheme.typography.displayMedium,
                        color = MaterialTheme.colorScheme.onPrimary,
                    )
                }
                Spacer(modifier = Modifier.width(40.dp))
                ColumnButton(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    onClick = { onClick(TrainScene.sea) },
                ) { isPressed, isFocused ->
                    Image(
                        modifier = Modifier
                            .width(480.dp)
                            .height(340.dp),
                        painter = painterResource(
                            id = if (isPressed || isFocused || selectedScene == TrainScene.sea) R.drawable.scene_choose_04 else R.drawable.scene_normal_04
                        ),
                        contentScale = ContentScale.FillBounds,
                        contentDescription = null
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = "海洋",
                        style = MaterialTheme.typography.displayMedium,
                        color = MaterialTheme.colorScheme.onPrimary,
                    )
                }
            }
        }
    }

}
