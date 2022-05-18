package com.lin.nls_export.entities

data class NlsItemBean(
        val key: String?, // null 表示没有该列，"" 表示该 cell 没有值或读取失败
        val en: String?, // null 表示没有该列，"" 表示该 cell 没有值或读取失败
        val sc: String?, // null 表示没有该列，"" 表示该 cell 没有值或读取失败
        val tc: String? // null 表示没有该列，"" 表示该 cell 没有值或读取失败
)