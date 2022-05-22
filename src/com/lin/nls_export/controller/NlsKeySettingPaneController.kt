package com.lin.nls_export.controller

import com.lin.nls_export.entities.NlsColumnInputBean
import com.lin.nls_export.ext.disableEdit
import com.lin.nls_export.ext.enableEdit
import javafx.fxml.FXML
import javafx.scene.control.CheckBox
import javafx.scene.control.Label
import javafx.scene.control.TextField

class NlsKeySettingPaneController {
    @FXML
    private lateinit var labelHandleName: Label // 需要控制的 label name

    @FXML
    private lateinit var cbIsRead: CheckBox // 是否读取的 checkbox

    @FXML
    private lateinit var textFieldForColumnName: TextField // 该字段在 excel 中的名字

    fun showIsReadCheckbox(show: Boolean, selectedListener: ((Boolean) -> Unit)? = null) {
        cbIsRead.visibleProperty().value = show

        if (show) {
            cbIsRead.selectedProperty().addListener { _, _, isSelected ->
                if (isSelected) {
                    textFieldForColumnName.enableEdit()
                } else {
                    textFieldForColumnName.disableEdit()
                }
                selectedListener?.invoke(isSelected)
            }
        }
    }

    fun isReadCheckBox(isRead: Boolean) {
        cbIsRead.isSelected = isRead
    }

    fun setLabel(label: String) {
        labelHandleName.text = label
    }

    fun setInputHint(hint: String) {
        textFieldForColumnName.promptText = hint
    }

    fun setInputColumnName(columnName: String) {
        textFieldForColumnName.text = columnName
    }

    fun getNlsColumnInputBean(): NlsColumnInputBean {
        return NlsColumnInputBean(
                columnName = textFieldForColumnName.text.orEmpty(),
                isRead = cbIsRead.isSelected
        )
    }
}