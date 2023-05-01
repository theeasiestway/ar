package com.theeasiestway.stereoar.ui.screens.model_view.ar_scene

import android.content.Context
import android.util.DisplayMetrics
import android.view.GestureDetector
import android.view.MotionEvent
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView
import com.google.ar.core.*
import com.google.ar.sceneform.*
import com.google.ar.sceneform.Camera
import com.google.ar.sceneform.rendering.ModelRenderable
import com.google.ar.sceneform.ux.FootprintSelectionVisualizer
import com.google.ar.sceneform.ux.TransformationSystem
import com.theeasiestway.stereoar.tools.node.ArNode

private const val anchorNodeName = "anchorNode"
private const val modelNodeName = "modelNode"

@Composable
fun ArScene(
    state: ArSceneState,
    modifier: Modifier = Modifier,
    onSceneCleared: () -> Unit
) {
    AndroidView(
        modifier = modifier,
        factory = { context ->
            val transformationSystem = createTransformationSystem(context.resources.displayMetrics, state.footPrintModel)
            ArSceneView(context).apply {
                scene.addOnPickTouchListener(
                    transformationSystem = transformationSystem,
                    gestureDetector = createGestureDetector(context) { motionEvent ->
                        handleGesture(motionEvent, transformationSystem, arFrame, scene) {
                            state.model
                        }
                    },
                )
                setupSession(createSession(context))
            }
        },
        update = { arSceneView ->
            arSceneView.resume()
            when {
                state.clearScene -> {
                    arSceneView.clearScene()
                    onSceneCleared()
                }
            }
        }
    )
}

private fun createSession(context: Context): Session {
    val session = Session(context)
    val config = Config(session).apply {
        lightEstimationMode = Config.LightEstimationMode.AMBIENT_INTENSITY
        updateMode = Config.UpdateMode.LATEST_CAMERA_IMAGE
        planeFindingMode = Config.PlaneFindingMode.HORIZONTAL
    }
    session.configure(config)
    return session
}

private fun ArSceneView.clearScene() {
    scene.children.forEach { node ->
        if (node is AnchorNode) {
            node.anchor?.detach()
        }
        if (node !is Camera && node !is Sun) {
            node.setParent(null)
            scene.removeChild(node)
        }
    }
}

private fun createTransformationSystem(displayMetrics: DisplayMetrics, footprintModel: ModelRenderable): TransformationSystem {
    val footprintSelectionVisualizer = FootprintSelectionVisualizer()
    val transformationSystem = TransformationSystem(displayMetrics, footprintSelectionVisualizer)
    footprintSelectionVisualizer.footprintRenderable = footprintModel
    return transformationSystem
}

private fun createGestureDetector(context: Context, onSingleTap: (MotionEvent) -> Unit): GestureDetector {
    return GestureDetector(context, object: GestureDetector.SimpleOnGestureListener() {
        override fun onSingleTapUp(e: MotionEvent): Boolean {
            onSingleTap(e)
            return true
        }

        override fun onDown(e: MotionEvent): Boolean {
            return true
        }
    })
}

private fun handleGesture(
    motionEvent: MotionEvent,
    transformationSystem: TransformationSystem,
    frame: Frame?,
    scene: Scene,
    onPlaceModelClicked: () -> ModelRenderable
) {
    if (frame == null) return
    val modelToPlace = onPlaceModelClicked()
    if (frame.camera.trackingState == TrackingState.TRACKING) {
        transformationSystem.selectNode(null)
        for (hit in frame.hitTest(motionEvent)) {
            val trackable = hit.trackable
            if (trackable is Plane && trackable.isPoseInPolygon(hit.hitPose)) {
                val anchor = hit.createAnchor()
                val anchorNode = AnchorNode(anchor).apply {
                    name = anchorNodeName
                    setParent(scene)
                }
                ArNode(transformationSystem).apply {
                    name = modelNodeName
                    renderable = modelToPlace
                    setParent(anchorNode)
                    select()
                }
                break
            }
        }
    }
}

private fun Scene.addOnPickTouchListener(gestureDetector: GestureDetector, transformationSystem: TransformationSystem) {
    addOnPeekTouchListener { hitTestResult, motionEvent ->
        transformationSystem.onTouch(hitTestResult, motionEvent)
        if (hitTestResult.node == null) {
            gestureDetector.onTouchEvent(motionEvent)
        }
    }
}