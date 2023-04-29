package com.theeasiestway.data.mappers

import android.content.ContentResolver
import android.content.res.Resources
import android.net.Uri
import androidx.annotation.RawRes

/**
 * Created by Alexey Loboda on 14.02.2022
 */

fun rawIdToString(@RawRes id: Int, resources: Resources): String {
    return with(resources) {
        Uri.Builder()
            .scheme(ContentResolver.SCHEME_ANDROID_RESOURCE)
            .authority(getResourcePackageName(id))
            .appendPath(getResourceTypeName(id))
            .appendPath(getResourceEntryName(id))
            .build()
            .toString()
    }
}