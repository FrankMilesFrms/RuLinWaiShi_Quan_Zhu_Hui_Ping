import org.apache.poi.ss.usermodel.Color
import org.apache.poi.ss.usermodel.Workbook

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
 * * Date  : 2024/08/26, 下午9:42
 * @author Frms(Frank Miles)
 */
object HuiPingColor
{
	/**
	 * Map color
	 *
	 * 卧闲草堂本评语：#3367D6
	 *
	 * 齐省堂增订本评语：#196B24
	 *
	 * 天目山樵的天一评、天二评：#FE019A
	 *
	 * 黄小田评点：#8B2500
	 *
	 * 则仙评点：#074F6A
	 *
	 * 陈美林评点：#D32C1F
	 *
	 * 平步青评点、童叶庚评批、“从好斋主人”徐允临、潘祖荫等其他评语：#6750A4
	 *
	 * @constructor Create empty Map color
	 */


	val TAG = arrayListOf(
		"卧评" to "3367D6",
		"齐评" to "196B24",
		"天一评" to "FE019A",
		"天二评" to "FE019A",
		"黄评" to "8B2500",
		"则仙评" to "074F6A"
	)
	fun mapColorCorrespondingToReviewer(text: String): String
	{
		for (i in TAG)
		{
			if(text.contains(i.first)) {
				return i.second
			}
		}

		return "6750A4"
	}
}