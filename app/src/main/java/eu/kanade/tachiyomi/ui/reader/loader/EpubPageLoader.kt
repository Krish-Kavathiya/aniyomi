package eu.kanade.tachiyomi.ui.reader.loader

import eu.kanade.tachiyomi.source.model.Page
import eu.kanade.tachiyomi.ui.reader.model.ReaderPage
import mihon.core.archive.EpubReader

/**
 * Loader used to load a chapter from a .epub file.
 */
internal class EpubPageLoader(private val reader: EpubReader) : PageLoader() {

    override var isLocal: Boolean = true

    override suspend fun getPages(): List<ReaderPage> {
        val images = reader.getImagesFromPages()
        if (images.isNotEmpty()) {
            return images.mapIndexed { i, path ->
                ReaderPage(i).apply {
                    stream = { reader.getInputStream(path)!! }
                    status = Page.State.READY
                }
            }
        } else {
            val htmlPages = reader.getHtmlPages()
            return htmlPages.mapIndexed { i, path ->
                ReaderPage(i).apply {
                    textContent = reader.getInputStream(path)?.bufferedReader()?.readText()
                    status = Page.State.READY
                }
            }
        }
    }

    override suspend fun loadPage(page: ReaderPage) {
        check(!isRecycled)
    }

    override fun recycle() {
        super.recycle()
        reader.close()
    }
}
