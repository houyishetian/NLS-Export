package com.lin.nls_export.entities

data class NlsItemBean(
        val key: String, // "" 表示表示没有该列 或 该 cell 没有值或读取失败
        val en: String, // "" 表示表示没有该列 或 该 cell 没有值或读取失败
        val sc: String, // "" 表示表示没有该列 或 该 cell 没有值或读取失败
        val tc: String // "" 表示表示没有该列 或 该 cell 没有值或读取失败
)