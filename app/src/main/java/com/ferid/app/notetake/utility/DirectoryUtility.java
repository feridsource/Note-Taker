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

package com.ferid.app.notetake.utility;

import android.os.Environment;

import java.io.File;

/**
 * Created by ferid.cafer on 10/12/2015.
 */
public class DirectoryUtility {
    //application's folder path
    private static final String PATH_FOLDER = Environment.getExternalStorageDirectory()
            + "/note_widget/";

    /**
     * Checks if external storage is available for read and write
     * @return
     */
    public static boolean isExternalStorageMounted() {
        String state = Environment.getExternalStorageState();

        return Environment.MEDIA_MOUNTED.equals(state);
    }

    /**
     * Create directory for the application's use
     */
    public static void createDirectory() {
        // Output stream
        // create a File object for the parent directory
        File directory = new File(PATH_FOLDER);
        // have the object build the directory structure, if needed.
        directory.mkdirs();
    }

    public static String getPathFolder() {
        return PATH_FOLDER;
    }
}
