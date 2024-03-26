package com.psk.shangxiazhi.customui

import android.view.Window
import androidx.compose.foundation.clickable
import androidx.compose.foundation.focusable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Divider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.paint
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.DialogProperties
import androidx.compose.ui.window.DialogWindowProvider
import com.psk.shangxiazhi.R

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun Dialog(
    onDismissRequest: () -> Unit = {},
    content: @Composable (Window) -> Unit
) {
    androidx.compose.ui.window.Dialog(
        onDismissRequest = onDismissRequest,
        properties = DialogProperties(decorFitsSystemWindows = false),
    ) {
        getDialogWindow()?.let {
            content(it)
        }
    }
}

@Composable
private fun getDialogWindow(): Window? = (LocalView.current.parent as? DialogWindowProvider)?.window

@Composable
fun Bg(contentAlignment: Alignment = Alignment.TopStart, content: @Composable BoxScope.() -> Unit) {
    Box(
        contentAlignment = contentAlignment,
        modifier = Modifier
            .fillMaxSize()
            .paint(painterResource(id = R.drawable.main_bg), contentScale = ContentScale.FillBounds)
            .padding(horizontal = 48.dp, vertical = 27.dp),
        content = content
    )
}

@Composable
fun Title(text: String) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .height(112.dp),
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = text,
                style = MaterialTheme.typography.headlineLarge,
                textAlign = TextAlign.Center
            )
        }
        Divider()
    }
}

@Composable
fun BoxButton(
    modifier: Modifier = Modifier,
    contentAlignment: Alignment = Alignment.TopStart,
    propagateMinConstraints: Boolean = false,
    onClick: () -> Unit = {},
    content: @Composable BoxScope.(Boolean, Boolean) -> Unit
) {
    // 获取按钮的状态
    val isPressedInteractionSource = remember {
        MutableInteractionSource()
    }
    val isFocusedInteractionSource = remember {
        MutableInteractionSource()
    }
    Box(
        modifier = modifier
            .clickable(interactionSource = isPressedInteractionSource, indication = null, onClick = onClick)
            .focusable(interactionSource = isFocusedInteractionSource),
        contentAlignment = contentAlignment,
        propagateMinConstraints = propagateMinConstraints
    ) {
        content(isPressedInteractionSource.collectIsPressedAsState().value, isFocusedInteractionSource.collectIsFocusedAsState().value)
    }
}