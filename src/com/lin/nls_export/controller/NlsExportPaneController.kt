package com.lin.nls_export.controller

import com.lin.entity.*
import com.lin.exceptions.DirectoryIsEmptyException
import com.lin.exceptions.OutputFileAlreadyExistException
import com.lin.ext.printException
import com.lin.ext.tryCatchAllExceptions
import com.lin.utils.*
import javafx.application.Platform
import javafx.beans.value.ChangeListener
import javafx.event.EventHandler
import javafx.fxml.FXML
import javafx.scene.control.*
import javafx.scene.image.ImageView
import javafx.scene.input.DragEvent
import javafx.scene.input.TransferMode
import javafx.scene.layout.Pane
import javafx.scene.paint.Color
import javafx.stage.DirectoryChooser
import java.io.File

class NlsExportPaneController {

    // 路径选择的 view
    @FXML
    lateinit var tfImageDirectory: TextField // 路径显示文本
    @FXML
    lateinit var btnSelectImageDirectory: Button // 路径选择文本

    @FXML
    lateinit var ivSetting: ImageView

    //图片格式的 checkbox
    @FXML
    lateinit var cbImageFormatPng: CheckBox
    @FXML
    lateinit var cbImageFormatJpg: CheckBox
    @FXML
    lateinit var cbImageFormatJpeg: CheckBox
    private val imageFormatCbList: List<CheckBox> by lazy {
        listOf(cbImageFormatPng, cbImageFormatJpg, cbImageFormatJpeg)
    }

    // 图片间距的 views
    @FXML
    lateinit var rbImageMargin10: RadioButton
    @FXML
    lateinit var rbImageMargin30: RadioButton
    @FXML
    lateinit var rbImageMargin50: RadioButton
    @FXML
    lateinit var rbImageMargin70: RadioButton
    @FXML
    lateinit var rbImageMarginCustomize: RadioButton
    @FXML
    lateinit var tfImageMarginCustomize: TextField
    private lateinit var tgImageMargin: ToggleGroup
    private val imageMarginRbList by lazy {
        listOf(rbImageMargin10, rbImageMargin30, rbImageMargin50, rbImageMargin70, rbImageMarginCustomize)
    }
    // 图片间距自定义输入时，只允许输入2位数字
    private val imageMarginCustomizeChangedListener: ChangeListener<String> by lazy {
        createTextFieldListener(tfImageMarginCustomize, "\\d{1,2}")
    }

    // 每行显示的 views
    @FXML
    lateinit var rbEachLine1: RadioButton
    @FXML
    lateinit var rbEachLine3: RadioButton
    @FXML
    lateinit var rbEachLine5: RadioButton
    @FXML
    lateinit var rbEachLineCustomize: RadioButton
    @FXML
    lateinit var tfEachLineCustomize: TextField
    private lateinit var tgEachLine: ToggleGroup
    private val eachLineRbList by lazy {
        listOf(rbEachLine1, rbEachLine3, rbEachLine5, rbEachLineCustomize)
    }
    // 每行显示自定义输入时，只允许输入2位数字
    private val eachLineCustomizeChangedListener: ChangeListener<String> by lazy {
        createTextFieldListener(tfEachLineCustomize, "\\d{1,2}")
    }

    // 合并质量的 views
    @FXML
    lateinit var rbImageQualityHigh: RadioButton
    @FXML
    lateinit var rbImageQualityMiddle: RadioButton
    @FXML
    lateinit var rbImageQualityNormal: RadioButton
    @FXML
    lateinit var rbImageQualityLow: RadioButton
    private lateinit var tgImageQuality: ToggleGroup
    private val imageQualityRbList by lazy {
        listOf(rbImageQualityHigh, rbImageQualityMiddle, rbImageQualityNormal, rbImageQualityLow)
    }

    // 排序方式
    @FXML
    lateinit var rbArrangeModeForm: RadioButton
    @FXML
    lateinit var rbArrangeModeSize: RadioButton
    private lateinit var tgArrangeMode: ToggleGroup
    private val arrangeModeRbList by lazy {
        listOf(rbArrangeModeForm, rbArrangeModeSize)
    }

    // 输出文件名
    @FXML
    lateinit var tfOutputName: TextField
    @FXML
    lateinit var cbUsingPathAsOutputName: CheckBox
    // 合并名称自定义输入时，只允许输入数字字母汉字以及下划线组成的20位字符串
    private val outputNameChangedListener: ChangeListener<String> by lazy {
        createTextFieldListener(tfOutputName, "[\\d\\w_\u4e00-\u9fa5]{1,20}")
    }

    // 合并状态
    @FXML
    lateinit var tfMergeStatus: Label

    // 开始合并按钮
    lateinit var btnStartMerge: Button

    // 整个 pane 对象，用来做 disable
    lateinit var pane: Pane

    fun onPathSelectedDragOver(dragEvent: DragEvent) {
        dragEvent.acceptTransferModes(*TransferMode.ANY)
    }

    fun onPathSelectedDragDropped(dragEvent: DragEvent) {
        dragEvent.dragboard.let {
            if (it.hasFiles()) {
                val file = it.files[0]
                if (file.isDirectory) {
                    tfImageDirectory.text = file.absolutePath
                    setOutputName(file)
                }
            }
        }
    }

    fun onSelectPathClicked() {
        val directoryChooser = DirectoryChooser()
        directoryChooser.title = "请选择图片所在文件夹"
        val directory = directoryChooser.showDialog(btnSelectImageDirectory.scene.window)
        tfImageDirectory.text = directory.absolutePath
        setOutputName(directory)
    }

    fun startMerge() {
        // reset status
        showSuccessMergeStatus("")
        try {
            val bean = ImageMergePropertiesBean.safeObject(
                    directoryPath = getDirectoryOfImages(),
                    imageFormats = getImageFormat(),
                    imageMargin = getImageMargin(),
                    eachLineNum = getEachLineNum(),
                    imageQuality = getMergeQuality(),
                    arrangeMode = getArrangeMode(),
                    outputName = getOutputName())
            mergeImage(bean)
        } catch (e: Exception) {
            e.printException()
            showFailedMergeStatus(e.message ?: "")
        }
    }

    fun initVaribles(defaultSettingBean: SettingBean) {
        tgImageMargin = ToggleGroup()
        bindToggleGroupAndItsChildren(tgImageMargin, rbImageMargin10, rbImageMargin30, rbImageMargin50, rbImageMargin70, rbImageMarginCustomize)
        bindCustomizedRadioBtnAndTextField(rbImageMarginCustomize, tfImageMarginCustomize)
        bindTextFieldListener(tfImageMarginCustomize, imageMarginCustomizeChangedListener)

        tgEachLine = ToggleGroup()
        bindToggleGroupAndItsChildren(tgEachLine, rbEachLine1, rbEachLine3, rbEachLine5, rbEachLineCustomize)
        bindCustomizedRadioBtnAndTextField(rbEachLineCustomize, tfEachLineCustomize)
        bindTextFieldListener(tfEachLineCustomize, eachLineCustomizeChangedListener)

        tgImageQuality = ToggleGroup()
        bindToggleGroupAndItsChildren(tgImageQuality, rbImageQualityHigh, rbImageQualityMiddle, rbImageQualityNormal, rbImageQualityLow)

        tgArrangeMode = ToggleGroup()
        bindToggleGroupAndItsChildren(tgArrangeMode, rbArrangeModeForm, rbArrangeModeSize)

        setOutputNameTextFieldListener(outputNameChangedListener)

        // 设置默认选中 item
        setDefaultSelectedItems(defaultSettingBean)

        initSettingMenu()
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
     * 将 customize radio button 和 后边的 customize text field 绑定起来
     */
    private fun bindCustomizedRadioBtnAndTextField(radioBtn: RadioButton, textField: TextField) {
        radioBtn.selectedProperty().addListener { _, _, selected ->
            if (selected) {
                textField.isEditable = true
                textField.isMouseTransparent = false
            } else {
                textField.isEditable = false
                textField.isMouseTransparent = true
                textField.text = ""
            }
        }
    }

    /**
     * 设置 output name 和 checkbox 之间的绑定关系
     */
    private fun setOutputNameTextFieldListener(textChangeListener: ChangeListener<String>) {
        cbUsingPathAsOutputName.selectedProperty().addListener { _, _, isSelected ->
            setOutputNameProperties(isSelected, textChangeListener)
        }
    }

    private fun setOutputNameProperties(isSelectedUsingPathAsOutputName: Boolean, textChangeListener: ChangeListener<String>) {
        // 如果选中了使用path作为输出文件名，则编辑框不可编辑
        if (isSelectedUsingPathAsOutputName) {
            // path 直接填充时，正则无效，以实际为准
            removeTextFieldListener(tfOutputName, textChangeListener)
            tfOutputName.isEditable = false
            tfOutputName.isMouseTransparent = true
            // 如果已经 selected，就读取path并显示
            tfOutputName.text = tfImageDirectory.text?.takeIf { it.isNotEmpty() }?.let { File(it).name }
            tfOutputName.promptText = "选择图片所在路径"
        } else {
            // user 自己填写时，需要符合正则
            bindTextFieldListener(tfOutputName, textChangeListener)
            // 恢复编辑状态
            tfOutputName.isEditable = true
            tfOutputName.isMouseTransparent = false
            tfOutputName.text = "" // 清空输入
            tfOutputName.promptText = "20位文件名，汉字数字字母下划线组成"
        }
    }

    private fun createTextFieldListener(textField: TextField, regex: String): ChangeListener<String> {
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

    private fun bindTextFieldListener(textField: TextField, changeListener: ChangeListener<String>) {
        textField.textProperty().addListener(changeListener)
    }

    private fun removeTextFieldListener(textField: TextField, changeListener: ChangeListener<String>) {
        textField.textProperty().removeListener(changeListener)
    }

    /**
     * 获取user输入的文件夹
     */
    private fun getDirectoryOfImages(): String? {
        return tfImageDirectory.text?.takeIf { it.isNotEmpty() }
    }

    /**
     * 获取 user 选择的图片格式
     */
    private fun getImageFormat(): List<String>? {
        return imageFormatCbList.map {
            imageFormatMap[it.takeIf { it.isSelected }?.run { it.text }]
        }.filterNotNull().takeIf { it.isNotEmpty() }
    }

    /**
     * 获取 user 选择的图片间距
     */
    private fun getImageMargin(): Int? {
        val selectedOne = tgImageMargin.selectedToggle as? RadioButton
        return when (selectedOne) {
            rbImageMarginCustomize -> {
                tryCatchAllExceptions({ tfImageMarginCustomize.text?.toInt() })
            }
            else -> {
                imageMarginMap[selectedOne?.text]
            }
        }
    }

    /**
     * 获取 user 选择的每行数量
     */
    private fun getEachLineNum(): Int? {
        val selectedOne = tgEachLine.selectedToggle as? RadioButton
        return when (selectedOne) {
            rbEachLineCustomize -> {
                tryCatchAllExceptions({ tfEachLineCustomize.text?.toInt() })
            }
            else -> {
                eachLineNumMap[selectedOne?.text]
            }
        }
    }

    /**
     * 获取 user 选择的图片质量
     */
    private fun getMergeQuality(): Float? {
        return imageQualityMap[(tgImageQuality.selectedToggle as? RadioButton)?.text]
    }

    private fun getArrangeMode(): MergeImageUtil.ArrangeMode? {
        return arrangeModeMap[(tgArrangeMode.selectedToggle as? RadioButton)?.text]
    }

    /**
     * 获取 user 输入的 合并图片名
     */
    private fun getOutputName(): String? {
        return tfOutputName.text?.takeIf { it.isNotEmpty() }
    }

    /**
     * 显示合并成功 status
     */
    private fun showSuccessMergeStatus(result: String, fromOtherThread: Boolean = false) {
        val logic = fun() {
            tfMergeStatus.textFill = Color.GREEN // 显示绿色
            tfMergeStatus.text = result
            tfMergeStatus.tooltip = Tooltip(result)
        }
        if (fromOtherThread) {
            Platform.runLater(logic)
        } else {
            logic.invoke()
        }
    }

    private fun updateBtnStatusFromOtherThread(text: String, disableBtn: Boolean = true) {
        Platform.runLater {
            btnStartMerge.text = text
            btnStartMerge.isDisable = disableBtn
        }
    }

    private fun updatePaneEvent(isDisabled: Boolean) {
        Platform.runLater {
            pane.isDisable = isDisabled
        }
    }

    /**
     * 显示合并失败 status
     */
    private fun showFailedMergeStatus(errorStatus: String, fromOtherThread: Boolean = false) {
        val logic = fun() {
            tfMergeStatus.textFill = Color.RED // 显示红色
            tfMergeStatus.text = errorStatus
            tfMergeStatus.tooltip = Tooltip(errorStatus)
        }
        if (fromOtherThread) {
            Platform.runLater(logic)
        } else {
            logic.invoke()
        }
    }

    private fun setDefaultSelectedItems(settingBean: SettingBean) {
        settingBean.imageFormatIndexes.mapNotNull { imageFormatCbList.getOrNull(it) }.forEach { it.isSelected = true }

        imageMarginRbList.getOrNull(settingBean.imageMarginIndex)?.isSelected = true
        if (settingBean.imageMarginIndex == imageMarginRbList.size - 1) {
            tfImageMarginCustomize.text = settingBean.imageMarginValue?.toString() ?: ""
        }

        eachLineRbList.getOrNull(settingBean.eachLineNumIndex)?.isSelected = true
        if (settingBean.eachLineNumIndex == eachLineRbList.size - 1) {
            tfEachLineCustomize.text = settingBean.eachLineNumValue?.toString() ?: ""
        }

        imageQualityRbList.getOrNull(settingBean.mergeQualityIndex)?.isSelected = true

        arrangeModeRbList.getOrNull(settingBean.arrangeModeIndex)?.isSelected = true

        cbUsingPathAsOutputName.isSelected = settingBean.usingPathAsOutputName
        setOutputNameProperties(settingBean.usingPathAsOutputName, outputNameChangedListener)
    }

    private fun setOutputName(directoryFile: File) {
        val name = directoryFile.name
        if (cbUsingPathAsOutputName.isSelected) {
            tfOutputName.text = name
        }
    }

    private fun mergeImage(bean: ImageMergePropertiesBean) {
        val thread = Thread {
            try {
                updateBtnStatusFromOtherThread("正在合并...")
                showSuccessMergeStatus("正在合并...", true)
                updatePaneEvent(true)  // 禁止所有屏幕事件
                bean.run {
                    val startTime = System.currentTimeMillis()

                    // 文件已存在
                    val desFile = File(File(directoryPath), "$outputName.png")
                    if (desFile.exists()) {
                        throw OutputFileAlreadyExistException(outputName)
                    }

                    FileUtil.getAllPics(directoryPath, imageFormats).let {
                        val readImagesList = ImageCompressUtil.compress(it, imageQuality)
                        readImagesList?.filterNotNull()?.takeIf { it.isNotEmpty() }?.let {
                            // 将所有压缩后的额图片合并，间距 30px，每行最多5个
                            MergeImageUtil(imageFiles = it,
                                    columnCount = eachLineNum,
                                    marginPxBetweenImage = imageMargin,
                                    arrangeMode = arrangeMode).mergeImage()?.let {
                                // 写入
                                FileUtil.writeImageToFile(it, desFile.absolutePath)
                                val cost = System.currentTimeMillis() - startTime
                                val mill = cost % 1000
                                val second = cost / 1000
                                showSuccessMergeStatus("合并成功，检查:\n${desFile.absolutePath}\n耗时: $second.${mill}秒", fromOtherThread = true)
                                updateBtnStatusFromOtherThread("开始合并", false)
                                updatePaneEvent(false) // 恢复所有屏幕事件
                            }
                        } ?: let {
                            throw DirectoryIsEmptyException(directoryPath, imageFormats.toString())
                        }
                    }
                }
            } catch (e: OutputFileAlreadyExistException) {
                e.printException()
                showOutputFileAlreadyExist(e.message ?: "")
            } catch (e: Exception) {
                e.printException()
                showFailedMergeStatus(e.message ?: "", true)
                updateBtnStatusFromOtherThread("开始合并", false)
                updatePaneEvent(false) // 恢复所有屏幕事件
            }
        }
        thread.start()
    }

    private fun showOutputFileAlreadyExist(message: String) {
        Platform.runLater {
            AlertUtil.newInstance(
                    alertType = Alert.AlertType.WARNING,
                    title = "文件已存在",
                    contentText = message,
                    onCloseRequest = EventHandler {
                        showFailedMergeStatus("合并取消!")
                        updateBtnStatusFromOtherThread("开始合并", false)
                        updatePaneEvent(false) // 恢复所有屏幕事件
                    }
            )
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
            setDefaultSelectedItems(SettingsUtil.getDefaultSetting())
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
        // image format 所有被选中的 items
        val imageFormatIndexes = imageFormatCbList.mapIndexedNotNull { index, item ->
            index.takeIf { item.isSelected }
        }

        val imageMarginIndex: Int = imageMarginRbList.indexOf(tgImageMargin.selectedToggle)
        var imageMarginValue: Int? = null
        if (imageMarginIndex == imageMarginRbList.size - 1) {
            imageMarginValue = tryCatchAllExceptions({ tfImageMarginCustomize.text?.toInt() })
        }

        val eachLineNumIndex: Int = eachLineRbList.indexOf(tgEachLine.selectedToggle)
        var eachLineNumValue: Int? = null
        if (eachLineNumIndex == eachLineRbList.size - 1) {
            eachLineNumValue = tryCatchAllExceptions({ tfEachLineCustomize.text?.toInt() })
        }

        val mergeQualityIndex: Int = imageQualityRbList.indexOf(tgImageQuality.selectedToggle)

        val arrangeModeIndex: Int = arrangeModeRbList.indexOf(tgArrangeMode.selectedToggle)

        val usingPathAsOutputName = cbUsingPathAsOutputName.isSelected

        return SettingBean(
                imageFormatIndexes = imageFormatIndexes,
                imageMarginIndex = imageMarginIndex,
                imageMarginValue = imageMarginValue,
                eachLineNumIndex = eachLineNumIndex,
                eachLineNumValue = eachLineNumValue,
                mergeQualityIndex = mergeQualityIndex,
                arrangeModeIndex = arrangeModeIndex,
                usingPathAsOutputName = usingPathAsOutputName
        )
    }
}