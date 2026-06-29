package com.kozvits.kislogtd.data.db

import android.content.Context
import com.kozvits.kislogtd.data.db.dao.NoteDao
import com.kozvits.kislogtd.data.db.dao.TaskDao
import com.kozvits.kislogtd.data.db.dao.WeeklyStatsDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): AppDatabase {
        return AppDatabase.getInstance(context)
    }

    @Provides
    fun provideTaskDao(db: AppDatabase): TaskDao {
        return db.taskDao()
    }

    @Provides
    fun provideWeeklyStatsDao(db: AppDatabase): WeeklyStatsDao {
        return db.weeklyStatsDao()
    }

    @Provides
    fun provideNoteDao(db: AppDatabase): NoteDao {
        return db.noteDao()
    }
}
