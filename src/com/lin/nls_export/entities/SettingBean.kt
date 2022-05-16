package com.lin.nls_export.entities

import java.io.Serializable

class SettingBean(
        val sheetSetting: Int,
        val sheetControlInput: String,
        val keyColumnSetting: NlsColumnInputBean,
        val enColumnSetting: NlsColumnInputBean,
        val scColumnSetting: NlsColumnInputBean,
        val tcColumnSetting: NlsColumnInputBean,
        val removeIllegalKeyLine: Boolean,
        val trimValue: Boolean,
        val usingPathAsOutputName: Boolean
) : Serializable

class NlsColumnInputBean(
        val columnName: String,
        val isRead: Boolean?
) : Serializable