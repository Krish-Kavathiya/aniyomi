package eu.kanade.tachiyomi.ui.reader.loader

import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.runBlocking
import mihon.core.archive.EpubReader
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import java.io.ByteArrayInputStream

class EpubPageLoaderTest {

    private val reader = mockk<EpubReader>(relaxed = true)
    private val loader = EpubPageLoader(reader)

    @Test
    fun `getPages should populate textContent when no images are found`() = runBlocking {
        every { reader.getImagesFromPages() } returns emptyList()
        every { reader.getHtmlPages() } returns listOf("page1.html", "page2.html")
        every { reader.getInputStream("page1.html") } returns ByteArrayInputStream("<html><body>Page 1 content</body></html>".toByteArray())
        every { reader.getInputStream("page2.html") } returns ByteArrayInputStream("<html><body>Page 2 content</body></html>".toByteArray())

        val pages = loader.getPages()

        assertEquals(2, pages.size)
        assertEquals("<html><body>Page 1 content</body></html>", pages[0].textContent)
        assertEquals("<html><body>Page 2 content</body></html>", pages[1].textContent)
    }

    @Test
    fun `getPages should populate stream when images are found`() = runBlocking {
        every { reader.getImagesFromPages() } returns listOf("image1.jpg")

        val pages = loader.getPages()

        assertEquals(1, pages.size)
        assertEquals(null, pages[0].textContent)
    }
}
