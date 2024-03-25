package com.psk.shangxiazhi.customui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.focusable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier

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