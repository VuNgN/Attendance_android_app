package com.vungn.attendancedemo.ui.nav.graph

import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.composable
import androidx.navigation.navigation
import com.vungn.attendancedemo.ui.activity.AuthActivity
import com.vungn.attendancedemo.ui.destination.Login
import com.vungn.attendancedemo.util.Graphs
import com.vungn.attendancedemo.util.Routes
import com.vungn.attendancedemo.vm.LoginViewModel
import com.vungn.attendancedemo.vm.impl.LoginViewModelImpl

fun NavGraphBuilder.loginGraph(activity: AuthActivity, navController: NavHostController) {
    navigation(route = Graphs.GRAPH_LOGIN.name, startDestination = Routes.ROUTE_LOGIN.name) {
        composable(route = Routes.ROUTE_LOGIN.name) {
            val vm: LoginViewModel = hiltViewModel<LoginViewModelImpl>()
            Login(viewModel = vm, navigateToHome = { activity.finish() })
        }
    }
}
