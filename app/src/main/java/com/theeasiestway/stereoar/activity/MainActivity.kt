package com.theeasiestway.stereoar.activity

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.theeasiestway.stereoar.di.modelsExplorerScopeId
import com.theeasiestway.stereoar.ui.screens.arview.ArViewModel
import com.theeasiestway.stereoar.ui.screens.arview.StereoArApp
import com.theeasiestway.stereoar.ui.screens.choose_model.viewmodel.ChooseArModelViewModelImpl
import org.koin.androidx.compose.getKoin
import org.koin.core.qualifier.named

/**
 * Created by Alexey Loboda on 17.01.2022
 */
class MainActivity: AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            StereoArApp()
        }
    }
}