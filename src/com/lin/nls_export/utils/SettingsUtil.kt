package com.lin.nls_export.utils

import com.lin.nls_export.entities.NlsColumnInputBean
import com.lin.nls_export.entities.SettingBean
import com.lin.nls_export.ext.tryCatchAllExceptions
import java.io.*

object SettingsUtil {

    private val savingFile: File by lazy {
        // 将文件保存到临时目录下
        // win7: C:\Users\登录用户~1\AppData\Local\Temp\
        // Linux: /tmp
        val tmpDirectoryPath = System.getProperty("java.io.tmpdir")
        File(tmpDirectoryPath, "NlsExport.txt")
    }

    /**
     * 序列化后保存对象
     */
    fun saveSetting(setting: SettingBean) {
        tryCatchAllExceptions({
            val outputStream = ObjectOutputStream(FileOutputStream(savingFile))
            outputStream.writeObject(setting)
            outputStream.close()
        })
    }

    /**
     * 取出保存的值并反序列化恢复为对象
     */
    fun getSetting(): SettingBean? {
        return tryCatchAllExceptions({
            val inputStream = ObjectInputStream(FileInputStream(savingFile))
            val bean = inputStream.readObject() as? SettingBean
            inputStream.close()
            bean
        })
    }

    /**
     * 删除文件
     */
    fun deleteExistingSetting() {
        savingFile.delete()
    }

    /**
     * 获取默认设置
     */
    fun getDefaultSetting(): SettingBean {
        return SettingBean(
                sheetSetting = 0,
                sheetControlInput = "",
                keyColumnSetting = NlsColumnInputBean("Key", true),
                enColumnSetting = NlsColumnInputBean("English", true),
                scColumnSetting = NlsColumnInputBean("Simple Chinese", true),
                tcColumnSetting = NlsColumnInputBean("Tradional Chinese", true),
                removeIllegalKeyLine = true,
                trimValue = true,
                autoCoverExistingFiles = true
        )
    }

    fun getSettingFilePath(): String {
        return savingFile.absolutePath
    }
}