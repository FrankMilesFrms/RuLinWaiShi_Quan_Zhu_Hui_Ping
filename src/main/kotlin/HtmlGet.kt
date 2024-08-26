
import cn.hutool.core.io.FileUtil
import org.jsoup.Jsoup
import org.jsoup.nodes.Document

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
 * * Date  : 2024/08/25, 上午11:38
 * @author Frms(Frank Miles)
 */
fun getHtml(url: String) = Jsoup.connect(url).get()

fun main()
{

	for (i in (0..62))
	{
		val html = getHtmlByIndex(i)
		FileUtil.writeString(
			html, "C:\\Users\\Frms\\Desktop\\RuLiWaiShi\\hui_ping\\$i.html", Charsets.UTF_8
		)
	}
}

fun getHtmlByIndex(index: Int): String
{
	val fixIndex = completionIndex(index)
	val html = getHtml("http://www.gsh.yzqz.cn/huipingben/rulinwaishi/mydoc0$fixIndex.htm")
	return html.html()
}

fun completionIndex(index: Int): String
{
	if (index < 10) {
		return "0$index"
	}
	return index.toString()
}
