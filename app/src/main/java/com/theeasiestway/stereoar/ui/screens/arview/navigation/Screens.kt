package com.theeasiestway.stereoar.ui.screens.arview.navigation

import androidx.compose.runtime.Composable
import com.theeasiestway.stereoar.ui.screens.arview.ArViewModel
import com.theeasiestway.stereoar.ui.screens.arview.CreateArView
import com.theeasiestway.stereoar.ui.screens.choose_model.ChooseArModel
import com.theeasiestway.stereoar.ui.screens.choose_model.ModelItem
import com.theeasiestway.stereoar.ui.screens.choose_model.viewmodel.ChooseArModelViewModel

/**
 * Created by Alexey Loboda on 04.09.2022
 */
sealed class Screens {
    abstract val route: String
    abstract val screen: @Composable () -> Unit

    class ChooseModel(
        viewModel: ChooseArModelViewModel,
        onModelChosen: (ModelItem) -> Unit,
        onBackPressed: () -> Unit
    ) : Screens() {
        override val route = "ChooseArModel"
        override val screen = @Composable {
            ChooseArModel(viewModel = viewModel, onModelChosen = onModelChosen, onBackPressed)
        }
    }

    class ModelView(
        viewModel: ArViewModel
    ) : Screens() {
        override val route = "ModelView"
        override val screen = @Composable {
            CreateArView(viewModel = viewModel)
        }
    }
}