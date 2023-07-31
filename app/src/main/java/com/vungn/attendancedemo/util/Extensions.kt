package com.vungn.attendancedemo.util

import com.vungn.attendancedemo.model.Clazz
import com.vungn.attendancedemo.model.OverviewClass
import com.vungn.attendancedemo.model.server.AttendClass
import java.util.Date

fun String.toOverviewClass(): OverviewClass {
    val newString = this.removeRange(0, 8)
    val split = newString.split("&")
    return OverviewClass(
        room = split[0], area = split[1], token = split[2]
    )
}

fun OverviewClass.toClazz(
    nearbyWifi: List<String>, nearbyBluetooth: List<String>, createDate: Date
): Clazz {
    return Clazz(
        room = this.room,
        area = this.area,
        token = this.token,
        nearbyWiFi = nearbyWifi,
        nearbyBluetooth = nearbyBluetooth,
        createdDate = createDate
    )
}

fun Clazz.toAttendClass(): AttendClass {
    return AttendClass(
        token = this.token,
        nearbyWiFi = this.nearbyWiFi,
        nearbyBluetooth = this.nearbyBluetooth,
        createDate = this.createdDate
    )
}
