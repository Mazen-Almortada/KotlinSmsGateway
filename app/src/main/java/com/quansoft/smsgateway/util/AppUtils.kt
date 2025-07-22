package com.quansoft.smsgateway.util

import android.content.Context
import android.content.pm.PackageInfo

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
}
