package eu.kanade.tachiyomi.ui.reader.viewer.text

import android.view.KeyEvent
import android.view.MotionEvent
import android.view.View
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.foundation.layout.padding
import tachiyomi.presentation.core.util.collectAsState
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.sp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import eu.kanade.tachiyomi.ui.reader.ReaderActivity
import eu.kanade.tachiyomi.ui.reader.model.ReaderPage
import eu.kanade.tachiyomi.ui.reader.model.ViewerChapters
import eu.kanade.tachiyomi.ui.reader.viewer.Viewer

/**
 * TextViewer: applies typography preferences via TextConfig and supports scrolling to pages.
 */

class TextViewer(private val activity: ReaderActivity) : Viewer {

    private val view = ComposeView(activity)
    private var chapters = mutableStateOf<ViewerChapters?>(null)
    private val uiScope = MainScope()

    // Exposed lazy list state from composition so moveToPage can scroll
    private val listState = mutableStateOf<LazyListState?>(null)

    private val textConfig = TextConfig()

    override fun getView(): View = view

    override fun destroy() {
        view.disposeComposition()
        uiScope.cancel()
    }

    override fun setChapters(chapters: ViewerChapters) {
        this.chapters.value = chapters
        view.setContent {
            val currChapters = this.chapters.value
            if (currChapters != null) {
                TextReaderScreen(
                    pages = currChapters.currChapter.pages ?: emptyList(),
                    textConfig = textConfig,
                    listStateHolder = listState,
                )
            }
        }
    }

    override fun moveToPage(page: ReaderPage) {
        val ls = listState.value ?: return
        uiScope.launch { ls.animateScrollToItem(page.index) }
    }

    override fun handleKeyEvent(event: KeyEvent): Boolean {
        return false
    }

    override fun handleGenericMotionEvent(event: MotionEvent): Boolean {
        return false
    }
}

@Composable
fun TextReaderScreen(
    pages: List<ReaderPage>,
    textConfig: TextConfig,
    listStateHolder: androidx.compose.runtime.MutableState<LazyListState?>,
) {
    val ls = rememberLazyListState()
    DisposableEffect(ls) {
        listStateHolder.value = ls
        onDispose { listStateHolder.value = null }
    }

    val fontPref by textConfig.fontPref().collectAsState()
    val fontSizePref by textConfig.fontSizePref().collectAsState()
    val lineHeightPercent by textConfig.lineHeightPercentPref().collectAsState()
    val marginDp by textConfig.marginDpPref().collectAsState()
    val theme by textConfig.themePref().collectAsState()

    val fontSizeUnit = fontSizePref.sp
    val lineHeightUnit: TextUnit = (fontSizePref * lineHeightPercent / 100.0).sp

    LazyColumn(state = ls, modifier = Modifier.fillMaxSize()) {
        items(pages) { page ->
            val content = page.textContent ?: "Loading page..."
            Text(
                text = content.parseHtmlToAnnotatedString(),
                style = TextStyle(
                    fontSize = fontSizeUnit,
                    lineHeight = lineHeightUnit,
                ),
                modifier = Modifier.padding(marginDp.dp),
            )
        }
    }
}
