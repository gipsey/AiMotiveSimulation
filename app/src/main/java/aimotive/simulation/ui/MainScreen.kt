package aimotive.simulation.ui

import aimotive.simulation.map.MapConfiguration
import aimotive.simulation.map.StyleSetupState
import aimotive.simulation.map.awaitMap
import aimotive.simulation.map.setUpUiSettings
import aimotive.simulation.model.MainLocationState
import aimotive.simulation.ui.theme.AiMotiveSimulationTheme
import aimotive.simulation.util.UiDialog
import aimotive.simulation.util.UiMessage
import aimotive.simulation.viewmdoel.MainViewModel
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import org.maplibre.android.MapLibre
import org.maplibre.android.camera.CameraUpdate
import org.maplibre.android.maps.MapLibreMap
import org.maplibre.android.maps.MapView
import org.maplibre.android.plugins.annotation.Symbol
import org.maplibre.android.plugins.annotation.SymbolManager
import org.maplibre.android.plugins.annotation.SymbolOptions

@Composable
internal fun MainScreen(
    viewModel: MainViewModel,
) {
    AiMotiveSimulationTheme {
        MainScreenContent(
            messageSharedFlow = viewModel.uiMessageFlow,
            dialogStateFlow = viewModel.uiDialogFlow,
            mainLocationStateFlow = viewModel.mainLocationState,
        )
    }
}

@Composable
private fun MainScreenContent(
    messageSharedFlow: SharedFlow<UiMessage>,
    dialogStateFlow: StateFlow<UiDialog?>,
    mainLocationStateFlow: StateFlow<MainLocationState?>,
) {
    val snackbarHostState = remember { SnackbarHostState() }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        modifier = Modifier
            .fillMaxSize()
    ) { paddingValues ->
        MapViewContainer(
            mainLocationStateFlow = mainLocationStateFlow,
            modifier = Modifier
                .padding(paddingValues)
        )
    }

    AlertDialog(
        dialogStateFlow = dialogStateFlow,
    )

    SnackbarEffect(
        snackbarHostState = snackbarHostState,
        messageSharedFlow = messageSharedFlow,
    )
}

@Composable
private fun MapViewContainer(
    mainLocationStateFlow: StateFlow<MainLocationState?>,
    modifier: Modifier,
) {
    val mapView = createMapView()

    MapContainer(
        mainLocationStateFlow = mainLocationStateFlow,
        mapView = mapView,
        modifier = modifier,
    )
}

@Composable
private fun MapContainer(
    mainLocationStateFlow: StateFlow<MainLocationState?>,
    mapView: MapView,
    modifier: Modifier,
) {
    var hasMapBeenSetUp by remember { mutableStateOf(false) }
    val coroutineScope = rememberCoroutineScope()

    LaunchedEffect(key1 = mapView, key2 = hasMapBeenSetUp) {
        if (hasMapBeenSetUp.not()) {
            coroutineScope.launch {
                mapView.awaitMap()
                    .setUpUiSettings()
                hasMapBeenSetUp = true
            }
        }
    }

    AndroidViewContainer(
        mapView = mapView,
        hasMapBeenSetUp = hasMapBeenSetUp,
        mainLocationStateFlow = mainLocationStateFlow,
        modifier = modifier,
    )
}

@Composable
private fun AndroidViewContainer(
    mapView: MapView,
    hasMapBeenSetUp: Boolean,
    mainLocationStateFlow: StateFlow<MainLocationState?>,
    modifier: Modifier,
) {
    var styleSetupState by remember { mutableStateOf<StyleSetupState>(StyleSetupState.NOT_READY) }

    val state = mainLocationStateFlow.collectAsStateWithLifecycle().value
    val isLocationSuccessStateAvailable = state is MainLocationState.Success
    val locationState = state as? MainLocationState.Success

    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    if (hasMapBeenSetUp) {
        // setting up the style only when there locationState is a [MainLocationState.Success],
        // in order to avoid the default camera position
        LaunchedEffect(key1 = mapView, key2 = styleSetupState, key3 = isLocationSuccessStateAvailable) {
            if (styleSetupState is StyleSetupState.NOT_READY && locationState != null) {
                coroutineScope.launch {
                    mapView.awaitMap().run {
                        setStyle(MapConfiguration.createStyleBuilder(context)) { style ->
                            val symbolManager =
                                SymbolManager(mapView, this, style)
                                    .apply {
                                        iconAllowOverlap = false
                                    }
                            styleSetupState =
                                StyleSetupState.READY(
                                    symbolManager = symbolManager,
                                    currentLocationSymbol = symbolManager.create(
                                        SymbolOptions()
                                            .withIconImage(locationState.type.locationDrawableId)
                                            .withLatLng(locationState.latLng)
                                            .withIconImage(MapConfiguration.LOCATION_DOT_DRAWABLE.id)
                                    )
                                )
                        }
                    }
                }
            }
        }
    }

    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier
    ) {
        AndroidView(
            factory = { mapView },
            update = {
                (styleSetupState as? StyleSetupState.READY)?.let { styleSetupState ->
                    updateMapView(
                        mapView = it,
                        coroutineScope = coroutineScope,
                        symbolManager = styleSetupState.symbolManager,
                        currentLocationSymbol = styleSetupState.currentLocationSymbol,
                        currentLocationState = locationState
                    )
                }
            },
        )

        if (state is MainLocationState.Loading) {
            Loading()
        } else if (styleSetupState !is StyleSetupState.READY) {
            Splash()
        }
    }
}

@Composable
private fun Loading() {
    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        CircularProgressIndicator()
    }
}

@Composable
private fun Splash() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    )
}

@Composable
private fun createMapView(): MapView {
    val context = LocalContext.current
    val mapView = remember {
        MapLibre.getInstance(context)
        MapView(context)
    }

    val observer = createLifecycleEventObserverBySettingUpWith(mapView)
    val lifecycle = LocalLifecycleOwner.current.lifecycle

    DisposableEffect(key1 = lifecycle) {
        lifecycle.addObserver(observer)
        onDispose {
            lifecycle.removeObserver(observer)
        }
    }

    return mapView
}

@Composable
private fun createLifecycleEventObserverBySettingUpWith(mapview: MapView) =
    remember(key1 = mapview) {
        LifecycleEventObserver { _, event ->
            mapview.run {
                when (event) {
                    Lifecycle.Event.ON_CREATE -> onCreate(null)
                    Lifecycle.Event.ON_START -> onStart()
                    Lifecycle.Event.ON_RESUME -> onResume()
                    Lifecycle.Event.ON_PAUSE -> onPause()
                    Lifecycle.Event.ON_STOP -> onStop()
                    Lifecycle.Event.ON_DESTROY -> onDestroy()
                    Lifecycle.Event.ON_ANY -> Unit
                }
            }
        }
    }

private fun updateMapView(
    mapView: MapView,
    coroutineScope: CoroutineScope,
    symbolManager: SymbolManager,
    currentLocationSymbol: Symbol,
    currentLocationState: MainLocationState.Success?,
) {
    currentLocationState?.let {
        coroutineScope.launch {
            mapView.awaitMap().run {
                currentLocationSymbol.iconImage = currentLocationState.type.locationDrawableId
                currentLocationSymbol.latLng = currentLocationState.latLng
                symbolManager.update(currentLocationSymbol)

                easeCamera(
                    object : CameraUpdate {
                        override fun getCameraPosition(maplibreMap: MapLibreMap) = currentLocationState.cameraPosition
                    }
                )
            }
        }
    }
}
