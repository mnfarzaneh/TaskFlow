package com.mnfarzaneh.taskflow.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mnfarzaneh.taskflow.data.repository.TaskRepository
import com.mnfarzaneh.taskflow.domain.model.Chain
import com.mnfarzaneh.taskflow.domain.model.TaskStatus
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject
import com.mnfarzaneh.taskflow.domain.usecase.DuplicateChainUseCase
import com.mnfarzaneh.taskflow.domain.model.Task

data class HomeStats(
    val totalChains: Int = 0,
    val totalTasks: Int = 0,
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
    val chainProgresses: List<ChainProgress> = emptyList(),  // ← اضافه شد
    val stats: HomeStats = HomeStats(),
    val isLoading: Boolean = true,
    val showDoneSheet: Boolean = false,
    val showOverdueSheet: Boolean = false,
    val doneTasks: List<Task> = emptyList(),
    val overdueTasks: List<Task> = emptyList()
)

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val repository: TaskRepository,
    private val duplicateChainUseCase: DuplicateChainUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            combine(
                repository.getAllChains(),
                repository.getAllTasks()
            ) { chains: List<Chain>, allTasks: List<Task> ->
                val now      = System.currentTimeMillis()
                val chainIds = chains.map { it.id }.toSet()
                val valid    = allTasks.filter { it.chainId in chainIds }

                // آمار هر زنجیره
                val progresses = chains.map { chain ->
                    val chainTasks   = valid.filter { it.chainId == chain.id }
                    val doneTasks    = chainTasks.count { it.status == TaskStatus.DONE }
                    val currentTask  = chainTasks
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

                val done    = valid.filter { it.status == TaskStatus.DONE && it.deadlineAt != null }
                val overdue = valid.filter {
                    it.status != TaskStatus.DONE &&
                            it.deadlineAt != null &&
                            it.deadlineAt < now
                }

                HomeUiState(
                    chains          = chains,
                    chainProgresses = progresses,
                    stats           = HomeStats(
                        totalChains  = chains.size,
                        doneTasks    = done.size,
                        overdueTasks = overdue.size
                    ),
                    doneTasks    = done,
                    overdueTasks = overdue,
                    isLoading    = false
                )
            }.collect { state ->
                _uiState.update {
                    state.copy(
                        showDoneSheet    = it.showDoneSheet,
                        showOverdueSheet = it.showOverdueSheet
                    )
                }
            }
        }
    }

    fun showDoneSheet()    { _uiState.update { it.copy(showDoneSheet = true) } }
    fun showOverdueSheet() { _uiState.update { it.copy(showOverdueSheet = true) } }
    fun hideSheets()       { _uiState.update { it.copy(showDoneSheet = false, showOverdueSheet = false) } }

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
}