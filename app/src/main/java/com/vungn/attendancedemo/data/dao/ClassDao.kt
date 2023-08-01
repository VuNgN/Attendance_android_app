package com.vungn.attendancedemo.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.vungn.attendancedemo.model.Clazz
import kotlinx.coroutines.flow.Flow

@Dao
interface ClassDao {
    @Query("SELECT * FROM class WHERE isSync = 0")
    fun getNotSyncs(): Flow<List<Clazz>>

    @Query("SELECT * FROM class WHERE id = :id")
    fun getClassById(id: Long): Clazz

    @Insert
    fun insert(overviewClass: Clazz): Long

    @Update
    fun update(overviewClass: Clazz)

    @Update
    fun updateAll(overviewClasses: List<Clazz>)
}
