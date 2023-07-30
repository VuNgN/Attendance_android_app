package com.vungn.attendancedemo.util

import java.util.Calendar
import java.util.Date

object DateUtil {
    fun minAgo(startDate: Date): Long {
        val endDate = Calendar.getInstance().time
        val different: Long = endDate.time - startDate.time
        println("startDate : $startDate")
        println("endDate : $endDate")
        println("different : $different")
        val secondsInMilli: Long = 1000
        val minutesInMilli = secondsInMilli * 60
        return different / minutesInMilli
    }
}
