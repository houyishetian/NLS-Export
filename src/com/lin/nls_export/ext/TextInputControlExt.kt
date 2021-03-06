package com.lin.nls_export.ext

import javafx.scene.control.TextInputControl

fun TextInputControl.disableEdit() {
    isEditable = false
    isMouseTransparent = true
}

fun TextInputControl.enableEdit() {
    isEditable = true
    isMouseTransparent = false
}

fun TextInputControl.enableEdit(enable: Boolean) {
    if (enable) enableEdit() else disableEdit()
}