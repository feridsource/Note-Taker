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

package com.ferid.app.notetake

import android.Manifest
import android.app.Activity
import android.appwidget.AppWidgetManager
import android.content.ActivityNotFoundException
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.os.Handler
import android.speech.RecognizerIntent
import android.text.TextUtils
import android.view.Gravity
import android.view.Menu
import android.view.MenuItem
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.ferid.app.notetake.dialogs.SaveAsDialog
import com.ferid.app.notetake.interfaces.PromptListener
import com.ferid.app.notetake.prefs.PrefsUtil
import com.ferid.app.notetake.utility.DirectoryUtility.createDirectory
import com.ferid.app.notetake.utility.DirectoryUtility.getPathFolder
import com.ferid.app.notetake.widget.NoteWidget
import com.google.android.material.snackbar.Snackbar
import java.io.File
import java.util.*

class MainActivity : AppCompatActivity() {

    private var mContext: Context? = null
    private var notePad: EditText? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        mContext = this

        var toolbar: Toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)

        notePad = findViewById(R.id.notePad)

        readNote()
    }

    /**
     * Retrieves from preferences
     */
    private fun readNote() {
       var handler = Handler().post(Runnable {
           var note: String? = PrefsUtil.getNote(mContext!!)

           writeNote(note)
       })
    }

    /**
     * Write a text into notePad
     * @param note note
     */
    private fun writeNote(note: String?) {
        notePad!!.setText(note)
        notePad!!.setSelection(notePad!!.getText()!!.length)
    }

    /**
     * Append note
     * @param note
     */
    private fun appendNote(note: String?) {
        //get the current note which was already written
        var currentNote: String? = notePad!!.getText().toString()
        //if there is something written before, go to a new line
        if (!currentNote.equals("")) {
            currentNote += "\n"
        }
        writeNote(currentNote + note)
    }

    /**
     * Erase but do not save immediately
     */
    private fun eraseAll() {
        val builderOperation = AlertDialog.Builder(mContext!!)
        builderOperation.setTitle(R.string.eraseAll)
        builderOperation.setMessage(getString(R.string.sure))
        builderOperation.setPositiveButton(getString(R.string.yes)) { dialog, which -> notePad!!.setText("") }
        builderOperation.setNegativeButton(getString(R.string.no), null)
        val alertDialog = builderOperation.create()
        alertDialog.show()
    }

    /**
     * Saves into preferences
     */
    private fun saveText() {
        PrefsUtil.setNote(mContext!!, notePad!!.getText().toString())

        updateNoteWidget()
    }

    /**
     * Shares note. Share all on the page.
     */
    private fun shareNote() {
        val intent = Intent(Intent.ACTION_SEND)
        intent.setType("text/plain")
        intent.putExtra(Intent.EXTRA_SUBJECT, getString(R.string.app_name))
        intent.putExtra(Intent.EXTRA_TEXT, notePad!!.getText().toString())
        startActivity(Intent.createChooser(intent, getString(R.string.share)))
    }

    /**
     * Voice listener
     */
    private fun listenToUser() {
        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH)
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
        try {
            startActivityForResult(intent, REQUEST_SPEECH)
        } catch (e: ActivityNotFoundException) {
            val toast = Toast.makeText(mContext,
                    getString(R.string.voiceRecognitionNotSupported),
                    Toast.LENGTH_SHORT)
            toast.setGravity(Gravity.CENTER, 0, 0)
            toast.show()
        }
    }

    /**
     * Get file name via asking user
     */
    private fun getFileName() {
        val saveAsDialog = SaveAsDialog(mContext!!)
        saveAsDialog.setPositiveButton(getString(R.string.save))

        saveAsDialog.setOnPositiveClickListener(object : PromptListener {
            override fun onPrompt(promptText: String) {
                saveAsDialog.dismiss()
                if (!TextUtils.isEmpty(promptText)) {
                    val fileName = promptText.trim { it <= ' ' }
                    if (!isFileExist(fileName)) {
                        saveAs(fileName)
                    } else {
                        Snackbar.make(notePad!!, getString(R.string.fileAlreadyExists),
                                Snackbar.LENGTH_LONG).setAction(getString(R.string.overwrite)
                        ) {
                            deleteExistingFile(fileName)
                            saveAs(fileName)
                        }.show()
                    }
                }
            }
        })
        saveAsDialog.show()
    }

    /**
     * Does the given file name already exist?
     * @param fileName Given file name
     * @return yes or no
     */
    private fun isFileExist(fileName: String): Boolean {
        val file = File(getPathFolder(mContext!!) + fileName + EXTENSION)
        return file.exists()
    }

    /**
     * Delete existing file
     * @param fileName file name
     */
    private fun deleteExistingFile(fileName: String) {
        val file = File(getPathFolder(mContext!!) + fileName + EXTENSION)
        if (file.exists()) {
            file.delete()
        }
    }

    /**
     * Save into a file
     */
    private fun saveAs(fileName: String) {
        createDirectory(mContext!!)

        val file = File(getPathFolder(mContext!!) + fileName + EXTENSION)
        file.writeText(notePad!!.text.toString())

        if (file.exists()) {
            Snackbar.make(notePad!!, getString(R.string.writeSuccess),
                    Snackbar.LENGTH_LONG).show()
        } else {
            Snackbar.make(notePad!!, getString(R.string.writeError),
                    Snackbar.LENGTH_LONG).show()
        }
    }

    /**
     * Ask for read-write external storage permission
     */
    private fun askForPermissionExternalStorage() {
        if (ContextCompat.checkSelfPermission(this,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) { //permission yet to be granted
            getPermissionExternalStorage()
        } else { //permission already granted
            getFileName()
        }
    }

    /**
     * Request and get the permission for external storage
     */
    private fun getPermissionExternalStorage() {
        if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
            Snackbar.make(notePad!!, R.string.grantPermission,
                    Snackbar.LENGTH_LONG)
                    .setAction(R.string.ok) {
                        ActivityCompat.requestPermissions(this,
                                arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE),
                                REQUEST_EXTERNAL_STORAGE)
                    }.show()
        } else {
            ActivityCompat.requestPermissions(this,
                    arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE),
                    REQUEST_EXTERNAL_STORAGE)
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int,
                                   permissions: Array<String>, grantResults: IntArray) {
        when (requestCode) {
            REQUEST_EXTERNAL_STORAGE -> {
                //if request is cancelled, the result arrays are empty.
                if (grantResults.isNotEmpty()
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    getFileName()
                }
            }
        }
    }

    /**
     * Capitalize the first letter of a given text
     * @param text
     * @return
     */
    private fun capitalizeFirstLetter(text: String): String? {
        return text.substring(0, 1).toUpperCase(Locale.getDefault()) +
                text.substring(1).toLowerCase(Locale.getDefault())
    }

    /**
     * Updates note widget
     */
    private fun updateNoteWidget() {
        val appWidgetManager = AppWidgetManager.getInstance(mContext)
        val thisAppWidget = ComponentName(getPackageName(),
                this.javaClass.name)
        val updateWidget = Intent(mContext, NoteWidget::class.java)
        val appWidgetIds = appWidgetManager.getAppWidgetIds(thisAppWidget)
        updateWidget.action = NoteWidget.APP_TO_WID
        updateWidget.putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, appWidgetIds)
        mContext!!.sendBroadcast(updateWidget)
    }

    override fun onBackPressed() {
        saveText()

        super.onBackPressed()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == REQUEST_SPEECH && resultCode == Activity.RESULT_OK && data != null) {
            var results: ArrayList<String> = data!!.getStringArrayListExtra(
                    RecognizerIntent.EXTRA_RESULTS
            )
            if (results.isNotEmpty()) {
                var spokenText: String? = results.get(0)

                //capitalise the first letter
                spokenText = capitalizeFirstLetter(spokenText!!)
                //now append the spoken text
                appendNote(spokenText)
            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.item_save -> {
                saveText()
                finish()
                true
            }
            R.id.item_delete -> {
                eraseAll()
                true
            }
            R.id.item_listen -> {
                listenToUser()
                true
            }
            R.id.item_share -> {
                shareNote()
                true
            }
            R.id.item_save_as -> {
                askForPermissionExternalStorage()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    companion object {
        const val REQUEST_SPEECH = 100
        const val REQUEST_EXTERNAL_STORAGE = 101
        const val EXTENSION = ".txt"
    }
}