package aimotive.simulation.domain

import aimotive.simulation.data.FilePersistenceRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

class PersistStartOfSessionUseCase(
    private val coroutineScope: CoroutineScope,
    private val repository: FilePersistenceRepository,
) {

    operator fun invoke() {
        coroutineScope.launch {
            repository.persistStartOfSession()
        }
    }
}
