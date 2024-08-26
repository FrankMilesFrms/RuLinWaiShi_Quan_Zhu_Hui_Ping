
import cn.hutool.core.io.FileUtil
import com.sun.tools.javac.code.Kinds.KindSelector.VAL
import jdk.internal.vm.vector.VectorSupport.test
import name.fraser.neil.plaintext.diff_match_patch
import org.jsoup.Jsoup
import java.io.File

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
 * * Date  : 2024/08/24, 下午11:36
 * @author Frms(Frank Miles)
 */
fun main()
{
	val html = HuiPingHtmlLexer(File("C:\\Users\\Frms\\Desktop\\RuLiWaiShi\\hui_ping\\3.html"))

	val res =html.getContent()

	val testPath = "C:\\Users\\Frms\\Desktop\\RuLiWaiShi\\html\\chapter002.html"
	val savePath = "C:\\Users\\Frms\\Desktop\\RuLiWaiShi\\res.docx"
	val htmlLexer = HtmlLexer(File(testPath))

	htmlLexer.bodyLexer { textList->
		val m = MergeText()
		val list = m.merage(textList, res)

		DocxCreator(list, savePath)

	}
}

