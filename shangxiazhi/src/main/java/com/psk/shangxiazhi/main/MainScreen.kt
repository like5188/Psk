package com.psk.shangxiazhi.main

import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.paint
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.psk.shangxiazhi.R

@Preview(widthDp = 1920, heightDp = 1080)
@Composable
private fun MainScreenPreview() {
    MainScreen(time = "2023-12-12 12:30:30")
}

@Composable
fun MainScreen(
    time: String = "",
    onAutonomyTrainingClick: () -> Unit = {},
    onTrainingRecordsClick: () -> Unit = {},
    onSettingClick: () -> Unit = {},
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .paint(painterResource(id = R.drawable.main_bg), contentScale = ContentScale.FillBounds)
            .padding(horizontal = 48.dp, vertical = 27.dp)
    ) {
        Text(
            text = time,
            style = MaterialTheme.typography.displaySmall,
            color = MaterialTheme.colorScheme.onPrimary,
        )
        // 获取按钮的状态
        val interactionSource0 = remember {
            MutableInteractionSource()
        }
        val settingIconTint = if (interactionSource0.collectIsPressedAsState().value) MaterialTheme.colorScheme.primary
        else MaterialTheme.colorScheme.onPrimary
        Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.TopEnd) {
            IconButton(onClick = onSettingClick, interactionSource = interactionSource0) {
                Icon(
                    modifier = Modifier
                        .width(160.dp)
                        .height(160.dp),
                    contentDescription = null,
                    painter = painterResource(id = R.drawable.ic_set_up),
                    tint = settingIconTint
                )
            }
        }
        Row(
            modifier = Modifier.fillMaxSize(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center,
        ) {
            // 获取按钮的状态
            val interactionSource1 = remember {
                MutableInteractionSource()
            }
            val imgRes1 = if (interactionSource1.collectIsPressedAsState().value) R.drawable.home_game_choose
            else R.drawable.home_game_normal
            Box(
                modifier = Modifier
                    .width(640.dp)
                    .height(640.dp)
                    .paint(painterResource(id = imgRes1), contentScale = ContentScale.FillBounds)
                    .clickable(
                        interactionSource = interactionSource1,
                        indication = null
                    ) {
                        onAutonomyTrainingClick()
                    },
                contentAlignment = Alignment.BottomEnd
            ) {
                Text(
                    modifier = Modifier.padding(bottom = 90.dp, end = 50.dp),
                    text = "自主训练",
                    style = MaterialTheme.typography.displayMedium,
                    color = MaterialTheme.colorScheme.onPrimary,
                    textAlign = TextAlign.End
                )
            }
            val interactionSource2 = remember {
                MutableInteractionSource()
            }
            val imgRes2 = if (interactionSource2.collectIsPressedAsState().value) R.drawable.home_history_choose
            else R.drawable.home_history_normal
            Box(
                modifier = Modifier
                    .width(640.dp)
                    .height(640.dp)
                    .paint(painterResource(id = imgRes2), contentScale = ContentScale.FillBounds)
                    .clickable(
                        interactionSource = interactionSource2,
                        indication = null
                    ) {
                        onTrainingRecordsClick()
                    },
                contentAlignment = Alignment.BottomEnd
            ) {
                Text(
                    modifier = Modifier.padding(bottom = 90.dp, end = 50.dp),
                    text = "训练记录",
                    style = MaterialTheme.typography.displayMedium,
                    color = MaterialTheme.colorScheme.onPrimary,
                    textAlign = TextAlign.End
                )
            }
        }
    }
}
