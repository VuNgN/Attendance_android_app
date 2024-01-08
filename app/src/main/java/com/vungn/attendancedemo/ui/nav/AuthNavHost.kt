package com.vungn.attendancedemo.ui.nav

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.rememberNavController
import com.vungn.attendancedemo.ui.activity.AuthActivity
import com.vungn.attendancedemo.ui.nav.graph.loginGraph
import com.vungn.attendancedemo.util.Graphs

@Composable
fun AuthNavHost(activity: AuthActivity, modifier: Modifier = Modifier) {
    val navController = rememberNavController()
    NavHost(
        modifier = modifier,
        navController = navController,
        startDestination = Graphs.GRAPH_LOGIN.name
    ) {
        loginGraph(navController = navController, activity = activity)
    }
}
