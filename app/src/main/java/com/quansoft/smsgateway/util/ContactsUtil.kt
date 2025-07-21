package com.quansoft.smsgateway.util

import android.annotation.SuppressLint
import android.content.Context
import android.provider.ContactsContract
import android.net.Uri

object ContactsUtil {

    // A simple cache to avoid querying the content provider repeatedly for the same number.
    private val contactCache = mutableMapOf<String, String?>()

    @SuppressLint("Range")
    fun findContactName(context: Context, phoneNumber: String): String? {
        if (contactCache.containsKey(phoneNumber)) {
            return contactCache[phoneNumber]
        }

        val uri = Uri.withAppendedPath(ContactsContract.PhoneLookup.CONTENT_FILTER_URI, Uri.encode(phoneNumber))
        val projection = arrayOf(ContactsContract.PhoneLookup.DISPLAY_NAME)
        var contactName: String? = null

        try {
            context.contentResolver.query(uri, projection, null, null, null)?.use { cursor ->
                if (cursor.moveToFirst()) {
                    contactName = cursor.getString(cursor.getColumnIndex(ContactsContract.PhoneLookup.DISPLAY_NAME))
                }
            }
        } catch (e: Exception) {
            // Handle exceptions, e.g., security exception if permission is not granted
            e.printStackTrace()
        }

        contactCache[phoneNumber] = contactName
        return contactName
    }
}
