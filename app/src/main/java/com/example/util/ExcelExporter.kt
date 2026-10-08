package com.example.util

import android.content.Context
import androidx.core.content.FileProvider
import com.example.data.model.PatientRecordEntity
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream
import java.io.OutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream

object ExcelExporter {

    private val HEADERS = listOf(
        "S NO",
        "IP NO",
        "NAME",
        "AGE",
        "SEX",
        "ADMISSION DATE & TIME",
        "PATIENT RECEIVED TIME",
        "BROAD SPECIALITY CATEGORY",
        "DIAGNOSIS",
        "AGE INTERVAL",
        "TAEI/PILLAR/TAEI NON PILLAR",
        "MEDICOLEGAL CATEGORY",
        "TRANSFERRED OUT",
        "TRANSFERRED OUT TIME",
        "EMERGENCY RESPONSE TIME"
    )

    fun getDefaultFilename(): String {
        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        return "Hospital_Patient_Records_${dateFormat.format(Date())}.xlsx"
    }

    fun generateXlsxBytes(records: List<PatientRecordEntity>): ByteArray {
        val byteOut = ByteArrayOutputStream()
        ZipOutputStream(byteOut).use { zip ->
            // 1. [Content_Types].xml
            zip.putNextEntry(ZipEntry("[Content_Types].xml"))
            zip.write(buildContentTypesXml().toByteArray(Charsets.UTF_8))
            zip.closeEntry()

            // 2. _rels/.rels
            zip.putNextEntry(ZipEntry("_rels/.rels"))
            zip.write(buildRootRelsXml().toByteArray(Charsets.UTF_8))
            zip.closeEntry()

            // 3. xl/_rels/workbook.xml.rels
            zip.putNextEntry(ZipEntry("xl/_rels/workbook.xml.rels"))
            zip.write(buildWorkbookRelsXml().toByteArray(Charsets.UTF_8))
            zip.closeEntry()

            // 4. xl/workbook.xml
            zip.putNextEntry(ZipEntry("xl/workbook.xml"))
            zip.write(buildWorkbookXml().toByteArray(Charsets.UTF_8))
            zip.closeEntry()

            // 5. xl/styles.xml
            zip.putNextEntry(ZipEntry("xl/styles.xml"))
            zip.write(buildStylesXml().toByteArray(Charsets.UTF_8))
            zip.closeEntry()

            // 6. xl/worksheets/sheet1.xml
            zip.putNextEntry(ZipEntry("xl/worksheets/sheet1.xml"))
            zip.write(buildSheetXml(records).toByteArray(Charsets.UTF_8))
            zip.closeEntry()
        }
        return byteOut.toByteArray()
    }

    fun exportToStream(records: List<PatientRecordEntity>, outputStream: OutputStream) {
        val bytes = generateXlsxBytes(records)
        outputStream.write(bytes)
        outputStream.flush()
    }

    fun exportToCacheFile(context: Context, filename: String, records: List<PatientRecordEntity>): File {
        val exportsDir = File(context.cacheDir, "exports")
        if (!exportsDir.exists()) {
            exportsDir.mkdirs()
        }
        val safeFilename = if (filename.endsWith(".xlsx", ignoreCase = true)) filename else "$filename.xlsx"
        val targetFile = File(exportsDir, safeFilename)
        FileOutputStream(targetFile).use { fos ->
            exportToStream(records, fos)
        }
        return targetFile
    }

    private fun escapeXml(input: String?): String {
        if (input == null) return ""
        val sb = StringBuilder()
        for (c in input) {
            when (c) {
                '&' -> sb.append("&amp;")
                '<' -> sb.append("&lt;")
                '>' -> sb.append("&gt;")
                '"' -> sb.append("&quot;")
                '\'' -> sb.append("&apos;")
                else -> {
                    if (c.code in 0x20..0xD7FF || c == '\t' || c == '\n' || c == '\r') {
                        sb.append(c)
                    }
                }
            }
        }
        return sb.toString()
    }

    private fun getColumnLetters(colIndexZeroBased: Int): String {
        var temp = colIndexZeroBased
        val result = StringBuilder()
        while (temp >= 0) {
            result.insert(0, ('A'.code + (temp % 26)).toChar())
            temp = temp / 26 - 1
        }
        return result.toString()
    }

    private fun buildContentTypesXml(): String {
        return """<?xml version="1.0" encoding="UTF-8" standalone="yes"?>
<Types xmlns="http://schemas.openxmlformats.org/package/2006/content-types">
  <Default Extension="rels" ContentType="application/vnd.openxmlformats-package.relationships+xml"/>
  <Default Extension="xml" ContentType="application/xml"/>
  <Override PartName="/xl/workbook.xml" ContentType="application/vnd.openxmlformats-officedocument.spreadsheetml.sheet.main+xml"/>
  <Override PartName="/xl/worksheets/sheet1.xml" ContentType="application/vnd.openxmlformats-officedocument.spreadsheetml.worksheet+xml"/>
  <Override PartName="/xl/styles.xml" ContentType="application/vnd.openxmlformats-officedocument.spreadsheetml.styles+xml"/>
</Types>"""
    }

    private fun buildRootRelsXml(): String {
        return """<?xml version="1.0" encoding="UTF-8" standalone="yes"?>
<Relationships xmlns="http://schemas.openxmlformats.org/package/2006/relationships">
  <Relationship Id="rId1" Type="http://schemas.openxmlformats.org/officeDocument/2006/relationships/officeDocument" Target="xl/workbook.xml"/>
</Relationships>"""
    }

    private fun buildWorkbookRelsXml(): String {
        return """<?xml version="1.0" encoding="UTF-8" standalone="yes"?>
<Relationships xmlns="http://schemas.openxmlformats.org/package/2006/relationships">
  <Relationship Id="rId1" Type="http://schemas.openxmlformats.org/officeDocument/2006/relationships/worksheet" Target="worksheets/sheet1.xml"/>
  <Relationship Id="rId2" Type="http://schemas.openxmlformats.org/officeDocument/2006/relationships/styles" Target="styles.xml"/>
</Relationships>"""
    }

    private fun buildWorkbookXml(): String {
        return """<?xml version="1.0" encoding="UTF-8" standalone="yes"?>
<workbook xmlns="http://schemas.openxmlformats.org/spreadsheetml/2006/main" xmlns:r="http://schemas.openxmlformats.org/officeDocument/2006/relationships">
  <sheets>
    <sheet name="Emergency Patients" sheetId="1" r:id="rId1"/>
  </sheets>
</workbook>"""
    }

    private fun buildStylesXml(): String {
        // Style index 0: standard normal
        // Style index 1: header (bold white text on medical dark teal background 00687A, centered)
        // Style index 2: normal cell with borders
        // Style index 3: centered number/code with borders
        // Style index 4: alternate zebra row light tint (F4F8FA) with borders
        return """<?xml version="1.0" encoding="UTF-8" standalone="yes"?>
<styleSheet xmlns="http://schemas.openxmlformats.org/spreadsheetml/2006/main">
  <fonts count="2">
    <font>
      <sz val="11"/>
      <name val="Calibri"/>
    </font>
    <font>
      <b/>
      <sz val="11"/>
      <color rgb="FFFFFFFF"/>
      <name val="Calibri"/>
    </font>
  </fonts>
  <fills count="4">
    <fill><patternFill patternType="none"/></fill>
    <fill><patternFill patternType="gray125"/></fill>
    <fill>
      <patternFill patternType="solid">
        <fgColor rgb="FF00687A"/>
      </patternFill>
    </fill>
    <fill>
      <patternFill patternType="solid">
        <fgColor rgb="FFF4F8FA"/>
      </patternFill>
    </fill>
  </fills>
  <borders count="2">
    <border>
      <left/><right/><top/><bottom/><diagonal/>
    </border>
    <border>
      <left style="thin"><color rgb="FFD0D7D9"/></left>
      <right style="thin"><color rgb="FFD0D7D9"/></right>
      <top style="thin"><color rgb="FFD0D7D9"/></top>
      <bottom style="thin"><color rgb="FFD0D7D9"/></bottom>
    </border>
  </borders>
  <cellStyleXfs count="1">
    <xf numFmtId="0" fontId="0" fillId="0" borderId="0"/>
  </cellStyleXfs>
  <cellXfs count="5">
    <xf numFmtId="0" fontId="0" fillId="0" borderId="0" xfId="0"/>
    <xf numFmtId="0" fontId="1" fillId="2" borderId="1" xfId="0" applyFont="1" applyFill="1" applyBorder="1" applyAlignment="1">
      <alignment horizontal="center" vertical="center" wrapText="1"/>
    </xf>
    <xf numFmtId="0" fontId="0" fillId="0" borderId="1" xfId="0" applyFont="1" applyBorder="1" applyAlignment="1">
      <alignment vertical="center"/>
    </xf>
    <xf numFmtId="0" fontId="0" fillId="0" borderId="1" xfId="0" applyFont="1" applyBorder="1" applyAlignment="1">
      <alignment horizontal="center" vertical="center"/>
    </xf>
    <xf numFmtId="0" fontId="0" fillId="3" borderId="1" xfId="0" applyFont="1" applyFill="1" applyBorder="1" applyAlignment="1">
      <alignment vertical="center"/>
    </xf>
  </cellXfs>
</styleSheet>"""
    }

    private fun buildSheetXml(records: List<PatientRecordEntity>): String {
        val sb = StringBuilder()
        sb.append("""<?xml version="1.0" encoding="UTF-8" standalone="yes"?>""")
        sb.append("""<worksheet xmlns="http://schemas.openxmlformats.org/spreadsheetml/2006/main">""")

        // Freeze top header row
        sb.append("""<sheetViews><sheetView tabSelected="1" workbookViewId="0">""")
        sb.append("""<pane ySplit="1" topLeftCell="A2" activePane="bottomLeft" state="frozen"/>""")
        sb.append("""</sheetView></sheetViews>""")

        // Set column widths matching hospital data
        sb.append("""<cols>""")
        val colWidths = listOf(8, 16, 26, 8, 8, 24, 22, 28, 26, 15, 30, 24, 20, 22, 26)
        colWidths.forEachIndexed { index, width ->
            val colNum = index + 1
            sb.append("""<col min="$colNum" max="$colNum" width="$width" customWidth="1"/>""")
        }
        sb.append("""</cols>""")

        sb.append("""<sheetData>""")

        // Header Row (row 1, height 28)
        sb.append("""<row r="1" ht="28" customHeight="1">""")
        HEADERS.forEachIndexed { colIdx, header ->
            val cellRef = "${getColumnLetters(colIdx)}1"
            sb.append("""<c r="$cellRef" s="1" t="inlineStr"><is><t>${escapeXml(header)}</t></is></c>""")
        }
        sb.append("""</row>""")

        // Data Rows
        records.forEachIndexed { rowIdx, record ->
            val rowNum = rowIdx + 2
            val isEven = rowIdx % 2 == 1
            val baseStyle = if (isEven) "4" else "2"
            val centerStyle = "3"

            sb.append("""<row r="$rowNum" ht="22" customHeight="1">""")

            // 1. S NO (numeric, centered)
            sb.append("""<c r="${getColumnLetters(0)}$rowNum" s="$centerStyle"><v>${record.serialNumber}</v></c>""")

            // 2. IP NO (text to preserve non-numeric prefixes or prevent scientific notation)
            sb.append("""<c r="${getColumnLetters(1)}$rowNum" s="$centerStyle" t="inlineStr"><is><t>${escapeXml(record.ipNumber)}</t></is></c>""")

            // 3. NAME
            sb.append("""<c r="${getColumnLetters(2)}$rowNum" s="$baseStyle" t="inlineStr"><is><t>${escapeXml(record.name)}</t></is></c>""")

            // 4. AGE (numeric)
            sb.append("""<c r="${getColumnLetters(3)}$rowNum" s="$centerStyle"><v>${record.age}</v></c>""")

            // 5. SEX
            sb.append("""<c r="${getColumnLetters(4)}$rowNum" s="$centerStyle" t="inlineStr"><is><t>${escapeXml(record.sex)}</t></is></c>""")

            // 6. ADMISSION DATE & TIME
            sb.append("""<c r="${getColumnLetters(5)}$rowNum" s="$baseStyle" t="inlineStr"><is><t>${escapeXml(record.admissionDateTime)}</t></is></c>""")

            // 7. PATIENT RECEIVED TIME
            sb.append("""<c r="${getColumnLetters(6)}$rowNum" s="$centerStyle" t="inlineStr"><is><t>${escapeXml(record.patientReceivedTime)}</t></is></c>""")

            // 8. BROAD SPECIALITY CATEGORY
            sb.append("""<c r="${getColumnLetters(7)}$rowNum" s="$baseStyle" t="inlineStr"><is><t>${escapeXml(record.broadSpecialityCategory)}</t></is></c>""")

            // 9. DIAGNOSIS
            sb.append("""<c r="${getColumnLetters(8)}$rowNum" s="$baseStyle" t="inlineStr"><is><t>${escapeXml(record.diagnosis)}</t></is></c>""")

            // 10. AGE INTERVAL
            sb.append("""<c r="${getColumnLetters(9)}$rowNum" s="$centerStyle" t="inlineStr"><is><t>${escapeXml(record.ageInterval)}</t></is></c>""")

            // 11. TAEI/PILLAR/TAEI NON PILLAR
            sb.append("""<c r="${getColumnLetters(10)}$rowNum" s="$centerStyle" t="inlineStr"><is><t>${escapeXml(record.pillarStatus)}</t></is></c>""")

            // 12. MEDICOLEGAL CATEGORY
            sb.append("""<c r="${getColumnLetters(11)}$rowNum" s="$centerStyle" t="inlineStr"><is><t>${escapeXml(record.medicolegalCategory)}</t></is></c>""")

            // 13. TRANSFERRED OUT
            sb.append("""<c r="${getColumnLetters(12)}$rowNum" s="$baseStyle" t="inlineStr"><is><t>${escapeXml(record.transferredOut)}</t></is></c>""")

            // 14. TRANSFERRED OUT TIME
            sb.append("""<c r="${getColumnLetters(13)}$rowNum" s="$centerStyle" t="inlineStr"><is><t>${escapeXml(record.transferredOutTime)}</t></is></c>""")

            // 15. EMERGENCY RESPONSE TIME
            sb.append("""<c r="${getColumnLetters(14)}$rowNum" s="$centerStyle" t="inlineStr"><is><t>${escapeXml(record.emergencyResponseTime)}</t></is></c>""")

            sb.append("""</row>""")
        }

        sb.append("""</sheetData>""")
        sb.append("""</worksheet>""")
        return sb.toString()
    }
}
