package com.lin.nls_export.utils

import com.lin.nls_export.entities.NlsItemBean
import com.lin.nls_export.entities.SheetNameFilter
import org.apache.poi.hssf.usermodel.HSSFWorkbook
import org.apache.poi.ss.usermodel.Cell
import org.apache.poi.ss.usermodel.CellType
import org.apache.poi.ss.usermodel.Sheet
import org.apache.poi.ss.usermodel.Workbook
import org.apache.poi.xssf.usermodel.XSSFWorkbook
import java.io.File
import java.text.NumberFormat

class ReadExcelUtil(private val filePath: String,
                    private val sheetNameFilter: SheetNameFilter,
                    private var keyColumnName: String?, // 为 null 或 empty 或 blank 表示不读取
                    private var enColumnName: String?, // 为 null 或 empty 或 blank 表示不读取
                    private var scColumnName: String?, // 为 null 或 empty 或 blank 表示不读取
                    private var tcColumnName: String? // 为 null 或 empty 或 blank 表示不读取
) {

    private val workBook: Workbook

    init {
        workBook = File(filePath).takeIf { it.exists() && it.isFile }?.let {
            it.inputStream().run {
                val result = when (it.extension) {
                    "xls" -> HSSFWorkbook(this)
                    "xlsx" -> XSSFWorkbook(this)
                    else -> throw IllegalArgumentException("$filePath 不是 xls/xlsx 文件!")
                }
                this.close()
                result
            }
        } ?: throw IllegalArgumentException("$filePath 不存在或不是文件!")

        keyColumnName = keyColumnName?.takeIf { it.isNotBlank() }
        enColumnName = enColumnName?.takeIf { it.isNotBlank() }
        scColumnName = scColumnName?.takeIf { it.isNotBlank() }
        tcColumnName = tcColumnName?.takeIf { it.isNotBlank() }

        listOfNotNull(keyColumnName, enColumnName, scColumnName, tcColumnName).let {
            // 不能有重复的 column name，不然无法识别
            if (it.size != it.distinct().size) {
                throw IllegalArgumentException("不允许使用相同的 column name，请检查上述输入!")
            }
        }
    }

    /**
     * 读取当前 excel 内的符合条件的 sheet list
     */
    private fun getAllSheet(): List<Sheet> {
        val result = mutableListOf<Sheet>()
        val iterator = workBook.sheetIterator()
        while (iterator.hasNext()) {
            result.add(iterator.next())
        }
        return when (sheetNameFilter) {
            SheetNameFilter.ReadAllSheetFilter -> result // 读取全部，直接返回
            is SheetNameFilter.RemoveBlackListFilter -> {
                // 读取全部，将黑名单中的去掉，剩余返回
                result.removeIf { it.sheetName in sheetNameFilter.blackList }
                result
            }
            is SheetNameFilter.ReadWhiteListFilter -> {
                // 读取全部，将白名单之外的去掉，剩余返回
                result.removeIf { it.sheetName !in sheetNameFilter.whiteList }
                result
            }
        }
    }

    /**
     * 获取当前单元格的内容
     */
    private fun getCellValue(cell: Cell?): String {
        return when (cell?.cellType) {
            CellType.NUMERIC -> {
                NumberFormat.getInstance().let {
                    it.isGroupingUsed = false
                    it.format(cell.numericCellValue)
                }
            }
            CellType.STRING -> cell.richStringCellValue.string
            else -> ""
        }
    }

    /**
     * 读取 sheet 第一行的内容，拿到想要的值的索引
     */
    private fun getReadColumnIndexes(sheet: Sheet): ColumnIndexes? {
        // 只处理第一行，认为第一行是 key 所在行
        return sheet.getRow(0)?.let {
            var keyIndex: Int = -1
            var enIndex: Int = -1
            var scIndex: Int = -1
            var tcIndex: Int = -1

            val shouldReadKey = keyColumnName != null
            val shouldReadEn = enColumnName != null
            val shouldReadSc = scColumnName != null
            val shouldReadTc = tcColumnName != null

            for (index in it.firstCellNum..it.lastCellNum) {
                // 读取当前 cell 值
                val currentCellValue = getCellValue(it.getCell(index))
                // 只读取第一个匹配到的 cell，即 index 为 -1；如果不为 -1 则不再读该字段
                when {
                    shouldReadKey && keyIndex == -1 && currentCellValue == keyColumnName -> keyIndex = index
                    shouldReadEn && enIndex == -1 && currentCellValue == enColumnName -> enIndex = index
                    shouldReadSc && scIndex == -1 && currentCellValue == scColumnName -> scIndex = index
                    shouldReadTc && tcIndex == -1 && currentCellValue == tcColumnName -> tcIndex = index
                }

                // 如果所有的 index 都已经 read 完毕，已经没有必要再遍历下去
                val keyAlreadyRead = (shouldReadKey && keyIndex != -1) || !shouldReadKey
                val enAlreadyRead = (shouldReadEn && enIndex != -1) || !shouldReadEn
                val scAlreadyRead = (shouldReadSc && scIndex != -1) || !shouldReadSc
                val tcAlreadyRead = (shouldReadTc && tcIndex != -1) || !shouldReadTc
                if (keyAlreadyRead && enAlreadyRead && scAlreadyRead && tcAlreadyRead) {
                    return ColumnIndexes(keyIndex, enIndex, scIndex, tcIndex)
                }
            }
            ColumnIndexes(keyIndex, enIndex, scIndex, tcIndex)
        }
    }

    fun read(): List<NlsItemBean> {
        val allNeedSheet = getAllSheet()
        val result = mutableListOf<NlsItemBean>()
        allNeedSheet.forEach {
            val indexes = getReadColumnIndexes(it)
            // 只读取有内容的 sheet，indexes 如果是 empty，说明该 sheet 无法获取到有效的 index
            indexes?.takeIf { it.isEmpty().not() }?.let { safeIndexes ->
                // 从第2行开始读
                for (rowIndex in 1..it.lastRowNum) {
                    it.getRow(rowIndex)?.let { row ->
                        // 读取当前行
                        // key
                        val key = safeIndexes.keyIndex.takeIf { it > -1 }?.let { getCellValue(row.getCell(it)) }.orEmpty()
                        // en
                        val en = safeIndexes.enIndex.takeIf { it > -1 }?.let { getCellValue(row.getCell(it)) }.orEmpty()
                        // sc
                        val sc = safeIndexes.scIndex.takeIf { it > -1 }?.let { getCellValue(row.getCell(it)) }.orEmpty()
                        // tc
                        val tc = safeIndexes.tcIndex.takeIf { it > -1 }?.let { getCellValue(row.getCell(it)) }.orEmpty()
                        result.add(NlsItemBean(key, en, sc, tc))
                    }
                }
            }
        }
        return result
    }

    private data class ColumnIndexes(val keyIndex: Int, val enIndex: Int, val scIndex: Int, val tcIndex: Int) {
        // true 表示所有 index 都不存在，即该 sheet 没有读取的必要
        fun isEmpty(): Boolean {
            return keyIndex < 0 && enIndex < 0 && scIndex < 0 && tcIndex < 0
        }
    }
}