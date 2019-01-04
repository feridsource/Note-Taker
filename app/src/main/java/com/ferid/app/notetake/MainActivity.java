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

package com.ferid.app.notetake;

import android.Manifest;
import android.appwidget.AppWidgetManager;
import android.content.ActivityNotFoundException;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.speech.RecognizerIntent;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.ferid.app.notetake.dialogs.PromptDialog;
import com.ferid.app.notetake.interfaces.PromptListener;
import com.ferid.app.notetake.prefs.PrefsUtil;
import com.ferid.app.notetake.utility.DirectoryUtility;
import com.ferid.app.notetake.widget.NoteWidget;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;

/**
 * @author Ferid Cafer
 */
public class MainActivity extends AppCompatActivity {
    private Context context;
    private EditText notePad;

    private static final int SPEECH_REQUEST_CODE = 100;
    private static final int REQUEST_EXTERNAL_STORAGE = 101;

    private static final String EXTENSION = ".txt";


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);

        context = this;

        final Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        notePad = findViewById(R.id.notePad);

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
                String note = PrefsUtil.getNote(context);

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
            PrefsUtil.setNote(context, notePad.getText().toString());

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
     * Get file name via asking user
     */
    private void getFileName() {
        final PromptDialog promptDialog = new PromptDialog(context);
        promptDialog.setPositiveButton(getString(R.string.save));
        promptDialog.setOnPositiveClickListener(new PromptListener() {
            @Override
            public void OnPrompt(String promptText) {

                promptDialog.dismiss();

                if (!TextUtils.isEmpty(promptText)) {
                    final String fileName = promptText.trim();

                    if (!isFileExist(fileName)) {
                        saveAs(fileName);
                    } else {
                        Snackbar.make(notePad, getString(R.string.fileAlreadyExists),
                                Snackbar.LENGTH_LONG).setAction(getString(R.string.overwrite),
                                new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {
                                        deleteExistingFile(fileName);

                                        saveAs(fileName);
                                    }
                                }).show();
                    }
                }
            }
        });
        promptDialog.show();
    }

    /**
     * Does the given file name already exist?
     * @param fileName Given file name
     * @return yes or no
     */
    private boolean isFileExist(String fileName) {
        File file = new File(DirectoryUtility.getPathFolder() + fileName + EXTENSION);

        return file.exists();
    }

    /**
     * Delete existing file
     * @param fileName file name
     */
    private void deleteExistingFile(String fileName) {
        File file = new File(DirectoryUtility.getPathFolder() + fileName + EXTENSION);
        if (file.exists()) {
            file.delete();
        }
    }

    /**
     * Save into a file
     */
    private void saveAs(String fileName) {
        boolean isFileOperationSuccessful = true;

        if (DirectoryUtility.isExternalStorageMounted()) {

            DirectoryUtility.createDirectory();

            FileOutputStream outputStream = null;

            try {
                outputStream = new FileOutputStream (
                        new File(DirectoryUtility.getPathFolder() + fileName + EXTENSION));
                outputStream.write(notePad.getText().toString().getBytes());
            } catch (IOException e) {
                isFileOperationSuccessful = false;
            } finally {
                if (outputStream != null) {
                    try {
                        outputStream.flush();
                        outputStream.close();
                    } catch (IOException e) {
                        isFileOperationSuccessful = false;
                    }
                }
            }

            if (isFileOperationSuccessful) {
                Snackbar.make(notePad, getString(R.string.writeSuccess),
                        Snackbar.LENGTH_LONG).show();
            } else {
                Snackbar.make(notePad, getString(R.string.writeError),
                        Snackbar.LENGTH_LONG).show();
            }
        } else {
            Snackbar.make(notePad, getString(R.string.mountExternalStorage),
                    Snackbar.LENGTH_LONG).show();
        }
    }

    /**
     * Ask for read-write external storage permission
     */
    private void askForPermissionExternalStorage() {
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) { //permission yet to be granted

            getPermissionExternalStorage();
        } else { //permission already granted
            getFileName();
        }
    }

    /**
     * Request and get the permission for external storage
     */
    private void getPermissionExternalStorage() {
        if (ActivityCompat.shouldShowRequestPermissionRationale(MainActivity.this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE)) {

            Snackbar.make(notePad, R.string.grantPermission,
                    Snackbar.LENGTH_LONG)
                    .setAction(R.string.ok, new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            ActivityCompat.requestPermissions(MainActivity.this,
                                    new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                                    REQUEST_EXTERNAL_STORAGE);
                        }
                    }).show();
        } else {
            ActivityCompat.requestPermissions(MainActivity.this,
                    new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                    REQUEST_EXTERNAL_STORAGE);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {

        if (requestCode == REQUEST_EXTERNAL_STORAGE) {
            //if request is cancelled, the result arrays are empty.
            if (grantResults.length > 0
                    && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                getFileName();
            }
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
        switch (item.getItemId()) {
            case R.id.item_save:
                saveText();
                finish();
                return true;
            case R.id.item_delete:
                eraseAll();
                return true;
            case R.id.item_listen:
                listenToUser();
                return true;
            case R.id.item_share:
                shareNotes();
                return true;
            case R.id.item_save_as:
                askForPermissionExternalStorage();
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