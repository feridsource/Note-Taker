/*
 * Copyright (C) 2015 Ferid Cafer
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

package com.ferid.app.notetake;

import android.app.AlertDialog;
import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;

import com.ferid.app.notetake.prefs.PrefsUtil;
import com.ferid.app.notetake.widget.NoteWidget;

public class MainActivity extends AppCompatActivity {
    private Context context;
    private EditText notePad;

    /**
     * Called when the activity is first created.
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);

        context = this;

        final Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        notePad = (EditText) findViewById(R.id.notePad);

        getText();
    }

    /**
     * Retrieves from preferences
     */
    private void getText() {
        Handler handler = new Handler();
        handler.post(new Runnable() {
            @Override
            public void run() {
                final String note = PrefsUtil.getInstance(context).getNote();
                if (notePad != null) {
                    notePad.setText(note);
                    notePad.setSelection(notePad.getText().length());
                }
            }
        });
    }

    /**
     * Erase but do not save immediately
     */
    private void eraseAll() {
        if (notePad != null) {
            AlertDialog.Builder builderOperation = new AlertDialog.Builder(context);
            builderOperation.setTitle(R.string.eraseAll);
            builderOperation.setMessage(getString(R.string.sure));
            builderOperation.setPositiveButton(getString(R.string.yes), new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    notePad.setText("");
                }
            });
            builderOperation.setNegativeButton(getString(R.string.no), null);
            builderOperation.create();
            builderOperation.show();
        }
    }

    /**
     * Saves into preferences
     */
    private void saveText() {
        if (notePad != null) {
            PrefsUtil.getInstance(context).setNote(notePad.getText().toString());

            updateNoteWidget();
        }
    }

    /**
     * Shares notes
     */
    private void shareNotes() {
        Intent intent = new Intent(Intent.ACTION_SEND);
        intent.setType("text/plain");
        intent.putExtra(Intent.EXTRA_SUBJECT, getString(R.string.app_name));
        intent.putExtra(Intent.EXTRA_TEXT, notePad.getText().toString());

        startActivity(Intent.createChooser(intent, getString(R.string.share)));
    }

    @Override
    public void onBackPressed() {
        saveText();

        finish();
    }

    /**
     * Updates note widget
     */
    private void updateNoteWidget() {
        AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);
        ComponentName thisAppWidget = new ComponentName(context.getPackageName(),
                this.getClass().getName());
        Intent updateWidget = new Intent(context, NoteWidget.class);
        int[] appWidgetIds = appWidgetManager.getAppWidgetIds(thisAppWidget);
        updateWidget.setAction(NoteWidget.APP_TO_WID);
        updateWidget.putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, appWidgetIds);
        context.sendBroadcast(updateWidget);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar actions click
        switch (item.getItemId()) {
            case R.id.item_save:
                saveText();
                finish();
                return true;
            case R.id.item_share:
                shareNotes();
                return true;
            case R.id.item_delete:
                eraseAll();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }
}