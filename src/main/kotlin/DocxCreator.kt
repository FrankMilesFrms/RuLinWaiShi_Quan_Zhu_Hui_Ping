import org.apache.poi.xwpf.usermodel.*
import java.io.FileOutputStream
/*
 * Copyright (C) 2023 - 2024 Frms, All Rights Reserved.
 * This file is part of RuLiWaiShiHtmlToDocx.
 *
 * RuLiWaiShiHtmlToDocx is free software: 
 * you can redistribute it and/or modify it under the terms of the GNU General Public License as published 
 * by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 *
 * RuLiWaiShiHtmlToDocx is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; 
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with RuLiWaiShiHtmlToDocx.
 * If not, see <https://www.gnu.org/licenses/>.
 */

/**
 *
 * * Email : FrankMiles@qq.com
 * * Date  : 2024/08/26, 下午5:16
 * @author Frms(Frank Miles)
 */


fun createWordDocumentWithFootnote() {
	val document = XWPFDocument()

	// 创建一个段落
	val paragraph = document.createParagraph()
	val run = paragraph.createRun()
	run.setText("这是一段带有脚注的文本。")

	val footerRecord = document.headerFooterPolicy

	val footnote = document.createFootnote()

	footnote.createParagraph().createRun().setText("脚注实例")

	// 将文档保存到文件
	val out = FileOutputStream("C:\\Users\\Frms\\Desktop\\RuLiWaiShi\\example_with_footnote.docx")
	document.write(out)
	out.close()
}

fun main() {
	createWordDocumentWithFootnote()
}