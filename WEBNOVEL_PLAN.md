# Build a Text Reader (Webnovel) on top of the Manga Architecture

This document outlines the architectural changes required to introduce native Webnovel (text reading) support into Aniyomi by extending the existing Manga architecture, rather than creating a completely separated third pillar.

## User Review Required

> [!WARNING]
> This approach reuses the `Manga` and `Chapter` database models to store Webnovels. Consequently, Webnovels will appear alongside Manga in your library and use the same categories and history tracking. Please confirm if this merging of content types in the UI is acceptable, or if you prefer a strict UI separation while sharing the database engine.

## Open Questions

> [!IMPORTANT]
> 1. **Data Source Integration:** Should we rely on existing Manga extensions to optionally serve text pages, or should we create a specific `TextSource` API for developers to implement Webnovel-only extensions?
> 2. **UI Implementation:** Should the text viewer be built using modern Jetpack Compose (`BasicText` / `LazyColumn`), or should we use a standard Android `WebView` which allows for richer HTML formatting (useful since many webnovel sources provide raw HTML chapters)?

## Proposed Changes

We will introduce a new `TextViewer` alongside the existing `PagerViewer` and `WebtoonViewer`. When a chapter is loaded, the app will detect if the content is text-based and seamlessly switch to the Text Viewer engine.

### 1. Identify Text Content
**Goal:** Allow the app to differentiate between an image chapter and a text chapter.
- **`eu/kanade/tachiyomi/source/model/Page.kt`**: Add a new property (e.g., `textUrl` or `content`) or create a subclass `TextPage` that can hold string/HTML data.
- **`eu/kanade/tachiyomi/data/database/models/manga/Manga.kt`**: Add a `viewer_flags` bit to enforce the Text Reading mode for specific titles.

### 2. The Text Viewer Engine
**Goal:** Create the UI component responsible for rendering text.
- **[NEW] `app/src/main/java/eu/kanade/tachiyomi/ui/reader/viewer/text/TextViewer.kt`**: Implement the `Viewer` interface. This viewer will manage a scrolling text surface. It will intercept `ReaderPage` items and concatenate or paginate their text.
- **[NEW] `app/src/main/java/eu/kanade/tachiyomi/ui/reader/viewer/text/TextConfig.kt`**: Manage typography settings like font size, line spacing, margins, and background colors (Sepia, Dark, Amoled).

### 3. Update the Reader Activity
**Goal:** Route text-based manga to the new viewer.
- **[MODIFY] `eu/kanade/tachiyomi/ui/reader/ReaderActivity.kt`**: Update `ReadingMode.toViewer()` logic to instantiate `TextViewer` when the manga's default reading mode is set to `TEXT` or when the loaded pages are identified as text pages.

### 4. Page Loaders
**Goal:** Ensure the caching and streaming engine can handle text streams instead of just image bitmaps.
- **[MODIFY] `eu/kanade/tachiyomi/ui/reader/loader/HttpPageLoader.kt`**: Allow the loader to download raw text/HTML from the extension and emit it directly to the `ReaderPage` rather than passing it to the ImageDecoder.
- **[MODIFY] `eu/kanade/tachiyomi/ui/reader/loader/EpubPageLoader.kt`**: Currently, this loader only extracts images from EPUBs. Update it to parse and extract chapter text using the `EpubReader` so local EPUBs can be read as text natively.

### 5. Reader Settings UI
**Goal:** Expose text customization to the user.
- **[MODIFY] `eu/kanade/tachiyomi/ui/reader/setting/ReaderSettingsScreen.kt`**: Add a new tab or section for "Typography" that only appears when the `TextViewer` is active.

---

## Verification Plan

### Automated Tests
- Create Unit Tests for the updated `EpubPageLoader` to ensure it extracts HTML/Text accurately instead of just images.
- Test the serialization/deserialization of the new properties added to the `Page` model.

### Manual Verification
1. Install a text-based extension (or load a local text-based EPUB).
2. Open the chapter and verify that `ReaderActivity` instantiates the `TextViewer` instead of the Image Viewers.
3. Verify that text wrapping, scrolling, and font size settings correctly apply in real-time.
4. Ensure image-based Manga still default to the `PagerViewer` and are completely unaffected by this change.
