package com.mnfarzaneh.taskflow.widget

import android.content.Context
import androidx.glance.appwidget.updateAll
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class WidgetUpdater @Inject constructor() {

    fun update(context: Context) {
        CoroutineScope(Dispatchers.IO).launch {
            TaskFlowWidget().updateAll(context)
        }
    }
}