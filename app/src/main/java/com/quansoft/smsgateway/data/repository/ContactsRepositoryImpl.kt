package com.quansoft.smsgateway.data.repository

import android.annotation.SuppressLint
import android.content.Context
import android.net.Uri
import android.provider.ContactsContract
import com.quansoft.smsgateway.domain.repository.ContactsRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ContactsRepositoryImpl @Inject constructor(
    @ApplicationContext private val context: Context // <-- هنا نستخدم Context بشكل صحيح
) : ContactsRepository {

    private val contactCache = mutableMapOf<String, String?>()

    @SuppressLint("Range")
    override fun findContactName(phoneNumber: String): String? {
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
            e.printStackTrace()
        }
        contactCache[phoneNumber] = contactName
        return contactName
    }
}