package com.amali.annotably

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.amali.annotably.features.home.ui.HomeScreen
import com.amali.annotably.features.search.ui.SearchScreen
import com.amali.annotably.ui.theme.AnnotablyTheme
import dagger.hilt.android.AndroidEntryPoint

private enum class AppDestination(val route: String) {
    Home("home"),
    Search("search")
}

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            AnnotablyTheme {
                AnnotablyNavHost()
            }
        }
    }
}

@Composable
private fun AnnotablyNavHost() {
    val navController = rememberNavController()
    NavHost(
        navController = navController,
        startDestination = AppDestination.Home.route
    ) {
        composable(AppDestination.Home.route) {
            HomeScreen(
                onSearchClick = { navController.navigate(AppDestination.Search.route) },
                modifier = Modifier
            )
        }
        composable(AppDestination.Search.route) {
            SearchScreen(
                onBackClick = { navController.popBackStack() },
                modifier = Modifier
            )
        }
    }
}

