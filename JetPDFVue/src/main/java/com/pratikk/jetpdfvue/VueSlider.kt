package com.pratikk.jetpdfvue

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.SliderPositions
import androidx.compose.material3.SliderState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.pratikk.jetpdfvue.state.HorizontalVueReaderState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VueHorizontalSlider(
    modifier: Modifier = Modifier,
    horizontalVueReaderState: HorizontalVueReaderState,
    onValueChange: (Float) -> Unit = {},
    onValueChangeFinished: () -> Unit = {},
    gap: Int = 1,
    showIndicator: Boolean = false,
    showLabel: Boolean = false,
    enabled: Boolean = true,
    thumb: @Composable (thumbValue: Int) -> Unit = {
        CustomSliderDefaults.Thumb(it.toString())
    },
    track: @Composable (sliderState: SliderState) -> Unit = { sliderState ->
        CustomSliderDefaults.Track(sliderState = sliderState)
    },
    indicator: @Composable (indicatorValue: Int) -> Unit = { indicatorValue ->
        CustomSliderDefaults.Indicator(indicatorValue = indicatorValue.toString())
    },
    label: @Composable (labelValue: Int) -> Unit = { labelValue ->
        CustomSliderDefaults.Label(labelValue = labelValue.toString())
    },
) {
    var sliderState by rememberSaveable {
        mutableStateOf(0f)
    }
    val moveToPage by remember(sliderState) {
        derivedStateOf {
            sliderState.toInt()
        }
    }
    LaunchedEffect(key1 = moveToPage, block = {
        horizontalVueReaderState.pagerState.animateScrollToPage(moveToPage)
    })

    CustomSlider(
        modifier = modifier,
        currentPage = { horizontalVueReaderState.currentPage },
        value = sliderState,
        onValueChange = {
            sliderState = it
            onValueChange(it)
        },
        gap = gap,
        showIndicator = showIndicator,
        showLabel = showLabel,
        enabled = enabled,
        onValueChangeFinished = onValueChangeFinished,
        valueRange = 0F..horizontalVueReaderState.pdfPageCount.toFloat(),
        thumb = thumb,
        track = track,
        indicator = indicator,
        label = label
    )
}
