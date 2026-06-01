package eu.kanade.tachiyomi.ui.reader.viewer.text

import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.buildAnnotatedString
import androidx.core.text.HtmlCompat

fun String.parseHtmlToAnnotatedString(): AnnotatedString {
    val spanned = HtmlCompat.fromHtml(this, HtmlCompat.FROM_HTML_MODE_COMPACT)
    return buildAnnotatedString {
        append(spanned.toString())
        // A complete implementation would parse styling from spans.
        // For basic text reading, plain extraction is sufficient as MVP.
    }
}
