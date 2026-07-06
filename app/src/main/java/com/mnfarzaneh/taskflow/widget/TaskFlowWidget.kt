package com.mnfarzaneh.taskflow.widget

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.glance.*
import androidx.glance.action.actionStartActivity
import androidx.glance.action.clickable
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetReceiver
import androidx.glance.appwidget.LinearProgressIndicator
import androidx.glance.appwidget.provideContent
import androidx.glance.background
import androidx.glance.layout.*
import androidx.glance.text.*
import androidx.glance.unit.ColorProvider
import com.mnfarzaneh.taskflow.MainActivity
import com.mnfarzaneh.taskflow.domain.model.TaskStatus
import com.mnfarzaneh.taskflow.utils.toPersian

class TaskFlowWidget : GlanceAppWidget() {

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        val chains = WidgetDataProvider.getChainData(context)
        provideContent {
            WidgetContent(chains = chains)
        }
    }
}

@Composable
private fun WidgetContent(chains: List<WidgetChainData>) {

    val bgColor      = ColorProvider(Color(0xFFEFF6E2))
    val matchaDark   = ColorProvider(Color(0xFF2F4412))
    val matchaMid    = ColorProvider(Color(0xFF5A7D24))
    val matchaLight  = ColorProvider(Color(0xFF8FBB47))
    val grayColor    = ColorProvider(Color(0xFF7A8070))
    val dangerColor  = ColorProvider(Color(0xFFC74A3F))
    val borderColor  = ColorProvider(Color(0xFFCBE0A0))

    Column(
        modifier = GlanceModifier
            .fillMaxSize()
            .background(bgColor)
            .padding(14.dp)
            .clickable(actionStartActivity<MainActivity>())
    ) {
        // ── هدر ────────────────────────────────────────
        Row(
            modifier          = GlanceModifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text  = "TaskFlow",
                style = TextStyle(
                    fontSize   = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color      = matchaDark
                ),
                modifier = GlanceModifier.defaultWeight()
            )
            Text(
                text  = "${chains.size.toPersian()} زنجیره فعال",
                style = TextStyle(
                    fontSize = 10.sp,
                    color    = grayColor
                )
            )
        }

        Spacer(GlanceModifier.height(10.dp))

        if (chains.isEmpty()) {
            // ── همه تموم شده ─────────────────────────
            Column(
                modifier            = GlanceModifier.fillMaxSize(),
                verticalAlignment   = Alignment.CenterVertically,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text  = "✅ همه وظایف انجام شده",
                    style = TextStyle(
                        fontSize   = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color      = matchaMid
                    )
                )
            }
        } else {
            // ── لیست زنجیره‌ها ────────────────────────
            chains.take(3).forEach { data ->
                WidgetChainRow(
                    data        = data,
                    matchaDark  = matchaDark,
                    matchaMid   = matchaMid,
                    matchaLight = matchaLight,
                    grayColor   = grayColor,
                    dangerColor = dangerColor
                )
                Spacer(GlanceModifier.height(8.dp))
            }

            if (chains.size > 3) {
                Text(
                    text  = "+ ${(chains.size - 3).toPersian()} زنجیره دیگه",
                    style = TextStyle(
                        fontSize = 10.sp,
                        color    = grayColor
                    )
                )
            }
        }
    }
}

@Composable
private fun WidgetChainRow(
    data: WidgetChainData,
    matchaDark: ColorProvider,
    matchaMid: ColorProvider,
    matchaLight: ColorProvider,
    grayColor: ColorProvider,
    dangerColor: ColorProvider
) {
    val now       = System.currentTimeMillis()
    val isOverdue = data.currentTask?.deadlineAt != null &&
            data.currentTask.deadlineAt < now

    val progress = if (data.totalCount > 0)
        data.doneCount.toFloat() / data.totalCount
    else 0f

    val progressColor = when {
        isOverdue    -> dangerColor
        progress > 0.6f -> matchaMid
        else         -> matchaLight
    }

    Column(modifier = GlanceModifier.fillMaxWidth()) {

        // عنوان زنجیره + درصد
        Row(
            modifier          = GlanceModifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text     = data.chain.title,
                style    = TextStyle(
                    fontSize   = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color      = matchaDark
                ),
                modifier = GlanceModifier.defaultWeight(),
                maxLines = 1
            )
            Text(
                text  = "${(progress * 100).toInt().toPersian()}٪",
                style = TextStyle(
                    fontSize   = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color      = progressColor
                )
            )
        }

        Spacer(GlanceModifier.height(4.dp))

        // progress bar
        LinearProgressIndicator(
            progress = progress,
            modifier = GlanceModifier.fillMaxWidth().height(4.dp),
            color    = progressColor,
            backgroundColor = ColorProvider(Color(0xFFCBE0A0))
        )

        Spacer(GlanceModifier.height(3.dp))

        // وظیفه جاری
        data.currentTask?.let { task ->
            val dotColor = when {
                isOverdue                            -> dangerColor
                task.status == TaskStatus.IN_PROGRESS -> matchaMid
                else                                 -> matchaLight
            }

            Row(
                modifier          = GlanceModifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Spacer(
                    modifier = GlanceModifier
                        .size(6.dp)
                        .background(dotColor)
                )
                Spacer(GlanceModifier.width(6.dp))
                Text(
                    text     = task.title,
                    style    = TextStyle(
                        fontSize = 11.sp,
                        color    = if (isOverdue) dangerColor else grayColor
                    ),
                    modifier = GlanceModifier.defaultWeight(),
                    maxLines = 1
                )
                Text(
                    text  = "${data.doneCount.toPersian()} از ${data.totalCount.toPersian()}",
                    style = TextStyle(
                        fontSize = 10.sp,
                        color    = grayColor
                    )
                )
            }
        }
    }
}

class TaskFlowWidgetReceiver : GlanceAppWidgetReceiver() {
    override val glanceAppWidget: GlanceAppWidget = TaskFlowWidget()
}