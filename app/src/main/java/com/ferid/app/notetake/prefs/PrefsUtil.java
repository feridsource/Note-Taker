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
    private static volatile PrefsUtil instance = null;
    private static SharedPreferences prefs;
    private static Context context;

    public static PrefsUtil getInstance(Context context__) {
        if (instance == null) {
            synchronized (PrefsUtil.class){
                if (instance == null) {
                    instance = new PrefsUtil();
                    context = context__;
                    prefs = context.getSharedPreferences(context.getString(R.string.sharedPreferences), 0);
                }
            }
        }
        return instance;
    }

    /**
     * Get latest update date by day of year
     * @return
     */
    public String getNote() {
        if (prefs != null) {
            return prefs.getString(context.getString(R.string.prefNote), "");
        } else {
            return "";
        }
    }

    /**
     * Set latest update date by day of year
     * @param value
     */
    public void setNote(String value) {
        if (prefs != null) {
            SharedPreferences.Editor editor = prefs.edit();
            editor.putString(context.getString(R.string.prefNote), value);
            editor.apply();
        }
    }

    /**
     * Is widget transparent
     * @return
     */
    public boolean isWidgetTransparent() {
        if (prefs != null) {
            return prefs.getBoolean(context.getString(R.string.prefTransparency), false);
        } else {
            return false;
        }
    }

    /**
     * Make widget either transparent or opaque.<br />
     * If it is transparent, convert to opaque. Vise a versa.
     */
    public void changeWidgetTransparency() {
        if (prefs != null) {
            boolean isTransparent = prefs.getBoolean(context.getString(R.string.prefTransparency),
                    false);

            SharedPreferences.Editor editor = prefs.edit();
            editor.putBoolean(context.getString(R.string.prefTransparency), !isTransparent);
            editor.apply();
        }
    }

}