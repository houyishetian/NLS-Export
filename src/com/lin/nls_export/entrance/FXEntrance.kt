package com.lin.nls_export.entrance

import com.lin.nls_export.controller.NlsExportPaneController
import com.lin.nls_export.utils.AlertUtil
import com.lin.nls_export.utils.SettingsUtil
import javafx.application.Application
import javafx.application.Platform
import javafx.fxml.FXMLLoader
import javafx.scene.Scene
import javafx.scene.control.Alert
import javafx.scene.control.ButtonType
import javafx.scene.image.Image
import javafx.scene.layout.Pane
import javafx.stage.Stage

class FXEntrance : Application() {

    private val screenWidth = 900.0
    private val screenHeight = 600.0

    override fun start(primaryStage: Stage?) {
        primaryStage?.run {

            icons.add(Image("image/export.png"))
            title = "Nls Export"


            val loader = FXMLLoader()
            loader.location = this@FXEntrance.javaClass.classLoader.getResource("com/lin/nls_export/fx/pane_nls_export.fxml")
            val pane = loader.load<Pane>()
            val controller = loader.getController<NlsExportPaneController>()

            controller.pane = pane
            // 设置默认值，如果有以前保存的，取出；如果没有，使用默认值
            controller.initVariables(SettingsUtil.getSetting() ?: SettingsUtil.getDefaultSetting())

            val scene = Scene(pane, screenWidth, screenHeight)

            setScene(scene)

            width = screenWidth
            height = screenHeight

            setCloseEvent(this)

            isResizable = false

            show()
        }
    }

    private fun setCloseEvent(primaryStage: Stage) {
        Platform.setImplicitExit(false)
        primaryStage.setOnCloseRequest {
            // 消费掉close event
            it.consume()

            val result = AlertUtil.newInstance(
                    alertType = Alert.AlertType.CONFIRMATION,
                    title = "退出程序",
                    contentText = "是否要退出程序？"
            )
            if (result.get() == ButtonType.OK) {
                Platform.exit()
            }
        }
    }
}

fun main(args: Array<String>) {
    Application.launch(FXEntrance::class.java)
}