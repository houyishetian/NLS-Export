package com.lin.nls_export.controller

import com.lin.nls_export.entities.SettingBean
import com.lin.nls_export.ext.disableEdit
import com.lin.nls_export.ext.enableEdit
import com.lin.nls_export.utils.AlertUtil
import com.lin.nls_export.utils.SettingsUtil
import com.lin.nls_export.utils.getAbsoluteX
import com.lin.nls_export.utils.getAbsoluteY
import javafx.application.Platform
import javafx.beans.value.ChangeListener
import javafx.fxml.FXML
import javafx.scene.control.*
import javafx.scene.image.ImageView
import javafx.scene.input.DragEvent
import javafx.scene.input.TransferMode
import javafx.scene.layout.HBox
import javafx.scene.layout.Pane
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

    // 输出设置
    @FXML
    lateinit var tfOutputDirectory: TextField
    @FXML
    lateinit var btnSelectOutputDirectory: Button
    @FXML
    lateinit var cbUsingPathAsOutputName: CheckBox

    // 处理状态
    @FXML
    lateinit var tfHandleStatus: Label

    // 开始合并按钮
    lateinit var btnStartHandle: Button

    // 整个 pane 对象，用来做 disable
    lateinit var pane: Pane

    fun onPathSelectedDragOver(dragEvent: DragEvent) {
        dragEvent.acceptTransferModes(*TransferMode.ANY)
    }

    fun onPathSelectedDragDropped(dragEvent: DragEvent) {
        dragEvent.dragboard.let {
            if (it.hasFiles()) {
                val file = it.files[0]
                if (file.isFile && file.extension.let { it == "xls" || it == "xlsx" }) {
                    tfExcelFile.text = file.absolutePath
                    setOutputName(file)
                }
            }
        }
    }

    fun onSelectExcelClicked() {
        val fileChooser = FileChooser()
        fileChooser.title = "请选择 Excel 文件"
        fileChooser.extensionFilters.addAll(
                FileChooser.ExtensionFilter("XLS", "*.xls"),
                FileChooser.ExtensionFilter("XLSX", "*.xlsx")
        )
        val directory = fileChooser.showOpenDialog(btnSelectExcelFile.scene.window)
        tfExcelFile.text = directory.absolutePath
        setOutputName(directory)
    }

    fun onSelectOutputPathClicked() {
        val directoryChooser = DirectoryChooser()
        directoryChooser.title = "请选择导出文件夹"
        val directory = directoryChooser.showDialog(btnSelectOutputDirectory.scene.window)
        tfOutputDirectory.text = directory.absolutePath
        setOutputName(directory)
    }

//    fun startMerge() {
//        // reset status
//        showSuccessMergeStatus("")
//        try {
//            val bean = ImageMergePropertiesBean.safeObject(
//                    directoryPath = getDirectoryOfImages(),
//                    imageFormats = getImageFormat(),
//                    imageMargin = getImageMargin(),
//                    eachLineNum = getEachLineNum(),
//                    imageQuality = getMergeQuality(),
//                    arrangeMode = getArrangeMode(),
//                    outputName = getOutputName())
//            mergeImage(bean)
//        } catch (e: Exception) {
//            e.printException()
//            showFailedMergeStatus(e.message ?: "")
//        }
//    }

    fun initVaribles(settingBean: SettingBean) {
        tgSheetSetting = ToggleGroup()
        bindToggleGroupAndItsChildren(tgSheetSetting, rbSheetSetting0, rbSheetSetting1, rbSheetSetting2)
        allSheetSettingRadioBtns = listOf(rbSheetSetting0, rbSheetSetting1, rbSheetSetting2)
        // 读取所有 sheet 时不需要输入
        rbSheetSetting0.selectedProperty().addListener { _, _, selected ->
            if (selected) {
                taSheetName.enableEdit()
            } else {
                taSheetName.disableEdit()
            }
        }
        taSheetName.textProperty().addListener(taSheetNameChangedListener)

        // 输出路径不允许编辑
        tfOutputDirectory.disableEdit()
        cbUsingPathAsOutputName.selectedProperty().addListener { _, _, selected ->
            if (selected) {
                tfOutputDirectory.text = tfExcelFile.text?.takeIf { it.isNotEmpty() }?.let { File(it).parent }
            }
        }

        loadSettings(settingBean)

        initSettingMenu()
    }

    private fun loadSettings(settingBean: SettingBean) {
        allSheetSettingRadioBtns.getOrNull(settingBean.sheetSetting)?.isSelected = true
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
        cbUsingPathAsOutputName.isSelected = settingBean.usingPathAsOutputName
    }

    /**
     * 将 toggle group 和其 child Radio button 绑定起来
     */
    private fun bindToggleGroupAndItsChildren(toggleGroup: ToggleGroup, vararg radioButtons: RadioButton) {
        radioButtons.forEach {
            it.toggleGroup = toggleGroup
        }
    }

    //
//    /**
//     * 设置 output name 和 checkbox 之间的绑定关系
//     */
//    private fun setOutputNameTextFieldListener(textChangeListener: ChangeListener<String>) {
//        cbUsingPathAsOutputName.selectedProperty().addListener { _, _, isSelected ->
//            setOutputNameProperties(isSelected, textChangeListener)
//        }
//    }
//
//    private fun setOutputNameProperties(isSelectedUsingPathAsOutputName: Boolean, textChangeListener: ChangeListener<String>) {
//        // 如果选中了使用path作为输出文件名，则编辑框不可编辑
//        if (isSelectedUsingPathAsOutputName) {
//            // path 直接填充时，正则无效，以实际为准
//            removeTextFieldListener(tfOutputName, textChangeListener)
//            tfOutputName.isEditable = false
//            tfOutputName.isMouseTransparent = true
//            // 如果已经 selected，就读取path并显示
//            tfOutputName.text = tfImageDirectory.text?.takeIf { it.isNotEmpty() }?.let { File(it).name }
//            tfOutputName.promptText = "选择图片所在路径"
//        } else {
//            // user 自己填写时，需要符合正则
//            bindTextFieldListener(tfOutputName, textChangeListener)
//            // 恢复编辑状态
//            tfOutputName.isEditable = true
//            tfOutputName.isMouseTransparent = false
//            tfOutputName.text = "" // 清空输入
//            tfOutputName.promptText = "20位文件名，汉字数字字母下划线组成"
//        }
//    }
//
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

    //
//    private fun bindTextFieldListener(textField: TextField, changeListener: ChangeListener<String>) {
//        textField.textProperty().addListener(changeListener)
//    }
//
//    private fun removeTextFieldListener(textField: TextField, changeListener: ChangeListener<String>) {
//        textField.textProperty().removeListener(changeListener)
//    }
//
//    /**
//     * 获取user输入的文件夹
//     */
//    private fun getDirectoryOfImages(): String? {
//        return tfImageDirectory.text?.takeIf { it.isNotEmpty() }
//    }
//
//    /**
//     * 获取 user 选择的图片格式
//     */
//    private fun getImageFormat(): List<String>? {
//        return imageFormatCbList.map {
//            imageFormatMap[it.takeIf { it.isSelected }?.run { it.text }]
//        }.filterNotNull().takeIf { it.isNotEmpty() }
//    }
//
//    /**
//     * 获取 user 选择的图片间距
//     */
//    private fun getImageMargin(): Int? {
//        val selectedOne = tgImageMargin.selectedToggle as? RadioButton
//        return when (selectedOne) {
//            rbImageMarginCustomize -> {
//                tryCatchAllExceptions({ tfImageMarginCustomize.text?.toInt() })
//            }
//            else -> {
//                imageMarginMap[selectedOne?.text]
//            }
//        }
//    }
//
//    /**
//     * 获取 user 选择的每行数量
//     */
//    private fun getEachLineNum(): Int? {
//        val selectedOne = tgEachLine.selectedToggle as? RadioButton
//        return when (selectedOne) {
//            rbEachLineCustomize -> {
//                tryCatchAllExceptions({ tfEachLineCustomize.text?.toInt() })
//            }
//            else -> {
//                eachLineNumMap[selectedOne?.text]
//            }
//        }
//    }
//
//    /**
//     * 获取 user 选择的图片质量
//     */
//    private fun getMergeQuality(): Float? {
//        return imageQualityMap[(tgImageQuality.selectedToggle as? RadioButton)?.text]
//    }
//
//    private fun getArrangeMode(): MergeImageUtil.ArrangeMode? {
//        return arrangeModeMap[(tgArrangeMode.selectedToggle as? RadioButton)?.text]
//    }
//
//    /**
//     * 获取 user 输入的 合并图片名
//     */
//    private fun getOutputName(): String? {
//        return tfOutputName.text?.takeIf { it.isNotEmpty() }
//    }
//
//    /**
//     * 显示合并成功 status
//     */
//    private fun showSuccessMergeStatus(result: String, fromOtherThread: Boolean = false) {
//        val logic = fun() {
//            tfMergeStatus.textFill = Color.GREEN // 显示绿色
//            tfMergeStatus.text = result
//            tfMergeStatus.tooltip = Tooltip(result)
//        }
//        if (fromOtherThread) {
//            Platform.runLater(logic)
//        } else {
//            logic.invoke()
//        }
//    }
//
//    private fun updateBtnStatusFromOtherThread(text: String, disableBtn: Boolean = true) {
//        Platform.runLater {
//            btnStartMerge.text = text
//            btnStartMerge.isDisable = disableBtn
//        }
//    }
//
//    private fun updatePaneEvent(isDisabled: Boolean) {
//        Platform.runLater {
//            pane.isDisable = isDisabled
//        }
//    }
//
//    /**
//     * 显示合并失败 status
//     */
//    private fun showFailedMergeStatus(errorStatus: String, fromOtherThread: Boolean = false) {
//        val logic = fun() {
//            tfMergeStatus.textFill = Color.RED // 显示红色
//            tfMergeStatus.text = errorStatus
//            tfMergeStatus.tooltip = Tooltip(errorStatus)
//        }
//        if (fromOtherThread) {
//            Platform.runLater(logic)
//        } else {
//            logic.invoke()
//        }
//    }
//
//    private fun setDefaultSelectedItems(settingBean: SettingBean) {
//        settingBean.imageFormatIndexes.mapNotNull { imageFormatCbList.getOrNull(it) }.forEach { it.isSelected = true }
//
//        imageMarginRbList.getOrNull(settingBean.imageMarginIndex)?.isSelected = true
//        if (settingBean.imageMarginIndex == imageMarginRbList.size - 1) {
//            tfImageMarginCustomize.text = settingBean.imageMarginValue?.toString() ?: ""
//        }
//
//        eachLineRbList.getOrNull(settingBean.eachLineNumIndex)?.isSelected = true
//        if (settingBean.eachLineNumIndex == eachLineRbList.size - 1) {
//            tfEachLineCustomize.text = settingBean.eachLineNumValue?.toString() ?: ""
//        }
//
//        imageQualityRbList.getOrNull(settingBean.mergeQualityIndex)?.isSelected = true
//
//        arrangeModeRbList.getOrNull(settingBean.arrangeModeIndex)?.isSelected = true
//
//        cbUsingPathAsOutputName.isSelected = settingBean.usingPathAsOutputName
//        setOutputNameProperties(settingBean.usingPathAsOutputName, outputNameChangedListener)
//    }
//
    private fun setOutputName(directoryFile: File) {
        val name = directoryFile.parentFile.absolutePath
        if (cbUsingPathAsOutputName.isSelected) {
            tfOutputDirectory.text = name
        }
    }

    //
//    private fun mergeImage(bean: ImageMergePropertiesBean) {
//        val thread = Thread {
//            try {
//                updateBtnStatusFromOtherThread("正在合并...")
//                showSuccessMergeStatus("正在合并...", true)
//                updatePaneEvent(true)  // 禁止所有屏幕事件
//                bean.run {
//                    val startTime = System.currentTimeMillis()
//
//                    // 文件已存在
//                    val desFile = File(File(directoryPath), "$outputName.png")
//                    if (desFile.exists()) {
//                        throw OutputFileAlreadyExistException(outputName)
//                    }
//
//                    FileUtil.getAllPics(directoryPath, imageFormats).let {
//                        val readImagesList = ImageCompressUtil.compress(it, imageQuality)
//                        readImagesList?.filterNotNull()?.takeIf { it.isNotEmpty() }?.let {
//                            // 将所有压缩后的额图片合并，间距 30px，每行最多5个
//                            MergeImageUtil(imageFiles = it,
//                                    columnCount = eachLineNum,
//                                    marginPxBetweenImage = imageMargin,
//                                    arrangeMode = arrangeMode).mergeImage()?.let {
//                                // 写入
//                                FileUtil.writeImageToFile(it, desFile.absolutePath)
//                                val cost = System.currentTimeMillis() - startTime
//                                val mill = cost % 1000
//                                val second = cost / 1000
//                                showSuccessMergeStatus("合并成功，检查:\n${desFile.absolutePath}\n耗时: $second.${mill}秒", fromOtherThread = true)
//                                updateBtnStatusFromOtherThread("开始合并", false)
//                                updatePaneEvent(false) // 恢复所有屏幕事件
//                            }
//                        } ?: let {
//                            throw DirectoryIsEmptyException(directoryPath, imageFormats.toString())
//                        }
//                    }
//                }
//            } catch (e: OutputFileAlreadyExistException) {
//                e.printException()
//                showOutputFileAlreadyExist(e.message ?: "")
//            } catch (e: Exception) {
//                e.printException()
//                showFailedMergeStatus(e.message ?: "", true)
//                updateBtnStatusFromOtherThread("开始合并", false)
//                updatePaneEvent(false) // 恢复所有屏幕事件
//            }
//        }
//        thread.start()
//    }
//
//    private fun showOutputFileAlreadyExist(message: String) {
//        Platform.runLater {
//            AlertUtil.newInstance(
//                    alertType = Alert.AlertType.WARNING,
//                    title = "文件已存在",
//                    contentText = message,
//                    onCloseRequest = EventHandler {
//                        showFailedMergeStatus("合并取消!")
//                        updateBtnStatusFromOtherThread("开始合并", false)
//                        updatePaneEvent(false) // 恢复所有屏幕事件
//                    }
//            )
//        }
//    }
//
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
        val usingPathAsOutputName = cbUsingPathAsOutputName.isSelected

        return SettingBean(
                sheetSetting = sheetSetting,
                sheetControlInput = sheetControlInput,
                keyColumnSetting = keyColumnSetting,
                enColumnSetting = enColumnSetting,
                scColumnSetting = scColumnSetting,
                tcColumnSetting = tcColumnSetting,
                removeIllegalKeyLine = removeIllegalKeyLine,
                trimValue = trimValue,
                usingPathAsOutputName = usingPathAsOutputName
        )
    }
}