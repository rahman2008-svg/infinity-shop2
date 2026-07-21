package com.example.util

import android.content.ContentValues
import android.content.Context
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream
import java.io.OutputStream

object StorageHelper {

    /**
     * Saves a text/JSON string directly to the public Downloads folder of the device.
     * On Android 10+ (API 29+), it uses MediaStore.Downloads.
     * On older Android versions, it uses direct File API in the public downloads directory.
     * Returns the user-friendly path/description where the file was saved.
     */
    fun saveTextFileToDownloads(
        context: Context,
        fileName: String,
        mimeType: String,
        content: String
    ): String? {
        return try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                val resolver = context.contentResolver
                val contentValues = ContentValues().apply {
                    put(MediaStore.MediaColumns.DISPLAY_NAME, fileName)
                    put(MediaStore.MediaColumns.MIME_TYPE, mimeType)
                    put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS)
                }
                val uri: Uri? = resolver.insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, contentValues)
                if (uri != null) {
                    resolver.openOutputStream(uri)?.use { outputStream ->
                        outputStream.write(content.toByteArray())
                    }
                    "Downloads/$fileName"
                } else {
                    // Fallback to legacy file system write under Scoped Storage constraints if resolver failed
                    saveLegacyFile(fileName, content.toByteArray())
                }
            } else {
                saveLegacyFile(fileName, content.toByteArray())
            }
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    /**
     * Saves raw bytes (like a PDF invoice) directly to the public Downloads folder of the device.
     */
    fun saveBytesToDownloads(
        context: Context,
        fileName: String,
        mimeType: String,
        bytes: ByteArray
    ): String? {
        return try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                val resolver = context.contentResolver
                val contentValues = ContentValues().apply {
                    put(MediaStore.MediaColumns.DISPLAY_NAME, fileName)
                    put(MediaStore.MediaColumns.MIME_TYPE, mimeType)
                    put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS)
                }
                val uri: Uri? = resolver.insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, contentValues)
                if (uri != null) {
                    resolver.openOutputStream(uri)?.use { outputStream ->
                        outputStream.write(bytes)
                    }
                    "Downloads/$fileName"
                } else {
                    saveLegacyFile(fileName, bytes)
                }
            } else {
                saveLegacyFile(fileName, bytes)
            }
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    /**
     * Helper to load a backup file content from the public Downloads folder.
     * Helps restore data directly from the user's local storage Downloads folder.
     */
    fun readTextFileFromDownloads(context: Context, fileName: String): String? {
        return try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                val resolver = context.contentResolver
                val projection = arrayOf(MediaStore.MediaColumns._ID)
                val selection = "${MediaStore.MediaColumns.DISPLAY_NAME} = ?"
                val selectionArgs = arrayOf(fileName)
                
                val cursor = resolver.query(
                    MediaStore.Downloads.EXTERNAL_CONTENT_URI,
                    projection,
                    selection,
                    selectionArgs,
                    null
                )
                
                var fileUri: Uri? = null
                cursor?.use { c ->
                    if (c.moveToFirst()) {
                        val idColumn = c.getColumnIndexOrThrow(MediaStore.MediaColumns._ID)
                        val id = c.getLong(idColumn)
                        fileUri = Uri.withAppendedPath(MediaStore.Downloads.EXTERNAL_CONTENT_URI, id.toString())
                    }
                }
                
                if (fileUri != null) {
                    resolver.openInputStream(fileUri!!)?.use { inputStream ->
                        inputStream.bufferedReader().use { it.readText() }
                    }
                } else {
                    readLegacyFile(fileName)
                }
            } else {
                readLegacyFile(fileName)
            }
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    private fun saveLegacyFile(fileName: String, data: ByteArray): String? {
        return try {
            val downloadsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
            if (!downloadsDir.exists()) {
                downloadsDir.mkdirs()
            }
            val file = File(downloadsDir, fileName)
            FileOutputStream(file).use { fos ->
                fos.write(data)
            }
            file.absolutePath
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    private fun readLegacyFile(fileName: String): String? {
        return try {
            val downloadsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
            val file = File(downloadsDir, fileName)
            if (file.exists()) {
                file.readText()
            } else {
                null
            }
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
}
