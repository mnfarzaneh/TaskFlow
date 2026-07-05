package com.mnfarzaneh.taskflow.ui.home

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.mnfarzaneh.taskflow.domain.model.Chain
import com.mnfarzaneh.taskflow.ui.theme.*
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.material3.rememberSwipeToDismissBoxState
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.draw.clip
import com.mnfarzaneh.taskflow.ui.theme.DeadlineDangerLight
import com.mnfarzaneh.taskflow.ui.theme.StatusDoneLight
import com.mnfarzaneh.taskflow.ui.theme.StatusDone
import com.mnfarzaneh.taskflow.ui.theme.DeadlineDanger
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import com.mnfarzaneh.taskflow.domain.model.Task
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import com.mnfarzaneh.taskflow.ui.theme.GlassBackground
import com.mnfarzaneh.taskflow.ui.theme.glassEffect
import com.mnfarzaneh.taskflow.ui.theme.glassMatchaEffect
import com.mnfarzaneh.taskflow.ui.theme.Matcha800
import com.mnfarzaneh.taskflow.ui.theme.Matcha600
import androidx.compose.foundation.clickable
import androidx.compose.foundation.shape.RoundedCornerShape
import com.mnfarzaneh.taskflow.utils.toPersian
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.filled.Check
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDirection
import com.mnfarzaneh.taskflow.utils.toPersian
import androidx.compose.foundation.Image
import androidx.compose.ui.res.painterResource
import com.mnfarzaneh.taskflow.R
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    onChainClick: (Long) -> Unit,
    onAddChain: () -> Unit,
    viewModel: HomeViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

        Box(modifier = Modifier.fillMaxSize()) {
            Column(modifier = Modifier.fillMaxSize()) {
                // ── هدر دستی به جای TopAppBar ──────────
                Text(
                    text       = "TaskFlow",
                    fontWeight = FontWeight.Bold,
                    color      = Matcha800,
                    style      = MaterialTheme.typography.headlineSmall,
                    modifier   = Modifier.padding(start = 20.dp, top = 24.dp, bottom = 8.dp)
                )
                if (uiState.isLoading) {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator()
                    }
                } else {
                    LazyColumn(
                        modifier        = Modifier.fillMaxSize(),
                        contentPadding  = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        item {
                            StatsRow(
                                stats          = uiState.stats,
                                onDoneClick    = { viewModel.showDoneSheet() },
                                onOverdueClick = { viewModel.showOverdueSheet() }
                            )
                        }
                        if (uiState.chains.isNotEmpty()) {
                            item {
                                Text(
                                    text       = "زنجیره‌های من",
                                    style      = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    modifier   = Modifier.padding(vertical = 4.dp)
                                )
                            }
                        }
                        if (uiState.chains.isEmpty()) {
                            item { EmptyState() }
                        } else {
                            items(uiState.chainProgresses, key = { it.chain.id }) { progress ->
                                SwipeableChainCard(
                                    chain       = progress.chain,
                                    progress    = progress,
                                    onClick     = { onChainClick(progress.chain.id) },
                                    onDelete    = { viewModel.deleteChain(progress.chain) },
                                    onDuplicate = { viewModel.duplicateChain(progress.chain.id) }
                                )
                            }
                        }
                        // فضای کافی برای اینکه FAB روی آخرین آیتم نیفته
                        item { Spacer(Modifier.height(88.dp)) }
                    }
                }
            }
            // ── FAB شناور ───────────────────────────────
            FloatingActionButton(
                onClick        = onAddChain,
                containerColor = Matcha600,
                contentColor   = Color.White,
                shape          = RoundedCornerShape(16.dp),
                modifier       = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(end = 20.dp, bottom = 2.dp)  // ← bottom رو از 20 به 12 کم کردم
            ) {
                Icon(Icons.Default.Add, contentDescription = "زنجیره جدید")
            }

            // ── Done Sheet ────────────────────────────────────────────
            if (uiState.showDoneSheet) {
                TaskListBottomSheet(
                    title       = "ددلاین‌های انجام شده",
                    tasks       = uiState.doneTasks,
                    color       = StatusDone,
                    onDismiss   = { viewModel.hideSheets() },
                    onTaskClick = onChainClick
                )
            }

            // ── Overdue Sheet ─────────────────────────────────────────
            if (uiState.showOverdueSheet) {
                TaskListBottomSheet(
                    title       = "ددلاین‌های منقضی",
                    tasks       = uiState.overdueTasks,
                    color       = DeadlineDanger,
                    onDismiss   = { viewModel.hideSheets() },
                    onTaskClick = onChainClick
                )
            }
        }

}

// ── ردیف آمار ─────────────────────────────────────────────

@Composable
private fun StatsRow(
    stats: HomeStats,
    onDoneClick: () -> Unit,
    onOverdueClick: () -> Unit
) {
    Row(
        modifier              = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        StatCard(
            modifier = Modifier.weight(1f),
            number   = stats.totalChains.toPersian(),
            label    = "زنجیره",
            color    = StatusActive,
            onClick  = null
        )
        StatCard(
            modifier = Modifier.weight(1f),
            number   = stats.doneTasks.toPersian(),
            label    = "ددلاین انجام شده",
            color    = StatusDone,
            onClick  = onDoneClick
        )
        StatCard(
            modifier = Modifier.weight(1f),
            number   = stats.overdueTasks.toPersian(),
            label    = "ددلاین منقضی",
            color    = if (stats.overdueTasks > 0) DeadlineDanger else StatusLocked,
            onClick  = if (stats.overdueTasks > 0) onOverdueClick else null
        )
    }
}
@Composable
private fun StatCard(
    number: String,
    label: String,
    color: androidx.compose.ui.graphics.Color,
    onClick: (() -> Unit)?,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .glassEffect(cornerRadius = 16.dp)
            .then(
                if (onClick != null)
                    Modifier.clickable { onClick() }
                else Modifier
            )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text       = number,
                style      = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color      = color
            )
            Text(
                text  = label,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )
        }
    }
}

// ── کارت زنجیره ───────────────────────────────────────────
@Composable
private fun ChainCard(
    chain: Chain,
    progress: ChainProgress,
    onClick: () -> Unit,
    onDelete: () -> Unit
) {
    var showDeleteDialog by remember { mutableStateOf(false) }

    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title   = { Text("حذف زنجیره") },
            text    = { Text("زنجیره «${chain.title}» حذف بشه؟") },
            confirmButton = {
                TextButton(onClick = {
                    onDelete()
                    showDeleteDialog = false
                }) {
                    Text("حذف", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) { Text("لغو") }
            }
        )
    }

    // رنگ progress bar بر اساس درصد
    val progressColor = when {
        progress.isDone          -> StatusDone
        progress.progress > 0.6f -> Matcha500
        progress.progress > 0.3f -> Matcha400
        else                     -> Matcha300
    }

    val progressBg = when {
        progress.isDone -> StatusDoneLight
        else            -> Matcha100.copy(alpha = 0.5f)
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .glassEffect(cornerRadius = 18.dp)
            .clickable { onClick() }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier              = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment     = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text       = chain.title,
                        style      = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        color      = if (progress.isDone) StatusDone
                        else MaterialTheme.colorScheme.onSurface
                    )
                    if (chain.description.isNotEmpty()) {
                        Spacer(Modifier.height(2.dp))
                        Text(
                            text     = chain.description,
                            style    = MaterialTheme.typography.bodySmall,
                            color    = MaterialTheme.colorScheme.outline,
                            maxLines = 1
                        )
                    }
                }

                // درصد یا تیک
                if (progress.isDone) {
                    Surface(shape = CircleShape, color = StatusDoneLight) {
                        Icon(
                            imageVector        = Icons.Default.Check,
                            contentDescription = null,
                            tint               = StatusDone,
                            modifier           = Modifier.padding(6.dp).size(16.dp)
                        )
                    }
                } else if (progress.totalTasks > 0) {
                    Text(
                        text       = "${(progress.progress * 100).toInt().toPersian()}٪",
                        style      = MaterialTheme.typography.labelMedium,
                        color      = progressColor,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            if (progress.totalTasks > 0) {
                Spacer(Modifier.height(10.dp))

                // progress bar
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(6.dp)
                        .clip(CircleShape)
                        .background(progressBg)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(progress.progress)
                            .fillMaxHeight()
                            .clip(CircleShape)
                            .background(progressColor)
                    )
                }

                Spacer(Modifier.height(6.dp))

                // وظیفه جاری
                // ردیف پایین — وظیفه جاری + دکمه حذف
                Row(
                    modifier              = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment     = Alignment.CenterVertically
                ) {
                    // وظیفه جاری
                    if (progress.isDone) {
                        Text(
                            text  = "✅ تمام وظایف انجام شد",
                            style = MaterialTheme.typography.labelSmall,
                            color = StatusDone,
                            modifier = Modifier.weight(1f)
                        )
                    } else if (progress.currentTaskTitle.isNotEmpty()) {
                        Text(
                            text     = "▶ ${progress.currentTaskTitle}",
                            style    = MaterialTheme.typography.labelSmall,
                            color    = MaterialTheme.colorScheme.outline,
                            maxLines = 1,
                            modifier = Modifier.weight(1f)
                        )
                    } else {
                        Spacer(Modifier.weight(1f))
                    }

                    // تعداد + دکمه حذف کنار هم
                    Row(
                        verticalAlignment     = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(
                            text  = "${progress.doneTasks.toPersian()} از ${progress.totalTasks.toPersian()}",
                            style = MaterialTheme.typography.labelSmall.copy(
                                textDirection = TextDirection.Rtl
                            ),
                            color = MaterialTheme.colorScheme.outline
                        )
                        IconButton(
                            onClick  = { showDeleteDialog = true },
                            modifier = Modifier.size(28.dp)
                        ) {
                            Icon(
                                Icons.Default.Delete,
                                contentDescription = "حذف",
                                tint     = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f),
                                modifier = Modifier.size(14.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

// ── حالت خالی ─────────────────────────────────────────────

@Composable
private fun EmptyState() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 48.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Image(
            painter            = painterResource(id = R.drawable.ic_empty_chains),
            contentDescription = null,
            modifier           = Modifier.size(120.dp)
        )
        Text(
            text       = "هنوز زنجیره‌ای نداری",
            style      = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color      = Matcha700
        )
        Text(
            text      = "با دکمه + اولین زنجیره وظایفت رو بساز",
            style     = MaterialTheme.typography.bodySmall,
            color     = MaterialTheme.colorScheme.outline,
            textAlign = TextAlign.Center,
            modifier  = Modifier.padding(horizontal = 32.dp)
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SwipeableChainCard(
    chain: Chain,
    progress: ChainProgress,        // ← اضافه شد
    onClick: () -> Unit,
    onDelete: () -> Unit,
    onDuplicate: () -> Unit
) {
    var showDeleteDialog  by remember { mutableStateOf(false) }
    var duplicateHandled  by remember { mutableStateOf(false) }

    val dismissState = rememberSwipeToDismissBoxState(
        confirmValueChange = { value ->
            when (value) {
                SwipeToDismissBoxValue.EndToStart -> {
                    showDeleteDialog = true
                    false
                }
                SwipeToDismissBoxValue.StartToEnd -> {
                    if (!duplicateHandled) {
                        duplicateHandled = true
                        onDuplicate()
                    }
                    false
                }
                else -> false
            }
        },
        positionalThreshold = { it * 0.4f }
    )

    // وقتی swipe برگشت، flag رو ریست کن
    LaunchedEffect(dismissState.currentValue) {
        if (dismissState.currentValue == SwipeToDismissBoxValue.Settled) {
            duplicateHandled = false
        }
    }

    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title   = { Text("حذف زنجیره") },
            text    = { Text("زنجیره «${chain.title}» حذف بشه؟") },
            confirmButton = {
                TextButton(onClick = {
                    onDelete()
                    showDeleteDialog = false
                }) {
                    Text("حذف", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) {
                    Text("لغو")
                }
            }
        )
    }

    SwipeToDismissBox(
        state             = dismissState,
        backgroundContent = { SwipeBackground(dismissState = dismissState) }
    ) {
        ChainCard(
            chain    = chain,
            progress = progress,     // ← اضافه شد
            onClick  = onClick,
            onDelete = { showDeleteDialog = true }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SwipeBackground(
    dismissState: SwipeToDismissBoxState
) {
    val direction = dismissState.dismissDirection

    val (bgColor, icon, text, alignment) = when (direction) {
        SwipeToDismissBoxValue.StartToEnd -> Quadruple(
            StatusDoneLight,
            Icons.Default.Add,
            "کپی زنجیره",
            Alignment.CenterStart
        )
        SwipeToDismissBoxValue.EndToStart -> Quadruple(
            DeadlineDangerLight,
            Icons.Default.Delete,
            "حذف",
            Alignment.CenterEnd
        )
        else -> Quadruple(
            Color.Transparent,
            Icons.Default.Add,
            "",
            Alignment.Center
        )
    }

    val iconColor = when (direction) {
        SwipeToDismissBoxValue.StartToEnd -> StatusDone
        SwipeToDismissBoxValue.EndToStart -> DeadlineDanger
        else                              -> Color.Transparent
    }

    Box(
        modifier         = Modifier
            .fillMaxSize()
            .clip(MaterialTheme.shapes.medium)
            .background(bgColor)
            .padding(horizontal = 20.dp),
        contentAlignment = alignment
    ) {
        Row(
            verticalAlignment     = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            if (direction == SwipeToDismissBoxValue.StartToEnd) {
                Icon(icon, contentDescription = null, tint = iconColor)
                Text(text, color = iconColor, fontWeight = FontWeight.Medium)
            } else {
                Text(text, color = iconColor, fontWeight = FontWeight.Medium)
                Icon(icon, contentDescription = null, tint = iconColor)
            }
        }
    }
}

private data class Quadruple<A, B, C, D>(
    val first: A, val second: B, val third: C, val fourth: D
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TaskListBottomSheet(
    title: String,
    tasks: List<Task>,
    color: androidx.compose.ui.graphics.Color,
    onDismiss: () -> Unit,
    onTaskClick: (Long) -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = false)
    val dateFormat = remember { SimpleDateFormat("yyyy/MM/dd HH:mm", Locale.getDefault()) }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState       = sheetState
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .navigationBarsPadding()
        ) {
            // هدر
            Row(
                modifier              = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment     = Alignment.CenterVertically
            ) {
                Text(
                    text       = title,
                    style      = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color      = color
                )
                Surface(
                    shape = MaterialTheme.shapes.small,
                    color = color.copy(alpha = 0.1f)
                ) {
                    Text(
                        text     = "${tasks.size} وظیفه",
                        style    = MaterialTheme.typography.labelMedium,
                        color    = color,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }

            Spacer(Modifier.height(12.dp))
            HorizontalDivider()
            Spacer(Modifier.height(8.dp))

            if (tasks.isEmpty()) {
                Box(
                    modifier         = Modifier
                        .fillMaxWidth()
                        .padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text  = "موردی وجود نداره",
                        color = MaterialTheme.colorScheme.outline
                    )
                }
            } else {
                LazyColumn(
                    modifier       = Modifier.heightIn(max = 400.dp),
                    contentPadding = PaddingValues(bottom = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(tasks, key = { it.id }) { task ->
                        SheetTaskCard(
                            task       = task,
                            color      = color,
                            dateFormat = dateFormat,
                            onClick    = {
                                onDismiss()
                                onTaskClick(task.chainId)
                            }
                        )
                    }
                }


            }
        }
    }
}

@Composable
private fun SheetTaskCard(
    task: Task,
    color: androidx.compose.ui.graphics.Color,
    dateFormat: SimpleDateFormat,
    onClick: () -> Unit
) {
    Card(
        onClick  = onClick,
        modifier = Modifier.fillMaxWidth(),
        colors   = CardDefaults.cardColors(
            containerColor = color.copy(alpha = 0.08f)
        )
    ) {
        Row(
            modifier              = Modifier.padding(12.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment     = Alignment.CenterVertically
        ) {
            Box(
                modifier         = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(color.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector        = Icons.Default.Check,
                    contentDescription = null,
                    tint               = color,
                    modifier           = Modifier.size(18.dp)
                )
            }

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text       = task.title,
                    style      = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium
                )
                task.deadlineAt?.let {
                    Text(
                        text  = dateFormat.format(Date(it)),
                        style = MaterialTheme.typography.labelSmall,
                        color = color
                    )
                }
            }

            Icon(
                imageVector        = Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = null,
                tint               = MaterialTheme.colorScheme.outline,
                modifier           = Modifier.size(16.dp)
            )
        }
    }
}