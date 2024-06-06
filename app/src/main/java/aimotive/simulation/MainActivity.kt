package aimotive.simulation

import aimotive.simulation.dependency.AppDependencyModule
import aimotive.simulation.location.LocationPermissionHelper
import aimotive.simulation.location.LocationSettingsHelper
import aimotive.simulation.ui.MainScreen
import aimotive.simulation.util.UiMessage
import aimotive.simulation.util.UiMessageAction
import aimotive.simulation.viewmdoel.MainViewModel
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.ActivityResult
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.IntentSenderRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {

    private val dependencyModule by lazy { AppDependencyModule(applicationContext) }

    private val viewModel by viewModels<MainViewModel> { dependencyModule.getMainViewModelFactory() }

    private val locationPermissionHelper by lazy { dependencyModule.getLocationPermissionHelper() }
    private val locationSettingsHelper by lazy { dependencyModule.getLocationSettingsHelper() }

    private lateinit var locationPermissionResultLauncher: ActivityResultLauncher<String>
    private lateinit var locationSettingsResultLauncher: ActivityResultLauncher<IntentSenderRequest>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        createResultLaunchers()

        setContent {
            MainScreen(viewModel = viewModel)
        }

        setUpFlowCollection()
    }

    override fun onStart() {
        super.onStart()
        viewModel.onStart()
        validateLocationPermission()
    }

    override fun onStop() {
        super.onStop()
        viewModel.onStop()
    }

    private fun createResultLaunchers() {
        locationPermissionResultLauncher =
            registerForActivityResult(ActivityResultContracts.RequestPermission(), ::onLocationPermissionResult)
        locationSettingsResultLauncher =
            registerForActivityResult(ActivityResultContracts.StartIntentSenderForResult(), ::onLocationSettingsResult)
    }

    private fun setUpFlowCollection() {
        lifecycleScope.launch {
            viewModel.validateLocationPermissionEventFlow.collect {
                validateLocationPermission()
            }
        }

        lifecycleScope.launch {
            viewModel.requestLocationPermissionEventFlow.collect {
                requestLocationPermission()
            }
        }
    }

    private fun validateLocationPermission() =
        if (locationPermissionHelper.isGranted(this)) {
            onLocationPermissionValidatedWithGrant()
        } else if (locationPermissionHelper.shouldShowRationale(this)) {
            viewModel.showLocationPermissionRationale()
        } else {
            requestLocationPermission()
        }

    private fun requestLocationPermission() =
        locationPermissionResultLauncher.launch(LocationPermissionHelper.PERMISSION)

    private fun onLocationPermissionResult(granted: Boolean?) =
        if (granted == true) {
            onLocationPermissionValidatedWithGrant()
        } else if (locationPermissionHelper.shouldShowRationale(this)) {
            viewModel.onLocationPermissionDeniedWithRationaleNeeded()
        } else {
            viewModel.onMessageShouldBeShown(
                UiMessage.OfResId(
                    R.string.location_access_deny_error_settings,
                    UiMessageAction(R.string.settings) { context ->
                        context.startActivity(
                            Intent().apply {
                                action = Settings.ACTION_APPLICATION_DETAILS_SETTINGS
                                data = Uri.fromParts("package", context.packageName, null)
                            }
                        )
                    }
                )
            )
            viewModel.onLocationPermissionPermanentlyDenied()
        }

    private fun onLocationPermissionValidatedWithGrant() {
        lifecycleScope.launch {
            when (val result = locationSettingsHelper.validateLocationSettings(this@MainActivity)) {
                is LocationSettingsHelper.Result.ResolutionRequired ->
                    locationSettingsResultLauncher.launch(
                        IntentSenderRequest.Builder(result.resolvableApiException.resolution).build()
                    )

                is LocationSettingsHelper.Result.Error ->
                    viewModel.onLocationSettingsEnablementError(result)

                LocationSettingsHelper.Result.Available ->
                    viewModel.onLocationPermissionGrantedAndSettingsAvailable()
            }
        }
    }

    private fun onLocationSettingsResult(result: ActivityResult?) =
        if (result?.resultCode == RESULT_OK) {
            viewModel.onLocationPermissionGrantedAndSettingsAvailable()
        } else {
            viewModel.onLocationSettingsEnablementDenied()
        }
}
