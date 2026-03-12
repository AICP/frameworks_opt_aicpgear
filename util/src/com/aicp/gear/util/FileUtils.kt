/*
 * SPDX-FileCopyrightText: 2016 The CyanogenMod Project
 * SPDX-FileCopyrightText: 2026 Android Ice Cold Project
 * SPDX-License-Identifier: Apache-2.0
 */
package com.aicp.gear.util

import android.util.Log
import java.io.BufferedReader
import java.io.BufferedWriter
import java.io.File
import java.io.FileNotFoundException
import java.io.FileReader
import java.io.FileWriter
import java.io.IOException
import java.lang.NullPointerException
import java.lang.SecurityException

object FileUtils {

    private const val TAG = "FileUtils"

    /**
     * Reads the first line of text from the given file.
     * Reference BufferedReader#readLine() for clarification on what a line is
     *
     * @return the read line contents, or null on failure
     */
    @JvmStatic
    fun readOneLine(fileName: String): String? {
        var line: String? = null
        var reader: BufferedReader? = null

        try {
            reader = BufferedReader(FileReader(fileName), 512)
            line = reader.readLine()
        } catch (e: FileNotFoundException) {
            Log.w(TAG, "No such file $fileName for reading", e)
        } catch (e: IOException) {
            Log.e(TAG, "Could not read from file $fileName", e)
        } finally {
            try {
                reader?.close()
            } catch (_: IOException) {
                // Ignored, not much we can do anyway
            }
        }

        return line
    }

    /**
     * Writes the given value into the given file
     *
     * @return true on success, false on failure
     */
    @JvmStatic
    fun writeLine(fileName: String, value: String): Boolean {
        var writer: BufferedWriter? = null

        try {
            writer = BufferedWriter(FileWriter(fileName))
            writer.write(value)
        } catch (e: FileNotFoundException) {
            Log.w(TAG, "No such file $fileName for writing", e)
            return false
        } catch (e: IOException) {
            Log.e(TAG, "Could not write to file $fileName", e)
            return false
        } finally {
            try {
                writer?.close()
            } catch (_: IOException) {
                // Ignored
            }
        }

        return true
    }

    /**
     * Checks whether the given file exists
     *
     * @return true if exists, false if not
     */
    @JvmStatic
    fun fileExists(fileName: String): Boolean {
        val file = File(fileName)
        return file.exists()
    }

    /**
     * Checks whether the given file is readable
     *
     * @return true if readable, false if not
     */
    @JvmStatic
    fun isFileReadable(fileName: String): Boolean {
        val file = File(fileName)
        return file.exists() && file.canRead()
    }

    /**
     * Checks whether the given file is writable
     *
     * @return true if writable, false if not
     */
    @JvmStatic
    fun isFileWritable(fileName: String): Boolean {
        val file = File(fileName)
        return file.exists() && file.canWrite()
    }

    /**
     * Deletes an existing file
     *
     * @return true if the delete was successful, false if not
     */
    @JvmStatic
    fun delete(fileName: String): Boolean {
        val file = File(fileName)
        var ok = false
        try {
            ok = file.delete()
        } catch (e: SecurityException) {
            Log.w(TAG, "SecurityException trying to delete $fileName", e)
        }
        return ok
    }

    /**
     * Renames an existing file
     *
     * @return true if the rename was successful, false if not
     */
    @JvmStatic
    fun rename(srcPath: String, dstPath: String): Boolean {
        val srcFile = File(srcPath)
        val dstFile = File(dstPath)
        var ok = false
        try {
            ok = srcFile.renameTo(dstFile)
        } catch (e: SecurityException) {
            Log.w(TAG, "SecurityException trying to rename $srcPath to $dstPath", e)
        } catch (e: NullPointerException) {
            Log.e(TAG, "NullPointerException trying to rename $srcPath to $dstPath", e)
        }
        return ok
    }
}
