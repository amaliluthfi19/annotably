package com.amali.annotably.features.search.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MaterialTheme.typography
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImagePainter
import coil.compose.SubcomposeAsyncImage
import coil.compose.SubcomposeAsyncImageContent
import coil.compose.rememberAsyncImagePainter
import coil.request.ImageRequest
import com.amali.annotably.data.model.Book

@Composable
fun BookItem(
    book: Book,
    onAddBook: () -> Unit,
    modifier: Modifier = Modifier,
    isLoading: Boolean = false
) {
    Row(
        modifier = modifier.fillMaxWidth().padding(16.dp).clip(RoundedCornerShape(8.dp)),
        verticalAlignment = Alignment.Top,
    ) {
        BookCoverImage(book = book)
        Spacer(modifier = Modifier.width(8.dp))
        Column(verticalArrangement = Arrangement.SpaceBetween, modifier = Modifier.weight(1f)) {
            Column {
                Text(
                    text = book.title ?: "Unknown Title",
                    style = typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = book.getFormattedAuthors(),
                    style = typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            book.firstPublish?.let { year ->
                Text(
                    text = "First published: $year",
                    style = typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
        Spacer(modifier = Modifier.width(8.dp))
        IconButton(onClick = onAddBook) {
            if (isLoading) {
                CircularProgressIndicator(modifier = Modifier.size(24.dp), strokeWidth = 4.dp)
            } else {
                Icon(
                    imageVector = Icons.Rounded.Add,
                    contentDescription = "Add book",
                    tint = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}

@Composable
private fun BookCoverImage(book: Book) {
    val context = LocalContext.current

    // Load small blurred version as placeholder
    val placeholderPainter =
        rememberAsyncImagePainter(
            model =
                ImageRequest.Builder(context)
                    .data(book.getCoverImageUrl("S"))
                    .crossfade(false)
                    .build()
        )

    SubcomposeAsyncImage(
        model =
            ImageRequest.Builder(context)
                .data(book.getCoverImageUrl("M"))
                .crossfade(300)
                .build(),
        contentDescription = book.title,
        modifier = Modifier.size(80.dp, 120.dp).clip(RoundedCornerShape(8.dp)),
        contentScale = ContentScale.Crop
    ) {
        when (painter.state) {
            is AsyncImagePainter.State.Loading -> {
                Image(
                    painter = placeholderPainter,
                    contentDescription = null,
                    modifier = Modifier.fillMaxSize().blur(12.dp),
                    contentScale = ContentScale.Crop
                )
            }
            is AsyncImagePainter.State.Error -> {
                Box(
                    modifier =
                        Modifier.fillMaxSize()
                            .background(MaterialTheme.colorScheme.surface),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        painter = painterResource(android.R.drawable.ic_menu_report_image),
                        contentDescription = null,
                        modifier = Modifier.size(36.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            else -> {
                SubcomposeAsyncImageContent()
            }
        }
    }
}



