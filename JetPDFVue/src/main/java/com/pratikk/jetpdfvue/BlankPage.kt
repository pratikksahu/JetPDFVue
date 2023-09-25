package com.pratikk.jetpdfvue

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

@Composable
fun BlankPage(
    modifier: Modifier = Modifier,
    width: Int,
    height: Int
) {
    Box(
        modifier = modifier
            .size(
                width = width.dp,
                height = height.dp
            )
            .background(color = Color.White)
    )
}