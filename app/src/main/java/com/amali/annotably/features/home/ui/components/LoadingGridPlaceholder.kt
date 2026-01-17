package com.amali.annotably.features.home.ui.components

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.unit.dp

@Composable
fun LoadingGridPlaceholder(
    itemCount: Int = 9,
    contentPadding: PaddingValues = PaddingValues(0.dp)
) {
    val shimmerBrush = rememberShimmerBrush()
    LazyVerticalGrid(
        columns = GridCells.Fixed(3),
        contentPadding = contentPadding,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier.padding(16.dp)
    ) {
        items(List(itemCount) { it }) {
            BookGridPlaceholderItem(shimmerBrush)
        }
    }
}

@Composable
private fun rememberShimmerBrush(): Brush {
    val gradientColors =
        listOf(
            MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
            MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.2f),
            MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)
        )
    val transition = rememberInfiniteTransition(label = "shimmer")
    val xShimmer by
        transition.animateFloat(
            initialValue = 0f,
            targetValue = 800f,
            animationSpec =
                infiniteRepeatable(
                    animation = tween(durationMillis = 1200, easing = LinearEasing)
                ),
            label = "shimmerOffset"
        )
    return Brush.linearGradient(
        colors = gradientColors,
        start = Offset(xShimmer - 200f, 0f),
        end = Offset(xShimmer, 0f)
    )
}

@Composable
private fun BookGridPlaceholderItem(brush: Brush) {
    Column(modifier = Modifier.fillMaxWidth()) {
        // Cover image placeholder
        Box(
            modifier =
                Modifier.fillMaxWidth()
                    .height(140.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(brush)
        )
        // Title placeholder
        Box(
            modifier =
                Modifier.fillMaxWidth()
                    .height(16.dp)
                    .padding(top = 8.dp)
                    .clip(RoundedCornerShape(4.dp))
                    .background(brush)
        )
    }
}
