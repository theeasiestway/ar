package com.theeasiestway.stereoar.tools.node.rotation

import com.google.ar.sceneform.math.Quaternion
import com.google.ar.sceneform.ux.BaseTransformableNode
import com.google.ar.sceneform.ux.RotationController
import com.google.ar.sceneform.ux.TwistGesture
import com.google.ar.sceneform.ux.TwistGestureRecognizer

/**
 * Created by Alexey Loboda on 09.07.2022
 */

class ArRotationController(
    transformableNode: BaseTransformableNode,
    gestureRecognizer: TwistGestureRecognizer
) : RotationController(transformableNode, gestureRecognizer) {

    var rotationAxis: RotationAxis = RotationAxis.Y

    override fun onContinueTransformation(gesture: TwistGesture) {
        val rotationAmount = -gesture.deltaRotationDegrees * rotationRateDegrees
        val rotationDelta = Quaternion(rotationAxis.toVector3(), rotationAmount)
        var localrotation = transformableNode.localRotation
        localrotation = Quaternion.multiply(localrotation, rotationDelta)
        transformableNode.localRotation = localrotation
    }
}