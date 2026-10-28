package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.data.model.Station

@Entity(tableName = "favorite_stations")
data class FavoriteStationEntity(
    @PrimaryKey val id: String,
    val name: String,
    val brand: String,
    val street: String,
    val houseNumber: String,
    val postCode: String,
    val place: String,
    val lat: Double,
    val lng: Double,
    val lastDiesel: Double? = null,
    val lastE5: Double? = null,
    val lastE10: Double? = null,
    val isOpen: Boolean = true,
    val addedAt: Long = System.currentTimeMillis()
) {
    fun toDomain(): Station {
        return Station(
            id = id,
            name = name,
            brand = brand,
            street = street,
            houseNumber = houseNumber,
            postCode = postCode,
            place = place,
            lat = lat,
            lng = lng,
            dist = 0.0,
            isOpen = isOpen,
            price = lastE5 ?: lastDiesel ?: lastE10,
            diesel = lastDiesel,
            e5 = lastE5,
            e10 = lastE10,
            isFavorite = true
        )
    }

    companion object {
        fun fromStation(station: Station): FavoriteStationEntity {
            return FavoriteStationEntity(
                id = station.id,
                name = station.name,
                brand = station.brand,
                street = station.street,
                houseNumber = station.houseNumber,
                postCode = station.postCode,
                place = station.place,
                lat = station.lat,
                lng = station.lng,
                lastDiesel = station.diesel,
                lastE5 = station.e5,
                lastE10 = station.e10,
                isOpen = station.isOpen
            )
        }
    }
}
