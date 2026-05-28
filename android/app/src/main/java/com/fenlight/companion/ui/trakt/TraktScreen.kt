package com.fenlight.companion.ui.trakt

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.*
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.fenlight.companion.data.model.TraktList
import com.fenlight.companion.data.model.TraktListItem
import com.fenlight.companion.data.model.TraktWatchedShow
import com.fenlight.companion.ui.components.ErrorMessage
import com.fenlight.companion.ui.components.LoadingIndicator

private fun placeholderColor(title: String): Color {
    val colors = listOf(
        Color(0xFF1C2D3E), Color(0xFF2E1C1C), Color(0xFF1E2040),
        Color(0xFF1C2E28), Color(0xFF2A1C2E), Color(0xFF1A2C2C),
    )
    return colors[Math.abs(title.hashCode()) % colors.size]
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TraktScreen(vm: TraktViewModel = viewModel()) {
    val state by vm.state.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(state.playMessage) {
        state.playMessage?.let { snackbarHostState.showSnackbar(it); vm.clearPlayMessage() }
    }

    Scaffold(snackbarHost = { SnackbarHost(snackbarHostState) }) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding)) {
            if (state.listItems.isNotEmpty() || state.selectedListName.isNotEmpty()) {
                // Show list items with pagination
                ListItemsScreen(
                    listName = state.selectedListName,
                    items = state.listItems,
                    isLoading = state.isLoading,
                    isRefreshing = state.isRefreshing,
                    isLoadingMore = state.listItemIsLoadingMore,
                    hasMore = state.listItemHasMore,
                    onLoadMore = vm::loadMoreListItems,
                    onRefresh = vm::refresh,
                    onBack = vm::clearListItems,
                    onPlayMovie = vm::playListMovie,
                )
                return@Column
            }

            TabRow(selectedTabIndex = state.tab.ordinal) {
                listOf("Continue Watching", "My Lists", "Liked Lists").forEachIndexed { i, label ->
                    Tab(
                        selected = state.tab.ordinal == i,
                        onClick = { vm.selectTab(TraktTab.values()[i]) },
                        text = { Text(label) },
                    )
                }
            }

            if (state.isLoading) {
                LoadingIndicator(modifier = Modifier.padding(32.dp))
                return@Column
            }

            state.error?.let {
                ErrorMessage(it, onRetry = { vm.selectTab(state.tab) }, modifier = Modifier.padding(16.dp))
                return@Column
            }

            PullToRefreshBox(
                isRefreshing = state.isRefreshing,
                onRefresh = vm::refresh,
                modifier = Modifier.fillMaxSize(),
            ) {
                when (state.tab) {
                    TraktTab.CONTINUE_WATCHING -> ContinueWatchingList(state.watchedShows, vm::playNextEpisode)
                    TraktTab.MY_LISTS -> TraktListList(state.myLists) { list ->
                        vm.loadListItems(list.slug, list.name, "me")
                    }
                    TraktTab.LIKED_LISTS -> TraktListList(state.likedLists) { list ->
                        val user = list.user?.username ?: "me"
                        vm.loadListItems(list.slug, list.name, user)
                    }
                }
            }
        }
    }
}

@Composable
private fun ContinueWatchingList(
    shows: List<TraktWatchedShow>,
    onPlay: (TraktWatchedShow) -> Unit,
) {
    if (shows.isEmpty()) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("No recently watched shows", color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        return
    }
    LazyColumn(modifier = Modifier.fillMaxSize(), contentPadding = PaddingValues(8.dp)) {
        items(shows) { watched ->
            val next = watched.nextEpisode()
            val initials = watched.show.title
                .split(' ')
                .take(2)
                .mapNotNull { it.firstOrNull()?.uppercaseChar() }
                .joinToString("")
            val progress = ((watched.plays % 20) / 20f).coerceIn(0f, 1f)

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                shape = RoundedCornerShape(10.dp),
            ) {
                Row(
                    modifier = Modifier.padding(10.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalAlignment = Alignment.Top,
                ) {
                    // Poster placeholder
                    Box(
                        modifier = Modifier
                            .width(46.dp)
                            .height(68.dp)
                            .clip(RoundedCornerShape(6.dp))
                            .background(placeholderColor(watched.show.title)),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(
                            text = initials,
                            style = MaterialTheme.typography.labelSmall,
                            color = Color.White.copy(alpha = 0.7f),
                        )
                    }

                    Column(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(4.dp),
                    ) {
                        Text(
                            text = watched.show.title,
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.SemiBold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                        if (next != null) {
                            Text(
                                text = "Next · S${next.first}E${next.second}",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        } else {
                            Text(
                                text = "${watched.plays} episodes watched",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                        LinearProgressIndicator(
                            progress = { progress },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(2.dp)
                                .clip(RoundedCornerShape(1.dp)),
                        )
                    }

                    // Play button
                    FilledIconButton(
                        onClick = { onPlay(watched) },
                        modifier = Modifier.size(28.dp),
                    ) {
                        Icon(
                            Icons.Default.PlayArrow,
                            contentDescription = "Play",
                            modifier = Modifier.size(16.dp),
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun TraktListList(
    lists: List<TraktList>,
    onListClick: (TraktList) -> Unit,
) {
    if (lists.isEmpty()) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("No lists found", color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        return
    }
    LazyColumn(modifier = Modifier.fillMaxSize(), contentPadding = PaddingValues(8.dp)) {
        items(lists) { list ->
            Card(
                modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                onClick = { onListClick(list) },
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text(list.name, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
                    if (list.description.isNotBlank()) {
                        Text(list.description, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 2)
                    }
                    Text("${list.itemCount} items · ♥ ${list.likes}", style = MaterialTheme.typography.labelSmall)
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ListItemsScreen(
    listName: String,
    items: List<TraktListItem>,
    isLoading: Boolean,
    isRefreshing: Boolean,
    isLoadingMore: Boolean,
    hasMore: Boolean,
    onLoadMore: () -> Unit,
    onRefresh: () -> Unit,
    onBack: () -> Unit,
    onPlayMovie: (TraktListItem) -> Unit,
) {
    val listState = rememberLazyListState()
    val shouldLoadMore by remember {
        derivedStateOf {
            val last = listState.layoutInfo.visibleItemsInfo.lastOrNull()?.index ?: 0
            last >= items.size - 5 && !isLoadingMore && hasMore
        }
    }
    LaunchedEffect(shouldLoadMore) { if (shouldLoadMore) onLoadMore() }

    Column(modifier = Modifier.fillMaxSize()) {
        TopAppBar(
            title = { Text(listName) },
            navigationIcon = {
                IconButton(onClick = onBack) { Icon(Icons.Default.ArrowBack, contentDescription = "Back") }
            },
        )
        if (isLoading && items.isEmpty()) {
            LoadingIndicator(modifier = Modifier.padding(32.dp))
            return@Column
        }
        PullToRefreshBox(
            isRefreshing = isRefreshing,
            onRefresh = onRefresh,
            modifier = Modifier.fillMaxSize(),
        ) {
            LazyColumn(state = listState, modifier = Modifier.fillMaxSize(), contentPadding = PaddingValues(8.dp)) {
                items(items) { item ->
                    val title = item.movie?.title ?: item.show?.title ?: "Unknown"
                    val year = (item.movie?.year ?: item.show?.year)?.toString() ?: ""
                    ListItem(
                        headlineContent = { Text(title) },
                        supportingContent = { Text(year + if (item.type.isNotBlank()) " · ${item.type}" else "") },
                        trailingContent = {
                            if (item.type == "movie" && item.movie?.ids?.tmdb != null) {
                                IconButton(onClick = { onPlayMovie(item) }) {
                                    Icon(Icons.Default.PlayArrow, contentDescription = "Play", tint = MaterialTheme.colorScheme.primary)
                                }
                            }
                        },
                    )
                    HorizontalDivider()
                }
                if (isLoadingMore) {
                    item {
                        LoadingIndicator(modifier = Modifier.padding(16.dp))
                    }
                }
            }
        }
    }
}
