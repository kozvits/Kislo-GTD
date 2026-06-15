package com.kozvits.kislogtd.data.repository

import com.kozvits.kislogtd.data.db.toDomain
import com.kozvits.kislogtd.data.db.toEntity
import com.kozvits.kislogtd.data.db.dao.TaskDao
import com.kozvits.kislogtd.domain.model.Task
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

interface TaskRepository {
    fun getAllTasks(): Flow<List<Task>>
    fun getTasksByCategory(categoryName: String): Flow<List<Task>>
    fun getTaskById(id: String): Flow<Task?>
    fun searchTasks(query: String): Flow<List<Task>>
    suspend fun upsertTask(task: Task)
    suspend fun deleteTask(task: Task)
    suspend fun getCompletedBetween(start: Long, end: Long): List<Task>
    fun getTasksByProject(projectId: String): Flow<List<Task>>
    fun getTasksByStatus(status: String): Flow<List<Task>>
}

@Singleton
class TaskRepositoryImpl @Inject constructor(
    private val taskDao: TaskDao
) : TaskRepository {

    override fun getAllTasks(): Flow<List<Task>> {
        return taskDao.getAll().map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override fun getTasksByCategory(categoryName: String): Flow<List<Task>> {
        return taskDao.getByCategory(categoryName).map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override fun getTaskById(id: String): Flow<Task?> {
        return taskDao.getById(id).map { entity ->
            entity?.toDomain()
        }
    }

    override fun searchTasks(query: String): Flow<List<Task>> {
        return taskDao.searchTasks(query).map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override suspend fun upsertTask(task: Task) {
        taskDao.upsert(task.toEntity())
    }

    override suspend fun deleteTask(task: Task) {
        taskDao.delete(task.toEntity())
    }

    override suspend fun getCompletedBetween(start: Long, end: Long): List<Task> {
        return taskDao.getCompletedBetween(start, end).map { it.toDomain() }
    }

    override fun getTasksByProject(projectId: String): Flow<List<Task>> {
        return taskDao.getByProjectId(projectId).map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override fun getTasksByStatus(status: String): Flow<List<Task>> {
        return taskDao.getByStatus(status).map { entities ->
            entities.map { it.toDomain() }
        }
    }
}
