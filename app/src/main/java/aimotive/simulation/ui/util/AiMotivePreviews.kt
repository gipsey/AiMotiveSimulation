package aimotive.simulation.ui.util

import android.content.res.Configuration
import androidx.compose.ui.tooling.preview.Preview

@Preview(
    fontScale = 1f,
    showBackground = true,
    backgroundColor = BACKGROUND_LIGHT,
    uiMode = Configuration.UI_MODE_NIGHT_NO,
    locale = "hu",
)
@Preview(
    fontScale = 1.5f,
    showBackground = true,
    backgroundColor = BACKGROUND_DARK,
    uiMode = Configuration.UI_MODE_NIGHT_YES,
    locale = "en",
)
annotation class AiMotivePreviews

private const val BACKGROUND_LIGHT = 0xFFECEFF6
private const val BACKGROUND_DARK = 0xFF2A2B2E
