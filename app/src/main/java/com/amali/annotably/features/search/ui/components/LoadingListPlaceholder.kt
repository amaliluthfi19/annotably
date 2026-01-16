package com.amali.annotably.features.search.ui.components

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.unit.dp

@Composable
fun LoadingListPlaceholder(itemCount: Int = 6) {
    val shimmerBrush = rememberShimmerBrush()
    LazyColumn { items(itemCount) { BookPlaceholderItem(shimmerBrush) } }
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
private fun BookPlaceholderItem(brush: Brush) {
    Row(modifier = Modifier.fillMaxWidth().padding(16.dp), verticalAlignment = Alignment.Top) {
        Box(
            modifier =
                Modifier.size(80.dp, 120.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(brush)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Column(verticalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.weight(1f)) {
            Box(
                modifier =
                    Modifier.fillMaxWidth(0.8f)
                        .height(20.dp)
                        .clip(RoundedCornerShape(6.dp))
                        .background(brush)
            )
            Box(
                modifier =
                    Modifier.fillMaxWidth()
                        .height(16.dp)
                        .clip(RoundedCornerShape(6.dp))
                        .background(brush)
            )
            Box(
                modifier =
                    Modifier.fillMaxWidth(0.5f)
                        .height(14.dp)
                        .clip(RoundedCornerShape(6.dp))
                        .background(brush)
            )
        }
    }
}