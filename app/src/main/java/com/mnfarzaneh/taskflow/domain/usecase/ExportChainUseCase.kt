package com.mnfarzaneh.taskflow.domain.usecase

import com.mnfarzaneh.taskflow.data.repository.TaskRepository
import com.mnfarzaneh.taskflow.domain.model.TaskStatus
import com.mnfarzaneh.taskflow.utils.formatPersianDate
import com.mnfarzaneh.taskflow.utils.toPersian
import kotlinx.coroutines.flow.first
import javax.inject.Inject

class ExportChainUseCase @Inject constructor(
    private val repository: TaskRepository
) {
    suspend operator fun invoke(chainId: Long): String {
        val chain = repository.getChainById(chainId) ?: return ""
        val tasks = repository.getTasksByChain(chainId)
            .first()
            .sortedBy { it.order }

        val sb = StringBuilder()

        // ── هدر ─────────────────────────────────────────
        sb.appendLine("═════════════════")
        sb.appendLine("  TaskFlow — گزارش زنجیره")
        sb.appendLine("═════════════════")
        sb.appendLine()
        sb.appendLine("📌 ${chain.title}")
        if (chain.description.isNotEmpty()) {
            sb.appendLine("   ${chain.description}")
        }
        sb.appendLine()

        // ── آمار ─────────────────────────────────────────
        val doneCount  = tasks.count { it.status == TaskStatus.DONE }
        val totalCount = tasks.size
        val percent    = if (totalCount > 0) (doneCount * 100) / totalCount else 0

        sb.appendLine("📊 پیشرفت: ${doneCount.toPersian()} از ${totalCount.toPersian()} وظیفه (${percent.toPersian()}٪)")
        sb.appendLine()
        sb.appendLine("─────────────────")
        sb.appendLine()

        // ── وظایف ────────────────────────────────────────
        tasks.forEachIndexed { index, task ->
            val statusIcon = when (task.status) {
                TaskStatus.DONE        -> "✅"
                TaskStatus.IN_PROGRESS -> "🔄"
                TaskStatus.PENDING     -> "⏳"
                TaskStatus.LOCKED      -> "🔒"
            }

            sb.appendLine("$statusIcon  ${(index + 1).toPersian()}. ${task.title}")

            if (task.description.isNotEmpty()) {
                sb.appendLine("      ${task.description}")
            }

            task.deadlineAt?.let {
                sb.appendLine("      ⏰ ددلاین: ${formatPersianDate(it)}")
            }

            task.reminderAt?.let {
                sb.appendLine("      🔔 یادآوری: ${formatPersianDate(it)}")
            }

            if (task.needsRevision) {
                sb.appendLine("      ⚠️ نیاز به بازبینی: ${task.revisionNote ?: ""}")
            }

            sb.appendLine()
        }

        // ── فوتر ─────────────────────────────────────────
        sb.appendLine("─────────────────")
        sb.appendLine("تاریخ گزارش: ${formatPersianDate(System.currentTimeMillis())}")
        sb.appendLine("═════════════════")

        return sb.toString()
    }
}