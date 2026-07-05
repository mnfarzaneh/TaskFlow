package com.mnfarzaneh.taskflow.ui.calendar

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.KeyboardArrowLeft
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.mnfarzaneh.taskflow.domain.model.Task
import com.mnfarzaneh.taskflow.domain.model.TaskStatus
import com.mnfarzaneh.taskflow.ui.theme.*
import com.mnfarzaneh.taskflow.utils.toPersian
import java.time.LocalDate
import java.time.ZoneId
import com.mnfarzaneh.taskflow.utils.PersianDate
import com.mnfarzaneh.taskflow.utils.toPersianCalendar
import com.mnfarzaneh.taskflow.utils.persianMonthName
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CalendarScreen(
    onTaskClick: (Long) -> Unit,
    viewModel: CalendarViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    // ماه شمسی جاری
    val today        = LocalDate.now()
    val todayPersian = today.toPersianCalendar()
    var currentPersianYear  by remember { mutableStateOf(todayPersian.year) }
    var currentPersianMonth by remember { mutableStateOf(todayPersian.month) }

    GlassBackground {
        Column(modifier = Modifier.fillMaxSize()) {

            Text(
                text       = "تقویم",
                fontWeight = FontWeight.Bold,
                color      = Matcha800,
                style      = MaterialTheme.typography.titleLarge,
                modifier   = Modifier.padding(start = 20.dp, top = 24.dp, bottom = 8.dp)
            )

            if (uiState.isLoading) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = Matcha600)
                }
                return@Column
            }

            LazyColumn(
                modifier       = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(bottom = 16.dp)
            ) {

                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                            .glassEffect(cornerRadius = 20.dp)
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {

                            // هدر ماه شمسی
                            PersianMonthHeader(
                                year  = currentPersianYear,
                                month = currentPersianMonth,
                                onPrev = {
                                    if (currentPersianMonth == 1) {
                                        currentPersianYear--
                                        currentPersianMonth = 12
                                    } else {
                                        currentPersianMonth--
                                    }
                                },
                                onNext = {
                                    if (currentPersianMonth == 12) {
                                        currentPersianYear++
                                        currentPersianMonth = 1
                                    } else {
                                        currentPersianMonth++
                                    }
                                }
                            )

                            Spacer(Modifier.height(8.dp))
                            PersianWeekDaysHeader()
                            Spacer(Modifier.height(4.dp))

                            PersianMonthGrid(
                                year         = currentPersianYear,
                                month        = currentPersianMonth,
                                selectedDate = uiState.selectedDate,
                                tasksByDate  = uiState.tasksByDate,
                                onDateClick  = { viewModel.onDateSelected(it) }
                            )
                        }
                    }
                }

                // عنوان روز انتخابی به شمسی
                item {
                    val persianSelected = uiState.selectedDate.toPersianCalendar()
                    val dayLabel = "${persianSelected.dayOfMonth.toPersian()} ${persianMonthName(persianSelected.month)}"
                    Text(
                        text       = "وظایف $dayLabel",
                        style      = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color      = Matcha800,
                        modifier   = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                    )
                }

                if (uiState.selectedDateTasks.isEmpty()) {
                    item {
                        Column(
                            modifier            = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 32.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Text(
                                text  = "📅",
                                style = MaterialTheme.typography.displaySmall
                            )
                            Text(
                                text  = "این روز وظیفه‌ای نداری",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.outline
                            )
                        }
                    }
                } else {
                    items(uiState.selectedDateTasks, key = { it.id }) { task ->
                        CalendarTaskCard(
                            task     = task,
                            onClick  = { onTaskClick(task.id) },
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
                        )
                    }
                }

                item { Spacer(Modifier.height(80.dp)) }
            }
        }
    }
}

// ── هدر ماه شمسی ─────────────────────────────────────────

@Composable
private fun PersianMonthHeader(
    year: Int,
    month: Int,
    onPrev: () -> Unit,
    onNext: () -> Unit
) {
    Row(
        modifier              = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment     = Alignment.CenterVertically
    ) {
        IconButton(onClick = onPrev) {
            Icon(Icons.Default.KeyboardArrowLeft, contentDescription = "ماه قبل", tint = Matcha700)
        }
        Text(
            text       = "${persianMonthName(month)} ${year.toPersian()}",
            style      = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color      = Matcha800
        )
        IconButton(onClick = onNext) {
            Icon(Icons.Default.KeyboardArrowRight, contentDescription = "ماه بعد", tint = Matcha700)
        }
    }
}

// ── هدر روزهای هفته شمسی ─────────────────────────────────

@Composable
private fun PersianWeekDaysHeader() {
    // هفته شمسی از شنبه شروع میشه
    val days = listOf("ش", "ی", "د", "س", "چ", "پ", "ج")
    Row(modifier = Modifier.fillMaxWidth()) {
        days.forEach { day ->
            Text(
                text       = day,
                modifier   = Modifier.weight(1f),
                textAlign  = TextAlign.Center,
                style      = MaterialTheme.typography.labelSmall,
                color      = MaterialTheme.colorScheme.outline,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

// ── گرید روزهای ماه شمسی ─────────────────────────────────

@Composable
private fun PersianMonthGrid(
    year: Int,
    month: Int,
    selectedDate: LocalDate,
    tasksByDate: Map<LocalDate, List<Task>>,
    onDateClick: (LocalDate) -> Unit
) {
    // اولین روز ماه شمسی
    val firstDayPersian = PersianDate(year, month, 1)
    val daysInMonth     = firstDayPersian.monthLength
    val firstDayOfWeek  = firstDayPersian.dayOfWeek - 1  // dayOfWeek: شنبه=1 ... جمعه=7

    val totalCells = firstDayOfWeek + daysInMonth
    val rows       = (totalCells + 6) / 7

    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        for (row in 0 until rows) {
            Row(modifier = Modifier.fillMaxWidth()) {
                for (col in 0 until 7) {
                    val cellIndex = row * 7 + col
                    val dayNum    = cellIndex - firstDayOfWeek + 1

                    if (dayNum < 1 || dayNum > daysInMonth) {
                        Box(modifier = Modifier.weight(1f))
                    } else {
                        // تبدیل به LocalDate میلادی برای مقایسه با tasksByDate
                        val persianDay = PersianDate(year, month, dayNum)
                        val localDate  = persianDay.toLocalDate()
                        val tasks      = tasksByDate[localDate]

                        val todayPersian = LocalDate.now().toPersianCalendar()
                        val isToday = year == todayPersian.year &&
                                month == todayPersian.month &&
                                dayNum == todayPersian.dayOfMonth
                        val isSelected   = localDate == selectedDate

                        PersianDayCell(
                            day        = dayNum,
                            isSelected = isSelected,
                            isToday    = isToday,
                            hasTask    = tasks != null,
                            allDone    = tasks?.all { it.status == TaskStatus.DONE } ?: false,
                            hasOverdue = tasks?.any {
                                it.status != TaskStatus.DONE &&
                                        it.deadlineAt != null &&
                                        it.deadlineAt < System.currentTimeMillis()
                            } ?: false,
                            onClick    = { onDateClick(localDate) },
                            modifier   = Modifier.weight(1f)
                        )
                    }
                }
            }
        }
    }
}

// ── سلول روز شمسی ────────────────────────────────────────

@Composable
private fun PersianDayCell(
    day: Int,
    isSelected: Boolean,
    isToday: Boolean,
    hasTask: Boolean,
    allDone: Boolean,
    hasOverdue: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val bgColor = when {
        isSelected -> Matcha600
        isToday    -> Matcha100
        else       -> Color.Transparent
    }

    val textColor = when {
        isSelected -> Color.White
        isToday    -> Matcha700
        else       -> MaterialTheme.colorScheme.onSurface
    }

    val dotColor = when {
        !hasTask   -> Color.Transparent
        hasOverdue -> DeadlineDanger
        allDone    -> StatusDone
        else       -> StatusPending
    }

    Box(
        modifier         = modifier
            .aspectRatio(1f)
            .padding(2.dp)
            .clip(CircleShape)
            .background(bgColor)
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text       = day.toPersian(),
                color      = textColor,
                fontSize   = 13.sp,
                fontWeight = if (isToday || isSelected) FontWeight.Bold else FontWeight.Normal,
                textAlign  = TextAlign.Center
            )
            Box(
                modifier = Modifier
                    .size(4.dp)
                    .clip(CircleShape)
                    .background(
                        if (isSelected) Color.White.copy(alpha = 0.7f) else dotColor
                    )
            )
        }
    }
}

// ── کارت وظیفه ───────────────────────────────────────────

@Composable
private fun CalendarTaskCard(
    task: Task,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val now       = System.currentTimeMillis()
    val isOverdue = task.deadlineAt != null &&
            task.deadlineAt < now &&
            task.status != TaskStatus.DONE

    val (tint, accentColor) = when {
        task.status == TaskStatus.DONE   -> Pair(StatusDoneLight,    StatusDone)
        isOverdue                        -> Pair(DeadlineDangerLight, DeadlineDanger)
        task.status == TaskStatus.LOCKED -> Pair(StatusLockedLight,  StatusLocked)
        else                             -> Pair(StatusPendingLight,  StatusPending)
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .glassEffect(
                cornerRadius    = 16.dp,
                backgroundColor = GlassWhite,
                borderColor     = tint.copy(alpha = 0.6f)
            )
            .clickable { onClick() }    ) {
        Row(
            modifier              = Modifier.padding(12.dp),
            verticalAlignment     = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Box(
                modifier         = Modifier
                    .size(32.dp)
                    .clip(CircleShape)
                    .background(accentColor.copy(alpha = 0.2f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector        = if (task.status == TaskStatus.LOCKED)
                        Icons.Default.Lock
                    else
                        Icons.Default.Check,
                    contentDescription = null,
                    tint               = accentColor,
                    modifier           = Modifier.size(16.dp)
                )
            }
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text       = task.title,
                    style      = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text  = when {
                        task.status == TaskStatus.DONE   -> "انجام شده"
                        isOverdue                        -> "منقضی شده"
                        task.status == TaskStatus.LOCKED -> "قفل شده"
                        else                              -> "در انتظار"
                    },
                    style = MaterialTheme.typography.labelSmall,
                    color = accentColor
                )
            }
        }
    }
}