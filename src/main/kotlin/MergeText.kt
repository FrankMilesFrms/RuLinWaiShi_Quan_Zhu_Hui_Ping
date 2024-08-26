import HuiPingColor.mapColorCorrespondingToReviewer
import jdk.internal.org.jline.utils.Colors.s
import name.fraser.neil.plaintext.diff_match_patch
import java.rmi.UnexpectedException
import kotlin.math.min

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
 * 思路：
 *
 * * Email : FrankMiles@qq.com
 * * Date  : 2024/08/25, 下午10:31
 * @author Frms(Frank Miles)
 */
class MergeText
{
	private val diffMatchPath = diff_match_patch()

	private val textCache= StringBuilder()
	// 因为文本是分段存储在List中。保存当前文本段的开始位置在绝对文本的位置，以便于检索任意绝对位置在第几段。
	private val arraySplitPoint = arrayListOf<Int>()

	fun merage(
		noteText: ArrayList<Pair<TextType, Message>>,

		remarkText: ArrayList<Pair<TextType, String>>
	): List<Article>
	{
		initText(noteText)

		var index = 0

		while (index < remarkText.size)
		{
			val appriseValue = remarkText[index]

			if (appriseValue.first == TextType.APPRAISE)
			{
				val nearestText = findNextNearestText(remarkText, index + 1)

				// 没找到说明这是全文最后一段评价
				if (noResult(nearestText))
				{
					noteText.add(
						//findBeforeFirstTextPosition(noteText),
						TextType.APPRAISE to Message(appriseValue.second)
					)
				} else
				{
					val text = nearestText.second
					val position = searchBestMatch(text)
					insertNote(position, noteText, appriseValue.second)
				}
			}
			index++
		}

		return lexerAndCreateStandStream(noteText)
	}

	/**
	 * Lexer and create stand stream
	 *因为列表中文本有动态引用评语，有注释节点混淆不清，所以这里同一整合成深度只有一的文本。
	 * 此外，为了方便接下来生成docx文本，每段也要区分。
	 * @param noteText
	 */
	private fun lexerAndCreateStandStream(noteText: java.util.ArrayList<Pair<TextType, Message>>): List<Article>
	{
		val resultList = arrayListOf<Article>()

		fun saveTempText(
			resultList: ArrayList<Article>, stringBuffer: StringBuilder
		)
		{
			resultList.add(
				Article.Text(stringBuffer.toString())
			)

			stringBuffer.clear()
		}


		noteText.forEach {
			when(it.first)
			{
				TextType.APPRAISE -> {
					val text = it.second.content
					val color = mapColorCorrespondingToReviewer(text)


					val final = wrapperText(text)

					resultList.add(Article.Apprise(color, final))
				}

				TextType.NOTE -> {
					val note = it.second as Note
					resultList.add(
						Article.Note(
							note.title, note.content
						)
					)
				}

				TextType.TEXT -> {
					val message = it.second
					val text = message.content
					val stringBuffer = StringBuilder()

					var index = 0
					while (index < text.length)
					{
						val char = text[index]

						if(char == '\n')
						{
							// 评价不可能换行
							// 不会会有连续的'\n'？这里不做判断。
							saveTempText(resultList, stringBuffer)

						} else if(message.appraiseMap.containsKey(index + 1)) // 向后插入
						{

							if(stringBuffer.isNotEmpty()) {
								saveTempText(resultList, stringBuffer)
							}

							val temp = message.appraiseMap[index + 1]!!
							val final = wrapperText(temp)
							resultList.add(
								Article.Apprise(
									color = mapColorCorrespondingToReviewer(final),
									final
								)
							)

							message.appraiseMap.remove(index + 1)
						} else {
							stringBuffer.append(char)
						}

						index++
					}

					// 不要遗漏结尾
					if(stringBuffer.isNotEmpty()) {
						saveTempText(resultList, stringBuffer)
					}
				}
			}
		}


		return resultList
	}

	/**
	 * Wrapper text
	 *这里只处理一种：打上结尾句号
	 * @param text
	 * @return
	 */
	private fun wrapperText(text: String): String
	{
		if(text.isEmpty()) {
			return ""
		}

		val lastIndex = text.length - 1

		if(text[lastIndex] == '。') {
			return text
		}

		return "$text。"
	}


	sealed class Article
	{
		data class Text(val content: String) : Article()

		data class Note(val noteName: String, val content: String) : Article()

		data class Apprise(val color: String, val content: String) : Article()
	}

	private fun initText(noteText: java.util.ArrayList<Pair<TextType, Message>>)
	{
		arraySplitPoint.clear()
		textCache.clear()

		noteText.forEach {
			arraySplitPoint.add(textCache.length)

			when (it.first)
			{
				TextType.TEXT ->
				{
					if(it.second.content.isEmpty()) {
						throw UnexpectedException("字符段为空")
					}
					textCache.append(it.second.content)
				}
				TextType.NOTE ->
				{
					textCache.append((it.second as Note).title)
				}
				else          ->
				{
					throw UnexpectedException("未知处理类型！$it")
				}
			}
		}

		diffMatchPath.Match_Distance = textCache.length shl 2
		diffMatchPath.Match_Threshold = 0.7f
	}

	/**
	 * Find the closest index binary search
	 *
	 * @param arr
	 * @param x
	 * @return 返回最接近但不超过x的值的下标, 如果没有找到小于等于x的数，则返回0
	 */
	private fun findClosestIndex(arr: ArrayList<Int>, x: Int): Int
	{
		var closestIndex = 0
		var i = 0

		while (i < arr.size)
		{
			if(arr[i] > x) {
				break
			}
			i++
			closestIndex++
		}

		return closestIndex - 1
	}

	/**
	 * Search best match
	 * 尽可能匹配位置
	 * @param text
	 * @param noteText
	 * @return 找不到就放在末尾
	 */
	private fun searchBestMatch(text: String): Int
	{
		val subText = text.substring(0, min(20, text.length-1))
		val matchIndex = diffMatchPath.match_main(
			textCache.toString(), subText, 0
		)

		if(matchIndex == -1)
		{
			System.err.println("检索失败：")
			println(subText)
			println("_________")
			println(textCache)
			println("_________")
			throw UnexpectedException("")

		} else {
			return matchIndex
		}
	}


	private fun insertNote(
		position: Int,
		noteText: java.util.ArrayList<Pair<TextType, Message>>,
		apprise: String)
	{

		val index = findClosestIndex(arraySplitPoint, position)

		val relativePosition = position - arraySplitPoint[index]

		if(relativePosition < 0) {
			throw UnexpectedException(
				"相对位置错误！pos=$position，index=$index，relativePosition=$relativePosition",
				Exception(
					arraySplitPoint.toString()
				)
			)
		}


		val text = noteText[index]

		text.second.append(relativePosition, apprise)

	}

	private fun noResult(nearestText: Pair<TextType, String>): Boolean
	{
		return nearestText.first == TextType.NOTE && nearestText.second.isEmpty()
	}

	/**
	 * Find next nearest text
	 *
	 * @param appriseList
	 * @param index
	 * @return
	 */
	private fun findNextNearestText(
		appriseList: ArrayList<Pair<TextType, String>>, index: Int
	): Pair<TextType, String>
	{
		var i = index
		while (i < appriseList.size)
		{
			if (appriseList[i].first == TextType.TEXT)
			{
				return appriseList[i]
			}
			i++
		}

		return TextType.NOTE to ""
	}

}