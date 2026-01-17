package com.amali.annotably.features.search.ui.components

import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.rounded.Clear
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import com.amali.annotably.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchTopBar(
        query: String,
        onQueryChange: (String) -> Unit,
        onSearch: () -> Unit,
        onBackClick: () -> Unit,
        onClear: () -> Unit
) {
    TopAppBar(
            title = {
                OutlinedTextField(
                        textStyle = MaterialTheme.typography.bodyMedium,
                        value = query,
                        onValueChange = onQueryChange,
                        modifier = Modifier.fillMaxWidth().padding(end = 8.dp),
                        shape = RoundedCornerShape(24.dp),
                        placeholder = { Text(text = stringResource(R.string.search)) },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                        keyboardActions = KeyboardActions(onSearch = { onSearch() }),
                        colors =
                                TextFieldDefaults.colors(
                                        unfocusedContainerColor =
                                                MaterialTheme.colorScheme.surfaceVariant,
                                        focusedContainerColor =
                                                MaterialTheme.colorScheme.background,
                                        unfocusedIndicatorColor = Color.Transparent,
                                        focusedIndicatorColor = Color.Transparent,
                                        disabledIndicatorColor = Color.Transparent,
                                        errorIndicatorColor = Color.Transparent
                                ),
                        trailingIcon = {
                            if (query.isNotEmpty()) {
                                IconButton(onClick = onClear) {
                                    Icon(
                                            imageVector = Icons.Filled.Clear,
                                            contentDescription = stringResource(R.string.clear),
                                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                )
            },
            navigationIcon = {
                IconButton(onClick = onBackClick) {
                    Icon(
                            imageVector = Icons.AutoMirrored.Rounded.ArrowBack,
                            contentDescription = stringResource(R.string.back),
                            tint = MaterialTheme.colorScheme.onPrimary
                    )
                }
            },
            windowInsets = WindowInsets.safeDrawing,
            colors =
                    TopAppBarDefaults.topAppBarColors(
                            containerColor = MaterialTheme.colorScheme.primary,
                            titleContentColor = MaterialTheme.colorScheme.onPrimary,
                            navigationIconContentColor = MaterialTheme.colorScheme.onPrimary
                    )
    )
}
