package com.lin.nls_export.entrance

import com.lin.nls_export.entities.SheetNameFilter
import com.lin.nls_export.utils.ReadExcelUtil

fun main(args: Array<String>) {

    val path = "C:\\Users\\lisonglin\\Desktop\\新建 Microsoft Excel 工作表.xlsx"

    val filter = SheetNameFilter.ReadWhiteListFilter(listOf("Sheet1", "Sheet2"))

    val keyColumn = "key"

    val enColumn = "en"

    val scColumn = "sc"

    val tcColumn = "tc"

    val util = ReadExcelUtil(path, filter, keyColumn, enColumn, scColumn, tcColumn)

    util.read().filter { it.key.isBlank().not() }.forEach {
        println(it)
    }
}