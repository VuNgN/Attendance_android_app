package com.vungn.attendancedemo.ui.nav

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.rememberNavController
import com.vungn.attendancedemo.ui.nav.graph.homeGraph
import com.vungn.attendancedemo.ui.nav.graph.loginGraph
import com.vungn.attendancedemo.util.Graphs

@Composable
fun MyNavHost(modifier: Modifier = Modifier) {
    val navController = rememberNavController()
    NavHost(
        modifier = modifier,
        navController = navController,
        startDestination = Graphs.GRAPH_HOME.name
    ) {
        loginGraph(navController)
        homeGraph(navController)
    }
}