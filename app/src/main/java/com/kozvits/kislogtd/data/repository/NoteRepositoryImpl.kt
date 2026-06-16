package com.kozvits.kislogtd.data.repository

import com.kozvits.kislogtd.data.db.toDomain
import com.kozvits.kislogtd.data.db.toEntity
import com.kozvits.kislogtd.data.db.dao.NoteDao
import com.kozvits.kislogtd.domain.model.Note
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class NoteRepositoryImpl @Inject constructor(
    private val noteDao: NoteDao
) : NoteRepository {

    override fun getAllNotes(): Flow<List<Note>> =
        noteDao.getAllNotes().map { entities -> entities.map { it.toDomain() } }

    override fun getNotesByTask(taskId: String): Flow<List<Note>> =
        noteDao.getNotesByTask(taskId).map { entities -> entities.map { it.toDomain() } }

    override suspend fun getNoteById(id: String): Note? =
        noteDao.getNoteById(id)?.toDomain()

    override fun searchNotes(query: String): Flow<List<Note>> =
        noteDao.searchNotes(query).map { entities -> entities.map { it.toDomain() } }

    override suspend fun upsertNote(note: Note) =
        noteDao.upsertNote(note.toEntity())

    override suspend fun deleteNote(id: String) =
        noteDao.deleteNoteById(id)
}
