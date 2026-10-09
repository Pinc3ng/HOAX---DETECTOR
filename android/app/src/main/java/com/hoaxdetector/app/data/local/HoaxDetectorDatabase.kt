package com.hoaxdetector.app.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.hoaxdetector.app.data.local.dao.CheckHistoryDao
import com.hoaxdetector.app.data.local.entity.AnalysisResultConverter
import com.hoaxdetector.app.data.local.entity.CheckHistoryEntity

@Database(
    entities = [CheckHistoryEntity::class],
    version = 2,
    exportSchema = false
)
@TypeConverters(AnalysisResultConverter::class)
abstract class HoaxDetectorDatabase : RoomDatabase() {
    abstract fun checkHistoryDao(): CheckHistoryDao

    companion object {
        const val DATABASE_NAME = "hoax_detector.db"
    }
}
