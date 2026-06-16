package com.kozvits.kislogtd.data.db

import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.kozvits.kislogtd.data.db.converter.Converters
import com.kozvits.kislogtd.data.db.dao.TaskDao
import com.kozvits.kislogtd.data.db.dao.WeeklyStatsDao
import com.kozvits.kislogtd.data.db.entity.TaskEntity
import com.kozvits.kislogtd.data.db.entity.WeeklyStatsEntity

@Database(
    entities = [TaskEntity::class, WeeklyStatsEntity::class],
    version = 1,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun taskDao(): TaskDao
    abstract fun weeklyStatsDao(): WeeklyStatsDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getInstance(context: android.content.Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "kislo_gtd_database"
                ).build().also { INSTANCE = it }
            }
        }
    }
}
