package com.vungn.attendancedemo.model.server

import java.util.Date

data class AttendClass(
    val token: String,
    val nearbyWiFi: List<String>,
    val nearbyBluetooth: List<String>,
    val createDate: Date
)
