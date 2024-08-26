import org.apache.poi.xwpf.usermodel.XWPFDocument
import org.apache.poi.xwpf.usermodel.XWPFParagraph
import org.apache.poi.xwpf.usermodel.XWPFRun
import org.apache.xmlbeans.impl.xb.xmlschema.SpaceAttribute
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTFtnEdn
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTP
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTRPr
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTVerticalAlignRun
import org.openxmlformats.schemas.wordprocessingml.x2006.main.STFtnEdn
import org.openxmlformats.schemas.wordprocessingml.x2006.main.STHighlightColor
import org.openxmlformats.schemas.wordprocessingml.x2006.main.STUnderline
import org.openxmlformats.schemas.wordprocessingml.x2006.main.STVerticalAlignRun
import java.io.FileOutputStream
import java.math.BigInteger
import kotlin.math.abs

/**
 * 原文链接: http://53873039oycg.iteye.com/blog/2158635
 * 不做代码整洁处理。
 *
 * 注意，一段中必须从第二个脚注开始使setParagraphTextStyleInfo的isNew=true以防止
 * 脚注全部置尾问题。
 * 例子：
 * ```kotlin
 * fun testAddFootnotesToDocument(savePath: String?)
 * {
 * 		val xdoc = XWPFDocument()
 *
 * 		val p2 = xdoc.createParagraph()
 *
 * 		xdoc.createParagraph().apply {
 * 			noteId = noteId.add(BigInteger.ONE)
 * 			setParagraphTextStyleInfo(
 * 				false, "新的测试"
 * 			)
 * 			addFootNote(xdoc, noteId, "新的脚注", colorVal = "FF0000")
 *
 * 			setParagraphTextStyleInfo(
 * 				true, "新的测试"
 * 			)
 * 			noteId = noteId.add(BigInteger.ONE)
 * 			addFootNote(xdoc, noteId, "新的脚注", colorVal = "FF0000")
 * 		}
 *
 * 		saveDocument(xdoc, savePath)
 * 	}
 * ````
 * * Email : FrankMiles@qq.com
 * * Date  : 2024/08/26, 下午5:16
 * modify by Frms(Frank Miles)
 */

object FootnoteCreator
{

	fun XWPFParagraph.addFootNote(
		xdoc: XWPFDocument,
		noteId: BigInteger?,
		noteContent: String?,
		notePrefix: String ="",
		noteSuffix: String ="",
		colorVal: String? = null,
		fontFamily: String = "DeJaVuFZJZ-Frms",
		fontSize: String? = "28",
		isBlod: Boolean = false,
		isItalic: Boolean = false,
		isStrike: Boolean = false,
		isUnderLine: Boolean = false,
		underLineStyle: Int = 0,
		underLineColor: String? = null,
		isHightLight: Boolean = false,
		hightLightValue: Int = 0,
		stRunEnum: STVerticalAlignRun.Enum = STVerticalAlignRun.SUPERSCRIPT,
		noteContentStRunEnum: STVerticalAlignRun.Enum? =null
	)
	{
		var r1 = createRun()
		setRunTextStyleInfo(
			r1, notePrefix, fontFamily, colorVal, fontSize, isBlod, isItalic, isStrike, isUnderLine, underLineStyle,
			underLineColor, isHightLight, hightLightValue, stRunEnum
		)

		setRunTextStyleInfo(
			r1, null, fontFamily, colorVal, fontSize, isBlod, isItalic, isStrike, isUnderLine, underLineStyle,
			underLineColor, isHightLight, hightLightValue, stRunEnum
		)

		val fr = r1.ctr.addNewFootnoteReference()
		fr.id = noteId

		r1 = createRun()
		setRunTextStyleInfo(
			r1, noteSuffix, fontFamily, colorVal, fontSize, isBlod, isItalic, isStrike, isUnderLine, underLineStyle,
			underLineColor, isHightLight, hightLightValue, stRunEnum
		)

		// 默认上，文字处脚注和解释处是一样的，当是，这不方便。所以取消了解释处上标状态。

		val footnotes = xdoc.createFootnotes()
		var ctNote: CTFtnEdn? = null
		var ctp: CTP? = null
		var p2: XWPFParagraph? = null
		ctNote = CTFtnEdn.Factory.newInstance() as CTFtnEdn
		ctNote.id = noteId
		ctNote!!.type = STFtnEdn.NORMAL

		ctp = ctNote.addNewP()
		p2 = XWPFParagraph(ctp, xdoc)
		r1 = p2.createRun()
		setRunTextStyleInfo(
			r1, notePrefix, fontFamily, colorVal, fontSize, isBlod, isItalic, isStrike, isUnderLine, underLineStyle,
			underLineColor, isHightLight, hightLightValue, noteContentStRunEnum
		)

		r1 = p2.createRun()
		setRunTextStyleInfo(
			r1, null, fontFamily, colorVal, fontSize, isBlod, isItalic, isStrike, isUnderLine, underLineStyle,
			underLineColor, isHightLight, hightLightValue, noteContentStRunEnum
		)
		r1.ctr.addNewFootnoteRef()

		r1 = p2.createRun()
		setRunTextStyleInfo(
			r1, noteSuffix, fontFamily, colorVal, fontSize, isBlod, isItalic, isStrike, isUnderLine, underLineStyle,
			underLineColor, isHightLight, hightLightValue, noteContentStRunEnum
		)

		r1 = p2.createRun()
		val ctText = r1.ctr.addNewT()
		ctText.stringValue = " "
		ctText.space = SpaceAttribute.Space.PRESERVE

		r1 = p2.createRun()
		setRunTextStyleInfo(
			r1, noteContent, fontFamily, colorVal, fontSize, isBlod, isItalic, isStrike, isUnderLine, underLineStyle,
			underLineColor, isHightLight, hightLightValue, noteContentStRunEnum
		)
		footnotes.addFootnote(ctNote)
	}

	private fun setRunTextStyleInfo(
		pRun: XWPFRun, content: String?, fontFamily: String?, colorVal: String?, fontSize: String?, isBlod: Boolean,
		isItalic: Boolean, isStrike: Boolean, isUnderLine: Boolean, underLineStyle: Int, underLineColor: String?,
		isHightLight: Boolean, hightLightValue: Int, stRunEnum: STVerticalAlignRun.Enum?
	)
	{
		if (content != null)
		{
			pRun.setText(content)
		}
		var pRpr: CTRPr? = null
		if (pRun.ctr != null)
		{
			pRpr = pRun.ctr.rPr
			if (pRpr == null)
			{
				pRpr = pRun.ctr.addNewRPr()
			}
		}

		if (fontFamily != null)
		{			// 设置字体
			val fonts = if (pRpr!!.isSetRFonts) pRpr.rFonts else pRpr.addNewRFonts()
			fonts.ascii = fontFamily
			fonts.eastAsia = fontFamily
			fonts.hAnsi = fontFamily
		}

		if (fontSize != null)
		{			// 设置字体大小
			val sz = if (pRpr!!.isSetSz) pRpr.sz else pRpr.addNewSz()
			sz.setVal(BigInteger(fontSize))
			val szCs = if (pRpr.isSetSzCs) pRpr.szCs else pRpr.addNewSzCs()
			szCs.setVal(BigInteger(fontSize))
		}

		if (colorVal != null)
		{
			pRun.color = colorVal
		}

		// 设置字体样式
		if (isBlod)
		{
			pRun.isBold = isBlod
		}
		if (isItalic)
		{
			pRun.isItalic = isItalic
		}
		if (isStrike)
		{
			pRun.isStrike = isStrike
		}
		if (colorVal != null)
		{
			pRun.color = colorVal
		}

		// 设置下划线样式
		if (isUnderLine)
		{
			val u = if (pRpr!!.isSetU) pRpr.u else pRpr.addNewU()
			u.setVal(STUnderline.Enum.forInt(abs((underLineStyle % 19).toDouble()).toInt()))
			if (underLineColor != null)
			{
				u.color = underLineColor
			}
		}		// 设置字突出显示文本
		if (isHightLight)
		{
			if (hightLightValue > 0 && hightLightValue < 17)
			{
				val hightLight = if (pRpr!!.isSetHighlight) pRpr.highlight else pRpr.addNewHighlight()
				hightLight.setVal(STHighlightColor.Enum.forInt(hightLightValue))
			}
		}

		if (stRunEnum != null)
		{
			val ctV = CTVerticalAlignRun.Factory.newInstance() as CTVerticalAlignRun
			ctV.setVal(stRunEnum)
			pRpr!!.vertAlign = ctV
		}
	}

	fun XWPFParagraph.setParagraphTextStyleInfo(
		isNew: Boolean,
		content: String?,
		colorVal: String ="000000",
		fontFamily: String = "DeJaVuFZJZ-Frms",
		fontSize: String = "32",
		isBlood: Boolean = false,
		isItalic: Boolean = false,
		isStrike: Boolean = false,
		isUnderLine: Boolean = false,
		underLineStyle: Int = 0,
		underLineColor: String? = null,
		isHightLight: Boolean = false,
		hightLightValue: Int = 0,
		stRunEnum: STVerticalAlignRun.Enum? = null
	)
	{
		var pRun = if (isNew)
		{
			createRun()
		} else
		{
			if (runs != null && runs.size > 0)
			{
				runs[0]
			} else
			{
				createRun()
			}
		}
		if (content != null)
		{
			pRun.setText(content)
		}

		var pRpr: CTRPr? = null
		if (pRun.ctr != null)
		{
			pRpr = pRun.ctr.rPr
			if (pRpr == null)
			{
				pRpr = pRun.ctr.addNewRPr()
			}
		}

		// 设置字体
		val fonts = if (pRpr!!.isSetRFonts) pRpr.rFonts else pRpr.addNewRFonts()
		fonts.ascii = fontFamily
		fonts.eastAsia = fontFamily
		fonts.hAnsi = fontFamily

		// 设置字体大小
		val sz = if (pRpr.isSetSz) pRpr.sz else pRpr.addNewSz()
		sz.setVal(BigInteger(fontSize))

		val szCs = if (pRpr.isSetSzCs) pRpr.szCs else pRpr.addNewSzCs()
		szCs.setVal(BigInteger(fontSize))

		if (colorVal != null)
		{
			pRun.color = colorVal
		}

		// 设置字体样式
		if (isBlood)
		{
			pRun.isBold = isBlood
		}
		if (isItalic)
		{
			pRun.isItalic = isItalic
		}
		if (isStrike)
		{
			pRun.isStrike = isStrike
		}
		if (colorVal != null)
		{
			pRun.color = colorVal
		}

		// 设置下划线样式
		if (isUnderLine)
		{
			val u = if (pRpr.isSetU) pRpr.u else pRpr.addNewU()
			u.setVal(STUnderline.Enum.forInt(abs((underLineStyle % 19).toDouble()).toInt()))
			if (underLineColor != null)
			{
				u.color = underLineColor
			}
		}		// 设置突出显示文本
		if (isHightLight)
		{
			if (hightLightValue > 0 && hightLightValue < 17)
			{
				val hightLight = if (pRpr.isSetHighlight) pRpr.highlight else pRpr.addNewHighlight()
				hightLight.setVal(STHighlightColor.Enum.forInt(hightLightValue))
			}
		}

		if (stRunEnum != null)
		{
			val ctV = CTVerticalAlignRun.Factory.newInstance() as CTVerticalAlignRun
			ctV.setVal(stRunEnum)
			pRpr.vertAlign = ctV
		}
	}

	@Throws(Exception::class)
	fun saveDocument(document: XWPFDocument, savePath: String)
	{
		val fos = FileOutputStream(savePath)
		document.write(fos)
		fos.close()
	}

}