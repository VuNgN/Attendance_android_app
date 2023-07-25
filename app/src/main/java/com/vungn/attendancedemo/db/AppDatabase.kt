package com.vungn.attendancedemo.db

import androidx.room.Database
import androidx.room.RoomDatabase
import com.vungn.attendancedemo.data.dao.ClassDao
import com.vungn.attendancedemo.model.Clazz

@Database(entities = [Clazz::class], version = 4)
abstract class AppDatabase : RoomDatabase() {
    abstract fun ClassDao(): ClassDao
}