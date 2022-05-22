package com.lin.nls_export.entrance

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
    // 至少要导出一项
    if (listOf(exportKey, exportEn, exportSc, exportTc).contains(true)) {
        val enFile = File(exportPath, "strings.xml")
        val scFile = File(exportPath, "strings_CN.xml")
        val tcFile = File(exportPath, "strings_TW.xml")

        val enString = StringBuffer()
        val scString = StringBuffer()
        val tcString = StringBuffer()

        this.forEach {
            val usedKey = if (exportKey) it.key else ""
            if (exportEn) {
                enString.append("<string name=\"$usedKey\">${it.en}</string>").append("\n")
            }
            if (exportSc) {
                scString.append("<string name=\"$usedKey\">${it.sc}</string>").append("\n")
            }
            if (exportTc) {
                tcString.append("<string name=\"$usedKey\">${it.tc}</string>").append("\n")
            }
        }
        enFile.writeText(enString.toString())
        scFile.writeText(scString.toString())
        tcFile.writeText(tcString.toString())
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
    val path = "C:\\Users\\lisonglin\\Desktop\\新建 Microsoft Excel 工作表.xlsx"
    val filter = SheetNameFilter.ReadWhiteListFilter(listOf("Sheet1", "Sheet2"))
    val keyColumn = "key"
    val enColumn = "en"
    val scColumn = "sc"
    val tcColumn = "tc"
    val removeIllegalKeyColumns = true
    val trimAllValues = true

    val exportPath = "C:\\Users\\lisonglin\\Desktop"
    val exportKey = true
    val exportEn = true
    val exportSc = true
    val exportTc = true

    readFromExcel(path, filter, keyColumn, enColumn, scColumn, tcColumn)
            .removeIllegalKeyRows(removeIllegalKeyColumns)
            .trim(trimAllValues)
            .interceptorHandling()
            .exportNlsDoc(exportPath, exportKey, exportEn, exportSc, exportTc)
}