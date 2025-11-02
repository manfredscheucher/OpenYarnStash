package org.example.project.ui.widgets

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import kotlin.math.max
import kotlin.math.min

@Composable
fun LazyColumnWithScrollbar(
    modifier: Modifier = Modifier,
    state: LazyListState = rememberLazyListState(),
    contentPadding: PaddingValues = PaddingValues(0.dp),
    verticalArrangement: Arrangement.Vertical = Arrangement.Top,
    userScrollEnabled: Boolean = true,
    scrollbarWidth: Dp = 6.dp,
    content: LazyListScope.() -> Unit
) {
    Box(modifier) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            state = state,
            contentPadding = contentPadding,
            verticalArrangement = verticalArrangement,
            userScrollEnabled = userScrollEnabled,
            content = content
        )
        VerticalScrollbarLazy(
            modifier = Modifier
                .align(Alignment.CenterEnd)
                .fillMaxHeight()
                .width(scrollbarWidth)
                .padding(vertical = 8.dp, horizontal = 2.dp),
            state = state
        )
    }
}

@Composable
fun ColumnWithScrollbar(
    modifier: Modifier = Modifier,
    scrollState: androidx.compose.foundation.ScrollState = rememberScrollState(),
    scrollbarWidth: Dp = 6.dp,
    content: @Composable ColumnScope.() -> Unit
) {
    Box(modifier) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState),
            content = content
        )
        VerticalScrollbarScroll(
            modifier = Modifier
                .align(Alignment.CenterEnd)
                .fillMaxHeight()
                .width(scrollbarWidth)
                .padding(vertical = 8.dp, horizontal = 2.dp),
            scrollState = scrollState
        )
    }
}

@Composable
private fun VerticalScrollbarLazy(
    modifier: Modifier = Modifier,
    state: LazyListState,
    trackColor: Color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
    thumbColor: Color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.45f),
    minThumbSize: Dp = 32.dp
) {
    val density = LocalDensity.current
    val layoutInfo by remember { derivedStateOf { state.layoutInfo } }

    val (contentHeightPx, viewportHeightPx, scrollOffsetPx) by remember {
        derivedStateOf {
            val viewport = (layoutInfo.viewportEndOffset - layoutInfo.viewportStartOffset).coerceAtLeast(1)
            val first = layoutInfo.visibleItemsInfo.firstOrNull()
            val avgItemSize = layoutInfo.visibleItemsInfo
                .takeIf { it.isNotEmpty() }?.map { it.size }?.average()?.toInt() ?: viewport
            val totalItems = max(layoutInfo.totalItemsCount, layoutInfo.visibleItemsInfo.size)
            val estimatedContent = max(avgItemSize * totalItems, viewport)
            val absoluteOffset = (first?.let { it.index * avgItemSize + it.offset } ?: 0)
            Triple(estimatedContent, viewport, absoluteOffset)
        }
    }

    val minThumbPx = with(density) { minThumbSize.toPx() }

    Canvas(
        modifier = modifier
            .background(Color.Transparent)
            .pointerInput(state) {
                detectDragGestures { change, drag ->
                    change.consume()
                    val proportion = drag.y / viewportHeightPx
                    val delta = (proportion * viewportHeightPx).toInt()
                    state.scrollBy(delta.toFloat())
                }
            }
    ) {
        val trackLeft = size.width / 2f
        val trackTop = 0f
        val trackBottom = size.height
        val trackHeight = trackBottom - trackTop

        val thumbHeightPx = max(trackHeight * (viewportHeightPx.toFloat() / contentHeightPx.toFloat()), minThumbPx)
        val maxThumbTop = trackHeight - thumbHeightPx
        val progress = scrollOffsetPx.toFloat() / (contentHeightPx - viewportHeightPx).coerceAtLeast(1)
        val thumbTop = max(0f, min(maxThumbTop, maxThumbTop * progress))

        drawLine(trackColor, Offset(trackLeft, trackTop), Offset(trackLeft, trackBottom), size.width)
        drawLine(thumbColor, Offset(trackLeft, thumbTop), Offset(trackLeft, thumbTop + thumbHeightPx), size.width)
    }
}

@Composable
private fun VerticalScrollbarScroll(
    modifier: Modifier = Modifier,
    scrollState: androidx.compose.foundation.ScrollState,
    trackColor: Color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
    thumbColor: Color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.45f),
    minThumbSize: Dp = 32.dp
) {
    val density = LocalDensity.current
    val minThumbPx = with(density) { minThumbSize.toPx() }
    val progress by remember {
        derivedStateOf {
            val maxValue = scrollState.maxValue.coerceAtLeast(1)
            scrollState.value.toFloat() / maxValue
        }
    }

    Canvas(
        modifier = modifier
            .background(Color.Transparent)
            .pointerInput(scrollState) {
                detectDragGestures { change, drag ->
                    change.consume()
                    val proportion = drag.y / size.height
                    val delta = (proportion * scrollState.maxValue).toInt()
                    scrollState.scrollTo((scrollState.value + delta).coerceIn(0, scrollState.maxValue))
                }
            }
    ) {
        val trackLeft = size.width / 2f
        val trackTop = 0f
        val trackBottom = size.height
        val trackHeight = trackBottom - trackTop

        val thumbHeightPx = max(trackHeight * 0.15f, minThumbPx)
        val maxThumbTop = trackHeight - thumbHeightPx
        val thumbTop = max(0f, min(maxThumbTop, maxThumbTop * progress))

        drawLine(trackColor, Offset(trackLeft, trackTop), Offset(trackLeft, trackBottom), size.width)
        drawLine(thumbColor, Offset(trackLeft, thumbTop), Offset(trackLeft, thumbTop + thumbHeightPx), size.width)
    }
}