package com.kozvits.kislogtd.data.db

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.kozvits.kislogtd.data.db.converter.Converters
import com.kozvits.kislogtd.data.db.dao.NoteDao
import com.kozvits.kislogtd.data.db.dao.TaskDao
import com.kozvits.kislogtd.data.db.dao.WeeklyStatsDao
import com.kozvits.kislogtd.data.db.entity.NoteEntity
import com.kozvits.kislogtd.data.db.entity.TaskEntity
import com.kozvits.kislogtd.data.db.entity.WeeklyStatsEntity

@Database(
    entities = [TaskEntity::class, WeeklyStatsEntity::class, NoteEntity::class],
    version = 2,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun taskDao(): TaskDao
    abstract fun weeklyStatsDao(): WeeklyStatsDao
    abstract fun noteDao(): NoteDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        private val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    """CREATE TABLE IF NOT EXISTS `notes` (
                        `id` TEXT NOT NULL PRIMARY KEY,
                        `title` TEXT NOT NULL,
                        `content` TEXT NOT NULL DEFAULT '',
                        `created_at` INTEGER NOT NULL,
                        `updated_at` INTEGER NOT NULL
                    )"""
                )
            }
        }

        fun getInstance(context: android.content.Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: androidx.room.Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "kislo_gtd_database"
                )
                    .addMigrations(MIGRATION_1_2)
                    .fallbackToDestructiveMigrationOnDowngrade()
                    .build().also { INSTANCE = it }
            }
        }
    }
}
