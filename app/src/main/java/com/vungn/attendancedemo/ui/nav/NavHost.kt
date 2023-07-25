package com.vungn.attendancedemo.ui.nav

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.vungn.attendancedemo.ui.Attendance
import com.vungn.attendancedemo.ui.Camera
import com.vungn.attendancedemo.ui.Main
import com.vungn.attendancedemo.util.Routes
import com.vungn.attendancedemo.vm.AttendanceViewModel
import com.vungn.attendancedemo.vm.CameraViewModel
import com.vungn.attendancedemo.vm.MainViewModel
import com.vungn.attendancedemo.vm.impl.AttendanceViewModelImpl
import com.vungn.attendancedemo.vm.impl.CameraViewModelImpl
import com.vungn.attendancedemo.vm.impl.MainViewModelImpl

@Composable
fun MyNavHost(modifier: Modifier = Modifier) {
    val navController = rememberNavController()
    NavHost(
        modifier = modifier, navController = navController, startDestination = Routes.MAIN.name
    ) {
        composable(route = Routes.MAIN.name) {
            val vm: MainViewModel = hiltViewModel<MainViewModelImpl>()
            Main(modifier = Modifier.fillMaxSize(),
                viewModel = vm,
                navigateToCamera = { navController.navigate(Routes.CAMERA.name) })
        }
        composable(route = Routes.CAMERA.name) {
            val vm: CameraViewModel = hiltViewModel<CameraViewModelImpl>()
            Camera(modifier = Modifier.fillMaxSize(),
                viewModel = vm,
                navigateBack = { navController.popBackStack() },
                navigateToAttendance = {
                    navController.navigate(Routes.ATTENDANCE.name) {
                        popUpTo(Routes.MAIN.name)
                    }
                })
        }
        composable(route = Routes.ATTENDANCE.name) {
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