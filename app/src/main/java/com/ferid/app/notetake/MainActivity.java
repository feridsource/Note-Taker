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

import android.appwidget.AppWidgetManager;
import android.content.ActivityNotFoundException;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.speech.RecognizerIntent;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.Toast;

import com.ferid.app.notetake.prefs.PrefsUtil;
import com.ferid.app.notetake.widget.NoteWidget;

import java.util.ArrayList;

/**
 * @author Ferid Cafer
 */
public class MainActivity extends AppCompatActivity {
    private Context context;
    private EditText notePad;

    private final int SPEECH_REQUEST_CODE = 0;

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
                String note = PrefsUtil.getInstance(context).getNote();

                writeIntoNote(note);
            }
        });
    }

    /**
     * Write a text into notePad Edittext
     * @param note
     */
    private void writeIntoNote(final String note) {
        if (notePad != null) {
            notePad.setText(note);
            notePad.setSelection(notePad.getText().length());
        }
    }

    /**
     * Append note
     * @param note
     */
    private void appendNote(final String note) {
        if (notePad != null) {
            //get the current note which was already written
            String currentNote = notePad.getText().toString();
            //if there is something written before, go to a new line
            if (!currentNote.equals("")) {
                currentNote += "\n";
            }
            //now write
            writeIntoNote(currentNote + note);
        }
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
            AlertDialog alertDialog = builderOperation.create();
            alertDialog.show();
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

    /**
     * Voice listener
     */
    private void listenToUser() {
        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        try {
            startActivityForResult(intent, SPEECH_REQUEST_CODE);
        } catch (ActivityNotFoundException e) {
            Toast toast = Toast.makeText(context,
                    getString(R.string.voiceRecognitionNotSupported),
                    Toast.LENGTH_SHORT);
            toast.setGravity(Gravity.CENTER, 0, 0);
            toast.show();
        }
    }

    /**
     * Capitalize the first letter of a given text
     * @param text
     * @return
     */
    private String capitalizeFirstLetter(String text) {
        return text.substring(0, 1).toUpperCase() + text.substring(1).toLowerCase();
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
            case R.id.item_delete:
                eraseAll();
                return true;
            case R.id.item_share:
                shareNotes();
                return true;
            case R.id.item_listen:
                listenToUser();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        if (requestCode == SPEECH_REQUEST_CODE && resultCode == RESULT_OK) {

            ArrayList<String> results = data.getStringArrayListExtra(
                    RecognizerIntent.EXTRA_RESULTS);
            String spokenText = results.get(0);

            //capitalize the first letter
            spokenText = capitalizeFirstLetter(spokenText);
            //now append the spoken text
            appendNote(spokenText);
        }

        super.onActivityResult(requestCode, resultCode, data);
    }
}