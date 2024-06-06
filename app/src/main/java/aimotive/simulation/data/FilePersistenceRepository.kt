package aimotive.simulation.data

import aimotive.simulation.model.LatitudeLongitudeItem
import java.time.LocalDateTime

class FilePersistenceRepository {

    fun persistStartOfSession() {
        persistStartOfSession(dateTime = "New session has been started at ${LocalDateTime.now()}")
    }

    fun persist(latitudeLongitudeItem: LatitudeLongitudeItem) {
        persist(latitudeLongitudeItem.lat, latitudeLongitudeItem.long)
    }

    private external fun persistStartOfSession(dateTime: String)

    private external fun persist(latitude: Double, longitude: Double)

    companion object {
        init {
            System.loadLibrary("file-persistence")
        }
    }
}
