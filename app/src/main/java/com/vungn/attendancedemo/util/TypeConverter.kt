package com.vungn.attendancedemo.util

import androidx.room.TypeConverter
import com.google.gson.Gson
import java.util.Date

object TypeConverter {
    @TypeConverter
    fun toDate(dateLong: Long?): Date? {
        return if (dateLong == null) null else Date(dateLong)
    }

    @TypeConverter
    fun fromDate(date: Date?): Long? {
        return date?.time
    }

    @TypeConverter
    fun toBoolean(isSync: Int?): Boolean? {
        return if (isSync == null) null else isSync == 1
    }

    @TypeConverter
    fun fromBoolean(isSync: Boolean?): Int? {
        return if (isSync == null) null else if (isSync) 1 else 0
    }

    @TypeConverter
    fun toListString(listString: String?): List<String>? {
        return if (listString == null) null else Gson().fromJson(
            listString, Array<String>::class.java
        ).toList()
    }

    @TypeConverter
    fun fromListString(listString: List<String>?): String? {
        return if (listString == null) null else Gson().toJson(listString)
    }
}