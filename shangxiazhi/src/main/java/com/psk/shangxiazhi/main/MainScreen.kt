package com.psk.shangxiazhi.main

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.paint
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.psk.shangxiazhi.R
import com.psk.shangxiazhi.customui.BoxButton

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
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Text(
                text = time,
                style = MaterialTheme.typography.displaySmall,
                color = MaterialTheme.colorScheme.onPrimary,
            )

            BoxButton(
                contentAlignment = Alignment.TopEnd,
                onClick = onSettingClick,
            ) { isPressed, isFocused ->
                Icon(
                    modifier = Modifier
                        .width(80.dp)
                        .height(80.dp),
                    contentDescription = null,
                    painter = painterResource(id = R.drawable.ic_set_up),
                    tint = if (isPressed) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onPrimary
                )
            }
        }

        Row(
            modifier = Modifier.fillMaxSize(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center,
        ) {
            BoxButton(
                modifier = Modifier
                    .width(640.dp)
                    .height(640.dp),
                contentAlignment = Alignment.BottomEnd,
                onClick = onAutonomyTrainingClick,
            ) { isPressed, isFocused ->
                Image(
                    modifier = Modifier.fillMaxSize(),
                    painter = painterResource(
                        id = if (isPressed || isFocused) R.drawable.home_game_choose else R.drawable.home_game_normal
                    ),
                    contentScale = ContentScale.FillBounds,
                    contentDescription = null
                )
                Text(
                    modifier = Modifier.padding(bottom = 90.dp, end = 50.dp),
                    text = "自主训练",
                    style = MaterialTheme.typography.displayMedium,
                    color = MaterialTheme.colorScheme.onPrimary,
                    textAlign = TextAlign.End
                )
            }

            BoxButton(
                modifier = Modifier
                    .width(640.dp)
                    .height(640.dp),
                contentAlignment = Alignment.BottomEnd,
                onClick = onTrainingRecordsClick,
            ) { isPressed, isFocused ->
                Image(
                    modifier = Modifier.fillMaxSize(),
                    painter = painterResource(
                        id = if (isPressed || isFocused) R.drawable.home_history_choose else R.drawable.home_history_normal
                    ),
                    contentScale = ContentScale.FillBounds,
                    contentDescription = null
                )
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
