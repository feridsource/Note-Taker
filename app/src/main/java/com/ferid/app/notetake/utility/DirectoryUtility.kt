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

package com.ferid.app.notetake.utility

import android.content.Context
import java.io.File

object DirectoryUtility {

    /**
     * Create directory for the application's use
     */
    fun createDirectory(context: Context) {
        var directory = File(getPathFolder(context))

        directory.mkdirs()
    }

    /**
     * Get folder where to save
     */
    fun getPathFolder(context: Context): String {
        return context.getExternalFilesDir(null)!!.absolutePath + "/note_widget/"
    }

}