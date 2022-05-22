package com.lin.nls_export.utils

import com.lin.nls_export.entities.NlsColumnInputBean
import com.lin.nls_export.entities.SheetNameFilter

fun validate(excelPath: String?,
             filter: SheetNameFilter?,
             keyColumnSetting: NlsColumnInputBean,
             enColumnSetting: NlsColumnInputBean,
             scColumnSetting: NlsColumnInputBean,
             tcColumnSetting: NlsColumnInputBean,
             exportPath: String?): Boolean {

    if (excelPath.isNullOrBlank()) {
        throw IllegalArgumentException("请输入 excel 文件")
    }
    if (filter == null) {
        throw IllegalArgumentException("请选择 sheet name 控制规则")
    }
    if (keyColumnSetting.let { it.isRead && it.columnName.isBlank() }) {
        throw IllegalArgumentException("请输入Key所在列名")
    }
    if (enColumnSetting.let { it.isRead && it.columnName.isBlank() }) {
        throw IllegalArgumentException("请输入英文所在列名")
    }
    if (scColumnSetting.let { it.isRead && it.columnName.isBlank() }) {
        throw IllegalArgumentException("请输入简体中文所在列名")
    }
    if (tcColumnSetting.let { it.isRead && it.columnName.isBlank() }) {
        throw IllegalArgumentException("请输入繁体中文所在列名")
    }
    if(listOf(keyColumnSetting, enColumnSetting,scColumnSetting, tcColumnSetting).find { it.isRead } == null){
        throw IllegalArgumentException("请重新选择读取项，至少需要读取 \"Key/英文/简体中文/繁体中文\" 中的一项")
    }
    if (exportPath.isNullOrBlank()) {
        throw IllegalArgumentException("请选择输出路径")
    }
    return true
}