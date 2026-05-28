package com.fenlight.companion.ui.home

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import coil.compose.AsyncImage
import com.fenlight.companion.ui.movies.MovieBrowseScreen
import com.fenlight.companion.ui.movies.MovieDetailScreen
import com.fenlight.companion.ui.realdebrid.RdScreen
import com.fenlight.companion.ui.tmdb.TmdbListsScreen
import com.fenlight.companion.ui.trakt.TraktScreen
import com.fenlight.companion.ui.tvshows.TvBrowseScreen
import com.fenlight.companion.ui.tvshows.TvDetailScreen

private sealed class TopDest(val route: String, val label: String, val iconUrl: String) {
    object Movies : TopDest("movies", "Movies", "https://i.imgur.com/pmyCOx7.png")
    object TV : TopDest("tv", "TV Shows", "https://i.imgur.com/R3NEEJl.png")
    object TmdbLists : TopDest("tmdb_lists", "TMDB Lists", "https://i.imgur.com/bOqItvH.png")
    object Trakt : TopDest("trakt", "Trakt", "https://i.imgur.com/sGq3ifV.png")
    object RealDebrid : TopDest("rd", "Real Debrid", "https://i.imgur.com/DotYAc3.png")
}

@Composable
fun HomeScreen(
    hasTmdbAuth: Boolean,
    hasTraktAuth: Boolean,
    hasRdAuth: Boolean,
    onGoToSettings: () -> Unit,
) {
    val navController = rememberNavController()
    val topDests = buildList {
        add(TopDest.Movies)
        add(TopDest.TV)
        if (hasTmdbAuth) add(TopDest.TmdbLists)
        if (hasTraktAuth) add(TopDest.Trakt)
        if (hasRdAuth) add(TopDest.RealDebrid)
    }

    Scaffold(
        topBar = {
            @OptIn(ExperimentalMaterial3Api::class)
            TopAppBar(
                title = { Text("FenLight+ Companion") },
                actions = {
                    IconButton(onClick = onGoToSettings) {
                        Icon(Icons.Default.Settings, contentDescription = "Settings")
                    }
                },
            )
        },
        bottomBar = {
            NavigationBar {
                val navBackStackEntry by navController.currentBackStackEntryAsState()
                val currentDest = navBackStackEntry?.destination
                topDests.forEach { dest ->
                    NavigationBarItem(
                        icon = {
                            AsyncImage(
                                model = dest.iconUrl,
                                contentDescription = dest.label,
                                modifier = Modifier.size(24.dp),
                            )
                        },
                        label = { Text(dest.label) },
                        selected = currentDest?.hierarchy?.any { it.route == dest.route } == true,
                        onClick = {
                            navController.navigate(dest.route) {
                                popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                                launchSingleTop = true
                                restoreState = true
                            }
                        },
                    )
                }
            }
        },
    ) { padding ->
        NavHost(
            navController = navController,
            startDestination = "movies",
            modifier = Modifier.fillMaxSize().padding(padding),
        ) {
            composable("movies") {
                MovieBrowseScreen(onMovieClick = { id -> navController.navigate("movie_detail/$id") })
            }
            composable("movie_detail/{id}") { back ->
                val id = back.arguments?.getString("id")?.toIntOrNull() ?: return@composable
                MovieDetailScreen(tmdbId = id, onBack = { navController.popBackStack() })
            }
            composable("tv") {
                TvBrowseScreen(onShowClick = { id -> navController.navigate("tv_detail/$id") })
            }
            composable("tv_detail/{id}") { back ->
                val id = back.arguments?.getString("id")?.toIntOrNull() ?: return@composable
                TvDetailScreen(tmdbId = id, onBack = { navController.popBackStack() })
            }
            composable("tmdb_lists") {
                TmdbListsScreen(
                    onMovieClick = { id -> navController.navigate("movie_detail/$id") },
                    onShowClick = { id -> navController.navigate("tv_detail/$id") },
                )
            }
            composable("trakt") { TraktScreen() }
            composable("rd") { RdScreen() }
        }
    }
}
