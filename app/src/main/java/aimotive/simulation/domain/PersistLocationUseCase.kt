package aimotive.simulation.domain

import aimotive.simulation.data.FilePersistenceRepository
import aimotive.simulation.model.LatitudeLongitudeItem
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

class PersistLocationUseCase(
    private val coroutineScope: CoroutineScope,
    private val repository: FilePersistenceRepository,
) {

    operator fun invoke(latitudeLongitudeItem: LatitudeLongitudeItem) {
        coroutineScope.launch {
            repository.persist(latitudeLongitudeItem)
        }
    }
}
