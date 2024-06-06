package aimotive.simulation.util

import android.content.Context
import androidx.annotation.StringRes

sealed class UiMessage(
    open val action: UiMessageAction? = null,
) {

    data class OfResId(
        @StringRes val value: Int,
        override val action: UiMessageAction? = null,
    ) : UiMessage(action)

    data class OfText(
        val value: String,
        override val action: UiMessageAction? = null,
    ) : UiMessage(action)
}

data class UiMessageAction(
    @StringRes val resId: Int,
    val runnable: (Context) -> Unit,
)
