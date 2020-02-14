/*
 * Copyright (C) 2016 Ferid Cafer
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.ferid.app.notetake.widget

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.widget.RemoteViews
import com.ferid.app.notetake.R
import com.ferid.app.notetake.MainActivity
import com.ferid.app.notetake.prefs.PrefsUtil.getNote

/**
 * Note widget
 */
class NoteWidget : AppWidgetProvider() {
    private var context: Context? = null
    protected var remoteViews: RemoteViews? = null
    protected var appWidgetManager: AppWidgetManager? = null
    protected var thisWidget: ComponentName? = null

    override fun onUpdate(context: Context, appWidgetManager: AppWidgetManager, appWidgetIds: IntArray) {
        this.context = context
        this.appWidgetManager = appWidgetManager
        remoteViews = RemoteViews(context.packageName, R.layout.widget_note)
        thisWidget = ComponentName(context, NoteWidget::class.java)
        note
    }

    override fun onReceive(context: Context, intent: Intent) {
        this.context = context
        appWidgetManager = AppWidgetManager.getInstance(context)
        remoteViews = RemoteViews(context.packageName, R.layout.widget_note)
        thisWidget = ComponentName(context, NoteWidget::class.java)
        if (intent.action == APP_TO_WID) {
            if (remoteViews != null) {
                note
            }
        }
        super.onReceive(context, intent)
    }

    /**
     * Read note
     */
    private val note: Unit
        private get() {
            val note = getNote(context!!)
            remoteViews!!.setTextViewText(R.id.note, note)
            val intent = Intent(context, MainActivity::class.java)
            val pendingIntent = PendingIntent.getActivity(context, 0, intent,
                    PendingIntent.FLAG_UPDATE_CURRENT)
            remoteViews!!.setOnClickPendingIntent(R.id.layoutBackground, pendingIntent)
            remoteViews!!.setBoolean(R.id.layoutBackground, "setEnabled", true)
            appWidgetManager!!.updateAppWidget(thisWidget, remoteViews)
        }

    companion object {
        const val APP_TO_WID = "com.ferid.app.notetake.widget.APP_TO_WID" //application triggers the widget
    }
}