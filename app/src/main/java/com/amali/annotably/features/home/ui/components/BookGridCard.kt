package com.amali.annotably.features.home.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MaterialTheme.typography
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImagePainter
import coil.compose.SubcomposeAsyncImage
import coil.compose.SubcomposeAsyncImageContent
import coil.compose.rememberAsyncImagePainter
import coil.request.ImageRequest
import com.amali.annotably.data.model.Book

@Composable
fun BookGridCard(
    book: Book,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxWidth()
    ) {
        BookCoverImageGrid(book = book)
        Text(
            text = book.title ?: "Unknown Title",
            style = typography.titleSmall,
            fontWeight = FontWeight.Bold,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.padding(top = 8.dp),
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}

@Composable
private fun BookCoverImageGrid(book: Book) {
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
        modifier = Modifier
            .fillMaxWidth()
            .height(140.dp)
            .clip(RoundedCornerShape(8.dp)),
        contentScale = ContentScale.Crop
    ) {
        when (painter.state) {
            is AsyncImagePainter.State.Loading -> {
                Image(
                    painter = placeholderPainter,
                    contentDescription = null,
                    modifier = Modifier.fillMaxWidth().height(140.dp).blur(12.dp),
                    contentScale = ContentScale.Crop
                )
            }
            is AsyncImagePainter.State.Error -> {
                Box(
                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .height(140.dp)
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
