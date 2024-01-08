package com.vungn.attendancedemo.ui.nav

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.rememberNavController
import com.vungn.attendancedemo.ui.nav.graph.homeGraph
import com.vungn.attendancedemo.util.Graphs

@Composable
fun MainNavHost(modifier: Modifier = Modifier) {
    val navController = rememberNavController()
    NavHost(
        modifier = modifier,
        navController = navController,
        startDestination = Graphs.GRAPH_HOME.name
    ) {
        homeGraph(navController)
    }
}