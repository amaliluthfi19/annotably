package com.amali.annotably.components

import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AnnotablyTopBar(
    title: String,
    textStyle: TextStyle = MaterialTheme.typography.titleLarge,
    fontWeight: FontWeight? = null,
    navigationIcon: @Composable (() -> Unit)? = null,
    actions: @Composable RowScope?.() -> Unit = {}
) {
    val resolvedStyle = fontWeight?.let { textStyle.copy(fontWeight = it) } ?: textStyle
    CenterAlignedTopAppBar(
        title = { Text(text = title, style = resolvedStyle) },
        navigationIcon = navigationIcon ?: {},
        windowInsets = WindowInsets.safeDrawing,
        colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
            containerColor = MaterialTheme.colorScheme.onPrimary,
            titleContentColor = MaterialTheme.colorScheme.primary
        ),
        actions = actions
    )
}