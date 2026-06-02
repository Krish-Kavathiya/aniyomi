Title: Webnovel / Text Viewer — Minimal TODO

Context:
- Core plumbing present: `Page.textContent` exists, `HttpPageLoader`/`EpubPageLoader` populate it, `TextViewer` MVP exists, and `ReaderActivity` auto-switches to `TextViewer` when pages contain `textContent`.
- Remaining work: preferences, UI settings, TextViewer polishing and optional reading-mode mapping.

Goal:
Provide a concise, actionable todo list so another agent/dev can finish Text (Webnovel) reader with minimal back-and-forth and minimal token usage.

Instructions for agent/dev (high level):
1) Implement typography config and persisted prefs
   - File(s): `app/src/main/java/eu/kanade/tachiyomi/ui/reader/viewer/text/TextConfig.kt` (new), edit `app/src/main/java/eu/kanade/tachiyomi/ui/reader/setting/ReaderPreferences.kt` (add keys)
   - Task: define runtime config (font family, fontSize, lineHeight, margins, theme: Light/Sepia/Dark/Amoled) and expose Compose-compatible flows/getters wired to preference store.
   - Output: `TextConfig` reads from `ReaderPreferences` and exposes simple values (e.g., `fontSizeSp: Float`, `lineHeight: Float`, `bgColorInt: Int`).
   - Priority: P0

2) Add Typography settings UI
   - File(s): edit `app/src/main/java/eu/kanade/presentation/reader/settings/ReaderSettingsDialog.kt` (add a tab), add `app/src/main/java/eu/kanade/presentation/reader/settings/TypographyPage.kt` (new composable)
   - Task: UI controls bound to `ReaderSettingsScreenModel` (font selector, font size slider, line spacing slider, theme selector). Show tab only if `screenModel.viewerFlow` indicates a `TextViewer` or show always but disable otherwise.
   - Minimal UX: sliders + dropdowns; updates should write prefs directly and apply instantly.
   - Priority: P0

3) Wire ReaderSettings model
   - File(s): `app/src/main/java/eu/kanade/tachiyomi/ui/reader/setting/ReaderSettingsScreenModel.kt` (edit)
   - Task: expose flows/getters for typography prefs so composables can observe and mutate them.
   - Priority: P1

4) Apply TextConfig in `TextViewer`
   - File(s): `app/src/main/java/eu/kanade/tachiyomi/ui/reader/viewer/text/TextViewer.kt` (edit)
   - Task: consume `TextConfig` values; pass font/spacing/bg to `TextReaderScreen`. Implement `moveToPage()` to scroll LazyColumn to page index (use `LazyListState` and `scrollToItem`/`animateScrollToItem`). Improve input handling (key/motion) to navigate pages.
   - Note: keep implementation MVP/simple; no advanced pagination required.
   - Priority: P0

5) Optional: add `TEXT` ReadingMode mapping
   - File(s): `app/src/main/java/eu/kanade/tachiyomi/ui/reader/setting/ReadingMode.kt` (edit)
   - Task: add enum entry `TEXT` and map in `toViewer()` to `TextViewer` so users can set per-manga default reading mode.
   - Priority: P2 (only if per-manga forcing is desired)

6) Migration and storage safety
   - File(s): add migration under `mihon/core/migration/migrations/` if new persisted keys require defaults.
   - Task: ensure upgrades don't break existing installs; initialize defaults.
   - Priority: P1

7) Tests and verification
   - Tests: add unit tests for `EpubPageLoader` (ensure text extraction) and a small test for `Page` serialization if used.
   - Manual verification steps (minimal):
     1. Install a text source or open a local .epub with text chapters.
     2. Open chapter; confirm `ReaderActivity` instantiates `TextViewer`.
     3. Open settings dialog -> Typography tab; change font size and theme; verify changes apply immediately in `TextViewer`.
     4. Test `moveToPage()` by jumping to a page index.

Tips for minimal-token agent usage:
- Use exact file paths and single-shot edits per file (one patch per file).
- Favor short, focused commits; avoid large diffs that require reanalysis.
- For UI work prefer small composables that bind directly to flows from `ReaderSettingsScreenModel`.

Conventions & expectations:
- Keep naming consistent: `TextConfig` (runtime), `ReaderPreferences` keys `text_font`, `text_font_size`, `text_line_height`, `text_margin`, `text_theme`.
- Values must be simple primitives (Int/Float/Enum/string) to avoid heavy serialization.
- Tests should be small and local to the module.

Acceptance criteria (minimal):
- Changing typography settings immediately updates `TextViewer` rendering.
- `moveToPage()` scrolls to correct page index.
- No regressions for image-based readers.

If you want, I can implement step 1 (prefs + `TextConfig`) now.