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

import android.os.Environment
import java.io.File

object DirectoryUtility {

    //application's folder path
    private val PATH_FOLDER: String = (Environment.getExternalStorageDirectory().toString()
            + "/note_widget/")

    /**
     * Checks if external storage is available for read and write
     * @return
     */
    fun isExternalMounted(): Boolean {
        var state = Environment.getExternalStorageState()

        return Environment.MEDIA_MOUNTED.equals(state)
    }

    /**
     * Create directory for the application's use
     */
    fun createDirectory() {
        var directory = File(PATH_FOLDER)

        directory.mkdir()
    }

    fun getPathFolder(): String {
        return PATH_FOLDER
    }
}