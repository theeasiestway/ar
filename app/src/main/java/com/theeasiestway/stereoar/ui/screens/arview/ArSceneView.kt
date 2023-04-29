package com.theeasiestway.stereoar.ui.screens.arview

import android.content.Context
import android.graphics.Color
import android.util.DisplayMetrics
import android.util.Log
import android.view.GestureDetector
import android.view.MotionEvent
import android.view.View
import com.google.ar.core.*
import com.google.ar.sceneform.*
import com.google.ar.sceneform.Camera
import com.google.ar.sceneform.math.Quaternion
import com.google.ar.sceneform.math.Vector3
import com.google.ar.sceneform.rendering.ModelRenderable
import com.google.ar.sceneform.rendering.ViewRenderable
import com.google.ar.sceneform.ux.FootprintSelectionVisualizer
import com.google.ar.sceneform.ux.TransformationSystem
import com.theeasiestway.stereoar.tools.node.ArNode
import com.theeasiestway.stereoar.tools.node.rotation.RotationAxis
import java.util.concurrent.TimeUnit


/**
 * Created by Alexey Loboda on 14.02.2022
 */
const val TAG_AR_SCENE_VIEW = "ArSceneView"
private const val modelNodeName = "modelNodeName"
private const val anchorNodeName = "anchorNodeName"
private val sceneUpdateListeners = mutableListOf<Scene.OnUpdateListener>()

private var sceneView: ArSceneView? = null
fun createScene(context: Context, footprintModel: ModelRenderable, onPlaceModelClicked: () -> ModelRenderable?): ArSceneView {
    destroyOldSceneView()
    sceneView = ArSceneView(context).apply {
        tag = TAG_AR_SCENE_VIEW
        val transformationSystem = createTransformationSystem(context.resources.displayMetrics, footprintModel)
        val gestureDetector = createGestureDetector(context) { handleGesture(it, transformationSystem, arFrame, scene, onPlaceModelClicked) }
        addOnPickTouchListener(scene, gestureDetector, transformationSystem)
        //setSceneBackground(scene, gestureDetector)
        setupSession(createSession(context))
    }
    return sceneView!!
}

fun destroyOldSceneView() {
    sceneView?.let { scene ->
        scene.session?.close()
        scene.destroy()
    }
}

fun ArSceneView.clearScene() {
    scene.children.toList().forEach { node ->
        if (node is AnchorNode) {
            node.anchor?.detach()
        }
        if (node !is Camera && node !is Sun) {
            node.setParent(null)
            scene.removeChild(node)
        }
    }
}

fun ArSceneView.setRotationAxis(axis: RotationAxis) {
    val modelNode = scene.findModelByName(modelNodeName)
    modelNode?.rotationController?.rotationAxis = axis
}

fun ArSceneView.setAnimationEnabled(enabled: Boolean, animationIndex: Int) {
    if (enabled) {
        val modelNode = scene.findModelByName(modelNodeName) ?: return
        val animator = modelNode.renderableInstance?.filamentAsset?.animator ?: return
        val startTime = System.nanoTime()
        Log.d("wqdqwdq", "[1] animationIndex: $animationIndex; animations count: ${animator.animationCount}")
        if (animator.animationCount > 0) {
            val updateListener = Scene.OnUpdateListener {
                animator.applyAnimation(animationIndex, // todo need to know animation index currently it's a magic number
                    ((System.nanoTime() - startTime) / TimeUnit.SECONDS.toNanos(1).toDouble()).toFloat() %
                            animator.getAnimationDuration(animationIndex)
                )
                animator.updateBoneMatrices()
            }
            scene.addOnUpdateListener(updateListener)
            sceneUpdateListeners.add(updateListener)
            Log.d("wqdqwdq", "listener added: $sceneUpdateListeners")
        }
    } else {
        sceneUpdateListeners.forEach { scene.removeOnUpdateListener(it) }
        sceneUpdateListeners.clear()
        Log.d("wqdqwdq", "listeners cleared: $sceneUpdateListeners")
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
    onPlaceModelClicked: () -> ModelRenderable?
) {
    if (frame == null) {
        Log.e(TAG_AR_SCENE_VIEW, "[handleGesture] motionEvent: $motionEvent; frame is null")
        return
    }
    val modelToPlace = onPlaceModelClicked()
    if (modelToPlace == null) {
        Log.w(TAG_AR_SCENE_VIEW, "[handleGesture] unable to place ar model because it's null")
    }
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

private fun addOnPickTouchListener(scene: Scene, gestureDetector: GestureDetector, transformationSystem: TransformationSystem) {
    scene.addOnPeekTouchListener { hitTestResult, motionEvent ->
        transformationSystem.onTouch(hitTestResult, motionEvent)
        if (hitTestResult.node == null) {
            gestureDetector.onTouchEvent(motionEvent)
        }
    }
}

private fun setSceneBackground(
    scene: Scene,
    gestureDetector: GestureDetector
) {
    val context = scene.view.context
    val wallsColor = Color.parseColor("#124516")
    ViewRenderable.builder()
        .setView(context, createBoxView(context, wallsColor))
        .build()
        .thenAccept { frontWall ->
            val frontWallNode = Node().apply {
                light = null
                localScale = Vector3(30f, 30f, 0f)
                localPosition = Vector3(0f, -50f, -25f)
                renderable = frontWall
                setOnTouchListener { _, motionEvent ->
                    gestureDetector.onTouchEvent(motionEvent)
                    true
                }
            }
            scene.addChild(frontWallNode)

            ViewRenderable.builder()
                .setView(context, createBoxView(context, wallsColor))
                .build()
                .thenAccept { leftWall ->
                    val leftWallNode = Node().apply {
                        light = null
                        localScale = Vector3(30f, 30f, 0f)
                        localPosition = Vector3(-10f, -50f, 0f)
                        localRotation = Quaternion.axisAngle(Vector3(0f, 1f, 0f), 90f)
                        renderable = leftWall
                        setOnTouchListener { _, motionEvent ->
                            gestureDetector.onTouchEvent(motionEvent)
                            true
                        }
                    }
                    scene.addChild(leftWallNode)

                    ViewRenderable.builder()
                        .setView(context, createBoxView(context, wallsColor))
                        .build()
                        .thenAccept { rightWall ->
                            val rightWallNode = Node().apply {
                                light = null
                                localScale = Vector3(30f, 30f, 0f)
                                localPosition = Vector3(10f, -50f, 0f)
                                localRotation = Quaternion.axisAngle(Vector3(0f, 1f, 0f), 90f)
                                renderable = rightWall
                                setOnTouchListener { _, motionEvent ->
                                    gestureDetector.onTouchEvent(motionEvent)
                                    true
                                }
                            }
                            scene.addChild(rightWallNode)

                            ViewRenderable.builder()
                                .setView(context, createBoxView(context, wallsColor))
                                .build()
                                .thenAccept { floor ->
                                    val floorNode = Node().apply {
                                        light = null
                                        localScale = Vector3(20f, 20f, 0f)
                                        localPosition = Vector3(0f, -10f, -25f)
                                        localRotation = Quaternion.axisAngle(Vector3(1f, 0f, 0f), 90f)
                                        renderable = floor
                                        setOnTouchListener { _, motionEvent ->
                                            gestureDetector.onTouchEvent(motionEvent)
                                            true
                                        }
                                    }
                                    scene.addChild(floorNode)
                                }
                        }

                    ViewRenderable.builder()
                        .setView(context, createBoxView(context, wallsColor))
                        .build()
                        .thenAccept { top ->
                            val topNode = Node().apply {
                                light = null
                                localScale = Vector3(20f, 20f, 0f)
                                localPosition = Vector3(0f, 10f, -25f)
                                localRotation = Quaternion.axisAngle(Vector3(1f, 0f, 0f), 90f)
                                renderable = top
                                setOnTouchListener { _, motionEvent ->
                                    gestureDetector.onTouchEvent(motionEvent)
                                    true
                                }
                            }
                            scene.addChild(topNode)
                        }

                    ViewRenderable.builder()
                        .setView(context, createBoxView(context, wallsColor))
                        .build()
                        .thenAccept { backWall ->
                            val backWallNode = Node().apply {
                                light = null
                                localScale = Vector3(20f, 20f, 0f)
                                //localPosition = Vector3(0f, -50f, -20f)
                                localPosition = Vector3(0f, -25f, 20f)
                                renderable = backWall
                                setOnTouchListener { _, motionEvent ->
                                    gestureDetector.onTouchEvent(motionEvent)
                                    true
                                }
                            }
                            scene.addChild(backWallNode)
                        }
                }
        }
}

fun createBoxView(context: Context, color: Int): View {
    /*val frameLayout = FrameLayout(context).apply {
        setBackgroundColor(Color.BLUE)
    }
    val leftWall = View(context).apply {
        rotationY = 20f
        setBackgroundColor(Color.YELLOW)
    }
    val leftParams = FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.MATCH_PARENT)
        .apply {
            gravity = Gravity.START
        }
    val rightWall = View(context).apply {
        rotationY = -20f
        setBackgroundColor(Color.RED)
    }
    val rightParams = FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.MATCH_PARENT)
        .apply {
            gravity = Gravity.END
        }
    frameLayout.addView(leftWall, leftParams)
    frameLayout.addView(rightWall, rightParams)
    return frameLayout*/
    return View(context).apply {
        setBackgroundColor(color)
    }
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

private fun Scene.findModelByName(modelName: String): ArNode? {
    val anchorNode = children.firstOrNull { it.name == anchorNodeName }
    return anchorNode?.children?.firstOrNull { it.name == modelName } as? ArNode
}