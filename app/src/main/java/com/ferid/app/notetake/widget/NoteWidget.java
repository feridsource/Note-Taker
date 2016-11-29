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

package com.ferid.app.notetake.widget;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.widget.RemoteViews;

import com.ferid.app.notetake.MainActivity;
import com.ferid.app.notetake.R;
import com.ferid.app.notetake.prefs.PrefsUtil;

/**
 * Created by ferid.cafer on 11/10/2014.
 */
public class NoteWidget extends AppWidgetProvider {
    private Context context;

    protected RemoteViews remoteViews;
    protected AppWidgetManager appWidgetManager;
    protected ComponentName thisWidget;

    public static final String APP_TO_WID = "com.ferid.app.notetake.widget.APP_TO_WID"; //application triggers the widget

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        this.context = context;
        this.appWidgetManager = appWidgetManager;

        remoteViews = new RemoteViews(context.getPackageName(), R.layout.note_widget);
        thisWidget = new ComponentName(context, NoteWidget.class);

        getNote();
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        this.context = context;
        this.appWidgetManager = AppWidgetManager.getInstance(context);
        remoteViews = new RemoteViews(context.getPackageName(), R.layout.note_widget);
        thisWidget = new ComponentName(context, NoteWidget.class);

        if (intent.getAction().equals(APP_TO_WID)) {
            if (remoteViews != null) {
                getNote();
            }
        }
        super.onReceive(context, intent);
    }

    /**
     * Read note
     */
    private void getNote() {
        String note = PrefsUtil.getNote(context);
        remoteViews.setTextViewText(R.id.note, note);

        Intent intent = new Intent(context, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, intent,
                PendingIntent.FLAG_UPDATE_CURRENT);
        remoteViews.setOnClickPendingIntent(R.id.layoutBackground, pendingIntent);
        remoteViews.setBoolean(R.id.layoutBackground, "setEnabled", true);

        appWidgetManager.updateAppWidget(thisWidget, remoteViews);
    }

}