package com.amali.annotably.features.home.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MaterialTheme.typography
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.amali.annotably.R
import com.amali.annotably.components.AnnotablyTopBar
import com.amali.annotably.features.home.ui.components.BookGridCard
import com.amali.annotably.features.home.ui.components.LoadingGridPlaceholder
import com.amali.annotably.features.home.view_model.HomeUiState
import com.amali.annotably.features.home.view_model.HomeViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
        onSearchClick: () -> Unit,
        modifier: Modifier = Modifier,
        viewModel: HomeViewModel = hiltViewModel()
) {
    val homeState by viewModel.homeState.collectAsState()
    val books by viewModel.books.collectAsState()
    val isRefreshing by viewModel.isRefreshing.collectAsState()

    val pullToRefreshState = rememberPullToRefreshState()

    Scaffold(
            topBar = {
                AnnotablyTopBar(
                        title = stringResource(R.string.app_name),
                        textStyle = MaterialTheme.typography.displaySmall,
                        fontWeight = FontWeight.Bold,
                        actions = {
                            IconButton(onClick = onSearchClick) {
                                Icon(
                                        imageVector = Icons.Rounded.Search,
                                        contentDescription = stringResource(R.string.search),
                                        modifier = Modifier.size(24.dp)
                                )
                            }
                        }
                )
            },
            containerColor = MaterialTheme.colorScheme.onPrimary,
            modifier = modifier.fillMaxSize()
    ) { innerPadding ->
        PullToRefreshBox(
                isRefreshing = isRefreshing,
                onRefresh = { viewModel.refreshBooks() },
                state = pullToRefreshState
        ) {
            when (homeState) {
                is HomeUiState.Loading -> {
                    LoadingGridPlaceholder(itemCount = 9, contentPadding = innerPadding)
                }
                is HomeUiState.Success -> {
                    LazyVerticalGrid(
                            columns = GridCells.Fixed(3),
                            contentPadding = innerPadding,
                            horizontalArrangement = Arrangement.spacedBy(16.dp),
                            verticalArrangement = Arrangement.spacedBy(16.dp),
                            modifier = Modifier.padding(16.dp)
                    ) {
                        items(books) { book ->
                            BookGridCard(book = book, modifier = Modifier.fillMaxWidth())
                        }
                    }
                }
                is HomeUiState.Empty -> {
                    Box(
                            modifier = Modifier.fillMaxSize().padding(innerPadding),
                            contentAlignment = Alignment.Center
                    ) {
                        Image(
                                painter = painterResource(R.drawable.empty_state_illustration),
                                contentDescription = "Empty state illustration",
                                modifier = Modifier.size(240.dp)
                        )
                    }
                }
                is HomeUiState.Error -> {
                    Box(
                            modifier = Modifier.fillMaxSize().padding(innerPadding),
                            contentAlignment = Alignment.Center
                    ) {
                        Text(
                                text = (homeState as HomeUiState.Error).message,
                                style = typography.bodyLarge,
                                color = MaterialTheme.colorScheme.error
                        )
                    }
                }
            }
        }
    }
}
