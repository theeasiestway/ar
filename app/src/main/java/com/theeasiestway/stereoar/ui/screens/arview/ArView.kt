package com.theeasiestway.stereoar.ui.screens.arview

import android.Manifest
import android.content.Context
import android.content.res.Configuration
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.util.Log
import android.view.SurfaceHolder
import android.view.SurfaceView
import android.widget.LinearLayout
import android.widget.Toast
import androidx.annotation.StringRes
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.google.accompanist.permissions.*
import com.google.ar.sceneform.ArSceneView
import com.google.ar.sceneform.rendering.ModelRenderable
import com.theeasiestway.stereoar.R
import com.theeasiestway.stereoar.tools.node.rotation.RotationAxis
import com.theeasiestway.stereoar.ui.theme.Colors
import java.util.*

/**
 * Created by Alexey Loboda on 29.01.2022
 */

private const val TAG_SURFACE_VIEW = "SurfaceView"
private const val TAG = "ArView"

@Composable
fun CreateArView(viewModel: ArViewModel) {
    RequestCameraPermission(
        onGranted = { CameraView(viewModel) },
        onCanBeRequested = { CameraPermissionDialogWithExplanation(it) },
        onCanNotBeRequested = { CameraPermissionDialogWithAppSettings() }
    )
}

@Composable
private fun CameraView(viewModel: ArViewModel) {
    val context = LocalContext.current
    val footprintModel = getFootprintModel(viewModel) ?: return
    val arModels by viewModel.getAllArModels().collectAsStateWithLifecycle(emptyList())
    var isVrMode by rememberSaveable { mutableStateOf(getLastVrMode(context)) }
    var showModelsList by remember { mutableStateOf(true) }
    var selectedVrModel by rememberSaveable { mutableStateOf<ModelRenderable?>(null) }
    var rotationAxis by remember { mutableStateOf<RotationAxis>(RotationAxis.Y) }
    var animationIndex by remember { mutableStateOf(0) }
    var needToClearScene by remember { mutableStateOf(false) }
    Box(modifier = Modifier.fillMaxSize()) {
        ArView(
            isVrMode = isVrMode,
            rotationAxis = rotationAxis,
            animationIndex = animationIndex,
            needToClearScene = needToClearScene,
            footprintModel = footprintModel,
            onPlaceModelClicked = { selectedVrModel },
            onModelsListClicked = { showModelsList = !showModelsList },
            onGetRandomModelClicked = {
                viewModel.getRandomModel {
                    showModelsList = false
                    selectedVrModel = it
                }
            },
            onVrOnClicked = {
                isVrMode = !isVrMode
                setLastVrMode(context, isVrMode)
            },
            onRotationAxisClicked = { rotationAxis = it },
            onNextAnimationClicked = {
                animationIndex = when(animationIndex) {
                    0 -> 1
                    else -> 0
                }
            },
            onClearClicked = { needToClearScene = true },
            onSceneCleared = { needToClearScene = false },
        )
        if (showModelsList) {
            ModelsList(arModels) {
                showModelsList = false
                viewModel.onModelClicked(it) { result -> result
                    .onFailure { error -> toastError(viewModel.getApplication(), R.string.error_loading_ar_model, error) }
                    .onSuccess { model -> selectedVrModel = model; Log.e("Wefewf", "model loaded: $model") }
                }
            }
        }
    }
}

fun getLastVrMode(context: Context): Boolean {
    val sp = context.getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
    return sp.getBoolean("vr_mode", false)
}

fun setLastVrMode(context: Context, vrMode: Boolean) {
    val sp = context.getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
    sp.edit().putBoolean("vr_mode", vrMode).apply()
}

@Composable
fun getFootprintModel(viewModel: ArViewModel): ModelRenderable? {
    val modelResult by viewModel.getFootprintArModel().collectAsStateWithLifecycle(null)
    val model = modelResult?.getOrNull()
    if (model == null) {
        toastError(LocalContext.current, R.string.error_start_ar_view)
    }
    return model
}

fun toastError(context: Context, @StringRes message: Int, error: Throwable? = null) {
    val text = context.getString(message)
    Toast.makeText(context, text, Toast.LENGTH_LONG).show()
    if (error != null) {
        Log.e(TAG, "$text; error: $error")
    }
}

@Composable
fun ModelsList(arModels: List<String>, onItemClicked: (String) -> Unit) {
    LazyColumn(
        modifier = Modifier
            .fillMaxWidth()
            .height(300.dp),
        verticalArrangement = Arrangement.Center,
    ) {
        itemsIndexed(arModels) { index, uri ->
            Box(modifier = Modifier
                .fillMaxWidth()
                .height(50.dp)
                .background(Colors.surface_default)
                .clickable { onItemClicked(uri) },
                contentAlignment = Alignment.CenterStart
            ) {
                Text(
                    text = "${stringResource(R.string.ar_view_models_list_model)}: $index; ${uri.split("/").last()}",
                    color = Colors.on_surface_primary
                )
            }
        }
    }
}

@Composable
private fun ArView(
    isVrMode: Boolean,
    rotationAxis: RotationAxis,
    animationIndex: Int,
    needToClearScene: Boolean,
    footprintModel: ModelRenderable,
    onPlaceModelClicked: () -> ModelRenderable?,
    onModelsListClicked: () -> Unit,
    onVrOnClicked: () -> Unit,
    onRotationAxisClicked: (RotationAxis) -> Unit,
    onNextAnimationClicked: () -> Unit,
    onGetRandomModelClicked: () -> Unit,
    onClearClicked: () -> Unit,
    onSceneCleared: () -> Unit
) {
    Box(modifier = Modifier.fillMaxSize()) {
        if (!isVrMode) {
            AndroidView(
                modifier = Modifier.fillMaxSize(),
                factory = { context ->
                    onRotationAxisClicked(RotationAxis.Y)
                    createScene(context, footprintModel, onPlaceModelClicked)
                },
                update = { arScene ->
                    arScene.setRotationAxis(rotationAxis)
                    arScene.setAnimationEnabled(true, animationIndex)
                    arScene.resume()
                    if (needToClearScene) {
                        arScene.clearScene()
                        arScene.setAnimationEnabled(false, animationIndex)
                        onSceneCleared()
                    }
                }
            )
        } else {
            AndroidView(
                modifier = Modifier.fillMaxSize(),
                factory = { context ->
                    onRotationAxisClicked(RotationAxis.Y)
                    val arScene = createScene(context, footprintModel, onPlaceModelClicked)
                    val surfaceView = SurfaceView(context).apply {
                        tag = TAG_SURFACE_VIEW
                    }
                    LinearLayout(context).apply {
                        val isLandscape = context.resources.configuration.orientation == Configuration.ORIENTATION_LANDSCAPE
                        val layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT).also {
                            it.weight = 1f
                        }
                        if (isLandscape) {
                            layoutParams.width = 0
                            orientation = LinearLayout.HORIZONTAL
                        } else {
                            layoutParams.height = 0
                            orientation = LinearLayout.VERTICAL
                        }
                        addView(arScene, layoutParams)
                        addView(surfaceView, layoutParams)
                    }
                },
                update = { view ->
                    val arScene = view.findViewWithTag<ArSceneView>(TAG_AR_SCENE_VIEW)
                    arScene.background = ColorDrawable(Color.CYAN)
                    val surfaceView = view.findViewWithTag<SurfaceView>(TAG_SURFACE_VIEW)
                    surfaceView.holder.addCallback(object : SurfaceHolder.Callback {
                        override fun surfaceChanged(holder: SurfaceHolder, format: Int, width: Int, height: Int) = Unit

                        override fun surfaceDestroyed(holder: SurfaceHolder) {
                            Log.d("wefewf", "DualCameraView surfaceDestroyed")
                            arScene.stopMirroringToSurface(holder.surface)
                            //arView.pause()
                        }

                        override fun surfaceCreated(holder: SurfaceHolder) {
                            Log.d("wefewf", "DualCameraView surfaceCreated")
                            arScene.resume()
                            arScene.startMirroringToSurface(holder.surface, 0, 0, arScene.width, arScene.height)
                        }
                    })
                    arScene.setAnimationEnabled(true, animationIndex)
                    arScene.setRotationAxis(rotationAxis)
                    if (needToClearScene) {
                        arScene.clearScene()
                        arScene.setAnimationEnabled(false, animationIndex)
                        onSceneCleared()
                    }
                }
            )
        }
        Row(
            modifier = Modifier.align(Alignment.BottomEnd),
            horizontalArrangement = Arrangement.End
        ) {
            /*Button(onClick = onNextAnimationClicked) {
                Text(text = "->")
            }*/
            Button(onClick = { onRotationAxisClicked(RotationAxis.X) }) {
                Text(text = stringResource(R.string.ar_view_rotation_axis_x))
            }
            Button(onClick = { onRotationAxisClicked(RotationAxis.Y) }) {
                Text(text = stringResource(R.string.ar_view_rotation_axis_y))
            }
            /*Button(onClick = { onRotationAxisClicked(RotationAxis.Z) }) {
                Text(text = stringResource(R.string.ar_view_rotation_axis_z))
            }*/
            Button(onClick = onGetRandomModelClicked) {
                Text("RND")
            }
            Button(onClick = onClearClicked) {
                Text(stringResource(R.string.ar_view_clear_scene).take(3))
            }
            Button(onClick = onModelsListClicked) {
                Text(stringResource(R.string.ar_view_models_list).take(3))
            }
            Button(onClick = onVrOnClicked) {
                Text(stringResource(if (!isVrMode) R.string.ar_view_turn_on_vr_mode else R.string.ar_view_turn_off_vr_mode).take(3).uppercase(Locale.getDefault()))
            }
        }
    }
}

@OptIn(ExperimentalPermissionsApi::class)
@Composable
private fun RequestCameraPermission(
    onGranted: @Composable () -> Unit,
    onCanBeRequested: @Composable (onOkClicked: () -> Unit) -> Unit,
    onCanNotBeRequested: @Composable () -> Unit,
) {
    var isGranted by remember { mutableStateOf(false) }
    val cameraPermissionState = rememberPermissionState(Manifest.permission.CAMERA) {
        isGranted = it
    }
    LaunchedEffect(cameraPermissionState) {
        cameraPermissionState.launchPermissionRequest()
    }
    if (isGranted) onGranted()
}
