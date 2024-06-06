package aimotive.simulation.ui

import aimotive.simulation.R
import aimotive.simulation.ui.theme.AiMotiveSimulationTheme
import aimotive.simulation.ui.util.AiMotivePreviews
import aimotive.simulation.util.UiMessage
import aimotive.simulation.util.UiMessageAction
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.launch

@Composable
fun SnackbarEffect(
    snackbarHostState: SnackbarHostState,
    messageSharedFlow: SharedFlow<UiMessage>,
) {
    messageSharedFlow.collectAsStateWithLifecycle(null).value?.let { message ->
        val messageText =
            when (message) {
                is UiMessage.OfResId -> stringResource(message.value)
                is UiMessage.OfText -> message.value
            }
        val actionLabel = message.action?.let { action -> stringResource(action.resId) }
        val context = LocalContext.current

        rememberCoroutineScope()
            .launch {
                snackbarHostState.showSnackbar(
                    message = messageText,
                    actionLabel = actionLabel,
                    duration = SnackbarDuration.Long
                ).let { result ->
                    if (result == SnackbarResult.ActionPerformed) {
                        message.action?.runnable?.invoke(context)
                    }
                }
            }
    }
}

/**
 * Note: can be used only with 'Run Preview' on a device.
 */
@AiMotivePreviews
@Composable
private fun SnackbarEffectPreview() {
    AiMotiveSimulationTheme {
        val messageSharedFlow = remember { MutableSharedFlow<UiMessage>() }

        val snackbarHostState = remember { SnackbarHostState() }

        SnackbarEffect(
            snackbarHostState = snackbarHostState,
            messageSharedFlow = messageSharedFlow,
        )

        Scaffold(
            snackbarHost = { SnackbarHost(snackbarHostState) },
        ) { paddingValues ->
            Box(
                modifier = Modifier
                    .padding(paddingValues)
                    .fillMaxSize()
            )
        }

        LaunchedEffect(key1 = Unit) {
            launch {
                messageSharedFlow.emit(
                    UiMessage.OfResId(
                        R.string.location_access_deny_error,
                        UiMessageAction(R.string.try_again) { }
                    )
                )
            }
        }
    }
}
