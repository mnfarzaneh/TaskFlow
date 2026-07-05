package com.mnfarzaneh.taskflow.ui.chain

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import androidx.compose.animation.*
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.mnfarzaneh.taskflow.ui.components.PersianDatePickerDialog
import com.mnfarzaneh.taskflow.ui.theme.*
import com.mnfarzaneh.taskflow.utils.formatPersianDate
import sh.calvin.reorderable.ReorderableItem
import sh.calvin.reorderable.rememberReorderableLazyListState
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditChainScreen(
    onBack: () -> Unit,
    onSaved: () -> Unit,
    viewModel: EditChainViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var expandedTaskId by remember { mutableStateOf<Int?>(null) }

    LaunchedEffect(uiState.isDone) {
        if (uiState.isDone) onSaved()
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
                    text       = "ویرایش زنجیره",
                    fontWeight = FontWeight.Bold,
                    color      = Matcha800,
                    style      = MaterialTheme.typography.titleLarge,
                    modifier   = Modifier.weight(1f)
                )
                TextButton(
                    onClick = { viewModel.saveChanges() },
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

            if (uiState.isLoading) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = Matcha600)
                }
                return@Column
            }

            val lazyListState = rememberLazyListState()
            val haptic         = LocalHapticFeedback.current
            val displayTasks by viewModel.displayTasks.collectAsStateWithLifecycle()

            val reorderableState = rememberReorderableLazyListState(
                lazyListState          = lazyListState,
                scrollThresholdPadding = PaddingValues(vertical = 80.dp)
            ) { from, to ->
                val fromIndex = from.index - 2
                val toIndex   = to.index - 2
                if (fromIndex >= 0 && toIndex >= 0) {
                    viewModel.onDragMove(fromIndex, toIndex)
                }
            }

            LazyColumn(
                state               = lazyListState,
                modifier            = Modifier.fillMaxSize().imePadding(),
                contentPadding      = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {

                item {
                    EditChainInfoSection(
                        title         = uiState.chainTitle,
                        description   = uiState.chainDescription,
                        onTitleChange = viewModel::onChainTitleChange,
                        onDescChange  = viewModel::onChainDescriptionChange
                    )
                    Spacer(Modifier.height(8.dp))
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

                itemsIndexed(displayTasks, key = { _, task -> task.id }) { index, task ->
                    val isExpanded = expandedTaskId == task.id

                    ReorderableItem(reorderableState, key = task.id) { isDragging ->
                        val elevation by animateDpAsState(
                            targetValue = if (isDragging) 8.dp else 0.dp,
                            label       = "elevation"
                        )

                        CompactTaskRow(
                            task       = task,
                            index      = index,
                            isExpanded = isExpanded,
                            isDragging = isDragging,
                            elevation  = elevation,
                            dragHandle = {
                                IconButton(
                                    modifier = Modifier
                                        .draggableHandle(
                                            onDragStarted = {
                                                viewModel.onDragStart()
                                                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                                expandedTaskId = null
                                            },
                                            onDragStopped = {
                                                viewModel.onDragEnd()
                                                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                            }
                                        )
                                        .size(40.dp),
                                    onClick = {}
                                ) {
                                    Icon(
                                        Icons.Default.Menu,
                                        contentDescription = "جابجایی",
                                        tint     = Matcha600,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                            },
                            onToggleExpand   = { expandedTaskId = if (isExpanded) null else task.id },
                            onTitleChange    = { viewModel.onTaskTitleChange(task.id, it) },
                            onDescChange     = { viewModel.onTaskDescriptionChange(task.id, it) },
                            onDeadlineChange = { viewModel.onTaskDeadlineChange(task.id, it) },
                            onRemove         = {
                                if (isExpanded) expandedTaskId = null
                                viewModel.removeTask(task.id)
                            }
                        )
                    }
                }

                item {
                    OutlinedButton(
                        onClick  = {
                            viewModel.addTask()
                            expandedTaskId = (uiState.tasks.maxOfOrNull { it.id } ?: 0) + 1
                        },
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

                item { Spacer(Modifier.height(80.dp)) }
            }
        }
    }
}

// ── ردیف compact وظیفه ───────────────────────────────────

@Composable
private fun CompactTaskRow(
    task: TaskDraft,
    index: Int,
    isExpanded: Boolean,
    isDragging: Boolean,
    elevation: androidx.compose.ui.unit.Dp,
    dragHandle: @Composable () -> Unit,
    onToggleExpand: () -> Unit,
    onTitleChange: (String) -> Unit,
    onDescChange: (String) -> Unit,
    onDeadlineChange: (Long?) -> Unit,
    onRemove: () -> Unit
) {
    val dateFormat = remember { SimpleDateFormat("yyyy/MM/dd HH:mm", Locale.getDefault()) }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .let {
                if (isDragging)
                    it.glassEffect(
                        cornerRadius    = 16.dp,
                        backgroundColor = Matcha100.copy(alpha = 0.85f),
                        borderColor     = Matcha500
                    )
                else
                    it.glassEffect(cornerRadius = 16.dp)
            }
    ) {
        Column {

            Row(
                modifier          = Modifier
                    .fillMaxWidth()
                    .padding(start = 4.dp, end = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                dragHandle()

                Surface(
                    shape    = MaterialTheme.shapes.small,
                    color    = Matcha100,
                    modifier = Modifier.padding(end = 8.dp)
                ) {
                    Text(
                        text       = "${index + 1}",
                        style      = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color      = Matcha700,
                        modifier   = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }

                Text(
                    text     = task.title.ifEmpty { "وظیفه ${index + 1}" },
                    style    = MaterialTheme.typography.bodyMedium,
                    color    = if (task.title.isEmpty())
                        MaterialTheme.colorScheme.outline
                    else
                        MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.weight(1f),
                    maxLines = 1
                )

                if (task.deadlineAt != null) {
                    Icon(
                        Icons.Default.DateRange,
                        contentDescription = null,
                        tint     = Matcha600,
                        modifier = Modifier.size(16.dp).padding(end = 4.dp)
                    )
                }

                IconButton(onClick = onToggleExpand, modifier = Modifier.size(40.dp)) {
                    Icon(
                        imageVector        = if (isExpanded)
                            Icons.Default.KeyboardArrowUp
                        else
                            Icons.Default.KeyboardArrowDown,
                        contentDescription = if (isExpanded) "بستن" else "ویرایش",
                        tint               = MaterialTheme.colorScheme.outline,
                        modifier           = Modifier.size(20.dp)
                    )
                }
            }

            AnimatedVisibility(
                visible = isExpanded,
                enter   = expandVertically() + fadeIn(),
                exit    = shrinkVertically() + fadeOut()
            ) {
                Column(
                    modifier            = Modifier
                        .fillMaxWidth()
                        .padding(start = 16.dp, end = 16.dp, bottom = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    HorizontalDivider(color = GlassBorderDark)

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

                    DeadlinePickerRow(
                        deadlineAt       = task.deadlineAt,
                        dateFormat       = dateFormat,
                        onDeadlineChange = onDeadlineChange
                    )

                    TextButton(
                        onClick = onRemove,
                        colors  = ButtonDefaults.textButtonColors(
                            contentColor = MaterialTheme.colorScheme.error
                        ),
                        modifier = Modifier.align(Alignment.End)
                    ) {
                        Icon(Icons.Default.Delete, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(Modifier.width(4.dp))
                        Text("حذف وظیفه")
                    }
                }
            }
        }
    }
}

// ── انتخاب ددلاین ─────────────────────────────────────────
@Composable
private fun DeadlinePickerRow(
    deadlineAt: Long?,
    dateFormat: SimpleDateFormat,
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
        modifier              = Modifier.fillMaxWidth(),
        verticalAlignment     = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        OutlinedButton(
            onClick  = { showDialog = true },
            modifier = Modifier.weight(1f),
            colors   = ButtonDefaults.outlinedButtonColors(contentColor = Matcha700)
        ) {
            Icon(Icons.Default.DateRange, contentDescription = null, modifier = Modifier.size(16.dp))
            Spacer(Modifier.width(6.dp))
            Text(
                text  = if (deadlineAt != null) formatPersianDate(deadlineAt) else "تعیین ددلاین",
                style = MaterialTheme.typography.bodySmall
            )
        }

        if (deadlineAt != null) {
            IconButton(
                onClick  = { onDeadlineChange(null) },
                modifier = Modifier.size(36.dp)
            ) {
                Icon(
                    Icons.Default.Close,
                    contentDescription = "حذف ددلاین",
                    tint               = MaterialTheme.colorScheme.outline,
                    modifier           = Modifier.size(18.dp)
                )
            }
        }
    }
}

// ── بخش اطلاعات زنجیره ───────────────────────────────────

@Composable
private fun EditChainInfoSection(
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