package com.mnfarzaneh.taskflow.ui.task

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mnfarzaneh.taskflow.data.repository.TaskRepository
import com.mnfarzaneh.taskflow.domain.model.Task
import com.mnfarzaneh.taskflow.domain.model.TaskStatus
import com.mnfarzaneh.taskflow.domain.usecase.CompleteTaskUseCase
import com.mnfarzaneh.taskflow.worker.NotificationScheduler
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class TaskUiState(
    val task: Task? = null,
    val isLoading: Boolean = true,
    val isDone: Boolean = false,
    val reminderSet: Boolean = false,
    val error: String? = null
)

@HiltViewModel
class TaskViewModel @Inject constructor(
    private val repository: TaskRepository,
    private val completeTaskUseCase: CompleteTaskUseCase,
    private val notificationScheduler: NotificationScheduler,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val taskId: Long = checkNotNull(savedStateHandle["taskId"])

    private val _uiState = MutableStateFlow(TaskUiState())
    val uiState: StateFlow<TaskUiState> = _uiState.asStateFlow()

    init {
        loadTask()
    }

    private fun loadTask() {
        viewModelScope.launch {
            val task = repository.getTaskById(taskId)
            _uiState.update { it.copy(task = task, isLoading = false) }
        }
    }

    fun completeTask() {
        val task = _uiState.value.task ?: return
        if (task.status == TaskStatus.LOCKED) return
        viewModelScope.launch {
            completeTaskUseCase(task)
            _uiState.update { it.copy(isDone = true) }
        }
    }

    fun startTask() {
        val task = _uiState.value.task ?: return
        if (task.status != TaskStatus.PENDING) return
        viewModelScope.launch {
            repository.updateTask(task.copy(status = TaskStatus.IN_PROGRESS))
            loadTask()
        }
    }

    fun setReminder(reminderAt: Long) {
        val task = _uiState.value.task ?: return
        viewModelScope.launch {
            repository.updateReminder(taskId, reminderAt)
            val chain       = repository.getChainById(task.chainId)
            val chainTitle  = chain?.title ?: ""
            val updatedTask = task.copy(reminderAt = reminderAt)
            notificationScheduler.cancelTaskNotifications(taskId)
            notificationScheduler.scheduleReminder(updatedTask, chainTitle)
            notificationScheduler.scheduleDeadline(updatedTask, chainTitle)
            _uiState.update {
                it.copy(task = updatedTask, reminderSet = true)
            }
        }
    }

    fun removeReminder() {
        viewModelScope.launch {
            repository.updateReminder(taskId, null)
            notificationScheduler.cancelTaskNotifications(taskId)
            val updatedTask = _uiState.value.task?.copy(reminderAt = null)
            _uiState.update { it.copy(task = updatedTask) }
        }
    }

    fun clearReminderSetFlag() {
        _uiState.update { it.copy(reminderSet = false) }
    }

    fun markForRevision(note: String) {
        val task = _uiState.value.task ?: return
        if (task.status != TaskStatus.DONE) return
        viewModelScope.launch {
            repository.setRevisionFlag(task.id, true, note)
            loadTask()
        }
    }

    fun clearRevisionFlag() {
        val task = _uiState.value.task ?: return
        viewModelScope.launch {
            repository.setRevisionFlag(task.id, false, null)
            loadTask()
        }
    }

}

