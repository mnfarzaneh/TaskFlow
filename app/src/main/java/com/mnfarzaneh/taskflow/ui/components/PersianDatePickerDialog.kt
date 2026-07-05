package com.mnfarzaneh.taskflow.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowLeft
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.KeyboardArrowUp
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
import androidx.compose.ui.window.Dialog
import com.mnfarzaneh.taskflow.ui.theme.Matcha100
import com.mnfarzaneh.taskflow.ui.theme.Matcha600
import com.mnfarzaneh.taskflow.ui.theme.Matcha700
import com.mnfarzaneh.taskflow.ui.theme.Matcha800
import com.mnfarzaneh.taskflow.utils.PersianDate
import com.mnfarzaneh.taskflow.utils.persianMonthName
import com.mnfarzaneh.taskflow.utils.toPersian
import com.mnfarzaneh.taskflow.utils.toPersianCalendar
import java.time.LocalDate
import java.time.ZoneId

@Composable
fun PersianDatePickerDialog(
    initialTimestamp: Long?,
    onDismiss: () -> Unit,
    onConfirm: (Long) -> Unit
) {
    val today = LocalDate.now().toPersianCalendar()
    val initial = initialTimestamp?.let {
        val ld = java.time.Instant.ofEpochMilli(it)
            .atZone(ZoneId.systemDefault()).toLocalDate()
        ld.toPersianCalendar()
    } ?: today

    var selectedYear  by remember { mutableStateOf(initial.year) }
    var selectedMonth by remember { mutableStateOf(initial.month) }
    var selectedDay   by remember { mutableStateOf(initial.dayOfMonth) }
    var hour   by remember { mutableStateOf(12) }
    var minute by remember { mutableStateOf(0) }
    var showTime by remember { mutableStateOf(false) }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(20.dp),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 4.dp
        ) {
            Column(modifier = Modifier.padding(20.dp)) {

                if (!showTime) {
                    // ── انتخاب تاریخ ─────────────────────────
                    Text(
                        text = "انتخاب تاریخ",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = Matcha800,
                        modifier = Modifier.fillMaxWidth(),
                        textAlign = TextAlign.Center
                    )

                    Spacer(Modifier.height(12.dp))

                    // هدر ماه
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(onClick = {
                            if (selectedMonth == 1) { selectedYear--; selectedMonth = 12 }
                            else selectedMonth--
                            selectedDay = minOf(selectedDay, PersianDate(selectedYear, selectedMonth, 1).monthLength)
                        }) {
                            Icon(Icons.Default.KeyboardArrowLeft, null, tint = Matcha700)
                        }
                        Text(
                            text = "${persianMonthName(selectedMonth)} ${selectedYear.toPersian()}",
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.Bold,
                            color = Matcha800
                        )
                        IconButton(onClick = {
                            if (selectedMonth == 12) { selectedYear++; selectedMonth = 1 }
                            else selectedMonth++
                            selectedDay = minOf(selectedDay, PersianDate(selectedYear, selectedMonth, 1).monthLength)
                        }) {
                            Icon(Icons.Default.KeyboardArrowRight, null, tint = Matcha700)
                        }
                    }

                    Spacer(Modifier.height(8.dp))

                    // روزهای هفته
                    val weekDays = listOf("ش", "ی", "د", "س", "چ", "پ", "ج")
                    Row(modifier = Modifier.fillMaxWidth()) {
                        weekDays.forEach { day ->
                            Text(
                                text = day,
                                modifier = Modifier.weight(1f),
                                textAlign = TextAlign.Center,
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.outline,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }

                    Spacer(Modifier.height(4.dp))

                    // گرید روزها
                    val firstDay = PersianDate(selectedYear, selectedMonth, 1)
                    val daysInMonth = firstDay.monthLength
                    val startOffset = firstDay.dayOfWeek - 1 // شنبه=0

                    val totalCells = startOffset + daysInMonth
                    val rows = (totalCells + 6) / 7

                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        for (row in 0 until rows) {
                            Row(modifier = Modifier.fillMaxWidth()) {
                                for (col in 0 until 7) {
                                    val cellIndex = row * 7 + col
                                    val dayNum = cellIndex - startOffset + 1
                                    if (dayNum < 1 || dayNum > daysInMonth) {
                                        Box(modifier = Modifier.weight(1f).aspectRatio(1f))
                                    } else {
                                        val isSelected = dayNum == selectedDay
                                        val isToday = selectedYear == today.year &&
                                                selectedMonth == today.month &&
                                                dayNum == today.dayOfMonth
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
                                        Box(
                                            modifier = Modifier
                                                .weight(1f)
                                                .aspectRatio(1f)
                                                .padding(2.dp)
                                                .clip(CircleShape)
                                                .background(bgColor)
                                                .clickable { selectedDay = dayNum },
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text(
                                                text = dayNum.toPersian(),
                                                fontSize = 13.sp,
                                                color = textColor,
                                                fontWeight = if (isSelected || isToday) FontWeight.Bold else FontWeight.Normal,
                                                textAlign = TextAlign.Center
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }

                    Spacer(Modifier.height(16.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        TextButton(onClick = onDismiss) { Text("لغو") }
                        Spacer(Modifier.width(8.dp))
                        Button(
                            onClick = { showTime = true },
                            colors = ButtonDefaults.buttonColors(containerColor = Matcha600)
                        ) { Text("بعدی") }
                    }

                } else {
                    // ── انتخاب ساعت ──────────────────────────
                    Text(
                        text = "انتخاب ساعت",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = Matcha800,
                        modifier = Modifier.fillMaxWidth(),
                        textAlign = TextAlign.Center
                    )

                    Spacer(Modifier.height(20.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // ساعت
                        NumberPicker(
                            value = hour,
                            range = 0..23,
                            onValueChange = { hour = it }
                        )
                        Text(
                            text = ":",
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 8.dp)
                        )
                        // دقیقه
                        NumberPicker(
                            value = minute,
                            range = 0..59,
                            onValueChange = { minute = it }
                        )
                    }

                    Spacer(Modifier.height(20.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        TextButton(onClick = { showTime = false }) { Text("برگشت") }
                        Spacer(Modifier.width(8.dp))
                        Button(
                            onClick = {
                                val localDate = PersianDate(selectedYear, selectedMonth, selectedDay).toLocalDate()
                                val timestamp = localDate.atTime(hour, minute)
                                    .atZone(ZoneId.systemDefault())
                                    .toInstant().toEpochMilli()
                                onConfirm(timestamp)
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Matcha600)
                        ) { Text("تأیید") }
                    }
                }
            }
        }
    }
}
@Composable
private fun NumberPicker(
    value: Int,
    range: IntRange,
    onValueChange: (Int) -> Unit
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        IconButton(onClick = {
            onValueChange(if (value == range.last) range.first else value + 1)
        }) {
            Icon(
                Icons.Default.KeyboardArrowUp,        // ← عوض شد
                contentDescription = null,
                tint = Matcha700,
                modifier = Modifier.size(28.dp)
            )
        }
        Text(
            text = value.toString().padStart(2, '0').toPersianDigits(),
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = Matcha800
        )
        IconButton(onClick = {
            onValueChange(if (value == range.first) range.last else value - 1)
        }) {
            Icon(
                Icons.Default.KeyboardArrowDown,      // ← عوض شد
                contentDescription = null,
                tint = Matcha700,
                modifier = Modifier.size(28.dp)
            )
        }
    }
}
private fun String.toPersianDigits(): String = this
    .replace('0', '۰').replace('1', '۱').replace('2', '۲')
    .replace('3', '۳').replace('4', '۴').replace('5', '۵')
    .replace('6', '۶').replace('7', '۷').replace('8', '۸')
    .replace('9', '۹')