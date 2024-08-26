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
import org.jsoup.Jsoup
import org.jsoup.nodes.Element
import org.jsoup.nodes.Node
import java.io.File
import java.lang.Exception
import java.rmi.UnexpectedException
import java.util.Stack


/**
 *
 * * Email : FrankMiles@qq.com
 * * Date  : 2024/08/24, 下午11:40
 * @author Frms(Frank Miles)
 */
class HtmlLexer(htmlPath: File)
{
	private val htmlDocument = Jsoup.parse(htmlPath.readText(), "utf-8")

	fun getTitle(): String
	{
		return htmlDocument.title()
	}

	fun bodyLexer(
		lexer: (ArrayList<Pair<TextType, Message>>, HashMap<String, Note>) -> Unit
	)
	{
		val body = htmlDocument.body()

		val textList = ArrayList<Pair<TextType, Message>>()

		val getHead1 = body.getElementsByTag("h1")[0]
		lexerText(getHead1, textList)

		val allParagraphs = body.getElementsByTag("p")
		val notesMap = HashMap<String, Note>()

		allParagraphs.forEach { paragraph ->

			if(isNotes(paragraph))
			{
				val note = lexerNotes(paragraph)
				notesMap[note.title] = note
			} else
			{
				lexerText(paragraph, textList)
			}
		}

		val res = mergeTextList(textList)
		lexer(res, notesMap)
	}

	private fun mergeTextList(textList: java.util.ArrayList<Pair<TextType, Message>>): ArrayList<Pair<TextType, Message>>
	{
		val stack = Stack<Pair<TextType, Message>>()
		for (pair in textList)
		{
			if(stack.isEmpty()) {
				stack.push(pair)
				continue
			}

			val peek = stack.peek()

			if(peek.first == pair.first && peek.first != TextType.NOTE) {
				val pop = stack.pop()

				stack.push(
					pop.first to
					Message(
						pop.second.content + pair.second.content
					)
				)
			}else {
				stack.push(pair)
			}
		}
		val res = ArrayList<Pair<TextType, Message>>()
		for (text in stack)
		{
			res.add(text)
		}
		return res
	}



	/**
	 * Lexer text
	 *
	 *例如：
	 * ``` html
	 * <p>正文 <a id="w32"></a><a href="../Text/chapter002.html#m32"><sup>[32]</sup></a> 其他正文</p>```
	 *
	 * @param paragraph
	 */
	private fun lexerText(paragraph: Element, array: ArrayList<Pair<TextType, Message>>)
	{
		if(isImageLabel(paragraph)) {
			return
		}

		val nodes = paragraph.childNodes()
		var index = 0

		while (index < nodes.size)
		{
			val node = nodes[index]

			if(node.nameIs("#text")) {

				array.add(
					TextType.TEXT to Message(
						content = node.toString()
					)
				)

				index++
			} else if(isAnchors(node))
			{
				// 以id为第一导向，依赖性地得到href
				if(node.attr("id").isNotEmpty())
				{
					val id = node.attr("id")
					index++

					val hrefNode = nodes[index]

					if(isAnchors(hrefNode))
					{
						if(hrefNode.attr("href").isNotEmpty())
						{
							val href = hrefNode.attr("href")

							array.add(
								TextType.NOTE to Note(
										id = id,
										href = href,
										title = getSupInAnchors(hrefNode, noSup = false),
										content = ""
								)
							)

							index++
						} else {
							throw UnexpectedException(
								"anchors标签class错误！value=$node",
								Exception("title = "+ getTitle())
							)
						}

					} else {
						throw UnexpectedException(
							"并非anchors标签！name=${hrefNode.nodeName()}, value=$hrefNode",
							Exception("title = "+ getTitle())
						)
					}


				}
			} else if(isImageInlineNode(node))
			{

//				println("跳过中间图片")
//				println("title = "+ getTitle())
//				println("value = $node")
//				println("---------------")
				index++
			} else
			{
				throw UnexpectedException("无法解析的Node！name = ${node.nodeName()}, value = $node", Exception("title = "+ getTitle()))
			}
		}

		array.add(
			TextType.TEXT to Message("\n")
		)
	}

	private fun isImageInlineNode(node: Node): Boolean
	{
		if (node.nameIs("img").not()) {
			return false
		}

		val element = node as Element
		return element.className() == "inline"
	}


	private fun isAnchors(node: Node): Boolean
	{
		return node.nameIs("a")
	}

	private fun isImageLabel(paragraph: Element): Boolean
	{
		if (paragraph.className().isEmpty()) {
			return false
		}

		when(paragraph.className())
		{
			"center", "img" -> return true
			else -> throw UnexpectedException("isImageLabel: 不是图片类。$paragraph")
		}
	}

	private fun isNotes(paragraph: Element): Boolean
	{
		return paragraph.className() == "note"
	}

	private fun lexerNotes(paragraph: Element): Note
	{
		// a 标签按格式，规定有且仅有两个，且第一个为ID，第二个为href
		val anchors = paragraph.getElementsByTag("a")
		val list = anchors.toList()

		if(list.size != 2)
		{
			throw UnexpectedException("注释标签错误！！", Exception(paragraph.html()))
		}

		val note = Note(
			id = list[0].attr("id"),
			href = list[1].attr("href"),
			title = getSupInAnchors(list[1], noSup = true),
			content = paragraph.ownText()
		)

		return note
	}

	/**
	 * Get sup in anchors
	 * 只会解析一种，a内添加任何其他标签均报错（纯文内容除外本）：
	 * ```html
	 * <a>
	 *     <sup> 文本 </sup>
	 * </a>
	 * ```
	 * @param node
	 * @return
	 */
	private fun getSupInAnchors(node: Node, noSup: Boolean): String
	{
		if(isAnchors(node).not()) {
			throw UnexpectedException("并非Anchors")
		}
		val element = node as Element

		if(element.childrenSize() != 1)
		{
			if((noSup && element.childrenSize() == 0).not()) {

				throw UnexpectedException("子标签有${element.childrenSize()}个！！")
			}
		}

		if(noSup.not())
		{
			val sup = element.getElementsByTag("sup")
			return sup.text()
		} else
		{
			return element.ownText()
		}

	}
}
	open class Message(
		open val content: String
	) {
		private val appraiseMap = hashMapOf<Int, String>()

		override fun toString(): String
		{
			val str = StringBuilder()
			var index = 0
			while (index < content.length)
			{
				str.append(content[index])
				index++
				if(appraiseMap.containsKey(index)) {
					str.append(appraiseMap[index]!!)
					appraiseMap.remove(index)
				}


			}

			if(appraiseMap.isNotEmpty())
			{
				str.append("---未知map位置---")
				str.append(appraiseMap.toString())
			}
			return str.toString()
		}

		fun append(position: Int, appraise: String)
		{
			if(appraiseMap.containsKey(position).not())
			{
				appraiseMap[position] = "【$appraise】"
			} else {
				appraiseMap[position] += " $appraise"
			}
		}

		fun getAppraise(): Map<Int, String> = appraiseMap

		fun isEmptyAppraise(): Boolean = appraiseMap.isEmpty()
	}
	data class Note(
		val id: String,
		val href: String,
		val title: String,
		override val content: String
	): Message(
		content = content
	) {
		override fun toString(): String
		{
			return "id=$id, href=$href, title=$title, content=${super.toString()}"
		}
	}


enum class TextType
{
	TEXT, NOTE, APPRAISE
}
