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

package com.ferid.app.notetake.prefs;

import android.content.Context;
import android.content.SharedPreferences;

import com.ferid.app.notetake.R;

/**
 * Created by ferid.cafer on 11/10/2014.
 */
public class PrefsUtil {

    private static SharedPreferences sPrefs;

    /**
     * Initialise shared preferences
     * @param context
     */
    private static void initialisePrefs(Context context) {
        sPrefs = context.getSharedPreferences(context.getString(R.string.sharedPreferences), 0);
    }

    /**
     * Get latest update date by day of year
     * @param context
     * @return
     */
    public static String getNote(Context context) {
        if (sPrefs == null) {
            initialisePrefs(context);
        }

        return sPrefs.getString(context.getString(R.string.prefNote), "");
    }

    /**
     * Set latest update date by day of year
     * @param context
     * @param value
     */
    public static void setNote(Context context, String value) {
        if (sPrefs == null) {
            initialisePrefs(context);
        }

        SharedPreferences.Editor editor = sPrefs.edit();
        editor.putString(context.getString(R.string.prefNote), value);
        editor.apply();
    }

    /**
     * Get font size
     * @param context
     * @return
     */
    public static int getFontSize(Context context) {
        if (sPrefs == null) {
            initialisePrefs(context);
        }

        return sPrefs.getInt(context.getString(R.string.prefFontSize), 0);
    }

    /**
     * Set font size
     * @param context
     * @param value
     */
    public static void setFontSize(Context context, int value) {
        if (sPrefs == null) {
            initialisePrefs(context);
        }

        SharedPreferences.Editor editor = sPrefs.edit();
        editor.putInt(context.getString(R.string.prefFontSize), value);
        editor.apply();
    }

}