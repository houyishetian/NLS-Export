package com.lin.nls_export.entities

sealed class SheetNameFilter {

    companion object {
        fun getControlSheetList(controlSheetStr: String?): List<String> {
            return controlSheetStr?.trim()?.takeIf { it.isNotBlank() }?.split("/")?.filter { it.isNotBlank() }
                    ?: emptyList()
        }
    }

    // 读所有 sheet
    object ReadAllSheetFilter : SheetNameFilter()

    // 处于黑名单中的 sheet 不读，其他都读
    data class RemoveBlackListFilter(val blackList: List<String>) : SheetNameFilter()

    // 处于白名单中的 sheet 才读，其他不读
    data class ReadWhiteListFilter(val whiteList: List<String>) : SheetNameFilter()
}