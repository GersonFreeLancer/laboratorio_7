package com.gerson.demodata.data.local.database

import androidx.room.Database
import androidx.room.RoomDatabase
import com.gerson.demodata.data.local.dao.GpsGoogleDao
import com.gerson.demodata.data.local.dao.GpsSensorsDao
import com.gerson.demodata.data.local.entity.GpsGoogleEntity
import com.gerson.demodata.data.local.entity.GpsSensorsEntity

@Database(
    entities = [
        GpsGoogleEntity::class,
        GpsSensorsEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun gpsGoogleDao(): GpsGoogleDao

    abstract fun gpsSensorsDao(): GpsSensorsDao
}