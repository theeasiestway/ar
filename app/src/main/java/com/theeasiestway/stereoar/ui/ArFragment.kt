package com.theeasiestway.stereoar.ui

import android.Manifest
import android.util.Log
import android.widget.Toast
import com.google.ar.core.exceptions.*
import com.google.ar.sceneform.ux.ArFragment

/**
 * Created by Alexey Loboda on 29.01.2022
 */
class ArFragment: ArFragment() {

    override fun getAdditionalPermissions(): Array<String> {
        return arrayOf(
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.READ_EXTERNAL_STORAGE
        )
    }

    override fun handleSessionException(sessionException: UnavailableException?) {
        val message = when (sessionException) {
            is UnavailableArcoreNotInstalledException -> "Please install ARCore"
            is UnavailableApkTooOldException -> "Please update ARCore"
            is UnavailableSdkTooOldException -> "Please update this app"
            is UnavailableDeviceNotCompatibleException -> "This device does not support AR"
            else -> "Failed to create AR session"
        }
        Log.e("ArFragment", "Error: $message $sessionException")
        Toast.makeText(requireActivity(), message, Toast.LENGTH_LONG).show()
    }
}