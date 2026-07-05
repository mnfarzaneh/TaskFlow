package com.mnfarzaneh.taskflow.ui.chain

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextDirection
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.mnfarzaneh.taskflow.domain.model.Task
import com.mnfarzaneh.taskflow.domain.model.TaskStatus
import com.mnfarzaneh.taskflow.ui.theme.*
import androidx.compose.foundation.clickable
import androidx.hilt.navigation.compose.hiltViewModel
import com.mnfarzaneh.taskflow.utils.toPersian
import androidx.compose.foundation.layout.size
import androidx.compose.ui.res.painterResource
import androidx.compose.foundation.Image
import com.mnfarzaneh.taskflow.R
import com.mnfarzaneh.taskflow.utils.formatPersianDateShort

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChainScreen(
    onBack: () -> Unit,
    onTaskClick: (Long) -> Unit,
    onEdit: () -> Unit,
    viewModel: ChainViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val exportText = uiState.exportText
    val context = LocalContext.current

    if (exportText != null) {
        ExportBottomSheet(
            text    = exportText,
            onShare = {
                val intent = android.content.Intent(android.content.Intent.ACTION_SEND).apply {
                    type = "text/plain"
                    putExtra(android.content.Intent.EXTRA_TEXT, exportText)
                }
                context.startActivity(
                    android.content.Intent.createChooser(intent, "اشتراک‌گذاری زنجیره")
                )
                viewModel.clearExport()
            },
            onCopy  = {
                val clipboard = context.getSystemService(
                    android.content.Context.CLIPBOARD_SERVICE
                ) as android.content.ClipboardManager
                clipboard.setPrimaryClip(
                    android.content.ClipData.newPlainText("TaskFlow", exportText)
                )
                viewModel.clearExport()
            },
            onDismiss = { viewModel.clearExport() }
        )
    }

    GlassBackground {
        Column(modifier = Modifier.fillMaxSize()) {

            // ── هدر دستی شیشه‌ای ─────────────────────────
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
                    text       = uiState.chainWithTasks?.chain?.title ?: "زنجیره",
                    fontWeight = FontWeight.Bold,
                    color      = Matcha800,
                    style      = MaterialTheme.typography.titleLarge,
                    modifier   = Modifier.weight(1f),
                    maxLines   = 1
                )
                IconButton(onClick = { viewModel.exportChain() }) {
                    Icon(Icons.Default.Share, contentDescription = "خروجی", tint = Matcha700)
                }
                IconButton(onClick = onEdit) {
                    Icon(Icons.Default.Edit, contentDescription = "ویرایش", tint = Matcha700)
                }
            }

            if (uiState.isLoading) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = Matcha600)
                }
                return@Column
            }

            val tasks = uiState.chainWithTasks?.tasks ?: emptyList()

            if (tasks.isEmpty()) {
                Box(
                    modifier         = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Image(
                            painter            = painterResource(id = R.drawable.ic_empty_tasks),
                            contentDescription = null,
                            modifier           = Modifier.size(100.dp)
                        )
                        Text(
                            text  = "این زنجیره وظیفه‌ای نداره",
                            style = MaterialTheme.typography.titleSmall,
                            color = Matcha700,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text  = "ویرایش کن و وظایف اضافه کن",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.outline
                        )
                    }
                }
                return@Column
            }

            val doneCount  = tasks.count { it.status == TaskStatus.DONE }
            val totalCount = tasks.size

            LazyColumn(
                modifier       = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(vertical = 8.dp, horizontal = 16.dp)
            ) {
                item {
                    ChainProgressCard(doneCount = doneCount, totalCount = totalCount)
                    Spacer(Modifier.height(20.dp))
                }

                itemsIndexed(tasks, key = { _, task -> task.id }) { index, task ->
                    TaskChainItem(
                        task       = task,
                        isLast     = index == tasks.lastIndex,
                        onClick    = { onTaskClick(task.id) },
                        onComplete = { viewModel.completeTask(task) }
                    )
                }

                item { Spacer(Modifier.height(24.dp)) }
            }
        }
    }
}

// ── کارت آمار ────────────────────────────────────────────

@Composable
private fun ChainProgressCard(
    doneCount: Int,
    totalCount: Int,
    modifier: Modifier = Modifier
) {
    val progress = if (totalCount > 0) doneCount.toFloat() / totalCount else 0f

    Box(
        modifier = modifier
            .fillMaxWidth()
            .glassMatchaEffect(cornerRadius = 20.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text       = "پیشرفت زنجیره",
                    style      = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                    color      = Matcha800
                )
                Text(
                    text = buildAnnotatedString { append("${doneCount.toPersian()} از ${totalCount.toPersian()}") },
                    style = MaterialTheme.typography.titleSmall.copy(
                        textDirection = TextDirection.ContentOrLtr
                    ),
                    color      = Matcha700,
                    fontWeight = FontWeight.Bold
                )
            }
            Spacer(Modifier.height(10.dp))
            LinearProgressIndicator(
                progress = { progress },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp)
                    .clip(CircleShape),
                color      = Matcha600,
                trackColor = Color.White.copy(alpha = 0.5f)
            )
        }
    }
}

// ── آیتم زنجیره ──────────────────────────────────────────

@Composable
private fun TaskChainItem(
    task: Task,
    isLast: Boolean,
    onClick: () -> Unit,
    onComplete: () -> Unit
) {
    Row(modifier = Modifier.fillMaxWidth()) {

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(start = 4.dp)
        ) {
            TaskStatusIcon(status = task.status, onComplete = onComplete)
            if (!isLast) {
                ConnectorLine(
                    isDone   = task.status == TaskStatus.DONE,
                    modifier = Modifier.height(44.dp)
                )
            }
        }

        Spacer(Modifier.width(12.dp))

        TaskCard(
            task     = task,
            onClick  = onClick,
            modifier = Modifier
                .weight(1f)
                .padding(bottom = 12.dp)
        )
    }
}

// ── آیکون وضعیت ──────────────────────────────────────────

@Composable
private fun TaskStatusIcon(
    status: TaskStatus,
    onComplete: () -> Unit
) {
    val bgColor = when (status) {
        TaskStatus.DONE        -> StatusDone
        TaskStatus.IN_PROGRESS -> StatusActive
        TaskStatus.PENDING     -> StatusPending
        TaskStatus.LOCKED      -> StatusLocked
    }
    val icon = if (status == TaskStatus.LOCKED) Icons.Default.Lock else Icons.Default.Check

    val isClickable = status == TaskStatus.PENDING || status == TaskStatus.IN_PROGRESS

    FilledIconButton(
        onClick  = { if (isClickable) onComplete() },
        modifier = Modifier.size(38.dp),
        colors   = IconButtonDefaults.filledIconButtonColors(
            containerColor = bgColor,
            contentColor   = Color.White
        ),
        enabled = isClickable
    ) {
        Icon(
            imageVector        = icon,
            contentDescription = status.name,
            modifier           = Modifier.size(18.dp)
        )
    }
}

// ── خط اتصال ─────────────────────────────────────────────

@Composable
private fun ConnectorLine(
    isDone: Boolean,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .width(2.dp)
            .padding(vertical = 2.dp)
    ) {
        HorizontalDivider(
            modifier  = Modifier.fillMaxHeight().width(2.dp),
            color     = if (isDone) ConnectorDone else ConnectorLocked,
            thickness = 2.dp
        )
    }
}

// ── کارت وظیفه شیشه‌ای ────────────────────────────────────
@Composable
private fun TaskCard(
    task: Task,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val (bgTint, textColor) = when (task.status) {
        TaskStatus.DONE        -> Pair(StatusDoneLight,    StatusDone)
        TaskStatus.IN_PROGRESS -> Pair(StatusActiveLight,  StatusActive)
        TaskStatus.PENDING     -> Pair(StatusPendingLight, StatusPending)
        TaskStatus.LOCKED      -> Pair(StatusLockedLight,  StatusLocked)
    }

    Box(
        modifier = modifier
            .glassEffect(
                cornerRadius    = 18.dp,
                backgroundColor = GlassWhite,
                borderColor     = bgTint.copy(alpha = 0.6f)
            )
            .then(
                if (task.status != TaskStatus.LOCKED)
                    Modifier.clickable(onClick = onClick)
                else
                    Modifier
            )
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Text(
                text           = task.title,
                style          = MaterialTheme.typography.bodyLarge,
                fontWeight     = FontWeight.Medium,
                color          = if (task.status == TaskStatus.LOCKED)
                    MaterialTheme.colorScheme.outline
                else
                    MaterialTheme.colorScheme.onSurface,
                textDecoration = if (task.status == TaskStatus.DONE)
                    TextDecoration.LineThrough
                else
                    TextDecoration.None
            )

            if (task.description.isNotEmpty() && task.status != TaskStatus.LOCKED) {
                Spacer(Modifier.height(4.dp))
                Text(
                    text  = task.description,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.outline
                )
            }

            if (task.deadlineAt != null && task.status != TaskStatus.LOCKED) {
                Spacer(Modifier.height(8.dp))
                DeadlineBadge(deadlineAt = task.deadlineAt)
            }

            if (task.needsRevision) {
                Spacer(Modifier.height(6.dp))
                Surface(
                    shape = MaterialTheme.shapes.small,
                    color = DeadlineWarning.copy(alpha = 0.15f)
                ) {
                    Text(
                        text     = "⚠️ نیاز به بازبینی",
                        style    = MaterialTheme.typography.labelSmall,
                        color    = DeadlineWarning,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }

            if (task.status == TaskStatus.LOCKED) {
                Spacer(Modifier.height(4.dp))
                Text(
                    text  = "🔒 منتظر تکمیل وظیفه قبلی",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.outline
                )
            }
        }
    }
}


// ── بج ددلاین ─────────────────────────────────────────────

@Composable
private fun DeadlineBadge(deadlineAt: Long) {
    val now      = System.currentTimeMillis()
    val diff     = deadlineAt - now
    val diffDays = diff / (1000 * 60 * 60 * 24)

    val (color, text) = when {
        diff < 0      -> Pair(DeadlineDanger,  "منقضی شده")
        diffDays < 1  -> Pair(DeadlineDanger,  "امروز!")
        diffDays < 3  -> Pair(DeadlineWarning, "${diffDays.toPersian()} روز مانده")
        else          -> Pair(StatusPending,   "${diffDays.toPersian()} روز مانده")
    }

    Surface(
        shape = MaterialTheme.shapes.small,
        color = color.copy(alpha = 0.15f)
    ) {
        Text(
            text     = "⏰ $text — ${formatPersianDateShort(deadlineAt)}",
            style    = MaterialTheme.typography.labelSmall,
            color    = color,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
        )
    }
}

// ── Export Bottom Sheet ───────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ExportBottomSheet(
    text: String,
    onShare: () -> Unit,
    onCopy: () -> Unit,
    onDismiss: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState()

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState       = sheetState,
        containerColor   = Color(0xFFFBFCF7)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text       = "خروجی زنجیره",
                style      = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color      = Matcha800
            )

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 200.dp)
                    .glassEffect(cornerRadius = 14.dp)
            ) {
                val scrollState = rememberScrollState()
                Text(
                    text       = text,
                    style      = MaterialTheme.typography.bodySmall,
                    modifier   = Modifier
                        .padding(12.dp)
                        .verticalScroll(scrollState),
                    fontFamily = FontFamily.Monospace
                )
            }

            Row(
                modifier              = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedButton(
                    onClick  = onCopy,
                    modifier = Modifier.weight(1f),
                    colors   = ButtonDefaults.outlinedButtonColors(contentColor = Matcha700)
                ) {
                    Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(6.dp))
                    Text("کپی")
                }
                Button(
                    onClick  = onShare,
                    modifier = Modifier.weight(1f),
                    colors   = ButtonDefaults.buttonColors(containerColor = Matcha600)
                ) {
                    Icon(Icons.Default.Share, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(6.dp))
                    Text("اشتراک‌گذاری")
                }
            }

            Spacer(Modifier.height(16.dp))
        }
    }
}