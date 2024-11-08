package com.pratikk.jetpackpdf.verticalSamples

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.RotateLeft
import androidx.compose.material.icons.filled.RotateRight
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.pratikk.jetpdfvue.VerticalVueReader
import com.pratikk.jetpdfvue.state.VerticalVueReaderState
import kotlinx.coroutines.launch


@Composable
fun VerticalSampleA(
    modifier: Modifier = Modifier,
    verticalVueReaderState: VerticalVueReaderState,
    import:() -> Unit
){
    Box(
        modifier = modifier
    ) {
        val scope = rememberCoroutineScope()

        val background = Modifier.background(MaterialTheme.colorScheme.background.copy(alpha = 0.75f),MaterialTheme.shapes.small)
            .border(width = 1.dp, color = MaterialTheme.colorScheme.outline, shape = MaterialTheme.shapes.small)
            .clip(MaterialTheme.shapes.small)
        val iconTint = MaterialTheme.colorScheme.onBackground

        VerticalVueReader(
            modifier = Modifier.fillMaxSize(),
            contentModifier = Modifier.fillMaxSize(),
            verticalVueReaderState = verticalVueReaderState
        )
        Row(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(horizontal = 8.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "${verticalVueReaderState.currentPage} of ${verticalVueReaderState.pdfPageCount}",
                modifier = Modifier
                    .then(background)
                    .padding(10.dp)
            )
            Spacer(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
            )
            Row {
                val context = LocalContext.current
                IconButton(
                    modifier = background,
                    onClick = import
                ) {
                    Icon(
                        imageVector = Icons.Filled.Add,
                        contentDescription = "Add Page",
                        tint = iconTint
                    )
                }
                Spacer(modifier = Modifier.width(5.dp))
                IconButton(
                    modifier = background,
                    onClick = { //Share
                        verticalVueReaderState.sharePDF(context)
                    }) {
                    Icon(
                        imageVector = Icons.Filled.Share,
                        contentDescription = "Share",
                        tint = iconTint
                    )
                }
            }
        }
        Row(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(horizontal = 8.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            val showPrevious by remember {
                derivedStateOf { verticalVueReaderState.currentPage != 1 }
            }
            val showNext by remember {
                derivedStateOf { verticalVueReaderState.currentPage != verticalVueReaderState.pdfPageCount }
            }
            if (showPrevious)
                IconButton(
                    modifier = background,
                    onClick = {
                        //Prev
                        scope.launch {
                            verticalVueReaderState.prevPage()
                        }
                    }) {
                    Icon(
                        imageVector = Icons.Filled.KeyboardArrowUp,
                        contentDescription = "Previous",
                        tint = iconTint
                    )
                }
            else
                Spacer(
                    modifier = Modifier
                        .size(48.dp)
                        .background(Color.Transparent)
                )
            Spacer(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
            )
            IconButton(
                modifier = background,
                onClick = {
                    //Rotate
                    verticalVueReaderState.rotate(-90f)
                }) {
                Icon(
                    imageVector = Icons.Filled.RotateLeft,
                    contentDescription = "Rotate Left",
                    tint = iconTint
                )
            }
            Spacer(modifier = Modifier.width(5.dp))
            IconButton(
                modifier = background,
                onClick = {
                    //Rotate
                    verticalVueReaderState.rotate(90f)
                }) {
                Icon(
                    imageVector = Icons.Filled.RotateRight,
                    contentDescription = "Rotate Right",
                    tint = iconTint
                )
            }
            Spacer(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
            )
            if (showNext)
                IconButton(
                    modifier = background,
                    onClick = {
                        //Next
                        scope.launch {
                            verticalVueReaderState.nextPage()
                        }
                    }) {
                    Icon(
                        imageVector = Icons.Filled.KeyboardArrowDown,
                        contentDescription = "Next",
                        tint = iconTint
                    )
                }
            else
                Spacer(
                    modifier = Modifier
                        .size(48.dp)
                        .background(Color.Transparent)
                )
        }
    }
}