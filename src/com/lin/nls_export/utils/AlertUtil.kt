package com.lin.nls_export.utils

import javafx.event.EventHandler
import javafx.scene.control.Alert
import javafx.scene.control.ButtonType
import javafx.scene.control.DialogEvent
import javafx.scene.image.Image
import javafx.stage.Stage
import java.util.*

object AlertUtil {

    /**
     * 显示一个通用样式的 Alert
     */
    fun newInstance(alertType: Alert.AlertType,
                    title: String,
                    contentText: String,
                    headerText: String? = null,
                    iconUrl: String = "image/export.png",
                    onCloseRequest: (EventHandler<DialogEvent>)? = null
    ): Optional<ButtonType> {
        val alert = Alert(alertType)
        (alert.dialogPane.scene.window as? Stage)?.icons?.add(Image(iconUrl))
        alert.title = title
        alert.headerText = headerText
        alert.contentText = contentText

        onCloseRequest?.let {
            alert.onCloseRequestProperty().set(it)
        }
        return alert.showAndWait()
    }
}