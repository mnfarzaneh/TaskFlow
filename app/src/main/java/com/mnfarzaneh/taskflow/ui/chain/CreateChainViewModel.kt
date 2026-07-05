package com.mnfarzaneh.taskflow.ui.chain

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mnfarzaneh.taskflow.domain.usecase.ChainInput
import com.mnfarzaneh.taskflow.domain.usecase.CreateChainUseCase
import com.mnfarzaneh.taskflow.domain.usecase.TaskInput
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class TaskDraft(
    val id: Int = 0,
    val title: String = "",
    val description: String = "",
    val deadlineAt: Long? = null,
    val reminderAt: Long? = null
)

data class CreateChainUiState(
    val chainTitle: String = "",
    val chainDescription: String = "",
    val tasks: List<TaskDraft> = listOf(TaskDraft(id = 0)),
    val isSaving: Boolean = false,
    val isDone: Boolean = false,
    val error: String? = null
)

@HiltViewModel
class CreateChainViewModel @Inject constructor(
    private val createChainUseCase: CreateChainUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(CreateChainUiState())
    val uiState: StateFlow<CreateChainUiState> = _uiState.asStateFlow()

    fun onChainTitleChange(title: String) {
        _uiState.update { it.copy(chainTitle = title) }
    }

    fun onChainDescriptionChange(desc: String) {
        _uiState.update { it.copy(chainDescription = desc) }
    }

    fun onTaskTitleChange(taskId: Int, title: String) {
        _uiState.update { state ->
            state.copy(
                tasks = state.tasks.map { task ->
                    if (task.id == taskId) task.copy(title = title) else task
                }
            )
        }
    }

    fun onTaskDescriptionChange(taskId: Int, desc: String) {
        _uiState.update { state ->
            state.copy(
                tasks = state.tasks.map { task ->
                    if (task.id == taskId) task.copy(description = desc) else task
                }
            )
        }
    }

    fun onTaskDeadlineChange(taskId: Int, deadlineAt: Long?) {
        _uiState.update { state ->
            state.copy(
                tasks = state.tasks.map { task ->
                    if (task.id == taskId) task.copy(deadlineAt = deadlineAt) else task
                }
            )
        }
    }

    fun addTask() {
        _uiState.update { state ->
            val newId = (state.tasks.maxOfOrNull { it.id } ?: 0) + 1
            state.copy(tasks = state.tasks + TaskDraft(id = newId))
        }
    }

    fun removeTask(taskId: Int) {
        _uiState.update { state ->
            if (state.tasks.size <= 1) return@update state
            state.copy(tasks = state.tasks.filter { it.id != taskId })
        }
    }

    fun saveChain() {
        val state = _uiState.value
        if (state.chainTitle.isBlank()) {
            _uiState.update { it.copy(error = "عنوان زنجیره رو بنویس") }
            return
        }
        if (state.tasks.any { it.title.isBlank() }) {
            _uiState.update { it.copy(error = "عنوان همه وظایف رو بنویس") }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true, error = null) }
            try {
                createChainUseCase(
                    ChainInput(
                        title       = state.chainTitle,
                        description = state.chainDescription,
                        tasks       = state.tasks.map { task ->
                            TaskInput(
                                title       = task.title,
                                description = task.description,
                                deadlineAt  = task.deadlineAt,
                                reminderAt  = task.reminderAt
                            )
                        }
                    )
                )
                _uiState.update { it.copy(isSaving = false, isDone = true) }
            } catch (e: Exception) {
                _uiState.update { it.copy(isSaving = false, error = "خطا در ذخیره‌سازی") }
            }
        }
    }
}