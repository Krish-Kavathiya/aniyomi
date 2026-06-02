package eu.kanade.tachiyomi.ui.reader.viewer.text

import eu.kanade.tachiyomi.ui.reader.setting.ReaderPreferences
import uy.kohesive.injekt.Injekt

/**
 * Small runtime wrapper to expose typography preferences to the TextViewer.
 * Keep minimal to avoid heavy dependencies.
 */
class TextConfig(
    private val preferences: ReaderPreferences = Injekt.get(),
) {

    fun fontPref() = preferences.textFont()

    fun fontSizePref() = preferences.textFontSize()

    fun lineHeightPercentPref() = preferences.textLineHeightPercent()

    fun marginDpPref() = preferences.textMarginDp()

    fun themePref() = preferences.textTheme()
}
