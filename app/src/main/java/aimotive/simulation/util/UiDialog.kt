package aimotive.simulation.util

import androidx.annotation.StringRes

sealed class UiDialog(
    open val onDismiss: () -> Unit,
    open val positiveAction: UiDialogAction,
    open val negativeAction: UiDialogAction? = null,
) {

    data class OfResId(
        @StringRes val title: Int,
        @StringRes val message: Int,
        override val onDismiss: () -> Unit,
        override val positiveAction: UiDialogAction,
        override val negativeAction: UiDialogAction? = null,
    ) : UiDialog(onDismiss = onDismiss, positiveAction = positiveAction, negativeAction = negativeAction)

    data class OfText(
        val title: String,
        val message: String,
        override val onDismiss: () -> Unit,
        override val positiveAction: UiDialogAction,
        override val negativeAction: UiDialogAction? = null,
    ) : UiDialog(onDismiss = onDismiss, positiveAction = positiveAction, negativeAction = negativeAction)
}

data class UiDialogAction(
    @StringRes val resId: Int,
    val runnable: () -> Unit,
)
