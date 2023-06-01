package com.theeasiestway.stereoar.ui.screens.common.ext

import android.content.ContentResolver
import android.content.res.Resources
import android.net.Uri
import androidx.annotation.RawRes

/**
 * Created by Alexey Loboda on 14.02.2022
 */

internal fun @receiver:RawRes Int.toUri(resources: Resources): String {
    return with(resources) {
        Uri.Builder()
            .scheme(ContentResolver.SCHEME_ANDROID_RESOURCE)
            .authority(getResourcePackageName(this@toUri))
            .appendPath(getResourceTypeName(this@toUri))
            .appendPath(getResourceEntryName(this@toUri))
            .build()
            .toString()
    }
}