package com.kozvits.kislogtd.data.repository

import com.kozvits.kislogtd.domain.model.Note
import kotlinx.coroutines.flow.Flow

interface NoteRepository {
    fun getAllNotes(): Flow<List<Note>>
    fun getNotesByTask(taskId: String): Flow<List<Note>>
    suspend fun getNoteById(id: String): Note?
    fun searchNotes(query: String): Flow<List<Note>>
    suspend fun upsertNote(note: Note)
    suspend fun deleteNote(id: String)
}
