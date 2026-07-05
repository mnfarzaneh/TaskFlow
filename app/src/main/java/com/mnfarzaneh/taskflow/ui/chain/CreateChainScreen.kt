package com.mnfarzaneh.taskflow.ui.chain

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.mnfarzaneh.taskflow.ui.components.PersianDatePickerDialog
import com.mnfarzaneh.taskflow.ui.theme.*
import com.mnfarzaneh.taskflow.utils.formatPersianDate
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateChainScreen(
    onBack: () -> Unit,
    onChainCreated: () -> Unit,
    viewModel: CreateChainViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(uiState.isDone) {
        if (uiState.isDone) onChainCreated()
    }

    GlassBackground {
        Column(modifier = Modifier.fillMaxSize()) {

            // ── هدر دستی ──────────────────────────────
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 4.dp, end = 16.dp, top = 20.dp, bottom = 8.dp),
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
                    text       = "زنجیره جدید",
                    fontWeight = FontWeight.Bold,
                    color      = Matcha800,
                    style      = MaterialTheme.typography.titleLarge,
                    modifier   = Modifier.weight(1f)
                )
                TextButton(
                    onClick = { viewModel.saveChain() },
                    enabled = !uiState.isSaving
                ) {
                    if (uiState.isSaving) {
                        CircularProgressIndicator(
                            modifier    = Modifier.size(16.dp),
                            strokeWidth = 2.dp,
                            color       = Matcha600
                        )
                    } else {
                        Text("ذخیره", fontWeight = FontWeight.Bold, color = Matcha700)
                    }
                }
            }

            LazyColumn(
                modifier            = Modifier.fillMaxSize().imePadding(),
                contentPadding      = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {

                item {
                    ChainInfoSection(
                        title         = uiState.chainTitle,
                        description   = uiState.chainDescription,
                        onTitleChange = viewModel::onChainTitleChange,
                        onDescChange  = viewModel::onChainDescriptionChange
                    )
                }

                item {
                    Row(
                        modifier              = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment     = Alignment.CenterVertically
                    ) {
                        Text(
                            text       = "وظایف زنجیره",
                            style      = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color      = Matcha800
                        )
                        Text(
                            text  = "${uiState.tasks.size} وظیفه",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.outline
                        )
                    }
                }

                itemsIndexed(uiState.tasks, key = { _, task -> task.id }) { index, task ->
                    TaskDraftCard(
                        task             = task,
                        index            = index,
                        canRemove        = uiState.tasks.size > 1,
                        onTitleChange    = { viewModel.onTaskTitleChange(task.id, it) },
                        onDescChange     = { viewModel.onTaskDescriptionChange(task.id, it) },
                        onDeadlineChange = { viewModel.onTaskDeadlineChange(task.id, it) },
                        onRemove         = { viewModel.removeTask(task.id) }
                    )
                }

                item {
                    OutlinedButton(
                        onClick  = { viewModel.addTask() },
                        modifier = Modifier.fillMaxWidth(),
                        colors   = ButtonDefaults.outlinedButtonColors(contentColor = Matcha700)
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null)
                        Spacer(Modifier.width(8.dp))
                        Text("افزودن وظیفه")
                    }
                }

                uiState.error?.let { error ->
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .glassEffect(
                                    cornerRadius    = 14.dp,
                                    backgroundColor = DeadlineDangerLight,
                                    borderColor     = DeadlineDanger.copy(alpha = 0.3f)
                                )
                        ) {
                            Text(
                                text     = error,
                                color    = DeadlineDanger,
                                modifier = Modifier.padding(12.dp)
                            )
                        }
                    }
                }

                item { Spacer(Modifier.height(24.dp)) }
            }
        }
    }
}

// ── بخش اطلاعات زنجیره ───────────────────────────────────

@Composable
private fun ChainInfoSection(
    title: String,
    description: String,
    onTitleChange: (String) -> Unit,
    onDescChange: (String) -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .glassMatchaEffect(cornerRadius = 20.dp)
    ) {
        Column(
            modifier            = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text       = "اطلاعات زنجیره",
                style      = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color      = Matcha800
            )
            OutlinedTextField(
                value           = title,
                onValueChange   = onTitleChange,
                label           = { Text("عنوان زنجیره") },
                placeholder     = { Text("مثلاً: راه‌اندازی پروژه") },
                modifier        = Modifier.fillMaxWidth(),
                singleLine      = true,
                keyboardOptions = KeyboardOptions(
                    capitalization = KeyboardCapitalization.Sentences
                ),
                colors = matchaTextFieldColors()
            )
            OutlinedTextField(
                value         = description,
                onValueChange = onDescChange,
                label         = { Text("توضیحات (اختیاری)") },
                modifier      = Modifier.fillMaxWidth(),
                minLines      = 2,
                maxLines      = 3,
                colors        = matchaTextFieldColors()
            )
        }
    }
}

// ── کارت پیش‌نویس وظیفه ──────────────────────────────────

@Composable
private fun TaskDraftCard(
    task: TaskDraft,
    index: Int,
    canRemove: Boolean,
    onTitleChange: (String) -> Unit,
    onDescChange: (String) -> Unit,
    onDeadlineChange: (Long?) -> Unit,
    onRemove: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .glassEffect(cornerRadius = 18.dp)
    ) {
        Column(
            modifier            = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {

            Row(
                modifier              = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment     = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment     = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Surface(
                        shape = MaterialTheme.shapes.small,
                        color = Matcha100
                    ) {
                        Text(
                            text       = "${index + 1}",
                            style      = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color      = Matcha700,
                            modifier   = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                    Text(
                        text       = "وظیفه ${index + 1}",
                        style      = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Medium
                    )
                    if (index == 0) {
                        Surface(
                            shape = MaterialTheme.shapes.small,
                            color = Matcha50
                        ) {
                            Text(
                                text     = "اول",
                                style    = MaterialTheme.typography.labelSmall,
                                color    = Matcha600,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }
                }
                if (canRemove) {
                    IconButton(onClick = onRemove, modifier = Modifier.size(32.dp)) {
                        Icon(
                            Icons.Default.Close,
                            contentDescription = "حذف وظیفه",
                            tint               = MaterialTheme.colorScheme.outline,
                            modifier           = Modifier.size(18.dp)
                        )
                    }
                }
            }

            OutlinedTextField(
                value           = task.title,
                onValueChange   = onTitleChange,
                label           = { Text("عنوان وظیفه") },
                modifier        = Modifier.fillMaxWidth(),
                singleLine      = true,
                keyboardOptions = KeyboardOptions(
                    capitalization = KeyboardCapitalization.Sentences
                ),
                colors = matchaTextFieldColors()
            )

            OutlinedTextField(
                value         = task.description,
                onValueChange = onDescChange,
                label         = { Text("توضیحات (اختیاری)") },
                modifier      = Modifier.fillMaxWidth(),
                maxLines      = 2,
                colors        = matchaTextFieldColors()
            )

            DeadlinePicker(
                deadlineAt       = task.deadlineAt,
                onDeadlineChange = onDeadlineChange
            )
        }
    }
}

// ── انتخاب ددلاین ─────────────────────────────────────────

@Composable
private fun DeadlinePicker(
    deadlineAt: Long?,
    onDeadlineChange: (Long?) -> Unit
) {
    var showDialog by remember { mutableStateOf(false) }

    if (showDialog) {
        PersianDatePickerDialog(
            initialTimestamp = deadlineAt,
            onDismiss = { showDialog = false },
            onConfirm = { timestamp ->
                onDeadlineChange(timestamp)
                showDialog = false
            }
        )
    }

    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        OutlinedButton(
            onClick = { showDialog = true },
            modifier = Modifier.weight(1f),
            colors = ButtonDefaults.outlinedButtonColors(contentColor = Matcha700)
        ) {
            Icon(Icons.Default.DateRange, contentDescription = null, modifier = Modifier.size(16.dp))
            Spacer(Modifier.width(6.dp))
            Text(
                text = if (deadlineAt != null) formatPersianDate(deadlineAt) else "تعیین ددلاین",
                style = MaterialTheme.typography.bodySmall
            )
        }

        if (deadlineAt != null) {
            IconButton(
                onClick = { onDeadlineChange(null) },
                modifier = Modifier.size(36.dp)
            ) {
                Icon(
                    Icons.Default.Close,
                    contentDescription = "حذف ددلاین",
                    tint = MaterialTheme.colorScheme.outline,
                    modifier = Modifier.size(18.dp)
                )
            }
        }
    }
}

// ── رنگ مشترک TextField ─────────────────────────────────

@Composable
fun matchaTextFieldColors() = OutlinedTextFieldDefaults.colors(
    focusedBorderColor   = Matcha500,
    unfocusedBorderColor = GlassBorderDark,
    focusedLabelColor    = Matcha600,
    cursorColor          = Matcha600
)