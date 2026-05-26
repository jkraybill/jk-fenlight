package com.fenlight.companion.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlaylistAdd
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.fenlight.companion.ui.lists.ListManagementViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ListManagementSheet(
    mediaId: Int,
    mediaType: String,
    title: String,
    posterUrl: String?,
    onDismiss: () -> Unit,
    vm: ListManagementViewModel = viewModel(),
) {
    val state by vm.state.collectAsStateWithLifecycle()
    var showTraktListPicker by remember { mutableStateOf(false) }
    var showTmdbListPicker by remember { mutableStateOf(false) }

    LaunchedEffect(state.actionMessage) {
        if (state.actionMessage != null) vm.clearActionMessage()
    }

    if (showTraktListPicker) {
        ModalBottomSheet(onDismissRequest = { showTraktListPicker = false }) {
            Text(
                "Add to Trakt List",
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
            )
            LazyColumn(contentPadding = PaddingValues(bottom = 16.dp)) {
                items(state.traktLists) { list ->
                    ListItem(
                        headlineContent = { Text(list.name) },
                        supportingContent = { Text("${list.itemCount} items") },
                        modifier = Modifier.clickable {
                            vm.addToTraktList(mediaId, mediaType, list.slug)
                            showTraktListPicker = false
                            onDismiss()
                        },
                    )
                    HorizontalDivider()
                }
                if (state.traktLists.isEmpty()) {
                    item {
                        Text(
                            "No lists found",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(16.dp),
                        )
                    }
                }
            }
        }
        return
    }

    if (showTmdbListPicker) {
        ModalBottomSheet(onDismissRequest = { showTmdbListPicker = false }) {
            Text(
                "Add to TMDB List",
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
            )
            LazyColumn(contentPadding = PaddingValues(bottom = 16.dp)) {
                items(state.tmdbLists) { list ->
                    ListItem(
                        headlineContent = { Text(list.name) },
                        supportingContent = { Text("${list.itemCount} items") },
                        modifier = Modifier.clickable {
                            vm.addToTmdbList(mediaId, mediaType, list.id)
                            showTmdbListPicker = false
                            onDismiss()
                        },
                    )
                    HorizontalDivider()
                }
                if (state.tmdbLists.isEmpty()) {
                    item {
                        Text(
                            "No lists found",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(16.dp),
                        )
                    }
                }
            }
        }
        return
    }

    ModalBottomSheet(onDismissRequest = onDismiss) {
        Column(modifier = Modifier.padding(bottom = 24.dp)) {
            // Header row
            ListItem(
                headlineContent = { Text(title, style = MaterialTheme.typography.titleMedium) },
                leadingContent = {
                    AsyncImage(
                        model = posterUrl,
                        contentDescription = null,
                        modifier = Modifier
                            .size(56.dp)
                            .clip(RoundedCornerShape(4.dp)),
                        contentScale = ContentScale.Crop,
                    )
                },
            )
            HorizontalDivider()

            // Trakt section
            Text(
                "Trakt",
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
            )

            val isWatchlisted = mediaId in state.watchlistedIds
            ListItem(
                headlineContent = { Text(if (isWatchlisted) "Remove from Watchlist" else "Add to Watchlist") },
                leadingContent = {
                    Icon(
                        if (isWatchlisted) Icons.Default.Star else Icons.Outlined.Star,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                    )
                },
                modifier = Modifier.clickable {
                    if (isWatchlisted) vm.removeFromTraktWatchlist(mediaId, mediaType)
                    else vm.addToTraktWatchlist(mediaId, mediaType)
                    onDismiss()
                },
            )
            ListItem(
                headlineContent = { Text("Add to Trakt List…") },
                leadingContent = { Icon(Icons.Default.PlaylistAdd, contentDescription = null) },
                modifier = Modifier.clickable {
                    vm.loadTraktLists()
                    showTraktListPicker = true
                },
            )

            HorizontalDivider()

            // TMDB section
            Text(
                "TMDB",
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
            )
            ListItem(
                headlineContent = { Text("Add to TMDB List…") },
                leadingContent = { Icon(Icons.Default.PlaylistAdd, contentDescription = null) },
                modifier = Modifier.clickable {
                    vm.loadTmdbLists()
                    showTmdbListPicker = true
                },
            )
        }
    }
}
