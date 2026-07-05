package com.mnfarzaneh.taskflow.widget

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.glance.*
import androidx.glance.action.actionStartActivity
import androidx.glance.action.clickable
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetReceiver
import androidx.glance.appwidget.provideContent
import androidx.glance.color.ColorProviders
import androidx.glance.layout.*
import androidx.glance.material3.ColorProviders
import androidx.glance.text.*
import com.mnfarzaneh.taskflow.MainActivity
import com.mnfarzaneh.taskflow.domain.model.Task
import com.mnfarzaneh.taskflow.domain.model.TaskStatus
import androidx.glance.appwidget.cornerRadius
import androidx.glance.background

class TaskFlowWidget : GlanceAppWidget() {

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        val tasks = WidgetDataProvider.getPendingTasks(context)
        provideContent {
            WidgetContent(tasks = tasks)
        }
    }
}

@Composable
private fun WidgetContent(tasks: List<Task>) {
    Column(
        modifier = GlanceModifier
            .fillMaxSize()
            .background(GlanceTheme.colors.surface)
            .padding(12.dp)
            .clickable(actionStartActivity<MainActivity>())
    ) {
        // هدر
        Text(
            text  = "TaskFlow",
            style = TextStyle(
                fontSize   = 14.sp,
                fontWeight = FontWeight.Bold,
                color      = GlanceTheme.colors.primary
            )
        )

        Spacer(GlanceModifier.height(8.dp))

        if (tasks.isEmpty()) {
            Text(
                text  = "همه وظایف انجام شده ✓",
                style = TextStyle(
                    fontSize = 12.sp,
                    color    = GlanceTheme.colors.onSurfaceVariant
                )
            )
        } else {
            tasks.take(3).forEach { task ->
                WidgetTaskRow(task = task)
                Spacer(GlanceModifier.height(6.dp))
            }

            if (tasks.size > 3) {
                Text(
                    text  = "+ ${tasks.size - 3} وظیفه دیگه",
                    style = TextStyle(
                        fontSize = 11.sp,
                        color    = GlanceTheme.colors.onSurfaceVariant
                    )
                )
            }
        }
    }
}

@Composable
private fun WidgetTaskRow(task: Task) {
    val now       = System.currentTimeMillis()
    val isOverdue = task.deadlineAt != null && task.deadlineAt < now

    val dotColor = when {
        isOverdue                        -> GlanceTheme.colors.error
        task.status == TaskStatus.IN_PROGRESS -> GlanceTheme.colors.primary
        else                             -> GlanceTheme.colors.secondary
    }

    Row(
        modifier          = GlanceModifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Spacer(
            modifier = GlanceModifier
                .size(8.dp)
                .background(dotColor)
        )
        Spacer(GlanceModifier.width(8.dp))

        Text(
            text     = task.title,
            style    = TextStyle(
                fontSize = 12.sp,
                color    = if (isOverdue)
                    GlanceTheme.colors.error
                else
                    GlanceTheme.colors.onSurface
            ),
            modifier = GlanceModifier.defaultWeight(),
            maxLines = 1
        )
    }
}

class TaskFlowWidgetReceiver : GlanceAppWidgetReceiver() {
    override val glanceAppWidget: GlanceAppWidget = TaskFlowWidget()
}