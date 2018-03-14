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

package com.ferid.app.notetake.dialogs;

import android.app.Dialog;
import android.content.Context;
import android.support.design.widget.TextInputLayout;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.ferid.app.notetake.R;
import com.ferid.app.notetake.interfaces.PromptListener;

/**
 * Created by ferid.cafer on 4/3/2015.
 */
public class PromptDialog extends Dialog {

    private Context context;

    private final TextInputLayout inputLayoutContent;
    private final EditText content;
    private final Button positiveButton;

    private PromptListener promptListener;


    public PromptDialog(Context context__) {
        super(context__);
        setContentView(R.layout.prompt_dialog);

        context = context__;

        inputLayoutContent = findViewById(R.id.inputLayoutContent);
        content = findViewById(R.id.content);
        positiveButton = findViewById(R.id.positiveButton);

        content.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int keyCode, KeyEvent event) {
                if (keyCode == EditorInfo.IME_ACTION_DONE) {
                    promptPositive();
                    return true;
                }
                return false;
            }
        });
    }

    /**
     * Set positive button
     * @param value String
     */
    public void setPositiveButton(String value) {
        positiveButton.setText(value);
        positiveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                promptPositive();
            }
        });
    }

    /**
     * Positive button click listener
     * @param promptListener PromptListener
     */
    public void setOnPositiveClickListener(PromptListener promptListener) {
        this.promptListener = promptListener;
    }

    /**
     * Check validation, then prompt as positive
     */
    private void promptPositive() {
        String input = content.getText().toString();

        if (TextUtils.isEmpty(input)) {
            inputLayoutContent.setError(context.getString(R.string.enterFileName));
        } else {
            inputLayoutContent.setErrorEnabled(false);

            promptPositive(input);
        }
    }

    /**
     * Directly prompt as positive
     * @param input String
     */
    private void promptPositive(String input) {
        if (promptListener != null) {
            promptListener.OnPrompt(input);
        }
    }

    @Override
    public void show() {
        if (getWindow() != null) {
            getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);
        }

        super.show();
    }

}