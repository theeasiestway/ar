package com.theeasiestway.stereoar.activity

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import com.theeasiestway.stereoar.ui.screens.arview.StereoArApp

/**
 * Created by Alexey Loboda on 17.01.2022
 */
class MainActivity: AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            StereoArApp(
                onCloseApp = {
                    finish()
                }
            )
        }
    }
}