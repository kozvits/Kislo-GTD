package com.kozvits.kislogtd.domain

data class WeeklyStats(
    val weekStartDate: Long,
    val totalCompleted: Int,
    val projectCompleted: Int,
    val projectPercent: Float,
    val diaryEntry: String
)
