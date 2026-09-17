package com.example.util

import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.graphics.Typeface
import android.graphics.pdf.PdfDocument
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import androidx.core.content.FileProvider
import com.example.data.FamilyMember
import com.example.data.Gender
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

enum class PdfOrientation {
    PORTRAIT,
    LANDSCAPE
}

data class PdfExportConfig(
    val selectedBranchId: Long? = null,
    val orientation: PdfOrientation = PdfOrientation.PORTRAIT,
    val includeCoverPage: Boolean = true
)

data class PdfExportResult(
    val file: File,
    val uri: Uri,
    val pageCount: Int,
    val fileSizeBytes: Long,
    val title: String
)

object FamilyTreePdfGenerator {

    // Colors matching Islamic Emerald & Heritage Gold palette
    private const val COLOR_EMERALD_DARK = 0xFF083B25.toInt()
    private const val COLOR_EMERALD_PRIMARY = 0xFF0D5C3A.toInt()
    private const val COLOR_EMERALD_LIGHT = 0xFFE8F5E9.toInt()
    private const val COLOR_EMERALD_CONTAINER = 0xFFD1FAE5.toInt()
    private const val COLOR_GOLD_PRIMARY = 0xFFD4A017.toInt()
    private const val COLOR_GOLD_DARK = 0xFFB8860B.toInt()
    private const val COLOR_GOLD_LIGHT = 0xFFFEF3C7.toInt()
    private const val COLOR_SLATE_DARK = 0xFF1E293B.toInt()
    private const val COLOR_SLATE_MUTED = 0xFF64748B.toInt()
    private const val COLOR_BORDER = 0xFFCBD5E1.toInt()
    private const val COLOR_RED_DECEASED = 0xFFDC2626.toInt()
    private const val COLOR_RED_BG = 0xFFFEE2E2.toInt()
    private const val COLOR_WHITE = 0xFFFFFFFF.toInt()
    private const val COLOR_BG = 0xFFFAFAFA.toInt()

    fun generatePdf(
        context: Context,
        allMembers: List<FamilyMember>,
        config: PdfExportConfig = PdfExportConfig()
    ): PdfExportResult {
        val isLandscape = config.orientation == PdfOrientation.LANDSCAPE
        val pageWidth = if (isLandscape) 842 else 595
        val pageHeight = if (isLandscape) 595 else 842

        val document = PdfDocument()
        var pageNumber = 1

        val rootMember = allMembers.find { it.fatherId == null } ?: allMembers.firstOrNull()
        val mainBranches = if (rootMember != null) {
            allMembers.filter { it.fatherId == rootMember.id }.sortedBy { it.displayOrder }
        } else emptyList()

        val childrenMap = allMembers.groupBy { it.fatherId }

        val branchesToRender = if (config.selectedBranchId != null) {
            mainBranches.filter { it.id == config.selectedBranchId }
        } else {
            mainBranches
        }

        // ==========================================
        // 1. COVER / EXECUTIVE OVERVIEW PAGE
        // ==========================================
        if (config.includeCoverPage) {
            val pageInfo = PdfDocument.PageInfo.Builder(pageWidth, pageHeight, pageNumber).create()
            val page = document.startPage(pageInfo)
            drawCoverPage(
                canvas = page.canvas,
                width = pageWidth.toFloat(),
                height = pageHeight.toFloat(),
                allMembers = allMembers,
                rootMember = rootMember,
                mainBranches = mainBranches,
                selectedBranch = branchesToRender.firstOrNull().takeIf { config.selectedBranchId != null }
            )
            document.finishPage(page)
            pageNumber++
        }

        // ==========================================
        // 2. DETAILED BRANCH VISUAL LINEAGE PAGES
        // ==========================================
        for (branch in branchesToRender) {
            pageNumber = drawBranchVisualPages(
                document = document,
                branch = branch,
                allMembers = allMembers,
                childrenMap = childrenMap,
                pageWidth = pageWidth,
                pageHeight = pageHeight,
                startPageNumber = pageNumber
            )
        }

        // Write document to private cache file
        val outputDir = File(context.cacheDir, "shajra_pdf_exports").apply { mkdirs() }
        val dateStr = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(Date())
        val branchSuffix = if (config.selectedBranchId != null) {
            val b = mainBranches.find { it.id == config.selectedBranchId }
            "_shakh_${b?.name?.replace(" ", "_") ?: "branch"}"
        } else "_full_tree"
        val pdfFile = File(outputDir, "Shajra_Nasab_Khandan_Muhammad_Ali${branchSuffix}_$dateStr.pdf")

        FileOutputStream(pdfFile).use { out ->
            document.writeTo(out)
        }
        document.close()

        val uri = try {
            FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                pdfFile
            )
        } catch (e: Exception) {
            Uri.fromFile(pdfFile)
        }

        return PdfExportResult(
            file = pdfFile,
            uri = uri,
            pageCount = pageNumber - 1,
            fileSizeBytes = pdfFile.length(),
            title = "شجرہ نسب خاندان محمد علی"
        )
    }

    // ==========================================
    // COVER PAGE DRAWING
    // ==========================================
    private fun drawCoverPage(
        canvas: Canvas,
        width: Float,
        height: Float,
        allMembers: List<FamilyMember>,
        rootMember: FamilyMember?,
        mainBranches: List<FamilyMember>,
        selectedBranch: FamilyMember?
    ) {
        val paint = Paint(Paint.ANTI_ALIAS_FLAG)

        // Background
        paint.color = COLOR_BG
        canvas.drawRect(0f, 0f, width, height, paint)

        // Outer Emerald Double Border
        paint.style = Paint.Style.STROKE
        paint.color = COLOR_EMERALD_PRIMARY
        paint.strokeWidth = 3f
        canvas.drawRect(20f, 20f, width - 20f, height - 20f, paint)

        // Inner Gold Border
        paint.color = COLOR_GOLD_PRIMARY
        paint.strokeWidth = 1.2f
        canvas.drawRect(25f, 25f, width - 25f, height - 25f, paint)

        // Corner Ornaments
        drawCornerOrnaments(canvas, 25f, 25f, width - 25f, height - 25f, paint)

        var y = 65f

        // Bismillah
        paint.style = Paint.Style.FILL
        paint.color = COLOR_EMERALD_DARK
        paint.textSize = 14f
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        paint.textAlign = Paint.Align.CENTER
        canvas.drawText("بِسْمِ اللَّهِ الرَّحْمَٰنِ الرَّحِيمِ", width / 2f, y, paint)

        y += 35f

        // Main Title: شجرہ نسب خاندان محمد علی
        paint.textSize = 24f
        paint.color = COLOR_EMERALD_DARK
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        canvas.drawText("شجرہ نسب خاندان محمد علی", width / 2f, y, paint)

        y += 8f
        // Gold Underline Bar
        paint.color = COLOR_GOLD_PRIMARY
        paint.strokeWidth = 2.5f
        paint.style = Paint.Style.STROKE
        canvas.drawLine(width / 2f - 110f, y, width / 2f + 110f, y, paint)
        paint.style = Paint.Style.FILL

        y += 24f
        // Subtitle
        paint.textSize = 12f
        paint.color = COLOR_SLATE_MUTED
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
        val subtitle = if (selectedBranch != null) {
            "شاخ: ${selectedBranch.name} • مورثِ اعلیٰ: ${rootMember?.name ?: "محمد علی"}"
        } else {
            "خاندانی شجرہ اور تفصیلی نسب نامہ • مورثِ اعلیٰ: ${rootMember?.name ?: "محمد علی"}"
        }
        canvas.drawText(subtitle, width / 2f, y, paint)

        y += 35f

        // ==========================================
        // STATS BOX
        // ==========================================
        val statsBoxRect = RectF(45f, y, width - 45f, y + 62f)
        paint.color = COLOR_EMERALD_LIGHT
        canvas.drawRoundRect(statsBoxRect, 12f, 12f, paint)
        paint.style = Paint.Style.STROKE
        paint.color = COLOR_EMERALD_PRIMARY
        paint.strokeWidth = 1.2f
        canvas.drawRoundRect(statsBoxRect, 12f, 12f, paint)
        paint.style = Paint.Style.FILL

        val maxGen = allMembers.maxOfOrNull { it.generation } ?: 6
        val dateUrdu = SimpleDateFormat("dd MMMM yyyy", Locale("ur")).format(Date())

        val colWidth = (width - 90f) / 4f
        val statLabels = listOf("کل افراد", "کل نسلیں", "اہم شاخیں", "تاریخِ تیاری")
        val statValues = listOf(
            "${allMembers.size}",
            "$maxGen نسلیں",
            "${mainBranches.size}",
            SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date())
        )

        for (i in 0 until 4) {
            val cx = 45f + colWidth * i + colWidth / 2f
            paint.textSize = 14f
            paint.color = COLOR_EMERALD_DARK
            paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            canvas.drawText(statValues[i], cx, y + 26f, paint)

            paint.textSize = 9.5f
            paint.color = COLOR_SLATE_MUTED
            paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
            canvas.drawText(statLabels[i], cx, y + 46f, paint)

            if (i < 3) {
                paint.color = COLOR_BORDER
                paint.strokeWidth = 1f
                paint.style = Paint.Style.STROKE
                val dividerX = 45f + colWidth * (i + 1)
                canvas.drawLine(dividerX, y + 10f, dividerX, y + 52f, paint)
                paint.style = Paint.Style.FILL
            }
        }

        y += 85f

        // ==========================================
        // ROOT & MAJOR BRANCHES OVERVIEW DIAGRAM
        // ==========================================
        paint.textSize = 13f
        paint.color = COLOR_EMERALD_DARK
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        paint.textAlign = Paint.Align.CENTER
        canvas.drawText("خاندانی بنیادی ڈھانچہ و اہم شاخیں", width / 2f, y, paint)

        y += 20f

        // Root Node Card (محمد علی)
        val rootCardWidth = 220f
        val rootCardHeight = 52f
        val rootLeft = width / 2f - rootCardWidth / 2f
        val rootRect = RectF(rootLeft, y, rootLeft + rootCardWidth, y + rootCardHeight)

        // Root Card Fill
        paint.color = COLOR_EMERALD_PRIMARY
        canvas.drawRoundRect(rootRect, 10f, 10f, paint)

        // Root Gold Accent
        paint.style = Paint.Style.STROKE
        paint.color = COLOR_GOLD_PRIMARY
        paint.strokeWidth = 2f
        canvas.drawRoundRect(rootRect, 10f, 10f, paint)
        paint.style = Paint.Style.FILL

        // Root Card Texts
        paint.color = COLOR_WHITE
        paint.textSize = 15f
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        canvas.drawText(rootMember?.name ?: "محمد علی", width / 2f, y + 24f, paint)

        paint.color = COLOR_GOLD_LIGHT
        paint.textSize = 10f
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
        canvas.drawText("مورثِ اعلیٰ • پہلی نسل (بانیِ شجرہ)", width / 2f, y + 42f, paint)

        val rootBottomY = y + rootCardHeight
        y = rootBottomY + 36f

        // Vertical Connector down from Root
        paint.color = COLOR_GOLD_PRIMARY
        paint.strokeWidth = 2f
        paint.style = Paint.Style.STROKE
        canvas.drawLine(width / 2f, rootBottomY, width / 2f, rootBottomY + 20f, paint)

        // 4 Branch Cards
        if (mainBranches.isNotEmpty()) {
            val numBranches = mainBranches.size
            val marginH = 40f
            val availableW = width - (marginH * 2f)
            val branchGap = 12f
            val branchCardW = (availableW - (branchGap * (numBranches - 1))) / numBranches
            val branchCardH = 75f

            val branchCenters = mutableListOf<Float>()
            for (i in 0 until numBranches) {
                val bLeft = marginH + i * (branchCardW + branchGap)
                branchCenters.add(bLeft + branchCardW / 2f)
            }

            // Horizontal Branch Crossbar
            val firstCenter = branchCenters.first()
            val lastCenter = branchCenters.last()
            canvas.drawLine(firstCenter, rootBottomY + 20f, lastCenter, rootBottomY + 20f, paint)

            // Connectors down to each branch
            for (cx in branchCenters) {
                canvas.drawLine(cx, rootBottomY + 20f, cx, y, paint)
            }

            paint.style = Paint.Style.FILL

            // Draw each Branch Card
            for (i in 0 until numBranches) {
                val branch = mainBranches[i]
                val bLeft = marginH + i * (branchCardW + branchGap)
                val bRect = RectF(bLeft, y, bLeft + branchCardW, y + branchCardH)

                // Branch Card Background
                val isSel = selectedBranch == null || selectedBranch.id == branch.id
                paint.color = if (isSel) COLOR_WHITE else COLOR_BG
                canvas.drawRoundRect(bRect, 8f, 8f, paint)

                // Branch Card Border
                paint.style = Paint.Style.STROKE
                paint.color = if (isSel) COLOR_EMERALD_PRIMARY else COLOR_BORDER
                paint.strokeWidth = if (isSel) 1.5f else 1f
                canvas.drawRoundRect(bRect, 8f, 8f, paint)
                paint.style = Paint.Style.FILL

                // Top Header Pill
                val topPillRect = RectF(bLeft, y, bLeft + branchCardW, y + 22f)
                paint.color = if (isSel) COLOR_EMERALD_LIGHT else 0xFFF1F5F9.toInt()
                canvas.drawRoundRect(topPillRect, 8f, 8f, paint)
                // Bottom corners flat
                canvas.drawRect(bLeft, y + 10f, bLeft + branchCardW, y + 22f, paint)

                // Branch Name
                paint.color = COLOR_EMERALD_DARK
                paint.textSize = 10f
                paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
                paint.textAlign = Paint.Align.CENTER
                canvas.drawText("شاخ ${branch.name}", bLeft + branchCardW / 2f, y + 15f, paint)

                // Member count in this branch
                val branchDescendants = countDescendants(branch.id, allMembers)
                paint.color = COLOR_SLATE_DARK
                paint.textSize = 10.5f
                paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
                canvas.drawText("${branchDescendants + 1} افراد", bLeft + branchCardW / 2f, y + 42f, paint)

                paint.color = COLOR_SLATE_MUTED
                paint.textSize = 8.5f
                paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
                canvas.drawText("دوسری تا چھٹی نسل", bLeft + branchCardW / 2f, y + 60f, paint)
            }
        }

        y += 120f

        // ==========================================
        // BOTTOM NOTICE & APPLICATION CREDITS
        // ==========================================
        val infoRect = RectF(45f, height - 105f, width - 45f, height - 45f)
        paint.color = COLOR_GOLD_LIGHT
        canvas.drawRoundRect(infoRect, 10f, 10f, paint)
        paint.style = Paint.Style.STROKE
        paint.color = COLOR_GOLD_PRIMARY
        paint.strokeWidth = 1f
        canvas.drawRoundRect(infoRect, 10f, 10f, paint)
        paint.style = Paint.Style.FILL

        paint.color = COLOR_EMERALD_DARK
        paint.textSize = 11f
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        paint.textAlign = Paint.Align.CENTER
        canvas.drawText("تخلیق و ڈیولپمنٹ: شمریز ایوب کالس", width / 2f, height - 80f, paint)

        paint.color = COLOR_SLATE_DARK
        paint.textSize = 9.5f
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
        canvas.drawText("معاونت برائے خاندانی شجرہ ڈیٹا: محمد شبیر کالس (سابق کونسلر)", width / 2f, height - 60f, paint)

        // Page number
        paint.textSize = 9f
        paint.color = COLOR_SLATE_MUTED
        canvas.drawText("صفحہ ۱", width / 2f, height - 25f, paint)
    }

    // ==========================================
    // DETAILED BRANCH PAGES (Visual Lineage Nodes)
    // ==========================================
    private fun drawBranchVisualPages(
        document: PdfDocument,
        branch: FamilyMember,
        allMembers: List<FamilyMember>,
        childrenMap: Map<Long?, List<FamilyMember>>,
        pageWidth: Int,
        pageHeight: Int,
        startPageNumber: Int
    ): Int {
        var currentPageNum = startPageNumber
        val paint = Paint(Paint.ANTI_ALIAS_FLAG)

        // Flatten all members in this branch in hierarchical order
        val branchMembersOrdered = mutableListOf<BranchMemberItem>()
        fun traverse(member: FamilyMember, depth: Int, parentLineage: String) {
            val children = childrenMap[member.id]?.sortedBy { it.displayOrder } ?: emptyList()
            branchMembersOrdered.add(
                BranchMemberItem(
                    member = member,
                    depth = depth,
                    hasChildren = children.isNotEmpty(),
                    childrenCount = children.size,
                    parentLineage = parentLineage
                )
            )
            for (c in children) {
                traverse(c, depth + 1, "$parentLineage > ${member.name}")
            }
        }

        // Start traverse from branch head
        traverse(branch, depth = 0, parentLineage = branch.name)

        var pageInfo = PdfDocument.PageInfo.Builder(pageWidth, pageHeight, currentPageNum).create()
        var page = document.startPage(pageInfo)
        var canvas = page.canvas

        drawPageFrameAndHeader(canvas, pageWidth.toFloat(), pageHeight.toFloat(), branch, currentPageNum)

        var currentY = 100f
        val maxY = pageHeight - 60f
        val marginX = 35f
        val contentW = pageWidth - 70f

        for (item in branchMembersOrdered) {
            val nodeHeight = 36f

            // If we are about to overflow the page, start a new page
            if (currentY + nodeHeight > maxY) {
                // Finish current page
                drawPageFooter(canvas, pageWidth.toFloat(), pageHeight.toFloat(), currentPageNum, branch.name)
                document.finishPage(page)
                currentPageNum++

                // Start new page
                pageInfo = PdfDocument.PageInfo.Builder(pageWidth, pageHeight, currentPageNum).create()
                page = document.startPage(pageInfo)
                canvas = page.canvas
                drawPageFrameAndHeader(canvas, pageWidth.toFloat(), pageHeight.toFloat(), branch, currentPageNum)
                currentY = 100f
            }

            // Draw visual member node
            drawMemberNode(
                canvas = canvas,
                item = item,
                startX = marginX,
                startY = currentY,
                totalWidth = contentW,
                nodeHeight = nodeHeight,
                paint = paint
            )

            currentY += nodeHeight + 8f
        }

        // Finish final page for this branch
        drawPageFooter(canvas, pageWidth.toFloat(), pageHeight.toFloat(), currentPageNum, branch.name)
        document.finishPage(page)
        currentPageNum++

        return currentPageNum
    }

    private fun drawMemberNode(
        canvas: Canvas,
        item: BranchMemberItem,
        startX: Float,
        startY: Float,
        totalWidth: Float,
        nodeHeight: Float,
        paint: Paint
    ) {
        val member = item.member
        val indent = item.depth * 20f
        val nodeX = startX + indent
        val nodeW = totalWidth - indent
        val rect = RectF(nodeX, startY, nodeX + nodeW, startY + nodeHeight)

        // Draw Tree Branch Line from right side (RTL context)
        if (item.depth > 0) {
            paint.style = Paint.Style.STROKE
            paint.color = COLOR_GOLD_PRIMARY
            paint.strokeWidth = 1.2f

            // Connector horizontal tick
            val branchLineY = startY + nodeHeight / 2f
            canvas.drawLine(nodeX - 14f, branchLineY, nodeX, branchLineY, paint)

            // Small connector circle
            paint.style = Paint.Style.FILL
            paint.color = COLOR_EMERALD_PRIMARY
            canvas.drawCircle(nodeX - 14f, branchLineY, 2.5f, paint)
        }

        // Card Fill
        paint.style = Paint.Style.FILL
        paint.color = when (item.depth) {
            0 -> COLOR_EMERALD_PRIMARY // Branch Head
            1 -> COLOR_EMERALD_LIGHT
            else -> COLOR_WHITE
        }
        canvas.drawRoundRect(rect, 6f, 6f, paint)

        // Card Border
        paint.style = Paint.Style.STROKE
        paint.color = when (item.depth) {
            0 -> COLOR_GOLD_PRIMARY
            1 -> COLOR_EMERALD_PRIMARY
            else -> COLOR_BORDER
        }
        paint.strokeWidth = if (item.depth <= 1) 1.2f else 0.8f
        canvas.drawRoundRect(rect, 6f, 6f, paint)
        paint.style = Paint.Style.FILL

        // Left accent bar
        val accentColor = if (member.isDeceased) COLOR_RED_DECEASED else COLOR_EMERALD_PRIMARY
        paint.color = accentColor
        canvas.drawRoundRect(RectF(nodeX, startY, nodeX + 4f, startY + nodeHeight), 2f, 2f, paint)

        // Text Drawing (RTL Urdu)
        val textRightX = nodeX + nodeW - 12f
        val textCenterY = startY + nodeHeight / 2f

        // Name
        paint.color = if (item.depth == 0) COLOR_WHITE else COLOR_EMERALD_DARK
        paint.textSize = if (item.depth == 0) 13f else 11.5f
        paint.typeface = Typeface.create(Typeface.DEFAULT, FontWeight(item.depth))
        paint.textAlign = Paint.Align.RIGHT

        val nameStr = buildString {
            append(member.name)
            if (member.fatherName != null && member.fatherName.isNotBlank() && item.depth > 0) {
                append(" بن ")
                append(member.fatherName)
            }
        }
        canvas.drawText(nameStr, textRightX, textCenterY + 4f, paint)

        // Left-aligned Badges & Tags:
        val badgeLeftX = nodeX + 12f
        paint.textAlign = Paint.Align.LEFT

        // Deceased Tag (مرحوم)
        var nextBadgeX = badgeLeftX
        if (member.isDeceased) {
            val deceasedPillRect = RectF(nextBadgeX, startY + 8f, nextBadgeX + 38f, startY + nodeHeight - 8f)
            paint.color = COLOR_RED_BG
            canvas.drawRoundRect(deceasedPillRect, 4f, 4f, paint)

            paint.color = COLOR_RED_DECEASED
            paint.textSize = 8.5f
            paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            canvas.drawText("مرحوم", nextBadgeX + 6f, textCenterY + 3.5f, paint)
            nextBadgeX += 44f
        }

        // Generation Pill (e.g. نسل ۳)
        val genText = "نسل ${toUrduNumerals(member.generation)}"
        paint.textSize = 8f
        val genTextW = paint.measureText(genText)
        val genPillRect = RectF(nextBadgeX, startY + 8f, nextBadgeX + genTextW + 12f, startY + nodeHeight - 8f)
        paint.color = if (item.depth == 0) COLOR_GOLD_LIGHT else COLOR_EMERALD_CONTAINER
        canvas.drawRoundRect(genPillRect, 4f, 4f, paint)

        paint.color = COLOR_EMERALD_DARK
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
        canvas.drawText(genText, nextBadgeX + 6f, textCenterY + 3f, paint)
        nextBadgeX += genTextW + 18f

        // Children count indicator if any
        if (item.hasChildren) {
            paint.color = COLOR_SLATE_MUTED
            paint.textSize = 8.5f
            paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
            val childrenStr = "(${toUrduNumerals(item.childrenCount)} اولاد)"
            canvas.drawText(childrenStr, nextBadgeX, textCenterY + 3f, paint)
        }
    }

    private fun drawPageFrameAndHeader(
        canvas: Canvas,
        width: Float,
        height: Float,
        branch: FamilyMember,
        pageNum: Int
    ) {
        val paint = Paint(Paint.ANTI_ALIAS_FLAG)

        // Background
        paint.color = COLOR_BG
        canvas.drawRect(0f, 0f, width, height, paint)

        // Outer Border
        paint.style = Paint.Style.STROKE
        paint.color = COLOR_EMERALD_PRIMARY
        paint.strokeWidth = 2f
        canvas.drawRect(20f, 20f, width - 20f, height - 20f, paint)

        // Inner Gold Thin Border
        paint.color = COLOR_GOLD_PRIMARY
        paint.strokeWidth = 0.8f
        canvas.drawRect(24f, 24f, width - 24f, height - 24f, paint)
        paint.style = Paint.Style.FILL

        // Top Banner Header
        val headerRect = RectF(30f, 32f, width - 30f, 82f)
        paint.color = COLOR_EMERALD_PRIMARY
        canvas.drawRoundRect(headerRect, 8f, 8f, paint)

        // Header Title
        paint.color = COLOR_WHITE
        paint.textSize = 15f
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        paint.textAlign = Paint.Align.CENTER
        val branchTitle = "شجرہ نسب: شاخ ${branch.name} بن محمد علی"
        canvas.drawText(branchTitle, width / 2f, 58f, paint)

        paint.color = COLOR_GOLD_LIGHT
        paint.textSize = 9.5f
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
        canvas.drawText("تفصیلی نسب نامہ و خاندانی تسلسل", width / 2f, 74f, paint)
    }

    private fun drawPageFooter(
        canvas: Canvas,
        width: Float,
        height: Float,
        pageNum: Int,
        branchName: String
    ) {
        val paint = Paint(Paint.ANTI_ALIAS_FLAG)
        paint.textSize = 9f
        paint.color = COLOR_SLATE_MUTED

        // Right side: Branch info
        paint.textAlign = Paint.Align.RIGHT
        canvas.drawText("شجرہ نسب خاندان محمد علی • شاخ $branchName", width - 35f, height - 30f, paint)

        // Left side: Credits
        paint.textAlign = Paint.Align.LEFT
        canvas.drawText("تخلیق: شمریز ایوب کالس", 35f, height - 30f, paint)

        // Center: Page number
        paint.textAlign = Paint.Align.CENTER
        paint.color = COLOR_EMERALD_DARK
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        canvas.drawText("صفحہ ${toUrduNumerals(pageNum)}", width / 2f, height - 30f, paint)
    }

    private fun drawCornerOrnaments(canvas: Canvas, l: Float, t: Float, r: Float, b: Float, paint: Paint) {
        paint.style = Paint.Style.FILL
        paint.color = COLOR_GOLD_PRIMARY
        val size = 6f
        canvas.drawRect(l, t, l + size, t + size, paint)
        canvas.drawRect(r - size, t, r, t + size, paint)
        canvas.drawRect(l, b - size, l + size, b, paint)
        canvas.drawRect(r - size, b - size, r, b, paint)
    }

    private fun FontWeight(depth: Int): Int {
        return if (depth <= 1) Typeface.BOLD else Typeface.NORMAL
    }

    private fun countDescendants(memberId: Long, allMembers: List<FamilyMember>): Int {
        var count = 0
        fun dfs(id: Long) {
            val children = allMembers.filter { it.fatherId == id }
            count += children.size
            for (c in children) {
                dfs(c.id)
            }
        }
        dfs(memberId)
        return count
    }

    private fun toUrduNumerals(number: Int): String {
        val digits = number.toString()
        val urduDigits = charArrayOf('۰', '۱', '۲', '۳', '۴', '۵', '۶', '۷', '۸', '۹')
        return digits.map { char ->
            if (char in '0'..'9') urduDigits[char - '0'] else char
        }.joinToString("")
    }

    // ==========================================
    // SAVE TO PUBLIC DOWNLOADS FOLDER
    // ==========================================
    fun saveToDownloads(context: Context, sourceFile: File): Uri? {
        val fileName = sourceFile.name
        return try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                val values = ContentValues().apply {
                    put(MediaStore.MediaColumns.DISPLAY_NAME, fileName)
                    put(MediaStore.MediaColumns.MIME_TYPE, "application/pdf")
                    put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS + "/ShajraKalas")
                }
                val resolver = context.contentResolver
                val uri = resolver.insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, values) ?: return null
                resolver.openOutputStream(uri)?.use { out ->
                    sourceFile.inputStream().use { input ->
                        input.copyTo(out)
                    }
                }
                uri
            } else {
                val targetDir = File(
                    Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS),
                    "ShajraKalas"
                ).apply { mkdirs() }
                val destFile = File(targetDir, fileName)
                sourceFile.copyTo(destFile, overwrite = true)
                Uri.fromFile(destFile)
            }
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    // ==========================================
    // INTENT HELPERS
    // ==========================================
    fun createViewIntent(context: Context, pdfFile: File): Intent {
        val uri = FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            pdfFile
        )
        return Intent(Intent.ACTION_VIEW).apply {
            setDataAndType(uri, "application/pdf")
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
    }

    fun createShareIntent(context: Context, pdfFile: File): Intent {
        val uri = FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            pdfFile
        )
        val shareIntent = Intent(Intent.ACTION_SEND).apply {
            type = "application/pdf"
            putExtra(Intent.EXTRA_STREAM, uri)
            putExtra(Intent.EXTRA_SUBJECT, "شجرہ نسب کالس - خاندانی شجرہ")
            putExtra(
                Intent.EXTRA_TEXT,
                "📜 شجرہ نسب کالس (خاندان محمد علی) کا تصویری و دستاویزی پی ڈی ایف شجرہ۔"
            )
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        return Intent.createChooser(shareIntent, "پی ڈی ایف شجرہ شیئر کریں")
    }

    /**
     * Generates a focused single-member lineage chain PDF document.
     * Starts from root ancestor down to the individual member.
     * Matches exact format:
     * 📜 شجرہ نسب برائے [نام]:
     * [جڑ / بزرگ] بن / ولد ... بن / ولد [موجودہ فرد]
     * خاندانی شجرہ نسب (اولاد [جڑ / بزرگ])
     */
    fun generateSpecificLineagePdf(
        context: Context,
        member: FamilyMember,
        lineageChain: List<FamilyMember>,
        rootAncestorName: String = "محمد علی"
    ): PdfExportResult {
        val pageWidth = 595
        val pageHeight = 842
        val document = PdfDocument()

        val pageInfo = PdfDocument.PageInfo.Builder(pageWidth, pageHeight, 1).create()
        val page = document.startPage(pageInfo)
        val canvas = page.canvas

        // Background
        canvas.drawColor(COLOR_BG)

        // Ornamental Islamic Borders
        val borderPaint = Paint().apply {
            color = COLOR_GOLD_PRIMARY
            style = Paint.Style.STROKE
            strokeWidth = 3f
            isAntiAlias = true
        }
        canvas.drawRect(20f, 20f, pageWidth - 20f, pageHeight - 20f, borderPaint)

        val innerBorderPaint = Paint().apply {
            color = COLOR_EMERALD_DARK
            style = Paint.Style.STROKE
            strokeWidth = 1f
            isAntiAlias = true
        }
        canvas.drawRect(26f, 26f, pageWidth - 26f, pageHeight - 26f, innerBorderPaint)

        // Bismillah
        val bismillahPaint = Paint().apply {
            color = COLOR_GOLD_DARK
            textSize = 15f
            textAlign = Paint.Align.CENTER
            typeface = Typeface.create(Typeface.SERIF, Typeface.BOLD)
            isAntiAlias = true
        }
        canvas.drawText("بِسْمِ اللَّهِ الرَّحْمَٰنِ الرَّحِيمِ", pageWidth / 2f, 54f, bismillahPaint)

        // Header Banner
        val bannerRect = RectF(36f, 68f, pageWidth - 36f, 138f)
        val bannerPaint = Paint().apply {
            color = COLOR_EMERALD_DARK
            style = Paint.Style.FILL
            isAntiAlias = true
        }
        canvas.drawRoundRect(bannerRect, 10f, 10f, bannerPaint)

        // Header Accent Strip
        val bannerAccent = Paint().apply {
            color = COLOR_GOLD_PRIMARY
            style = Paint.Style.FILL
            isAntiAlias = true
        }
        canvas.drawRoundRect(RectF(36f, 134f, pageWidth - 36f, 138f), 2f, 2f, bannerAccent)

        // Title: 📜 شجرہ نسب برائے [نام]
        val titlePaint = Paint().apply {
            color = COLOR_GOLD_LIGHT
            textSize = 18f
            textAlign = Paint.Align.CENTER
            typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD)
            isAntiAlias = true
        }
        canvas.drawText("📜 شجرہ نسب برائے ${member.name}", pageWidth / 2f, 98f, titlePaint)

        // Subtitle: خاندانی شجرہ نسب (اولاد [rootAncestorName])
        val subtitlePaint = Paint().apply {
            color = COLOR_WHITE
            textSize = 13f
            textAlign = Paint.Align.CENTER
            typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.NORMAL)
            isAntiAlias = true
        }
        canvas.drawText("خاندانی شجرہ نسب (اولاد $rootAncestorName)", pageWidth / 2f, 122f, subtitlePaint)

        // Lineage Chain Box (Root to Target)
        val chainBoxRect = RectF(36f, 150f, pageWidth - 36f, 230f)
        val chainBoxBg = Paint().apply {
            color = COLOR_GOLD_LIGHT
            style = Paint.Style.FILL
            isAntiAlias = true
        }
        canvas.drawRoundRect(chainBoxRect, 8f, 8f, chainBoxBg)

        val chainBoxBorder = Paint().apply {
            color = COLOR_GOLD_PRIMARY
            style = Paint.Style.STROKE
            strokeWidth = 1.2f
            isAntiAlias = true
        }
        canvas.drawRoundRect(chainBoxRect, 8f, 8f, chainBoxBorder)

        val chainLabelPaint = Paint().apply {
            color = COLOR_EMERALD_DARK
            textSize = 11.5f
            typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD)
            textAlign = Paint.Align.RIGHT
            isAntiAlias = true
        }
        canvas.drawText(":مستند سلسلہ نسب (جڑ سے لے کر فرد تک)", pageWidth - 50f, 172f, chainLabelPaint)

        val fullChainUrdu = lineageChain.joinToString(" بن / ولد ") { it.name }
        val chainTextPaint = Paint().apply {
            color = COLOR_SLATE_DARK
            textSize = 11f
            typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD)
            textAlign = Paint.Align.RIGHT
            isAntiAlias = true
        }
        drawMultiLineRtl(canvas, fullChainUrdu, pageWidth - 50f, 194f, pageWidth - 100f, 16f, chainTextPaint)

        // Timeline Chain Cards
        var currentY = 246f
        val timelineX = pageWidth / 2f
        val cardWidth = 460f
        val count = lineageChain.size.coerceAtLeast(1)
        val availableHeight = (pageHeight - 90f) - currentY
        val cardHeight = (availableHeight / count).coerceIn(34f, 60f)
        val spacing = ((availableHeight - (cardHeight * count)) / count).coerceAtLeast(4f)

        val stepPaint = Paint().apply { isAntiAlias = true }

        for (i in lineageChain.indices) {
            val ancestor = lineageChain[i]
            val isTarget = (i == lineageChain.size - 1)
            val isRoot = (i == 0)

            val left = (pageWidth - cardWidth) / 2f
            val top = currentY
            val right = left + cardWidth
            val bottom = top + cardHeight
            val rect = RectF(left, top, right, bottom)

            // Connecting vertical line
            if (i > 0) {
                stepPaint.color = COLOR_GOLD_PRIMARY
                stepPaint.strokeWidth = 2.5f
                stepPaint.style = Paint.Style.STROKE
                canvas.drawLine(timelineX, top - spacing, timelineX, top, stepPaint)
            }

            // Card background
            stepPaint.style = Paint.Style.FILL
            stepPaint.color = when {
                isTarget -> COLOR_EMERALD_CONTAINER
                isRoot -> COLOR_GOLD_LIGHT
                else -> COLOR_WHITE
            }
            canvas.drawRoundRect(rect, 7f, 7f, stepPaint)

            // Card border
            stepPaint.style = Paint.Style.STROKE
            stepPaint.strokeWidth = if (isTarget || isRoot) 1.8f else 0.8f
            stepPaint.color = when {
                isTarget -> COLOR_EMERALD_PRIMARY
                isRoot -> COLOR_GOLD_DARK
                else -> COLOR_BORDER
            }
            canvas.drawRoundRect(rect, 7f, 7f, stepPaint)

            // Step number badge (Right)
            val badgeX = right - 24f
            val badgeY = top + (cardHeight / 2f)
            stepPaint.style = Paint.Style.FILL
            stepPaint.color = if (isTarget) COLOR_EMERALD_PRIMARY else if (isRoot) COLOR_GOLD_DARK else COLOR_SLATE_MUTED
            canvas.drawCircle(badgeX, badgeY, 12f, stepPaint)

            val badgeTextPaint = Paint().apply {
                color = COLOR_WHITE
                textSize = 9.5f
                typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD)
                textAlign = Paint.Align.CENTER
                isAntiAlias = true
            }
            canvas.drawText("${i + 1}", badgeX, badgeY + 3.5f, badgeTextPaint)

            // Name
            val namePaint = Paint().apply {
                color = if (isTarget) COLOR_EMERALD_DARK else COLOR_SLATE_DARK
                textSize = if (isTarget) 13.5f else 12f
                typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD)
                textAlign = Paint.Align.RIGHT
                isAntiAlias = true
            }
            val titleText = when {
                isRoot -> "${ancestor.name} (جد اعلیٰ / بزرگ خاندان)"
                isTarget -> "⭐ ${ancestor.name} (فرد مطلوب)"
                else -> ancestor.name
            }
            canvas.drawText(titleText, badgeX - 18f, top + (cardHeight * 0.44f), namePaint)

            // Father & generation info
            val subPaint = Paint().apply {
                color = COLOR_SLATE_MUTED
                textSize = 9.5f
                typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.NORMAL)
                textAlign = Paint.Align.RIGHT
                isAntiAlias = true
            }
            val subText = buildString {
                append("نسل ${ancestor.generation}")
                if (!ancestor.fatherName.isNullOrBlank() && !isRoot) {
                    append(" • ولدیت: ${ancestor.fatherName}")
                }
                if (!ancestor.location.isNullOrBlank()) {
                    append(" • ${ancestor.location}")
                }
            }
            canvas.drawText(subText, badgeX - 18f, top + (cardHeight * 0.78f), subPaint)

            currentY += cardHeight + spacing
        }

        // Footer Section
        val footerDate = SimpleDateFormat("dd MMMM yyyy", Locale.getDefault()).format(Date())
        val footerPaint = Paint().apply {
            color = COLOR_SLATE_MUTED
            textSize = 9.5f
            textAlign = Paint.Align.CENTER
            typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.NORMAL)
            isAntiAlias = true
        }
        canvas.drawText(
            "تاریخ اجرا: $footerDate • تصدیق شدہ شجرہ نسب کالس (اولاد $rootAncestorName)",
            pageWidth / 2f,
            pageHeight - 32f,
            footerPaint
        )

        document.finishPage(page)

        val outputDir = File(context.cacheDir, "lineage_pdfs").apply { mkdirs() }
        val sanitizeName = member.name.replace(Regex("[^a-zA-Z0-9\\u0600-\\u06FF]"), "_")
        val outputFile = File(outputDir, "shajra_lineage_${sanitizeName}_${System.currentTimeMillis()}.pdf")

        FileOutputStream(outputFile).use { out ->
            document.writeTo(out)
        }
        document.close()

        val uri = FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            outputFile
        )

        return PdfExportResult(
            file = outputFile,
            uri = uri,
            pageCount = 1,
            fileSizeBytes = outputFile.length(),
            title = "شجرہ نسب برائے ${member.name}"
        )
    }

    private fun drawMultiLineRtl(
        canvas: Canvas,
        text: String,
        startX: Float,
        startY: Float,
        maxWidth: Float,
        lineHeight: Float,
        paint: Paint
    ) {
        val words = text.split(" ")
        var currentLine = ""
        var y = startY
        for (w in words) {
            val testLine = if (currentLine.isEmpty()) w else "$currentLine $w"
            if (paint.measureText(testLine) > maxWidth && currentLine.isNotEmpty()) {
                canvas.drawText(currentLine, startX, y, paint)
                y += lineHeight
                currentLine = w
            } else {
                currentLine = testLine
            }
        }
        if (currentLine.isNotEmpty()) {
            canvas.drawText(currentLine, startX, y, paint)
        }
    }

    private data class BranchMemberItem(
        val member: FamilyMember,
        val depth: Int,
        val hasChildren: Boolean,
        val childrenCount: Int,
        val parentLineage: String
    )
}
