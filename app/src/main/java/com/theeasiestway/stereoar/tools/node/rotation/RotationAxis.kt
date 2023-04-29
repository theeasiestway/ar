package com.theeasiestway.stereoar.tools.node.rotation

import com.google.ar.sceneform.math.Vector3

/**
 * Created by Alexey Loboda on 09.07.2022
 */

sealed class RotationAxis {
    object X: RotationAxis()
    object Y: RotationAxis()
    object Z: RotationAxis()
}

fun RotationAxis.toVector3(): Vector3 {
    return when(this) {
        RotationAxis.X -> Vector3.right()
        RotationAxis.Y -> Vector3.up()
        RotationAxis.Z -> Vector3.forward()
    }
}