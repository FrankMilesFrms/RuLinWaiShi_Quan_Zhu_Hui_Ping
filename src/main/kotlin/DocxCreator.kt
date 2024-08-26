import FootnoteCreator.addFootNote
import FootnoteCreator.saveDocument
import FootnoteCreator.setParagraphTextStyleInfo
import HuiPingColor.mapColorCorrespondingToReviewer
import org.apache.poi.xwpf.usermodel.*
import java.math.BigInteger

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


class DocxCreator(
	textPair: List<MergeText.Article>,
	savePath: String
)
{
	init
	{
		val doc = XWPFDocument()
		var paragraph = doc.createParagraph()

		var bigInteger = BigInteger.ONE

		var index = 0

		while (index < textPair.size)
		{
			when(val article = textPair[index])
			{
				is MergeText.Article.Apprise -> {
					paragraph.setParagraphTextStyleInfo(
						isNew = true,
						colorVal = article.color,
						content = article.content
					)
				}
				is MergeText.Article.Note    -> {
					paragraph.addFootNote(
						xdoc = doc,
						bigInteger,
						noteContent = article.content
					)

					bigInteger = bigInteger.increase()
				}
				is MergeText.Article.Text    -> {
					if(index > 0)
					{
						val pre = textPair[index - 1]
						if(pre is MergeText.Article.Text) {
							paragraph = doc.createParagraph()
						}

					}
					paragraph.setParagraphTextStyleInfo(
						isNew = index > 0,
						content = article.content
					)
				}
			}
			index++
		}
		saveDocument(doc, savePath)
	}

	private fun BigInteger.increase(): BigInteger
	{
		return add(BigInteger.ONE)
	}
}