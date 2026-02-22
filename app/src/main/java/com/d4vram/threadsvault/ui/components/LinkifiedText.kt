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
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import com.d4vram.threadsvault.utils.TextUrlParser

private const val UrlTag = "url"

@Composable
fun LinkifiedText(
    text: String,
    modifier: Modifier = Modifier,
    style: TextStyle,
    color: Color = Color.Unspecified,
    maxLines: Int = Int.MAX_VALUE,
    overflow: TextOverflow = TextOverflow.Clip
) {
    val uriHandler = LocalUriHandler.current
    val linkColor = MaterialTheme.colorScheme.primary

    val annotatedText = remember(text, linkColor) {
        buildAnnotatedString {
            append(text)
            TextUrlParser.findUrls(text).forEach { match ->
                addStringAnnotation(
                    tag = UrlTag,
                    annotation = match.url,
                    start = match.start,
                    end = match.endExclusive
                )
                addStyle(
                    style = SpanStyle(
                        color = linkColor,
                        textDecoration = TextDecoration.Underline
                    ),
                    start = match.start,
                    end = match.endExclusive
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
            ?.let { annotation ->
                runCatching { uriHandler.openUri(annotation.item) }
            }
    }
}
