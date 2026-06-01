package eu.kanade.tachiyomi.ui.reader.viewer.text

import android.view.KeyEvent
import android.view.MotionEvent
import android.view.View
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.ComposeView
import eu.kanade.tachiyomi.ui.reader.ReaderActivity
import eu.kanade.tachiyomi.ui.reader.model.ReaderPage
import eu.kanade.tachiyomi.ui.reader.model.ViewerChapters
import eu.kanade.tachiyomi.ui.reader.viewer.Viewer

class TextViewer(private val activity: ReaderActivity) : Viewer {

    private val view = ComposeView(activity)
    private var chapters = mutableStateOf<ViewerChapters?>(null)

    override fun getView(): View = view

    override fun destroy() {
        view.disposeComposition()
    }

    override fun setChapters(chapters: ViewerChapters) {
        this.chapters.value = chapters
        view.setContent {
            val currChapters = this.chapters.value
            if (currChapters != null) {
                TextReaderScreen(pages = currChapters.currChapter.pages ?: emptyList())
            }
        }
    }

    override fun moveToPage(page: ReaderPage) {
        // Not fully implemented for MVP. In a complete implementation,
        // this would scroll the LazyColumn to the specific page index.
    }

    override fun handleKeyEvent(event: KeyEvent): Boolean {
        return false
    }

    override fun handleGenericMotionEvent(event: MotionEvent): Boolean {
        return false
    }
}

@Composable
fun TextReaderScreen(pages: List<ReaderPage>) {
    LazyColumn(modifier = Modifier.fillMaxSize()) {
        items(pages) { page ->
            val content = page.textContent ?: "Loading page..."
            Text(text = content.parseHtmlToAnnotatedString())
        }
    }
}
