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

package com.ferid.app.notetake.prefs

import android.content.Context
import android.content.Context.MODE_PRIVATE
import android.content.SharedPreferences
import com.ferid.app.notetake.R

/**
 * Shared preferences
 */
object PrefsUtil {

    private var prefs: SharedPreferences? = null

    /**
     * Initialise shared preferences
     * @param context
     */
    private fun initialisePrefs(context: Context) {
        prefs = context.getSharedPreferences(context.getString(R.string.sharedPreferences),
                MODE_PRIVATE)
    }

    /**
     * Get note
     */
    fun getNote(context: Context): String? {
        if (prefs == null) {
            initialisePrefs(context)
        }

        return prefs!!.getString(context.getString(R.string.prefNote), "")
    }

    /**
     * Set note
     * @param context
     * @param value
     */
    fun setNote(context: Context, value: String) {
        if (prefs == null) {
            initialisePrefs(context)
        }

        val editor = prefs!!.edit()
        editor.putString(context.getString(R.string.prefNote), value)
        editor.apply()
    }
}