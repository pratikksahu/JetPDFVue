package com.pratikk.jetpackpdf.horizontalSamples

import android.content.res.Configuration
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.RotateLeft
import androidx.compose.material.icons.filled.RotateRight
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import com.pratikk.jetpdfvue.HorizontalVueReader
import com.pratikk.jetpdfvue.VueHorizontalSlider
import com.pratikk.jetpdfvue.state.HorizontalVueReaderState
import com.pratikk.jetpdfvue.util.reduceSize
import kotlinx.coroutines.launch

@Composable
fun HorizontalSampleB(
    modifier: Modifier = Modifier,
    horizontalVueReaderState: HorizontalVueReaderState,
    import: () -> Unit
) {
    BoxWithConstraints(
        modifier = modifier
    ) {
        val background = Modifier.background(MaterialTheme.colorScheme.background.copy(alpha = 0.75f),MaterialTheme.shapes.small)
            .border(width = 1.dp, color = MaterialTheme.colorScheme.outline, shape = MaterialTheme.shapes.small)
            .clip(MaterialTheme.shapes.small)
        val iconTint = MaterialTheme.colorScheme.onBackground

        HorizontalVueReader(
            modifier = Modifier.fillMaxSize(),
            contentModifier = Modifier.fillMaxSize(),
            horizontalVueReaderState = horizontalVueReaderState
        )
        Row(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(horizontal = 8.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "${horizontalVueReaderState.currentPage} of ${horizontalVueReaderState.pdfPageCount}",
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
                        horizontalVueReaderState.sharePDF(context)
                    }) {
                    Icon(
                        imageVector = Icons.Filled.Share,
                        contentDescription = "Share",
                        tint = iconTint
                    )
                }
            }
        }
        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(horizontal = 8.dp, vertical = 12.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Spacer(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                )
                IconButton(
                    modifier = background,
                    onClick = {
                        //Rotate
                        horizontalVueReaderState.rotate(-90f)
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
                        horizontalVueReaderState.rotate(90f)
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
            }
            VueHorizontalSlider(
                modifier = Modifier
                    .padding(horizontal = 10.dp, vertical = 10.dp)
                    .fillMaxWidth()
                    .height(40.dp),
                horizontalVueReaderState = horizontalVueReaderState
            )
        }
    }
}