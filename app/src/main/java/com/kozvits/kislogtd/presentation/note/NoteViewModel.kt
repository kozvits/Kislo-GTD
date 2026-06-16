package com.kozvits.kislogtd.presentation.note

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kozvits.kislogtd.data.repository.NoteRepository
import com.kozvits.kislogtd.domain.model.Note
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class NoteListUiState(
    val notes: List<Note> = emptyList(),
    val searchQuery: String = "",
    val isLoading: Boolean = true
)

data class NoteDetailUiState(
    val note: Note? = null,
    val title: String = "",
    val body: String = "",
    val isSaving: Boolean = false,
    val showDeleteConfirm: Boolean = false
)

@HiltViewModel
class NoteListViewModel @Inject constructor(
    private val noteRepo: NoteRepository
) : ViewModel() {

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery

    @OptIn(ExperimentalCoroutinesApi::class)
    val uiState: StateFlow<NoteListUiState> = _searchQuery
        .flatMapLatest { query ->
            if (query.isBlank()) noteRepo.getAllNotes()
            else noteRepo.searchNotes(query)
        }
        .map { NoteListUiState(notes = it, isLoading = false) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), NoteListUiState())

    fun setSearchQuery(query: String) { _searchQuery.value = query }

    fun deleteNote(id: String) {
        viewModelScope.launch { noteRepo.deleteNote(id) }
    }
}

@HiltViewModel
class NoteDetailViewModel @Inject constructor(
    private val noteRepo: NoteRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(NoteDetailUiState())
    val uiState: StateFlow<NoteDetailUiState> = _uiState.asStateFlow()

    fun loadNote(noteId: String?) {
        if (noteId == null) return
        viewModelScope.launch {
            val note = noteRepo.getNoteById(noteId)
            if (note != null) {
                _uiState.value = NoteDetailUiState(
                    note = note,
                    title = note.title,
                    body = note.body
                )
            }
        }
    }

    fun newNote(taskId: String? = null) {
        _uiState.value = NoteDetailUiState(
            note = Note(
                taskId = taskId,
                createdAt = System.currentTimeMillis(),
                updatedAt = System.currentTimeMillis()
            ),
            title = "",
            body = ""
        )
    }

    fun setTitle(title: String) {
        _uiState.value = _uiState.value.copy(title = title)
    }

    fun setBody(body: String) {
        _uiState.value = _uiState.value.copy(body = body)
    }

    fun saveNote(onSaved: (String) -> Unit) {
        val state = _uiState.value
        val note = state.note ?: return
        viewModelScope.launch {
            _uiState.value = state.copy(isSaving = true)
            val updated = note.copy(
                title = state.title.trim(),
                body = state.body.trim(),
                updatedAt = System.currentTimeMillis()
            )
            noteRepo.upsertNote(updated)
            _uiState.value = _uiState.value.copy(isSaving = false)
            onSaved(updated.id)
        }
    }

    fun showDeleteConfirm() {
        _uiState.value = _uiState.value.copy(showDeleteConfirm = true)
    }

    fun dismissDeleteConfirm() {
        _uiState.value = _uiState.value.copy(showDeleteConfirm = false)
    }

    fun deleteNote(onDeleted: () -> Unit) {
        val id = _uiState.value.note?.id ?: return
        viewModelScope.launch {
            noteRepo.deleteNote(id)
            onDeleted()
        }
    }
}
