package com.gymia.ui.widget

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.content.Intent
import android.view.View
import android.widget.RemoteViews
import com.gymia.MainActivity
import com.gymia.R
import com.gymia.data.repository.WidgetRepository

class GymWidgetProvider : AppWidgetProvider() {

    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {
        appWidgetIds.forEach { appWidgetId ->
            updateWidget(context, appWidgetManager, appWidgetId)
        }
    }

    private fun updateWidget(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetId: Int
    ) {
        val repository = WidgetRepository(context)
        val views = RemoteViews(context.packageName, R.layout.gym_widget)
        val nextDay = repository.getNextWorkoutDay()

        if (nextDay != null) {
            val (dayLabel, exercises) = nextDay
            views.setTextViewText(R.id.widget_day_label, dayLabel)
            val exerciseViewIds = listOf(R.id.widget_exercise_1, R.id.widget_exercise_2, R.id.widget_exercise_3)
            exerciseViewIds.forEachIndexed { index, viewId ->
                val exercise = exercises.getOrNull(index)
                if (exercise != null) {
                    views.setTextViewText(viewId, "• $exercise")
                    views.setViewVisibility(viewId, View.VISIBLE)
                } else {
                    views.setViewVisibility(viewId, View.GONE)
                }
            }
        } else {
            views.setTextViewText(R.id.widget_day_label, "No active plan")
            views.setTextViewText(R.id.widget_exercise_1, "Open GymAI to create one")
            views.setViewVisibility(R.id.widget_exercise_1, View.VISIBLE)
            views.setViewVisibility(R.id.widget_exercise_2, View.GONE)
            views.setViewVisibility(R.id.widget_exercise_3, View.GONE)
        }

        val intent = Intent(context, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(
            context, 0, intent, PendingIntent.FLAG_IMMUTABLE
        )
        views.setOnClickPendingIntent(R.id.widget_root, pendingIntent)

        appWidgetManager.updateAppWidget(appWidgetId, views)
    }
}
