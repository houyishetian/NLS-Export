package com.lin.nls_export.entrance

import com.lin.nls_export.constant.EN_FILE_NAME
import com.lin.nls_export.constant.NLS_ITEM
import com.lin.nls_export.constant.SC_FILE_NAME
import com.lin.nls_export.constant.TC_FILE_NAME
import com.lin.nls_export.entities.NlsItemBean
import com.lin.nls_export.entities.SheetNameFilter
import com.lin.nls_export.utils.CellInterceptorUtil
import com.lin.nls_export.utils.ReadExcelUtil
import java.io.File

fun List<NlsItemBean>.exportNlsDoc(exportPath: String,
                                   exportKey: Boolean,
                                   exportEn: Boolean,
                                   exportSc: Boolean,
                                   exportTc: Boolean) {

    // 至少要导出一个key
    if (listOf(exportKey, exportEn, exportSc, exportTc).contains(true)) {
        val keyString = StringBuffer()
        val enString = StringBuffer()
        val scString = StringBuffer()
        val tcString = StringBuffer()

        this.forEach {
            val usedKey = if (exportKey) it.key else ""
            if (exportKey) {
                // 只导出 key，value 为 ""
                keyString.append(String.format(NLS_ITEM, usedKey, "")).append("\n")
            }
            if (exportEn) {
                // 导出简体英文
                keyString.append(String.format(NLS_ITEM, usedKey, it.en)).append("\n")
            }
            if (exportSc) {
                // 导出中文
                keyString.append(String.format(NLS_ITEM, usedKey, it.sc)).append("\n")
            }
            if (exportTc) {
                // 导出繁体中文
                keyString.append(String.format(NLS_ITEM, usedKey, it.tc)).append("\n")
            }
        }

        if (exportKey && listOf(exportEn, exportSc, exportTc).contains(true).not()) {
            // 如果只导出key，就需要生成一个 strings 文件即可
            val enFile = File(exportPath, EN_FILE_NAME)
            enFile.writeText(keyString.toString())
        } else {
            // 不然导出其他任何语言都有对应的文件
            if (exportEn) {
                // 如果没有读取到任何value，则会有一个空文件生成
                val enFile = File(exportPath, EN_FILE_NAME)
                enFile.writeText(enString.toString())
            }
            if (exportSc) {
                // 如果没有读取到任何value，则会有一个空文件生成
                val scFile = File(exportPath, SC_FILE_NAME)
                scFile.writeText(scString.toString())
            }
            if (exportTc) {
                // 如果没有读取到任何value，则会有一个空文件生成
                val tcFile = File(exportPath, TC_FILE_NAME)
                tcFile.writeText(tcString.toString())
            }
        }
    }
}

fun readFromExcel(excelPath: String,
                  sheetNameFilter: SheetNameFilter,
                  keyColumnName: String?,
                  enColumnName: String?,
                  scColumnName: String?,
                  tcColumnName: String?): List<NlsItemBean> {
    val util = ReadExcelUtil(excelPath, sheetNameFilter, keyColumnName, enColumnName, scColumnName, tcColumnName)
    return util.read()
}

fun List<NlsItemBean>.removeIllegalKeyRows(removeIllegalKeyRows: Boolean): List<NlsItemBean> {
    return if (removeIllegalKeyRows) {
        this.filter { it.key.isBlank().not() }
    } else {
        this
    }
}

fun List<NlsItemBean>.trim(trim: Boolean): List<NlsItemBean> {
    return if (trim) {
        this.map { NlsItemBean(it.key.trim(), it.en.trim(), it.sc.trim(), it.tc.trim()) }
    } else {
        this
    }
}

fun List<NlsItemBean>.interceptorHandling(): List<NlsItemBean> {
    val keyInterceptor = CellInterceptorUtil.createKeyInterceptor()
    val enInterceptor = CellInterceptorUtil.createEnInterceptor()
    val scInterceptor = CellInterceptorUtil.createScInterceptor()
    val tcInterceptor = CellInterceptorUtil.createTcInterceptor()
    return this.map {
        val newKey = keyInterceptor.handle(it.key)
        val newEn = enInterceptor.handle(it.en)
        val newSc = scInterceptor.handle(it.sc)
        val newTc = tcInterceptor.handle(it.tc)
        NlsItemBean(newKey, newEn, newSc, newTc)
    }
}

fun main(args: Array<String>) {
    val path = "C:\\Users\\lisonglin\\Desktop\\新建文件夹 (2)\\新建 Microsoft Excel 工作表.xlsx"
    val filter = SheetNameFilter.ReadWhiteListFilter(listOf("Sheet1"))
    val keyColumn = "Key"
    val enColumn = "English"
    val scColumn = "Simple Chinese"
    val tcColumn = "Tradional Chinese"
    val removeIllegalKeyColumns = true
    val trimAllValues = true

    val exportPath = "C:\\Users\\lisonglin\\Desktop\\新建文件夹 (2)"
    val exportKey = true
    val exportEn = false
    val exportSc = false
    val exportTc = false

    readFromExcel(path, filter, keyColumn, enColumn, scColumn, tcColumn)
            .removeIllegalKeyRows(removeIllegalKeyColumns)
            .trim(trimAllValues)
            .interceptorHandling()
            .exportNlsDoc(exportPath, exportKey, exportEn, exportSc, exportTc)
}