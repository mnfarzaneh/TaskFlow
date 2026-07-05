package com.mnfarzaneh.taskflow.ui.chain

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mnfarzaneh.taskflow.data.repository.TaskRepository
import com.mnfarzaneh.taskflow.domain.model.Task
import com.mnfarzaneh.taskflow.domain.model.TaskStatus
import com.mnfarzaneh.taskflow.domain.usecase.ChainWithTasks
import com.mnfarzaneh.taskflow.domain.usecase.CompleteTaskUseCase
import com.mnfarzaneh.taskflow.domain.usecase.ExportChainUseCase
import com.mnfarzaneh.taskflow.domain.usecase.GetChainWithTasksUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ChainUiState(
    val chainWithTasks: ChainWithTasks? = null,
    val isLoading: Boolean = true,
    val exportText: String? = null,
    val error: String? = null
)

@HiltViewModel
class ChainViewModel @Inject constructor(
    private val getChainWithTasksUseCase: GetChainWithTasksUseCase,
    private val completeTaskUseCase: CompleteTaskUseCase,
    private val exportChainUseCase: ExportChainUseCase,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val chainId: Long = checkNotNull(savedStateHandle["chainId"])

    private val _uiState = MutableStateFlow(ChainUiState())
    val uiState: StateFlow<ChainUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            getChainWithTasksUseCase(chainId).collect { chainWithTasks ->
                _uiState.update {
                    it.copy(
                        chainWithTasks = chainWithTasks,
                        isLoading      = false
                    )
                }
            }
        }
    }

    fun completeTask(task: Task) {
        if (task.status == TaskStatus.LOCKED) return
        viewModelScope.launch {
            completeTaskUseCase(task)
        }
    }

    fun exportChain() {
        viewModelScope.launch {
            val text = exportChainUseCase(chainId)
            _uiState.update { it.copy(exportText = text) }
        }
    }

    fun clearExport() {
        _uiState.update { it.copy(exportText = null) }
    }
}