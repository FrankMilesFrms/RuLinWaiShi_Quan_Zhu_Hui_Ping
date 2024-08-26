import name.fraser.neil.plaintext.diff_match_patch
import java.awt.SystemColor.text
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
	)
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

//					if (appriseValue.second.contains("一句话即见元章自处")) {
//						System.err.println("测试文本位置：$position")
//						System.err.println("检索文本：$text")
//						throw UnexpectedException("")
//					}

					insetNote(position, noteText, appriseValue.second)
				}
			}
			index++
		}
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
	 * Find before first text position
	 *为了解决开头有多个批注而制作。
	 * @param noteText
	 * @return
	 */
	private fun findBeforeFirstTextPosition(noteText: java.util.ArrayList<Pair<TextType, Message>>): Int
	{
		var index = 0
		while (index < noteText.size)
		{
			val value = noteText[index]
			if (value.first == TextType.TEXT)
			{
				return index
			}
			index++
		}
		return index
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


	private fun insetNote(
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

//		System.err.println("插入评价：")
//		System.err.println("插入评价：")
//		System.err.println(apprise)
//		System.err.println("-----")
//		System.err.println(apprise)
//		System.err.println("-----")
//		System.err.println("位置：$relativePosition")
	}

	fun noResult(nearestText: Pair<TextType, String>): Boolean
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

	fun printCache()
	{
		println(textCache)
	}
}