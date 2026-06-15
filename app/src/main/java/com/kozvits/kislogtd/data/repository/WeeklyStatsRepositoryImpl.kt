package com.kozvits.kislogtd.data.repository

import com.kozvits.kislogtd.data.db.dao.WeeklyStatsDao
import com.kozvits.kislogtd.data.db.entity.WeeklyStatsEntity
import com.kozvits.kislogtd.domain.WeeklyStats
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

interface WeeklyStatsRepository {
    fun getAllStats(): Flow<List<WeeklyStats>>
    suspend fun saveStats(stats: WeeklyStats)
    suspend fun getLatestStats(): WeeklyStats?
}

@Singleton
class WeeklyStatsRepositoryImpl @Inject constructor(
    private val weeklyStatsDao: WeeklyStatsDao
) : WeeklyStatsRepository {

    override fun getAllStats(): Flow<List<WeeklyStats>> {
        return weeklyStatsDao.getAll().map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override suspend fun saveStats(stats: WeeklyStats) {
        weeklyStatsDao.upsert(WeeklyStatsEntity.fromDomain(stats))
    }

    override suspend fun getLatestStats(): WeeklyStats? {
        return weeklyStatsDao.getLatest()?.toDomain()
    }
}
