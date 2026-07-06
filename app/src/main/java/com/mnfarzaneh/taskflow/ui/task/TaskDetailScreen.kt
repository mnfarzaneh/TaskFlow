package com.mnfarzaneh.taskflow.ui.task

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.mnfarzaneh.taskflow.domain.model.Task
import com.mnfarzaneh.taskflow.domain.model.TaskStatus
import com.mnfarzaneh.taskflow.ui.theme.*
import java.text.SimpleDateFormat
import java.util.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.filled.Warning
import com.mnfarzaneh.taskflow.ui.chain.matchaTextFieldColors
import com.mnfarzaneh.taskflow.utils.formatPersianDate
import com.mnfarzaneh.taskflow.utils.toPersian
import com.mnfarzaneh.taskflow.ui.components.PersianDatePickerDialog

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TaskDetailScreen(
    onBack: () -> Unit,
    viewModel: TaskViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(uiState.reminderSet) {
        if (uiState.reminderSet) {
            snackbarHostState.showSnackbar("یادآوری تنظیم شد ✓")
            viewModel.clearReminderSetFlag()
        }
    }
    LaunchedEffect(uiState.isDone) {
        if (uiState.isDone) onBack()
    }

    GlassBackground {
        Box(modifier = Modifier.fillMaxSize()) {

            Column(modifier = Modifier.fillMaxSize()) {

                // ── هدر دستی ──────────────────────────────
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 4.dp, end = 12.dp, top = 20.dp, bottom = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = onBack) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "برگشت",
                            tint = Matcha800
                        )
                    }
                    Text(
                        text       = "جزئیات وظیفه",
                        fontWeight = FontWeight.Bold,
                        color      = Matcha800,
                        style      = MaterialTheme.typography.titleLarge
                    )
                }

                when {
                    uiState.isLoading -> {
                        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            CircularProgressIndicator(color = Matcha600)
                        }
                    }
                    uiState.task == null -> {
                        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Text("وظیفه پیدا نشد", color = MaterialTheme.colorScheme.outline)
                        }
                    }
                    else -> {
                        TaskDetailContent(
                            task             = uiState.task!!,
                            onComplete       = { viewModel.completeTask() },
                            onStart          = { viewModel.startTask() },
                            onSetReminder    = { viewModel.setReminder(it) },
                            onRemoveReminder = { viewModel.removeReminder() },
                            onMarkRevision   = { viewModel.markForRevision(it) },
                            onClearRevision  = { viewModel.clearRevisionFlag() }
                        )
                    }
                }
            }

            SnackbarHost(
                hostState = snackbarHostState,
                modifier  = Modifier.align(Alignment.BottomCenter)
            )
        }
    }
}

@Composable
private fun TaskDetailContent(
    task: Task,
    onComplete: () -> Unit,
    onStart: () -> Unit,
    onSetReminder: (Long) -> Unit,
    onRemoveReminder: () -> Unit,
    onMarkRevision: (String) -> Unit,      // ← اضافه شد
    onClearRevision: () -> Unit
) {
    val dateFormat = remember { SimpleDateFormat("yyyy/MM/dd HH:mm", Locale.getDefault()) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {

        StatusCard(status = task.status)


        // بعد از StatusCard اضافه کن
        if (task.status == TaskStatus.DONE) {
            RevisionSection(
                needsRevision = task.needsRevision,
                revisionNote  = task.revisionNote,
                onMark        = onMarkRevision,
                onClear       = onClearRevision
            )
        }

        // ── اطلاعات اصلی ────────────────────────────────
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .glassEffect(cornerRadius = 20.dp)
        ) {
            Column(
                modifier            = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text       = task.title,
                    style      = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color      = Matcha800
                )

                if (task.description.isNotEmpty()) {
                    HorizontalDivider(color = GlassBorderDark)
                    Text(
                        text  = task.description,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                HorizontalDivider(color = GlassBorderDark)

                InfoRow(label = "ترتیب در زنجیره", value = "وظیفه ${(task.order + 1).toPersian()}")
                InfoRow(
                    label = "تاریخ ساخت",
                    value = formatPersianDate(task.createdAt)
                )            }
        }

        // ── ددلاین ───────────────────────────────────────
        task.deadlineAt?.let { deadline ->
            DeadlineCard(deadlineAt = deadline, dateFormat = dateFormat)
        }

        // ── reminder ─────────────────────────────────────
        if (task.status != TaskStatus.DONE && task.status != TaskStatus.LOCKED) {
            ReminderSection(
                reminderAt       = task.reminderAt,
                onSetReminder    = onSetReminder,
                onRemoveReminder = onRemoveReminder
            )
        } else if (task.reminderAt != null) {
            // فقط نمایش وقتی DONE یا LOCKED هست و reminder ثبت شده بود
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .glassEffect(
                        cornerRadius    = 18.dp,
                        backgroundColor = StatusPendingLight,
                        borderColor     = StatusPending.copy(alpha = 0.3f)
                    )
            ) {
                Row(
                    modifier              = Modifier.padding(16.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment     = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.Check, contentDescription = null, tint = StatusPending)
                    Column {
                        Text(
                            text       = "یادآوری",
                            style      = MaterialTheme.typography.labelMedium,
                            color      = StatusPending,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text  = dateFormat.format(Date(task.reminderAt)),
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
            }
        }

        Spacer(Modifier.height(8.dp))

        ActionButtons(
            status     = task.status,
            onComplete = onComplete,
            onStart    = onStart
        )

        Spacer(Modifier.height(24.dp))
    }
}

// ── کارت وضعیت ───────────────────────────────────────────

@Composable
private fun StatusCard(status: TaskStatus) {
    val (tint, textColor, label, icon) = when (status) {
        TaskStatus.DONE        -> Quadruple(StatusDoneLight,    StatusDone,    "انجام شده",    Icons.Default.Check)
        TaskStatus.IN_PROGRESS -> Quadruple(StatusActiveLight,  StatusActive,  "در حال انجام", Icons.Default.Check)
        TaskStatus.PENDING     -> Quadruple(StatusPendingLight, StatusPending, "آماده انجام",  Icons.Default.Check)
        TaskStatus.LOCKED      -> Quadruple(StatusLockedLight,  StatusLocked,  "قفل شده",      Icons.Default.Lock)
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .glassEffect(
                cornerRadius    = 18.dp,
                backgroundColor = tint,
                borderColor     = textColor.copy(alpha = 0.3f)
            )
    ) {
        Row(
            modifier              = Modifier.padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment     = Alignment.CenterVertically
        ) {
            Icon(icon, contentDescription = null, tint = textColor)
            Text(
                text       = label,
                style      = MaterialTheme.typography.titleMedium,
                color      = textColor,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

// ── کارت ددلاین ──────────────────────────────────────────

@Composable
private fun DeadlineCard(
    deadlineAt: Long,
    dateFormat: SimpleDateFormat
) {
    val now      = System.currentTimeMillis()
    val diff     = deadlineAt - now
    val diffDays = diff / (1000 * 60 * 60 * 24)

    val (tint, textColor, label) = when {
        diff < 0      -> Triple(DeadlineDangerLight,  DeadlineDanger,  "منقضی شده!")
        diffDays < 1  -> Triple(DeadlineDangerLight,  DeadlineDanger,  "امروز!")
        diffDays < 3  -> Triple(DeadlineWarningLight, DeadlineWarning, "${diffDays.toPersian()} روز مانده")
        else          -> Triple(StatusPendingLight,   StatusPending,   "${diffDays.toPersian()} روز مانده")
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .glassEffect(
                cornerRadius    = 18.dp,
                backgroundColor = tint,
                borderColor     = textColor.copy(alpha = 0.3f)
            )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier              = Modifier.fillMaxWidth()
            ) {
                Text(
                    text       = "ددلاین",
                    style      = MaterialTheme.typography.labelMedium,
                    color      = textColor,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text       = label,
                    style      = MaterialTheme.typography.labelMedium,
                    color      = textColor,
                    fontWeight = FontWeight.Bold
                )
            }
            Spacer(Modifier.height(4.dp))
            // تاریخ شمسی
            Text(
                text  = formatPersianDate(deadlineAt),
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}

// ── ردیف اطلاعات ─────────────────────────────────────────

@Composable
private fun InfoRow(label: String, value: String) {
    Row(
        modifier              = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text  = label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.outline
        )
        Text(
            text       = value,
            style      = MaterialTheme.typography.bodySmall,
            fontWeight = FontWeight.Medium
        )
    }
}

// ── دکمه‌های عملیات ───────────────────────────────────────

@Composable
private fun ActionButtons(
    status: TaskStatus,
    onComplete: () -> Unit,
    onStart: () -> Unit
) {
    when (status) {
        TaskStatus.LOCKED -> {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .glassEffect(
                        cornerRadius    = 18.dp,
                        backgroundColor = StatusLockedLight,
                        borderColor     = StatusLocked.copy(alpha = 0.3f)
                    )
            ) {
                Row(
                    modifier              = Modifier.padding(16.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment     = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.Lock, contentDescription = null, tint = StatusLocked)
                    Text(
                        text  = "وظیفه قفله — ابتدا وظیفه قبلی رو انجام بده",
                        style = MaterialTheme.typography.bodySmall,
                        color = StatusLocked
                    )
                }
            }
        }

        TaskStatus.PENDING -> {
            Row(
                modifier              = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedButton(
                    onClick  = onStart,
                    modifier = Modifier.weight(1f),
                    colors   = ButtonDefaults.outlinedButtonColors(contentColor = Matcha700)
                ) {
                    Text("شروع کن")
                }
                Button(
                    onClick  = onComplete,
                    modifier = Modifier.weight(1f),
                    colors   = ButtonDefaults.buttonColors(containerColor = Matcha600)
                ) {
                    Icon(Icons.Default.Check, contentDescription = null)
                    Spacer(Modifier.width(6.dp))
                    Text("انجام شد")
                }
            }
        }

        TaskStatus.IN_PROGRESS -> {
            Button(
                onClick  = onComplete,
                modifier = Modifier.fillMaxWidth(),
                colors   = ButtonDefaults.buttonColors(containerColor = StatusDone)
            ) {
                Icon(Icons.Default.Check, contentDescription = null)
                Spacer(Modifier.width(8.dp))
                Text("تکمیل وظیفه", fontWeight = FontWeight.Bold)
            }
        }

        TaskStatus.DONE -> {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .glassEffect(
                        cornerRadius    = 18.dp,
                        backgroundColor = StatusDoneLight,
                        borderColor     = StatusDone.copy(alpha = 0.3f)
                    )
            ) {
                Row(
                    modifier              = Modifier.padding(16.dp),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment     = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.Check, contentDescription = null, tint = StatusDone)
                    Spacer(Modifier.width(8.dp))
                    Text(
                        text       = "این وظیفه انجام شده",
                        color      = StatusDone,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

// ── بخش تنظیم یادآوری ────────────────────────────────────
@Composable
private fun ReminderSection(
    reminderAt: Long?,
    onSetReminder: (Long) -> Unit,
    onRemoveReminder: () -> Unit
) {
    var showDialog by remember { mutableStateOf(false) }

    if (showDialog) {
        PersianDatePickerDialog(
            initialTimestamp = reminderAt,
            onDismiss        = { showDialog = false },
            onConfirm        = { timestamp ->
                if (timestamp > System.currentTimeMillis()) {
                    onSetReminder(timestamp)
                }
                showDialog = false
            }
        )
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .glassEffect(cornerRadius = 18.dp)
    ) {
        Column(
            modifier            = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Text(
                text       = "یادآوری",
                style      = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color      = Matcha700
            )

            if (reminderAt != null) {
                Row(
                    modifier              = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment     = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text       = "یادآوری فعال",
                            style      = MaterialTheme.typography.bodySmall,
                            color      = StatusPending,
                            fontWeight = FontWeight.Medium
                        )
                        Text(
                            text  = formatPersianDate(reminderAt),
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedButton(
                            onClick = { showDialog = true },
                            colors  = ButtonDefaults.outlinedButtonColors(contentColor = Matcha700)
                        ) {
                            Text("تغییر", style = MaterialTheme.typography.labelMedium)
                        }
                        OutlinedButton(
                            onClick = onRemoveReminder,
                            colors  = ButtonDefaults.outlinedButtonColors(
                                contentColor = MaterialTheme.colorScheme.error
                            )
                        ) {
                            Text("حذف", style = MaterialTheme.typography.labelMedium)
                        }
                    }
                }
            } else {
                OutlinedButton(
                    onClick  = { showDialog = true },
                    modifier = Modifier.fillMaxWidth(),
                    colors   = ButtonDefaults.outlinedButtonColors(contentColor = Matcha700)
                ) {
                    Icon(
                        Icons.Default.Check,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(Modifier.width(8.dp))
                    Text("تنظیم یادآوری")
                }
            }
        }
    }
}

@Composable
private fun RevisionSection(
    needsRevision: Boolean,
    revisionNote: String?,
    onMark: (String) -> Unit,
    onClear: () -> Unit
) {
    var showDialog by remember { mutableStateOf(false) }
    var noteText   by remember { mutableStateOf("") }

    if (showDialog) {
        AlertDialog(
            onDismissRequest = { showDialog = false },
            title   = { Text("علامت‌گذاری برای اصلاح") },
            text    = {
                Column {
                    Text(
                        "چرا این وظیفه نیاز به بازبینی داره؟",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.outline
                    )
                    Spacer(Modifier.height(8.dp))
                    OutlinedTextField(
                        value         = noteText,
                        onValueChange = { noteText = it },
                        placeholder   = { Text("مثلاً: نیاز به بهبود کیفیت") },
                        modifier      = Modifier.fillMaxWidth(),
                        minLines      = 2,
                        maxLines      = 4,
                        colors        = matchaTextFieldColors()
                    )
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        if (noteText.isNotBlank()) {
                            onMark(noteText)
                            showDialog = false
                            noteText = ""
                        }
                    }
                ) {
                    Text("ثبت", color = DeadlineWarning, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDialog = false }) {
                    Text("لغو")
                }
            }
        )
    }

    if (needsRevision) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .glassEffect(
                    cornerRadius    = 18.dp,
                    backgroundColor = DeadlineWarningLight,
                    borderColor     = DeadlineWarning.copy(alpha = 0.4f)
                )
        ) {
            Column(
                modifier            = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Row(
                    verticalAlignment     = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        Icons.Default.Warning,
                        contentDescription = null,
                        tint = DeadlineWarning
                    )
                    Text(
                        text       = "نیاز به بازبینی",
                        style      = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color      = DeadlineWarning
                    )
                }
                if (!revisionNote.isNullOrEmpty()) {
                    Text(
                        text  = revisionNote,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                TextButton(
                    onClick  = onClear,
                    modifier = Modifier.align(Alignment.End),
                    colors   = ButtonDefaults.textButtonColors(contentColor = StatusDone)
                ) {
                    Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(4.dp))
                    Text("اصلاح شد — برطرف کن")
                }
            }
        }
    } else {
        OutlinedButton(
            onClick  = { showDialog = true },
            modifier = Modifier.fillMaxWidth(),
            colors   = ButtonDefaults.outlinedButtonColors(contentColor = DeadlineWarning)
        ) {
            Icon(Icons.Default.Warning, contentDescription = null, modifier = Modifier.size(16.dp))
            Spacer(Modifier.width(8.dp))
            Text("علامت‌گذاری برای اصلاح")
        }
    }
}

// ── helper ────────────────────────────────────────────────

private data class Quadruple<A, B, C, D>(val first: A, val second: B, val third: C, val fourth: D)