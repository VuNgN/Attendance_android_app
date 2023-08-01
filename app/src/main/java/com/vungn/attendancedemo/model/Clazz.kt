package com.vungn.attendancedemo.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import com.vungn.attendancedemo.util.TypeConverter
import java.util.Date

@Entity(tableName = "class")
@TypeConverters(TypeConverter::class)
data class Clazz(
    @PrimaryKey(autoGenerate = true) val id: Int? = null,
    val room: String,
    val area: String,
    val token: String,
    val nearbyWiFi: List<String>,
    val nearbyBluetooth: List<String>,
    val createdDate: Date,
    var isSync: Boolean = false
)
