package aimotive.simulation.ui

import aimotive.simulation.R
import aimotive.simulation.ui.theme.AiMotiveSimulationTheme
import aimotive.simulation.ui.util.AiMotivePreviews
import aimotive.simulation.util.UiDialog
import aimotive.simulation.util.UiDialogAction
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

@Composable
fun AlertDialog(
    dialogStateFlow: StateFlow<UiDialog?>,
) {
    dialogStateFlow.collectAsStateWithLifecycle(null).value?.let { dialog ->
        val titleText =
            when (dialog) {
                is UiDialog.OfResId -> stringResource(dialog.title)
                is UiDialog.OfText -> dialog.title
            }
        val messageText =
            when (dialog) {
                is UiDialog.OfResId -> stringResource(dialog.message)
                is UiDialog.OfText -> dialog.message
            }

        AlertDialog(
            title = titleText,
            message = messageText,
            onDismiss = dialog.onDismiss,
            negativeAction = dialog.negativeAction,
            positiveAction = dialog.positiveAction,
        )
    }
}

@Composable
private fun AlertDialog(
    title: String,
    message: String,
    onDismiss: () -> Unit,
    negativeAction: UiDialogAction?,
    positiveAction: UiDialogAction,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        dismissButton =
        if (negativeAction == null) {
            null
        } else {
            {
                TextButton(
                    onClick = {
                        onDismiss()
                        negativeAction.runnable()
                    },
                ) {
                    Text(
                        text = stringResource(negativeAction.resId),
                    )
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    onDismiss()
                    positiveAction.runnable.invoke()
                },
            ) {
                Text(
                    text = stringResource(positiveAction.resId),
                )
            }
        },
        title = {
            Text(
                text = title,
                textAlign = TextAlign.Start,
            )
        },
        text = {
            Text(
                text = message,
                textAlign = TextAlign.Start,
            )
        },
    )
}

@AiMotivePreviews
@Composable
private fun AlertDialogPreview() {
    AiMotiveSimulationTheme {
        val dialogFlow = remember { MutableStateFlow<UiDialog?>(null) }

        LaunchedEffect(key1 = Unit) {
            dialogFlow.value =
                UiDialog.OfResId(
                    title = R.string.location_access_rationale_title,
                    message = R.string.location_access_rationale,
                    onDismiss = { dialogFlow.value = null },
                    positiveAction = UiDialogAction(R.string.agree) { dialogFlow.value = null },
                    negativeAction = UiDialogAction(R.string.cancel) { dialogFlow.value = null },
                )
        }

        AlertDialog(
            dialogStateFlow = dialogFlow.asStateFlow(),
        )
    }
}
