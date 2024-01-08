package com.vungn.attendancedemo.ui.nav.graph

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.composable
import androidx.navigation.navigation
import com.vungn.attendancedemo.ui.destination.Attendance
import com.vungn.attendancedemo.ui.destination.Camera
import com.vungn.attendancedemo.ui.destination.Main
import com.vungn.attendancedemo.util.Graphs
import com.vungn.attendancedemo.util.Routes
import com.vungn.attendancedemo.vm.AttendanceViewModel
import com.vungn.attendancedemo.vm.CameraViewModel
import com.vungn.attendancedemo.vm.MainViewModel
import com.vungn.attendancedemo.vm.impl.AttendanceViewModelImpl
import com.vungn.attendancedemo.vm.impl.CameraViewModelImpl
import com.vungn.attendancedemo.vm.impl.MainViewModelImpl

fun NavGraphBuilder.homeGraph(navController: NavHostController) {
    navigation(route = Graphs.GRAPH_HOME.name, startDestination = Routes.ROUTE_MAIN.name) {
        composable(route = Routes.ROUTE_MAIN.name) {
            val vm: MainViewModel = hiltViewModel<MainViewModelImpl>()
            Main(modifier = Modifier.fillMaxSize(),
                viewModel = vm,
                navigateToCamera = { navController.navigate(Routes.ROUTE_CAMERA.name) })
        }
        composable(route = Routes.ROUTE_CAMERA.name) {
            val vm: CameraViewModel = hiltViewModel<CameraViewModelImpl>()
            Camera(modifier = Modifier.fillMaxSize(),
                viewModel = vm,
                navigateBack = { navController.popBackStack() },
                navigateToAttendance = {
                    navController.navigate(Routes.ROUTE_ATTENDANCE.name) {
                        popUpTo(Routes.ROUTE_MAIN.name)
                    }
                })
        }
        composable(route = Routes.ROUTE_ATTENDANCE.name) {
            val vm: AttendanceViewModel = hiltViewModel<AttendanceViewModelImpl>()
            Attendance(modifier = Modifier.fillMaxSize(), viewModel = vm, navigateBack = {
                navController.popBackStack().also {
                    if (it) {
                        vm.clearQrCode()
                    }
                }
            })
        }
    }
}
