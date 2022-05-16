package com.lin.nls_export.utils

import javafx.event.EventTarget
import javafx.scene.Node
import javafx.scene.Scene
import javafx.stage.Stage

fun EventTarget.getAbsoluteX(): Double {
    return when (this) {
        is Stage -> this.x
        is Scene -> this.x + this.window.getAbsoluteX()
        is Node -> {
            if (this.parent == null) {
                this.layoutX + this.scene.getAbsoluteX()
            } else {
                this.layoutX + this.parent.getAbsoluteX()
            }
        }
        else -> 0.0
    }
}

fun EventTarget.getAbsoluteY(): Double {
    return when (this) {
        is Stage -> this.y
        is Scene -> this.y + this.window.getAbsoluteY()
        is Node -> {
            if (this.parent == null) {
                this.layoutY + this.scene.getAbsoluteY()
            } else {
                this.layoutY + this.parent.getAbsoluteY()
            }
        }
        else -> 0.0
    }
}