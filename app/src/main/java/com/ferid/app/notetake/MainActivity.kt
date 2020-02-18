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

import android.app.Activity
import android.appwidget.AppWidgetManager
import android.content.ActivityNotFoundException
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.speech.RecognizerIntent
import android.view.Gravity
import android.view.Menu
import android.view.MenuItem
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import com.ferid.app.notetake.prefs.PrefsUtil
import com.ferid.app.notetake.widget.NoteWidget
import java.util.*

class MainActivity : AppCompatActivity() {

    private var mContext: Context? = null
    private var notePad: EditText? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        mContext = this

        val toolbar: Toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)

        notePad = findViewById(R.id.notePad)

        readNote()
    }

    /**
     * Retrieves from preferences
     */
    private fun readNote() {
       Handler().post {
           writeNote(PrefsUtil.getNote(mContext!!))
       }
    }

    /**
     * Write a text into notePad
     * @param note note
     */
    private fun writeNote(note: String?) {
        notePad!!.setText(note)
        notePad!!.setSelection(notePad!!.text!!.length)
    }

    /**
     * Append note
     * @param note
     */
    private fun appendNote(note: String?) {
        //get the current note which was already written
        var currentNote: String? = notePad!!.text.toString()
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
        PrefsUtil.setNote(mContext!!, notePad!!.text.toString())

        updateNoteWidget()
    }

    /**
     * Shares note. Share all on the page.
     */
    private fun shareNote() {
        val intent = Intent(Intent.ACTION_SEND)
        intent.type = "text/plain"
        intent.putExtra(Intent.EXTRA_SUBJECT, getString(R.string.app_name))
        intent.putExtra(Intent.EXTRA_TEXT, notePad!!.text.toString())
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
        val thisAppWidget = ComponentName(packageName,
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
            val results: ArrayList<String> = data.getStringArrayListExtra(
                    RecognizerIntent.EXTRA_RESULTS
            )
            if (results.isNotEmpty()) {
                var spokenText: String? = results[0]

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
            else -> super.onOptionsItemSelected(item)
        }
    }

    companion object {
        const val REQUEST_SPEECH = 100
    }
}