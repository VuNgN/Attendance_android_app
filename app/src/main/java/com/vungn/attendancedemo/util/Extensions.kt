package com.vungn.attendancedemo.util

import android.app.AlarmManager
import android.app.PendingIntent
import com.vungn.attendancedemo.model.Clazz
import com.vungn.attendancedemo.model.OverviewClass
import com.vungn.attendancedemo.model.server.AttendClass
import java.util.Calendar
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

fun AlarmManager.startScan(
    pendingIntent: PendingIntent, startTime: Calendar
) {
    this.set(
        AlarmManager.RTC, startTime.timeInMillis, pendingIntent
    )
}

fun Calendar.startClassLesson(lesson: Int): Calendar {
    return this.apply {
        timeInMillis = System.currentTimeMillis()
        when (lesson) {
            1 -> {
                set(Calendar.HOUR_OF_DAY, 6)
                set(Calendar.MINUTE, 55)
            }

            2 -> {
                set(Calendar.HOUR_OF_DAY, 7)
                set(Calendar.MINUTE, 50)
            }

            3 -> {
                set(Calendar.HOUR_OF_DAY, 8)
                set(Calendar.MINUTE, 45)
            }

            4 -> {
                set(Calendar.HOUR_OF_DAY, 9)
                set(Calendar.MINUTE, 40)
            }

            5 -> {
                set(Calendar.HOUR_OF_DAY, 10)
                set(Calendar.MINUTE, 35)
            }

            6 -> {
                set(Calendar.HOUR_OF_DAY, 11)
                set(Calendar.MINUTE, 30)
            }

            7 -> {
                set(Calendar.HOUR_OF_DAY, 12)
                set(Calendar.MINUTE, 50)
            }

            8 -> {
                set(Calendar.HOUR_OF_DAY, 13)
                set(Calendar.MINUTE, 45)
            }

            9 -> {
                set(Calendar.HOUR_OF_DAY, 14)
                set(Calendar.MINUTE, 40)
            }

            10 -> {
                set(Calendar.HOUR_OF_DAY, 15)
                set(Calendar.MINUTE, 35)
            }

            11 -> {
                set(Calendar.HOUR_OF_DAY, 16)
                set(Calendar.MINUTE, 30)
            }

            12 -> {
                set(Calendar.HOUR_OF_DAY, 17)
                set(Calendar.MINUTE, 25)
            }

            13 -> {
                set(Calendar.HOUR_OF_DAY, 18)
                set(Calendar.MINUTE, 45)
            }

            14 -> {
                set(Calendar.HOUR_OF_DAY, 19)
                set(Calendar.MINUTE, 40)
            }

            15 -> {
                set(Calendar.HOUR_OF_DAY, 20)
                set(Calendar.MINUTE, 35)
            }
        }
    }
}