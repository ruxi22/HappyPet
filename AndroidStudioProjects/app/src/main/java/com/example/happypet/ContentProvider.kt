package com.example.happypet

import android.content.ContentProvider
import android.content.ContentValues
import android.content.UriMatcher
import android.database.Cursor
import android.database.MatrixCursor
import android.net.Uri

class InstagramContentProvider : ContentProvider() {

    companion object {
        const val AUTHORITY = "com.example.happypet.provider"
        const val INSTAGRAM_PATH = "instagram_link"
        const val INSTAGRAM_LINK = 1

        val CONTENT_URI: Uri = Uri.parse("content://$AUTHORITY/$INSTAGRAM_PATH")

        private val uriMatcher = UriMatcher(UriMatcher.NO_MATCH).apply {
            addURI(AUTHORITY, INSTAGRAM_PATH, INSTAGRAM_LINK)
        }
    }

    override fun onCreate(): Boolean {
        return true
    }

    override fun query(
        uri: Uri,
        projection: Array<out String>?,
        selection: String?,
        selectionArgs: Array<out String>?,
        sortOrder: String?
    ): Cursor? {
        if (uriMatcher.match(uri) == INSTAGRAM_LINK) {
            val cursor = MatrixCursor(arrayOf("link"))
            cursor.addRow(arrayOf("https://www.instagram.com/happy.pet.app/"))
            return cursor
        }
        return null
    }

    override fun getType(uri: Uri): String? {
        return when (uriMatcher.match(uri)) {
            INSTAGRAM_LINK -> "vnd.android.cursor.item/vnd.$AUTHORITY.$INSTAGRAM_PATH"
            else -> null
        }
    }

    override fun insert(uri: Uri, values: ContentValues?): Uri? {
        throw UnsupportedOperationException("Insert operation is not supported")
    }

    override fun delete(uri: Uri, selection: String?, selectionArgs: Array<out String>?): Int {
        throw UnsupportedOperationException("Delete operation is not supported")
    }

    override fun update(
        uri: Uri,
        values: ContentValues?,
        selection: String?,
        selectionArgs: Array<out String>?
    ): Int {
        throw UnsupportedOperationException("Update operation is not supported")
    }
}