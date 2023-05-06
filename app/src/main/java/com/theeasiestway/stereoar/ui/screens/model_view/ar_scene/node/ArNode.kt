package com.theeasiestway.stereoar.ui.screens.model_view.ar_scene.node

import com.google.ar.sceneform.ux.TransformableNode
import com.google.ar.sceneform.ux.TransformationSystem
import com.theeasiestway.stereoar.ui.screens.model_view.ar_scene.node.rotation.ArRotationController

/**
 * Created by Alexey Loboda on 09.07.2022
 */
class ArNode(transformationSystem: TransformationSystem) : TransformableNode(transformationSystem) {

    private val arRotationController = ArRotationController(this, transformationSystem.twistRecognizer).apply {
        addTransformationController(this)
    }

    init {
        removeTransformationController(super.getRotationController())
    }

    override fun getRotationController(): ArRotationController {
        return arRotationController
    }
}