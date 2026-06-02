package eu.kanade.presentation.reader.settings

import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import eu.kanade.tachiyomi.ui.reader.setting.ReaderPreferences
import eu.kanade.tachiyomi.ui.reader.setting.ReaderSettingsScreenModel
import tachiyomi.i18n.MR
import tachiyomi.presentation.core.components.HeadingItem
import tachiyomi.presentation.core.components.SettingsChipRow
import tachiyomi.presentation.core.components.SliderItem
import tachiyomi.presentation.core.i18n.stringResource
import tachiyomi.presentation.core.util.collectAsState

@Composable
internal fun ColumnScope.TypographyPage(screenModel: ReaderSettingsScreenModel) {
    HeadingItem(MR.strings.player_sheets_sub_typography_title)

    // Font family selection (minimal set)
    val fontPref by screenModel.preferences.textFont().collectAsState()
    SettingsChipRow(MR.strings.player_sheets_sub_typography_font) {
        listOf("sans-serif", "serif", "monospace").map { font ->
            FilterChip(
                selected = fontPref == font,
                onClick = { screenModel.preferences.textFont().set(font) },
                label = { Text(font) },
            )
        }
    }

    // Font size
    val fontSize by screenModel.preferences.textFontSize().collectAsState()
    SliderItem(
        label = stringResource(MR.strings.player_sheets_sub_typography_font_size),
        value = fontSize,
        valueText = "$fontSize",
        onChange = { screenModel.preferences.textFontSize().set(it) },
        max = 40,
        min = 10,
    )

    // Line height (percent)
    val lineHeight by screenModel.preferences.textLineHeightPercent().collectAsState()
    SliderItem(
        label = "Line height",
        value = lineHeight,
        valueText = "$lineHeight%",
        onChange = { screenModel.preferences.textLineHeightPercent().set(it) },
        max = 200,
        min = 100,
    )

    // Margin
    val margin by screenModel.preferences.textMarginDp().collectAsState()
    SliderItem(
        label = stringResource(MR.strings.player_sheets_sub_typography_border_size),
        value = margin,
        valueText = "$margin dp",
        onChange = { screenModel.preferences.textMarginDp().set(it) },
        max = 48,
        min = 0,
    )

    // Theme selection
    val theme by screenModel.preferences.textTheme().collectAsState()
    SettingsChipRow(MR.strings.player_sheets_sub_typography_title) {
        listOf(
            0 to "Light",
            1 to "Sepia",
            2 to "Dark",
            3 to "Amoled",
        ).map { (value, label) ->
            FilterChip(
                selected = theme == value,
                onClick = { screenModel.preferences.textTheme().set(value) },
                label = { Text(label) },
            )
        }
    }
}
