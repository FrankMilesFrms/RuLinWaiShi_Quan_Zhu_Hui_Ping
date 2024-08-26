import HuiPingColor.mapColorCorrespondingToReviewer
import org.apache.poi.ss.usermodel.Color
import org.jsoup.Jsoup
import org.jsoup.nodes.Element
import org.jsoup.nodes.Node
import org.jsoup.select.Elements
import java.awt.SystemColor.text
import java.io.File
import java.rmi.UnexpectedException

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
 * * Date  : 2024/08/25, 下午4:08
 * @author Frms(Frank Miles)
 */
class HuiPingHtmlLexer(htmlPath: File)
{
	private val file = htmlPath
	private val htmlDocument = Jsoup.parse(htmlPath.readText(), "utf-8")

	private val result = ArrayList<Pair<TextType, String>>()

	/**
	 * Get title
	 *并非html的标题，而是Body内部的标题
	 * @return
	 */
	fun getTitle(): String
	{
		// 筛选所有align="center"且class="style1"的<p>标签
		val elements = htmlDocument
			.body()
			.select("p[align=center].STYLE6, p[align=center].style1")
			.not(".MsoNormal")

		if(elements.size != 1) {
			elements.forEach { element ->
				System.err.println(element)
			}
			throw UnexpectedException(
				"无法命中标题！",
				Exception(
					"file = $file\n命中数=${elements.size}"
				)
			)
		}

		return elements[0].text()
	}

	fun getContent(): ArrayList<Pair<TextType, String>>
	{
		val elements = htmlDocument.body()
		// 后记
		val text = elements.select("p.STYLE2, p.Paragraph")

		lexerText(text)

		return result
	}

	private fun lexerText(text: Elements)
	{
		val p = text.size
		for(i in 0 until p)
		{
			lexerParagraph(text[i])
		}
	}

	private fun lexerParagraph(element: Element)
	{
		fun isBrSkip(it: Node) = it.nameIs("br").not()

		val node = element.childNodes()
		node.forEach {

			if(isBrSkip(it))
			{
				val pair = getTypeAndText(it, node)
				addAndMargeMessage(pair)
			}

		}

	}

	private fun addAndMargeMessage(pair: Pair<TextType, String>)
	{
		fun emptyOrIsNote(p: Pair<TextType, String>): Boolean {
			return p.first == TextType.APPRAISE || result.isEmpty()
		}

		if(emptyOrIsNote(pair)) {
			result.add(pair)
			return
		}

		fun isTextType(p: Pair<TextType, String>) = p.first == TextType.TEXT

		val peek = result.last()

		if (isTextType(peek) && isTextType(pair))
		{
			result.removeAt(result.size -1)

			result.add(
				TextType.TEXT to peek.second + pair.second
			)
			return
		}

		result.add(pair)
	}


	private fun getTypeAndText(it: Node, node: List<Node>) =
		if (it.nameIs("#text"))
		{
			// 正文
			val text = it.toString().clearErroneousNewlines()
			TextType.TEXT to text
		} else if (it.nameIs("font"))
		{    //评注
			TextType.APPRAISE to getContentAsElement(it)
		} else if (it.nameIs("span"))
		{
			TextType.APPRAISE to getContentAsElement(it)
		} else
		{
			throw UnexpectedException("未知标签！！node=${it.nodeName()}, title = ${getTitle()}")
		}

	private fun getContentAsElement(it: Node): String
	{
		val e = it as Element
		return e.ownText()
	}
}

private fun String.clearErroneousNewlines(): String
{
	val first = replace("\n", "")

	return first.replace(" 　　 ", "\n")
		.replace("　　", "\n")
}


