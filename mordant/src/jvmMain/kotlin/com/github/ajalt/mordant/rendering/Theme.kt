package com.github.ajalt.mordant.rendering

import com.github.ajalt.mordant.AnsiColor.brightWhite
import com.github.ajalt.mordant.AnsiColor.gray

interface Theme  {
    val listNumber: TextStyle get() = DEFAULT_STYLE
    val listNumberSeparator: String get() = "."
    val listBullet: TextStyle get() = DEFAULT_STYLE
    val listBulletText: String get() = "•"
    val blockQuote: TextStyle get() = DEFAULT_STYLE
    val horizontalRule: TextStyle get() = DEFAULT_STYLE
    val horizontalRuleTitle: TextStyle get() = DEFAULT_STYLE

    val markdownText: TextStyle get() = DEFAULT_STYLE
    val markdownEmph: TextStyle get() = TextStyle(italic = true)
    val markdownStrong: TextStyle get() = TextStyle(bold = true)
    val markdownStikethrough: TextStyle get() = TextStyle(strikethrough = true)
    val markdownCodeBlock: TextStyle get() = TextStyle(brightWhite, gray)
    val markdownCodeBlockBorder: Boolean get() = true
    val markdownCodeSpan: TextStyle get() = TextStyle(brightWhite, gray)
    val markdownHeaderPadding: Int get() = 1
    val markdownH1: TextStyle get() = TextStyle(bold = true)
    val markdownH2: TextStyle get() = TextStyle(bold = true)
    val markdownH3: TextStyle get() = TextStyle(bold = true, underline = true)
    val markdownH4: TextStyle get() = TextStyle(underline = true)
    val markdownH5: TextStyle get() = TextStyle(italic = true)
    val markdownH6: TextStyle get() = TextStyle(dim = true)

    val markdownH1Rule: String get() = "═"
    val markdownH2Rule: String get() = "─"
    val markdownH3Rule: String get() = " "
    val markdownH4Rule: String get() = " "
    val markdownH5Rule: String get() = " "
    val markdownH6Rule: String get() = " "

    companion object {
        val ASCII = object : Theme {
            override val listBulletText: String get() = "*"
            override val markdownH1Rule: String get() = "="
            override val markdownH2Rule: String get() = "-"
        }
    }
}

internal val DEFAULT_THEME = object : Theme {}