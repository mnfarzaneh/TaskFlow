package com.mnfarzaneh.taskflow.ui.calendar

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mnfarzaneh.taskflow.data.repository.TaskRepository
import com.mnfarzaneh.taskflow.domain.model.Task
import com.mnfarzaneh.taskflow.domain.model.TaskStatus
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.ZoneId
import javax.inject.Inject
import com.mnfarzaneh.taskflow.utils.PersianDate
import com.mnfarzaneh.taskflow.utils.toPersianCalendar
import com.mnfarzaneh.taskflow.utils.persianMonthName

data class CalendarUiState(
    val tasksByDate: Map<LocalDate, List<Task>> = emptyMap(),
    val selectedDate: LocalDate = LocalDate.now(),
    val selectedDateTasks: List<Task> = emptyList(),
    val isLoading: Boolean = true
)

@HiltViewModel
class CalendarViewModel @Inject constructor(
    private val repository: TaskRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(CalendarUiState())
    val uiState: StateFlow<CalendarUiState> = _uiState.asStateFlow()

    init {
        loadTasks()
    }

    private fun loadTasks() {
        viewModelScope.launch {
            repository.getTasksWithDeadline().collect { tasks ->
                val tasksByDate = tasks
                    .filter { it.deadlineAt != null }
                    .groupBy { task ->
                        task.deadlineAt!!
                            .let { java.util.Date(it) }
                            .toInstant()
                            .atZone(ZoneId.systemDefault())
                            .toLocalDate()
                    }

                val selectedTasks = tasksByDate[_uiState.value.selectedDate] ?: emptyList()

                _uiState.update {
                    it.copy(
                        tasksByDate       = tasksByDate,
                        selectedDateTasks = selectedTasks,
                        isLoading         = false
                    )
                }
            }
        }
    }

    fun onDateSelected(date: LocalDate) {
        _uiState.update {
            it.copy(
                selectedDate      = date,
                selectedDateTasks = it.tasksByDate[date] ?: emptyList()
            )
        }
    }
}

// ── helper های تبدیل تاریخ شمسی ──────────────────────────



fun persianMonthName(month: Int): String = when (month) {
    1  -> "فروردین"
    2  -> "اردیبهشت"
    3  -> "خرداد"
    4  -> "تیر"
    5  -> "مرداد"
    6  -> "شهریور"
    7  -> "مهر"
    8  -> "آبان"
    9  -> "آذر"
    10 -> "دی"
    11 -> "بهمن"
    12 -> "اسفند"
    else -> ""
}