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

package com.ferid.app.notetake.dialogs

import android.app.Dialog
import android.content.Context
import android.text.TextUtils
import android.view.WindowManager
import android.view.inputmethod.EditorInfo
import android.widget.Button
import android.widget.EditText
import android.widget.TextView.OnEditorActionListener
import com.ferid.app.notetake.R
import com.ferid.app.notetake.interfaces.PromptListener
import com.google.android.material.textfield.TextInputLayout

/**
 * Ask user the name of the file to be saved
 */
class SaveAsDialog(context: Context) : Dialog(context) {

    private val inputLayoutContent: TextInputLayout
    private val content: EditText
    private val positiveButton: Button
    private var promptListener: PromptListener? = null
    /**
     * Set positive button
     * @param value String
     */
    fun setPositiveButton(value: String?) {
        positiveButton.text = value
        positiveButton.setOnClickListener { promptPositive() }
    }

    /**
     * Positive button click listener
     * @param promptListener PromptListener
     */
    fun setOnPositiveClickListener(promptListener: PromptListener?) {
        this.promptListener = promptListener
    }

    /**
     * Check validation, then prompt as positive
     */
    private fun promptPositive() {
        val input = content.text.toString()
        if (TextUtils.isEmpty(input)) {
            inputLayoutContent.error = context.getString(R.string.enterFileName)
        } else {
            inputLayoutContent.isErrorEnabled = false
            promptPositive(input)
        }
    }

    /**
     * Directly prompt as positive
     * @param input String
     */
    private fun promptPositive(input: String) {
        if (promptListener != null) {
            promptListener!!.onPrompt(input)
        }
    }

    /**
     * Show keyboard to type file name
     */
    override fun show() {
        if (window != null) {
            getWindow()!!.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE)
        }
        super.show()
    }

    init {
        setContentView(R.layout.dialog_save_as)

        inputLayoutContent = findViewById(R.id.inputLayoutContent)
        content = findViewById(R.id.content)
        positiveButton = findViewById(R.id.positiveButton)
        content.setOnEditorActionListener(OnEditorActionListener { v, keyCode, event ->
            if (keyCode == EditorInfo.IME_ACTION_DONE) {
                promptPositive()
                return@OnEditorActionListener true
            }
            false
        })
    }
}