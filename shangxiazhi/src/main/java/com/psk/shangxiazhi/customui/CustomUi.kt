package com.psk.shangxiazhi.customui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.focusable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Divider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp

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