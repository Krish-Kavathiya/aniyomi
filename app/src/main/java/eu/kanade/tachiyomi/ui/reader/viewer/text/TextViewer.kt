package eu.kanade.tachiyomi.ui.reader.viewer.text

import android.view.KeyEvent
import android.view.MotionEvent
import android.view.View
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import eu.kanade.tachiyomi.ui.reader.ReaderActivity
import eu.kanade.tachiyomi.ui.reader.model.ReaderPage
import eu.kanade.tachiyomi.ui.reader.model.ViewerChapters
import eu.kanade.tachiyomi.ui.reader.setting.ReaderPreferences
import eu.kanade.tachiyomi.ui.reader.viewer.Viewer
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import tachiyomi.presentation.core.util.collectAsState
import uy.kohesive.injekt.Injekt
import uy.kohesive.injekt.api.get

/**
 * TextViewer: applies typography preferences via TextConfig and supports scrolling to pages.
 */
class TextViewer(private val activity: ReaderActivity) : Viewer {

    private val view = ComposeView(activity)
    private var chapters = mutableStateOf<ViewerChapters?>(null)
    private val uiScope = MainScope()

    private val preferences = Injekt.get<ReaderPreferences>()

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
                    onClick = { activity.toggleMenu() },
                )
            }
        }
    }

    override fun moveToPage(page: ReaderPage) {
        val ls = listState.value ?: return
        uiScope.launch { ls.scrollToItem(page.index) }
    }

    override fun handleKeyEvent(event: KeyEvent): Boolean {
        val isUp = event.action == KeyEvent.ACTION_UP
        val isVolumeUp = event.keyCode == KeyEvent.KEYCODE_VOLUME_UP
        val isVolumeDown = event.keyCode == KeyEvent.KEYCODE_VOLUME_DOWN

        if (isVolumeUp || isVolumeDown) {
            val enabled = preferences.readWithVolumeKeys().get()
            if (!enabled || activity.viewModel.state.value.menuVisible) return false

            if (isUp) {
                val inverted = preferences.readWithVolumeKeysInverted().get()
                val prev = if (inverted) isVolumeDown else isVolumeUp

                val ls = listState.value ?: return false
                val currentItem = ls.firstVisibleItemIndex
                val pages = chapters.value?.currChapter?.pages ?: return false
                val target = if (prev) currentItem - 1 else currentItem + 1

                if (target in pages.indices) {
                    uiScope.launch { ls.animateScrollToItem(target) }
                }
            }
            return true
        }
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
    onClick: () -> Unit,
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

    val backgroundColor = when (theme) {
        1 -> Color(0xFFF4ECD8) // Sepia
        2 -> Color(0xFF121212) // Dark
        3 -> Color.Black // Amoled
        else -> Color.White // Light
    }
    val textColor = if (theme >= 2) Color.White else Color.Black

    val fontSizeUnit = fontSizePref.sp
    val lineHeightUnit: TextUnit = (fontSizePref * lineHeightPercent / 100.0).sp

    LazyColumn(
        state = ls,
        modifier = Modifier
            .fillMaxSize()
            .background(backgroundColor)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onClick,
            ),
    ) {
        items(pages) { page ->
            val content = page.textContent ?: "Loading page..."
            Text(
                text = content.parseHtmlToAnnotatedString(),
                style = TextStyle(
                    fontSize = fontSizeUnit,
                    lineHeight = lineHeightUnit,
                    color = textColor,
                ),
                modifier = Modifier.padding(marginDp.dp),
            )
        }
    }
}
