package com.quansoft.smsgateway.util

import android.content.Context
import android.content.pm.PackageInfo
import android.text.format.DateUtils
import androidx.compose.runtime.Composable

object AppUtils {
    /**
     * Retrieves the application's version name from the package manager.
     */
    fun getAppVersion(context: Context): String? {
        return try {
            val pInfo: PackageInfo = context.packageManager.getPackageInfo(context.packageName, 0)
            pInfo.versionName
        } catch (e: Exception) {
            e.printStackTrace()
            "N/A"
        }
    }
    @Composable
    fun formatTimestamp(timestamp: Long): String {
        return DateUtils.getRelativeTimeSpanString(
            timestamp,
            System.currentTimeMillis(),
            DateUtils.MINUTE_IN_MILLIS
        ).toString()
    }
}
