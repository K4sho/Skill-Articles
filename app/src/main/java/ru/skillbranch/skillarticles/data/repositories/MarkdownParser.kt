package ru.skillbranch.skillarticles.ui.custom.markdown

import android.util.Log
import java.util.regex.Pattern

object MarkdownParser {
    private val LINE_SEPARATOR = System.getProperty("line.separator") ?: "\n"

    private const val UNORDERED_LIST_ITEM_GROUP = "(^[*+-] .+)"
    private const val HEADER_GROUP = "(^#{1,6} .+?$)"
    private const val QUOTE_GROUP = "(^> .+?$)"
    private const val ITALIC_GROUP = "((?<!\\*)\\*[^*].*?[^*]?\\*(?!\\*)|(?<!_)_[^_].*?[^_]?_(?!_))"
    private const val BOLD_GROUP =
        "((?<!\\*)\\*{2}[^*].*?[^*]?\\*{2}(?!\\*)|(?<!_)_{2}[^_].*?[^_]?_{2}(?!_))"
    private const val STRIKE_GROUP = "((?<!~)~{2}[^~].*?~{2}(?!~))"
    private const val RULE_GROUP = "(^[-_*]{3}$)"
    private const val INLINE_GROUP = "((?<!`)`[^`\\s].*?[`\\s]?`(?!`))"
    private const val LINK_GROUP = "(\\[[^\\[\\]]*?]\\(.+?\\)|^\\[*?]\\(.*?\\))"
    private const val BLOCK_CODE_GROUP = "((?<!`)`{3}[^` ][\\s\\S]*?[^`]?`{3}(?![^`\n]))"
    private const val ORDERED_LIST_ITEM_GROUP = "(^\\d{1,2}\\. .+$)"//"(^\\d{1,2}\\. \\s.+?$)"
    private const val IMAGE_GROUP = "(!\\[[^\\[\\]]*?\\]\\(.*?\\))"

    private const val MARKDOWN_GROUPS =
        "$UNORDERED_LIST_ITEM_GROUP|$HEADER_GROUP|$QUOTE_GROUP|$ITALIC_GROUP" +
                "|$BOLD_GROUP|$STRIKE_GROUP|$RULE_GROUP|$INLINE_GROUP|$LINK_GROUP|$BLOCK_CODE_GROUP" +
                "|$ORDERED_LIST_ITEM_GROUP|$IMAGE_GROUP"

    private val elementsPattern by lazy { Pattern.compile(MARKDOWN_GROUPS, Pattern.MULTILINE) }

    /**
     * Парсит маркдаун текст на элементы
     */
    fun parse(string: String): MarkdownText {
        val elements = mutableListOf<Element>()
        elements.addAll(findElements(string))

        return MarkdownText(elements)
    }

    /**
     * Очищает маркдаун разметку, оставляя лишь чистый текст
     */
    fun clear(string: String?): String? {
        if (string == null) return null
        val markdown = parse(string)
        return StringBuilder().apply {
            markdown.elements.forEach {
                if (it.elements.isNotEmpty())
                    clear(it.text.toString(), this)
                else
                    apply { append(it.text) }
            }
        }.toString()
    }

    private fun clear(string: String, builder: StringBuilder) {
        val markdown = parse(string)
        builder.apply {
            markdown.elements.forEach {
                if (it.elements.isNotEmpty())
                    clear(it.text.toString(), this)
                else
                    apply { append(it.text) }
            }
        }
    }

    fun findElements(string: CharSequence): List<Element> {
        val parents = mutableListOf<Element>()
        val matcher = elementsPattern.matcher(string)
        var lastStartIndex = 0

        loop@ while (matcher.find(lastStartIndex)) {
            val startIndex = matcher.start()
            val endIndex = matcher.end()

            // if something is found then everything before - TEXT
            if (lastStartIndex < startIndex) {
                parents.add(Element.Text(string.subSequence(lastStartIndex, startIndex)))
            }

            // found text
            var text: CharSequence

            // groups range for iterate by groups
            val groups = 1..12
            var group = -1
            for (gr in groups) {
                if (matcher.group(gr) != null) {
                    group = gr
                    break
                }
            }

            when (group) {
                // NOT FOUND -> BREAK
                -1 -> break@loop

                // UNORDERED LIST
                1 -> {
                    // text without "*. "
                    text = string.subSequence(startIndex.plus(2), endIndex)
                    // find inner elements
                    val subs = findElements(text)
                    val element = Element.UnorderedListItem(text, subs)
                    parents.add(element)

                    // next find start from "endIndex"(last regex character)
                    lastStartIndex = endIndex
                }

                // HEADER
                2 -> {
                    val reg = "^#{1,6}".toRegex().find(string.subSequence(startIndex, endIndex))
                    val level = reg!!.value.length

                    // text without "{#} "
                    text = string.subSequence(startIndex.plus(level.inc()), endIndex)

                    val element = Element.Header(level, text)
                    parents.add(element)
                    lastStartIndex = endIndex
                }
                // QUOTE
                3 -> {
                    // text without "> "
                    text = string.subSequence(startIndex.plus(2), endIndex)
                    val subelements = findElements(text)
                    val element = Element.Quote(text, subelements)
                    parents.add(element)
                    lastStartIndex = endIndex
                }
                // ITALIC
                4 -> {
                    // text without "*{}*"
                    text = string.subSequence(startIndex.inc(), endIndex.dec())
                    val subelements = findElements(text)
                    val element = Element.Italic(text, subelements)
                    parents.add(element)
                    lastStartIndex = endIndex
                }
                // BOLD
                5 -> {
                    text = string.subSequence(startIndex.plus(2), endIndex.plus(-2))

                    val sub = findElements(text)
                    val element = Element.Bold(text, sub)

                    parents.add(element)
                    lastStartIndex = endIndex
                }
                // STRIKE
                6 -> {
                    text = string.subSequence(startIndex.plus(2), endIndex.minus(2))

                    val sub = findElements(text)
                    val element = Element.Strike(text, sub)

                    parents.add(element)
                    lastStartIndex = endIndex
                }
                // RULE
                7 -> {
                    val element = Element.Rule()
                    parents.add(element)
                    lastStartIndex = endIndex
                }
                // INLINE CODE
                8 -> {
                    text = string.subSequence(startIndex.inc(), endIndex.dec())

                    val element = Element.InlineCode(text)

                    parents.add(element)
                    lastStartIndex = endIndex
                }
                // LINK
                9 -> {
                    text = string.subSequence(startIndex, endIndex)
                    val (title: String, link: String) = "\\[(.*)]\\((.*)\\)".toRegex()
                        .find(text)!!.destructured
                    val element = Element.Link(link, title)
                    parents.add(element)
                    lastStartIndex = endIndex
                }
                // Block Code
                10 -> {
                    text = string.subSequence(startIndex.plus(3), endIndex.plus(-3))
                    val element = Element.BlockCode(text = text)
                    parents.add(element)
                    lastStartIndex = endIndex
                }
                // ORDERED LIST
                11 -> {
                    // text without "1. "
                    val reg =
                        "(^\\d{1,2}.)".toRegex().find(string.subSequence(startIndex, endIndex))
                    if (reg != null) {
                        val order = reg!!.value

                        text = string.subSequence(startIndex.plus(order.length.inc()), endIndex)
                            .toString()

                        // find inner elements
                        val subs = findElements(text)
                        val element = Element.OrderedListItem(order, text.toString(), subs)
                        parents.add(element)
                    }

                    // next find start from position "endIndex" (last regex character)
                    lastStartIndex = endIndex
                }
                // IMAGE
                12 -> {
                    // text without "![]()". Тут нужно найти все символы регулярки этой
                    text = string.subSequence(startIndex, endIndex)
                    val (alt: String, link: String) = "(?:!\\[(.*?)\\]\\((.*?)\\))".toRegex()
                        .find(text)!!.destructured
                    val sepLinkAndDesc = link.split('"')
                    // Условия 2-х вложенных if можно объединить.
                    //
                    //Если условие isNotEmpty не сработает, то есть строка будет empty,
                    // то второе условие (isNotBlank) будет ложно всегда, и смысла в нем нет.
                    // Возможно подразумевалось &&?
                    val textString =
                        if (sepLinkAndDesc.size > 1 && (sepLinkAndDesc[1].isNotEmpty() || sepLinkAndDesc[1].isNotBlank())) {
                            sepLinkAndDesc.subList(1, sepLinkAndDesc.size)
                                .joinToString(separator = "")
                        } else ""

                    val realAlt = alt.takeIf { it.isNotEmpty() }
                    val element = Element.Image(sepLinkAndDesc.first().trim(), realAlt, textString)
                    parents.add(element)
                    lastStartIndex = endIndex
                }
            }
        }

        if (lastStartIndex < string.length) {
            val text = string.subSequence(lastStartIndex, string.length)
            parents.add(Element.Text(text))
        }

        return parents
    }
}


data class MarkdownText(val elements: List<Element>)

sealed class Element {
    abstract val text: CharSequence
    abstract val elements: List<Element>

    data class Text(
        override val text: CharSequence,
        override val elements: List<Element> = emptyList()
    ) : Element()

    data class UnorderedListItem(
        override val text: CharSequence,
        override val elements: List<Element> = emptyList()
    ) : Element()

    data class Header(
        val level: Int = 1,
        override val text: CharSequence,
        override val elements: List<Element> = emptyList()
    ) : Element()

    data class Quote(
        override val text: CharSequence,
        override val elements: List<Element> = emptyList()
    ) : Element()

    data class Italic(
        override val text: CharSequence,
        override val elements: List<Element> = emptyList()
    ) : Element()

    data class Bold(
        override val text: CharSequence,
        override val elements: List<Element> = emptyList()
    ) : Element()

    data class Strike(
        override val text: CharSequence,
        override val elements: List<Element> = emptyList()
    ) : Element()

    data class Rule(
        override val text: CharSequence = " ",
        override val elements: List<Element> = emptyList()
    ) : Element()

    data class InlineCode(
        override val text: CharSequence,
        override val elements: List<Element> = emptyList()
    ) : Element()

    data class Link(
        val link: String,
        override val text: CharSequence,
        override val elements: List<Element> = emptyList()
    ) : Element()

    data class OrderedListItem(
        val order: String,
        override val text: CharSequence,
        override val elements: List<Element> = emptyList()
    ) : Element()

    data class BlockCode(
        val type: Type = Type.MIDDLE,
        override val text: String,
        override val elements: List<Element> = emptyList()
    ) : Element() {
        enum class Type { START, END, MIDDLE, SINGLE }
    }

    data class Image(
        val url: String,
        val alt: String?,
        override val text: String,
        override val elements: List<Element> = emptyList()
    ) : Element()
}

private fun Element.spread(): List<Element> {
    val elements = mutableListOf<Element>()
    if (this.elements.isNotEmpty()) elements.addAll(this.elements.spread())
    else elements.add(this)
    return elements
}

private fun List<Element>.spread(): List<Element> {
    val elements = mutableListOf<Element>()
    forEach { elements.addAll(it.spread()) }
    return elements
}

sealed class MarkdownElement() {
    abstract val offset: Int
    val bounds: Pair<Int, Int> by lazy {
        when (this) {
            is Text -> {
                val end = elements.fold(offset) { acc, el ->
                    acc + el.spread().map { it.text.length }.sum()
                }
                offset to end
            }
            is Image -> offset to image.text.length + offset
            is Scroll -> offset to blockCode.text.length + offset
            else -> 0 to 0
        }
    }

    data class Text(
        val elements: MutableList<Element>,
        override val offset: Int = 0
    ) : MarkdownElement()

    data class Image(
        val image: Element.Image,
        override val offset: Int = 0
    ) : MarkdownElement()

    data class Scroll(
        val blockCode: Element.BlockCode,
        override val offset: Int = 0
    ) : MarkdownElement()
}

private fun Element.clearContent(): String {
    return StringBuilder().apply {
        val element = this@clearContent
        if (element.elements.isEmpty()) append(element.text)
        else element.elements.forEach { append(it.clearContent()) }
    }.toString()
}

fun MarkdownText.clearContent(): String {
    return StringBuilder().apply {
        elements.forEach {
            if (it.elements.isEmpty()) append(it.text)
            else it.elements.forEach { el -> append(el.clearContent()) }
        }
    }.toString()
}

fun List<MarkdownElement>.clearContent(): String {
    return StringBuilder().apply {
        this@clearContent.forEach {
            when (it) {
                is MarkdownElement.Text -> it.elements.forEach { el -> append(el.clearContent()) }
                is MarkdownElement.Image -> append(it.image.clearContent())
                is MarkdownElement.Scroll -> append(it.blockCode.clearContent())
                else -> {}
            }
        }
    }.toString()
}