package com.d4vram.threadsvault.ui.components

import androidx.compose.foundation.text.ClickableText
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import com.d4vram.threadsvault.utils.TextUrlParser

private const val UrlTag = "url"
private const val HashtagTag = "hashtag"
private val HashtagRegex = Regex("""#([\p{L}\p{N}_]{2,40})""")

@Composable
fun LinkifiedText(
    text: String,
    modifier: Modifier = Modifier,
    style: TextStyle,
    color: Color = Color.Unspecified,
    maxLines: Int = Int.MAX_VALUE,
    overflow: TextOverflow = TextOverflow.Clip,
    onHashtagClick: ((String) -> Unit)? = null
) {
    val uriHandler = LocalUriHandler.current
    val linkColor = MaterialTheme.colorScheme.primary
    val hashtagColor = MaterialTheme.colorScheme.secondary

    val annotatedText = remember(text, linkColor, hashtagColor) {
        buildAnnotatedString {
            append(text)

            // URL spans
            TextUrlParser.findUrls(text).forEach { match ->
                addStringAnnotation(
                    tag = UrlTag,
                    annotation = match.url,
                    start = match.start,
                    end = match.endExclusive
                )
                addStyle(
                    style = SpanStyle(color = linkColor, textDecoration = TextDecoration.Underline),
                    start = match.start,
                    end = match.endExclusive
                )
            }

            // Hashtag spans — always styled, clickable only when callback provided
            val urlRanges = TextUrlParser.findUrls(text).map { it.start until it.endExclusive }
            HashtagRegex.findAll(text).forEach { match ->
                // Skip hashtags that fall inside a URL (e.g. #fragment in a link)
                if (urlRanges.any { match.range.first in it }) return@forEach
                val start = match.range.first
                val end = match.range.last + 1
                addStringAnnotation(tag = HashtagTag, annotation = match.groupValues[1], start = start, end = end)
                addStyle(
                    style = SpanStyle(color = hashtagColor, fontWeight = FontWeight.Medium),
                    start = start,
                    end = end
                )
            }
        }
    }

    ClickableText(
        text = annotatedText,
        modifier = modifier,
        style = style.copy(color = color),
        maxLines = maxLines,
        overflow = overflow
    ) { offset ->
        annotatedText.getStringAnnotations(tag = UrlTag, start = offset, end = offset)
            .firstOrNull()
            ?.let { annotation -> runCatching { uriHandler.openUri(annotation.item) } }

        annotatedText.getStringAnnotations(tag = HashtagTag, start = offset, end = offset)
            .firstOrNull()
            ?.let { annotation -> onHashtagClick?.invoke(annotation.item) }
    }
}
