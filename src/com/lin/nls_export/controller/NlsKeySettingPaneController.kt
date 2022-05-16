package com.lin.nls_export.controller

import javafx.fxml.FXML
import javafx.scene.control.CheckBox
import javafx.scene.control.Label
import javafx.scene.control.TextField

class NlsKeySettingPaneController {
    @FXML
    lateinit var labelHandleName: Label // 需要控制的 label name

    @FXML
    lateinit var cbIsRead: CheckBox // 是否读取的 checkbox

    @FXML
    lateinit var textFieldForColumnName: TextField // 该字段在 excel 中的名字

    init {
//        cbIsRead.selectedProperty().addListener { _, _, isSelected ->
//            if (isSelected) {
//                textFieldForColumnName.isEditable = true
//                textFieldForColumnName.isMouseTransparent = false
//            } else {
//                textFieldForColumnName.isEditable = false
//                textFieldForColumnName.isMouseTransparent = true
//            }
//        }
    }

    fun showIsReadCheckbox(show: Boolean) {
        cbIsRead.visibleProperty().value = show
    }

    fun setLabel(label: String) {
        labelHandleName.text = label
    }

    fun setInputHint(hint: String) {
        textFieldForColumnName.promptText = hint
    }

    fun getInputColumnName(): String? {
        return takeIf { cbIsRead.isSelected }?.let { textFieldForColumnName.text }
    }
}