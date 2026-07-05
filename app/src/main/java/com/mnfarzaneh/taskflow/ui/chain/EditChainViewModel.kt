package com.mnfarzaneh.taskflow.ui.chain

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mnfarzaneh.taskflow.data.repository.TaskRepository
import com.mnfarzaneh.taskflow.domain.model.Chain
import com.mnfarzaneh.taskflow.domain.model.Task
import com.mnfarzaneh.taskflow.domain.model.TaskStatus
import com.mnfarzaneh.taskflow.worker.NotificationScheduler
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
data class EditChainUiState(
    val chainTitle: String = "",
    val chainDescription: String = "",
    val tasks: List<TaskDraft> = emptyList(),
    val isLoading: Boolean = true,
    val isSaving: Boolean = false,
    val isDone: Boolean = false,
    val error: String? = null
)

@HiltViewModel
class EditChainViewModel @Inject constructor(
    private val repository: TaskRepository,
    private val notificationScheduler: NotificationScheduler,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val chainId: Long = checkNotNull(savedStateHandle["chainId"])

    private val _uiState = MutableStateFlow(EditChainUiState())
    val uiState: StateFlow<EditChainUiState> = _uiState.asStateFlow()

    // state اصلی دست نخوره — فقط موقع drop آپدیت بشه
    private val _draggedTasks = MutableStateFlow<List<TaskDraft>?>(null)

    // این رو به uiState اضافه کن
    val displayTasks: StateFlow<List<TaskDraft>> = combine(
        _uiState,
        _draggedTasks
    ) { state, dragged ->
        dragged ?: state.tasks
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(), emptyList())

    fun onDragStart() {
        _draggedTasks.value = _uiState.value.tasks.toList()
    }

    fun onDragMove(fromIndex: Int, toIndex: Int) {
        val current = _draggedTasks.value ?: return
        if (fromIndex < 0 || toIndex < 0) return
        if (fromIndex >= current.size || toIndex >= current.size) return
        val tasks = current.toMutableList()
        tasks.add(toIndex, tasks.removeAt(fromIndex))
        _draggedTasks.value = tasks
    }

    fun onDragEnd() {
        val dragged = _draggedTasks.value ?: return
        _uiState.update { state ->
            state.copy(
                tasks = dragged.mapIndexed { index, task -> task.copy(id = index) }
            )
        }
        _draggedTasks.value = null
    }

    fun onDragCancel() {
        _draggedTasks.value = null
    }

    init {
        loadChain()
    }

    private fun loadChain() {
        viewModelScope.launch {
            val chain = repository.getChainById(chainId) ?: return@launch
            val tasks = repository.getTasksByChain(chainId).first()

            _uiState.update {
                it.copy(
                    chainTitle       = chain.title,
                    chainDescription = chain.description,
                    tasks            = tasks.sortedBy { t -> t.order }.map { task ->
                        TaskDraft(
                            id          = task.order,
                            title       = task.title,
                            description = task.description,
                            deadlineAt  = task.deadlineAt,
                            reminderAt  = task.reminderAt
                        )
                    },
                    isLoading = false
                )
            }
        }
    }

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

    fun saveChanges() {
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
                // ۱. زنجیره رو آپدیت کن
                val chain = repository.getChainById(chainId) ?: return@launch
                repository.updateChain(
                    chain.copy(
                        title       = state.chainTitle,
                        description = state.chainDescription
                    )
                )

                // ۲. وظایف فعلی رو بگیر
                val oldTasks = repository.getTasksByChain(chainId)
                    .first()
                    .sortedBy { it.order }

                // ۳. وظایف رو آپدیت یا حذف یا اضافه کن
                state.tasks.forEachIndexed { index, draft ->
                    val existingTask = oldTasks.getOrNull(index)

                    if (existingTask != null) {
                        // وظیفه موجود رو آپدیت کن — status حفظ میشه
                        val updatedTask = existingTask.copy(
                            title       = draft.title,
                            description = draft.description,
                            order       = index,
                            deadlineAt  = draft.deadlineAt,
                            reminderAt  = draft.reminderAt
                        )
                        repository.updateTask(updatedTask)

                        // هشدار رو آپدیت کن
                        notificationScheduler.cancelTaskNotifications(existingTask.id)
                        if (existingTask.status != TaskStatus.DONE) {
                            notificationScheduler.scheduleReminder(updatedTask)
                            notificationScheduler.scheduleDeadline(updatedTask)
                        }
                    } else {
                        // وظیفه جدید — اضافه کن
                        val taskId = repository.insertTask(
                            Task(
                                chainId     = chainId,
                                title       = draft.title,
                                description = draft.description,
                                status      = TaskStatus.LOCKED,
                                order       = index,
                                deadlineAt  = draft.deadlineAt,
                                reminderAt  = draft.reminderAt
                            )
                        )
                        // اگه وظیفه قبلیش Done شده، این رو آزاد کن
                        val prevTask = oldTasks.getOrNull(index - 1)
                        if (prevTask?.status == TaskStatus.DONE) {
                            repository.updateTask(
                                Task(
                                    id          = taskId,
                                    chainId     = chainId,
                                    title       = draft.title,
                                    description = draft.description,
                                    status      = TaskStatus.PENDING,
                                    order       = index,
                                    deadlineAt  = draft.deadlineAt,
                                    reminderAt  = draft.reminderAt
                                )
                            )
                        }
                    }
                }

                // ۴. وظایف اضافه که حذف شدن رو پاک کن
                if (oldTasks.size > state.tasks.size) {
                    oldTasks.drop(state.tasks.size).forEach { task ->
                        notificationScheduler.cancelTaskNotifications(task.id)
                        repository.deleteTask(task)
                    }
                }

                _uiState.update { it.copy(isSaving = false, isDone = true) }
            } catch (e: Exception) {
                _uiState.update { it.copy(isSaving = false, error = "خطا در ذخیره‌سازی") }
            }
        }
    }
    fun reorderTasks(fromIndex: Int, toIndex: Int) {
        if (fromIndex == toIndex) return
        if (fromIndex < 0 || toIndex < 0) return
        _uiState.update { state ->
            if (fromIndex >= state.tasks.size || toIndex >= state.tasks.size) return@update state
            val tasks = state.tasks.toMutableList()
            tasks.add(toIndex, tasks.removeAt(fromIndex))
            state.copy(tasks = tasks.mapIndexed { index, task -> task.copy(id = index) })
        }
    }
}