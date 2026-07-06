package com.mnfarzaneh.taskflow.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mnfarzaneh.taskflow.data.repository.TaskRepository
import com.mnfarzaneh.taskflow.domain.model.Chain
import com.mnfarzaneh.taskflow.domain.model.Task
import com.mnfarzaneh.taskflow.domain.model.TaskStatus
import com.mnfarzaneh.taskflow.domain.usecase.DuplicateChainUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class HomeStats(
    val totalChains: Int = 0,
    val doneTasks: Int = 0,
    val overdueTasks: Int = 0
)

data class ChainProgress(
    val chain: Chain,
    val totalTasks: Int = 0,
    val doneTasks: Int = 0,
    val currentTaskTitle: String = "",
    val currentTaskOrder: Int = 0
) {
    val progress: Float get() = if (totalTasks > 0) doneTasks.toFloat() / totalTasks else 0f
    val isDone: Boolean get() = totalTasks > 0 && doneTasks == totalTasks
}

data class HomeUiState(
    val chains: List<Chain> = emptyList(),
    val chainProgresses: List<ChainProgress> = emptyList(),
    val stats: HomeStats = HomeStats(),
    val isLoading: Boolean = true,
    val showDoneSheet: Boolean = false,
    val showOverdueSheet: Boolean = false,
    val doneTasks: List<Task> = emptyList(),
    val overdueTasks: List<Task> = emptyList(),
    val doneTasksWithChain: List<TaskWithChain> = emptyList(),    // ← جدید
    val overdueTasksWithChain: List<TaskWithChain> = emptyList(), // ← جدید
)

data class TaskWithChain(
    val task: Task,
    val chainTitle: String
)

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val repository: TaskRepository,
    private val duplicateChainUseCase: DuplicateChainUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    init {
        // ── مشترک شدن در تغییرات دیتابیس ────────────────
        viewModelScope.launch {
            combine(
                repository.getAllChains(),
                repository.getAllTasks()
            ) { chains: List<Chain>, allTasks: List<Task> ->
                chains to allTasks
            }.collect { (chains, allTasks) ->
                updateStats(chains, allTasks)
            }
        }

        // ── ticker برای آپدیت overdue هر ۱ دقیقه ────────
        viewModelScope.launch {
            while (true) {
                delay(60_000)
                val chains   = repository.getAllChains().first()
                val allTasks = repository.getAllTasks().first()
                updateStats(chains, allTasks)
            }
        }
    }

    private fun updateStats(chains: List<Chain>, allTasks: List<Task>) {
        val now      = System.currentTimeMillis()
        val chainIds = chains.map { it.id }.toSet()
        val chainsMap = chains.associateBy { it.id }
        val valid    = allTasks.filter { it.chainId in chainIds }

        // progress هر زنجیره
        val progresses = chains.map { chain ->
            val chainTasks  = valid.filter { it.chainId == chain.id }
            val doneTasks   = chainTasks.count { it.status == TaskStatus.DONE }
            val currentTask = chainTasks
                .sortedBy { it.order }
                .firstOrNull {
                    it.status == TaskStatus.PENDING ||
                            it.status == TaskStatus.IN_PROGRESS
                }
            ChainProgress(
                chain            = chain,
                totalTasks       = chainTasks.size,
                doneTasks        = doneTasks,
                currentTaskTitle = currentTask?.title ?: "",
                currentTaskOrder = currentTask?.order ?: 0
            )
        }

        // آمار کلی — اول تعریف کن
        val done = valid.filter {
            it.status == TaskStatus.DONE && it.deadlineAt != null
        }
        val overdue = valid.filter {
            it.status != TaskStatus.DONE &&
                    it.deadlineAt != null &&
                    it.deadlineAt < now
        }

        // بعد استفاده کن
        val doneTasksWithChain = done.map { task ->
            TaskWithChain(
                task       = task,
                chainTitle = chainsMap[task.chainId]?.title ?: ""
            )
        }
        val overdueTasksWithChain = overdue.map { task ->
            TaskWithChain(
                task       = task,
                chainTitle = chainsMap[task.chainId]?.title ?: ""
            )
        }

        _uiState.update { currentState ->
            HomeUiState(
                chains          = chains,
                chainProgresses = progresses,
                stats           = HomeStats(
                    totalChains  = chains.size,
                    doneTasks    = done.size,
                    overdueTasks = overdue.size
                ),
                doneTasks             = done,
                overdueTasks          = overdue,
                doneTasksWithChain    = doneTasksWithChain,
                overdueTasksWithChain = overdueTasksWithChain,
                isLoading             = false,
                showDoneSheet         = currentState.showDoneSheet,
                showOverdueSheet      = currentState.showOverdueSheet
            )
        }
    }

    fun deleteChain(chain: Chain) {
        viewModelScope.launch {
            repository.deleteChain(chain)
        }
    }

    fun duplicateChain(chainId: Long) {
        viewModelScope.launch {
            duplicateChainUseCase(chainId)
        }
    }

    fun showDoneSheet()    { _uiState.update { it.copy(showDoneSheet = true) } }
    fun showOverdueSheet() { _uiState.update { it.copy(showOverdueSheet = true) } }
    fun hideSheets()       { _uiState.update { it.copy(showDoneSheet = false, showOverdueSheet = false) } }
}