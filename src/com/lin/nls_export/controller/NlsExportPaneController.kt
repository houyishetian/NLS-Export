package com.lin.nls_export.controller

import com.lin.nls_export.entities.SettingBean
import com.lin.nls_export.entities.SheetNameFilter
import com.lin.nls_export.entrance.*
import com.lin.nls_export.ext.disableEdit
import com.lin.nls_export.ext.enableEdit
import com.lin.nls_export.utils.*
import javafx.application.Platform
import javafx.beans.value.ChangeListener
import javafx.fxml.FXML
import javafx.scene.control.*
import javafx.scene.image.ImageView
import javafx.scene.input.DragEvent
import javafx.scene.input.TransferMode
import javafx.scene.layout.HBox
import javafx.scene.layout.Pane
import javafx.scene.paint.Color
import javafx.stage.DirectoryChooser
import javafx.stage.FileChooser
import java.io.File

class NlsExportPaneController {

    // 路径选择的 view
    @FXML
    lateinit var tfExcelFile: TextField // 路径显示文本
    @FXML
    lateinit var btnSelectExcelFile: Button // 路径选择文本

    @FXML
    lateinit var ivSetting: ImageView

    // sheet 设置的各项 radioButton
    @FXML
    lateinit var rbSheetSetting0: RadioButton
    @FXML
    lateinit var rbSheetSetting1: RadioButton
    @FXML
    lateinit var rbSheetSetting2: RadioButton
    private lateinit var tgSheetSetting: ToggleGroup
    private lateinit var allSheetSettingRadioBtns: List<RadioButton>
    @FXML
    lateinit var taSheetName: TextArea
    private val taSheetNameChangedListener: ChangeListener<String> by lazy {
        createTextFieldListener(taSheetName, "[a-zA-Z0-9 _/]{0,30}")
    }

    // key/en/sc/tc 的相关设置
    @FXML
    lateinit var keyColumnNameSetting: HBox
    @FXML
    lateinit var enColumnNameSetting: HBox
    @FXML
    lateinit var scColumnNameSetting: HBox
    @FXML
    lateinit var tcColumnNameSetting: HBox
    @FXML
    lateinit var keyColumnNameSettingController: NlsKeySettingPaneController
    @FXML
    lateinit var enColumnNameSettingController: NlsKeySettingPaneController
    @FXML
    lateinit var scColumnNameSettingController: NlsKeySettingPaneController
    @FXML
    lateinit var tcColumnNameSettingController: NlsKeySettingPaneController

    // 设置过滤或处理项
    @FXML
    lateinit var cbRemoveIllegalKeyLine: CheckBox

    @FXML
    lateinit var cbTrimValue: CheckBox

    @FXML
    lateinit var cbAutoCoverExistingFiles: CheckBox

    // 输出设置
    @FXML
    lateinit var tfOutputDirectory: TextField
    @FXML
    lateinit var btnSelectOutputDirectory: Button

    // 处理状态
    @FXML
    lateinit var tfHandleStatus: Label

    // 开始合并按钮
    lateinit var btnStartHandle: Button

    // 整个 pane 对象，用来做 disable
    lateinit var pane: Pane

    /**
     * 接收任意类型的拖动
     */
    fun onPathSelectedDragOver(dragEvent: DragEvent) {
        dragEvent.acceptTransferModes(*TransferMode.ANY)
    }

    /**
     * 处理拖动结束后的逻辑
     */
    fun onPathSelectedDragDropped(dragEvent: DragEvent) {
        dragEvent.dragboard.let {
            if (it.hasFiles()) {
                val file = it.files[0]
                if (file.isFile && file.extension.let { it == "xls" || it == "xlsx" }) {
                    tfExcelFile.text = file.absolutePath

                    // 如果 output path 还未设置，此处自动设置
                    if (tfOutputDirectory.text.isNullOrBlank()) {
                        tfOutputDirectory.text = file.parentFile.absolutePath
                    }
                }
            }
        }
    }

    /**
     * 用户自主选择 excel 文件
     */
    fun onSelectExcelClicked() {
        val fileChooser = FileChooser()
        fileChooser.title = "请选择 Excel 文件"
        fileChooser.extensionFilters.addAll(
                FileChooser.ExtensionFilter("XLS", "*.xls"),
                FileChooser.ExtensionFilter("XLSX", "*.xlsx")
        )
        val excelFile = fileChooser.showOpenDialog(btnSelectExcelFile.scene.window)
        tfExcelFile.text = excelFile.absolutePath

        // 如果 output path 还未设置，此处自动设置
        if (tfOutputDirectory.text.isNullOrBlank()) {
            tfOutputDirectory.text = excelFile.parentFile.absolutePath
        }
    }

    /**
     * 用户自主选择输出路径
     */
    fun onSelectOutputPathClicked() {
        val directoryChooser = DirectoryChooser()
        directoryChooser.title = "请选择导出文件夹"
        directoryChooser.initialDirectory = tfExcelFile.text?.let { File(it) }?.parentFile
        val directory = directoryChooser.showDialog(btnSelectOutputDirectory.scene.window)
        tfOutputDirectory.text = directory.absolutePath
    }

    fun initVaribles(settingBean: SettingBean) {
        tgSheetSetting = ToggleGroup()
        bindToggleGroupAndItsChildren(tgSheetSetting, rbSheetSetting0, rbSheetSetting1, rbSheetSetting2)
        allSheetSettingRadioBtns = listOf(rbSheetSetting0, rbSheetSetting1, rbSheetSetting2)

        // 读取所有 sheet 时不需要输入
        rbSheetSetting0.selectedProperty().addListener { _, _, selected ->
            taSheetName.enableEdit(!selected)
        }
        taSheetName.textProperty().addListener(taSheetNameChangedListener)

        // 输出路径不允许编辑
        tfOutputDirectory.disableEdit()

        loadSettings(settingBean)

        initSettingMenu()
    }

    /**
     * 加载已有的配置信息
     */
    private fun loadSettings(settingBean: SettingBean) {
        allSheetSettingRadioBtns.getOrNull(settingBean.sheetSetting)?.isSelected = true
        taSheetName.enableEdit(tgSheetSetting.selectedToggle != rbSheetSetting0)
        taSheetName.text = settingBean.sheetControlInput
        settingBean.keyColumnSetting.run {
            keyColumnNameSettingController.let {
                it.setLabel("Key:")
                it.setInputHint("请输入Key所在列名")
                it.setInputColumnName(columnName)
                it.showIsReadCheckbox(true)
                it.isReadCheckBox(isRead)
            }
        }

        settingBean.enColumnSetting.run {
            enColumnNameSettingController.let {
                it.setLabel("英文:")
                it.setInputHint("请输入英文所在列名")
                it.setInputColumnName(columnName)
                it.showIsReadCheckbox(true)
                it.isReadCheckBox(isRead)
            }
        }
        settingBean.scColumnSetting.run {
            scColumnNameSettingController.let {
                it.setLabel("简体中文:")
                it.setInputHint("请输入简体中文所在列名")
                it.setInputColumnName(columnName)
                it.showIsReadCheckbox(true)
                it.isReadCheckBox(isRead)
            }
        }
        settingBean.tcColumnSetting.run {
            tcColumnNameSettingController.let {
                it.setLabel("繁体中文:")
                it.setInputHint("请输入繁体中文所在列名")
                it.setInputColumnName(columnName)
                it.showIsReadCheckbox(true)
                it.isReadCheckBox(isRead)
            }
        }
        cbRemoveIllegalKeyLine.isSelected = settingBean.removeIllegalKeyLine
        cbTrimValue.isSelected = settingBean.trimValue
        cbAutoCoverExistingFiles.isSelected = settingBean.autoCoverExistingFiles
    }

    /**
     * 将 toggle group 和其 child Radio button 绑定起来
     */
    private fun bindToggleGroupAndItsChildren(toggleGroup: ToggleGroup, vararg radioButtons: RadioButton) {
        radioButtons.forEach {
            it.toggleGroup = toggleGroup
        }
    }

    /**
     * 根据 regex 创建输入监听
     */
    private fun createTextFieldListener(textField: TextInputControl, regex: String): ChangeListener<String> {
        return ChangeListener { _, old, new ->
            // 允许清空
            if (new.isNotEmpty()) {
                // 如果不为空，就必须符合要求
                val matchRegex = new.matches(Regex(regex))
                if (!matchRegex) {
                    textField.text = old
                }
            }
        }
    }

    /**
     * 更改 btn text 以及 enable 属性
     */
    private fun updateBtnStatusFromOtherThread(text: String, disableBtn: Boolean = true) {
        Platform.runLater {
            btnStartHandle.text = text
            btnStartHandle.isDisable = disableBtn
        }
    }

    /**
     * 更改整个 pane 的 enable 属性
     */
    private fun updatePaneEvent(isDisabled: Boolean) {
        Platform.runLater {
            pane.isDisable = isDisabled
        }
    }

    private fun showSuccessStatus(result: String, fromOtherThread: Boolean = false) {
        val logic = fun() {
            tfHandleStatus.textFill = Color.GREEN // 显示绿色
            tfHandleStatus.text = result
            tfHandleStatus.tooltip = Tooltip(result)
        }
        if (fromOtherThread) {
            Platform.runLater(logic)
        } else {
            logic.invoke()
        }
    }

    private fun showFailedStatus(errorStatus: String, fromOtherThread: Boolean = false) {
        val logic = fun() {
            tfHandleStatus.textFill = Color.RED // 显示红色
            tfHandleStatus.text = errorStatus
            tfHandleStatus.tooltip = Tooltip(errorStatus)
        }
        if (fromOtherThread) {
            Platform.runLater(logic)
        } else {
            logic.invoke()
        }
    }

    private fun initSettingMenu() {
        val saveItem = MenuItem("保存")
        val resetItem = MenuItem("重置")

        val contextMenu = ContextMenu()
        contextMenu.items.addAll(saveItem, resetItem)

        saveItem.setOnAction {
            // 保存最新设置项
            SettingsUtil.saveSetting(getLatestSettingBean())
            showInformation("设置信息已保存，保存路径 ${SettingsUtil.getSettingFilePath()}")
        }

        resetItem.setOnAction {
            // 重置选项
            loadSettings(SettingsUtil.getDefaultSetting())
            // 删除旧文件
            SettingsUtil.deleteExistingSetting()
            showInformation("设置信息已重置，设置文件已删除!")
        }

        ivSetting.setOnMouseClicked {
            contextMenu.hide()

            // 显示位置在 ivSetting 的正中央
            val showX = ivSetting.getAbsoluteX() + ivSetting.fitWidth / 2
            val showY = ivSetting.getAbsoluteY() + ivSetting.fitHeight / 2

            contextMenu.show(ivSetting, showX, showY)
        }
    }

    /**
     * 操作完毕后，提示
     */
    private fun showInformation(message: String) {
        Platform.runLater {
            AlertUtil.newInstance(
                    alertType = Alert.AlertType.INFORMATION,
                    title = "操作成功",
                    contentText = message
            )
        }
    }

    /**
     * 获取并封装setting 信息
     */
    private fun getLatestSettingBean(): SettingBean {
        val sheetSetting = allSheetSettingRadioBtns.indexOf(tgSheetSetting.selectedToggle)
        val sheetControlInput = taSheetName.text.orEmpty()
        val keyColumnSetting = keyColumnNameSettingController.getNlsColumnInputBean()
        val enColumnSetting = enColumnNameSettingController.getNlsColumnInputBean()
        val scColumnSetting = scColumnNameSettingController.getNlsColumnInputBean()
        val tcColumnSetting = tcColumnNameSettingController.getNlsColumnInputBean()
        val removeIllegalKeyLine = cbRemoveIllegalKeyLine.isSelected
        val trimValue = cbTrimValue.isSelected
        val autoCoverExistingFiles = cbAutoCoverExistingFiles.isSelected

        return SettingBean(
                sheetSetting = sheetSetting,
                sheetControlInput = sheetControlInput,
                keyColumnSetting = keyColumnSetting,
                enColumnSetting = enColumnSetting,
                scColumnSetting = scColumnSetting,
                tcColumnSetting = tcColumnSetting,
                removeIllegalKeyLine = removeIllegalKeyLine,
                trimValue = trimValue,
                autoCoverExistingFiles = autoCoverExistingFiles
        )
    }

    /**
     * 开始处理按钮点击事件
     */
    fun onHandleBtnClicked() {
        val excelPath = tfExcelFile.text
        val filter = when (tgSheetSetting.selectedToggle) {
            rbSheetSetting0 -> {
                SheetNameFilter.ReadAllSheetFilter
            }
            rbSheetSetting1 -> {
                SheetNameFilter.RemoveBlackListFilter(SheetNameFilter.getControlSheetList(taSheetName.text))
            }
            rbSheetSetting2 -> {
                SheetNameFilter.ReadWhiteListFilter(SheetNameFilter.getControlSheetList(taSheetName.text))
            }
            else -> SheetNameFilter.ReadAllSheetFilter
        }
        val keyColumnSetting = keyColumnNameSettingController.getNlsColumnInputBean()
        val enColumnSetting = enColumnNameSettingController.getNlsColumnInputBean()
        val scColumnSetting = scColumnNameSettingController.getNlsColumnInputBean()
        val tcColumnSetting = tcColumnNameSettingController.getNlsColumnInputBean()

        val removeIllegalKeyColumns = cbRemoveIllegalKeyLine.isSelected
        val trimAllValues = cbTrimValue.isSelected
        val autoCoverExistingFiles = cbAutoCoverExistingFiles.isSelected

        val exportPath = tfOutputDirectory.text

        showSuccessStatus("")

        try {
            // 校验字段
            validate(excelPath,
                    filter,
                    keyColumnSetting,
                    enColumnSetting,
                    scColumnSetting,
                    tcColumnSetting,
                    autoCoverExistingFiles,
                    exportPath)

            val thread = Thread {

                val startTime = System.currentTimeMillis()

                updateBtnStatusFromOtherThread("正在合并...")
                showSuccessStatus("正在导出...", true)
                updatePaneEvent(true)  // 禁止所有屏幕事件

                val keyColumn = keyColumnSetting.takeIf { it.isRead }?.columnName
                val enColumn = enColumnSetting.takeIf { it.isRead }?.columnName
                val scColumn = scColumnSetting.takeIf { it.isRead }?.columnName
                val tcColumn = tcColumnSetting.takeIf { it.isRead }?.columnName

                // 开始处理
                readFromExcel(excelPath, filter, keyColumn, enColumn, scColumn, tcColumn)
                        .removeIllegalKeyRows(removeIllegalKeyColumns)
                        .trim(trimAllValues)
                        .interceptorHandling()
                        .exportNlsDoc(exportPath, keyColumnSetting.isRead, enColumnSetting.isRead, scColumnSetting.isRead, tcColumnSetting.isRead)

                val spendTime = System.currentTimeMillis() - startTime
                showSuccessStatus("导出成功，检查:\n$exportPath 下的 strings.xml\n耗时: ${spendTime.toSecondString()}秒", fromOtherThread = true)
                updateBtnStatusFromOtherThread("开始处理", false)
                updatePaneEvent(false) // 恢复所有屏幕事件
            }
            thread.start()
        } catch (e: Exception) {
            showFailedStatus(e.message.orEmpty())
        }
    }
}