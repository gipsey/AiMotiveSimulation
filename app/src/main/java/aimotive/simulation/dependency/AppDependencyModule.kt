package aimotive.simulation.dependency

import aimotive.simulation.data.FilePersistenceRepository
import aimotive.simulation.domain.PersistLocationUseCase
import aimotive.simulation.domain.PersistStartOfSessionUseCase
import aimotive.simulation.location.LocationPermissionHelper
import aimotive.simulation.location.LocationProvider
import aimotive.simulation.location.LocationSettingsHelper
import aimotive.simulation.viewmdoel.MainViewModel
import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.CreationExtras
import com.google.android.gms.location.LocationServices
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers

class AppDependencyModule(private val context: Context) {

    private val ioCoroutineDispatcher: CoroutineDispatcher = Dispatchers.IO

    private val useCaseCoroutineScope = CoroutineScope(context = ioCoroutineDispatcher)

    private val filePersistenceRepository = FilePersistenceRepository()

    fun getMainViewModelFactory() =
        object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>, extras: CreationExtras) =
                MainViewModel(
                    locationProvider = getLocationProvider(),
                    persistStartOfSessionUseCase = getPersistStartOfSessionUseCase(),
                    persistLocationUseCase = getPersistLocationUseCase(),
                ) as T
        }

    fun getLocationPermissionHelper() = LocationPermissionHelper()

    fun getLocationSettingsHelper() = LocationSettingsHelper()

    fun getLocationProvider() = LocationProvider(
        client = getFusedLocationProviderClient(),
    )

    fun getPersistStartOfSessionUseCase() = PersistStartOfSessionUseCase(
        coroutineScope = useCaseCoroutineScope,
        repository = filePersistenceRepository,
    )

    fun getPersistLocationUseCase() = PersistLocationUseCase(
        coroutineScope = useCaseCoroutineScope,
        repository = filePersistenceRepository,
    )

    private fun getFusedLocationProviderClient() = LocationServices.getFusedLocationProviderClient(context)
}
