package com.example.data.model

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

enum class FuelType(
    val apiParam: String,
    val displayName: String,
    val shortName: String,
    val subtitle: String
) {
    DIESEL("diesel", "Diesel", "Diesel", "Dieselkraftstoff"),
    E5("e5", "Super E5", "E5", "Super 95 (max. 5% Ethanol)"),
    E10("e10", "Super E10", "E10", "Super 95 (max. 10% Ethanol)"),
    ALL("all", "Alle Sorten", "Alle", "Alle Kraftstoffpreise im Überblick")
}

enum class SortOption(val label: String) {
    PRICE("Günstigste zuerst"),
    DISTANCE("Nächste zuerst")
}

data class PriceHistoryPoint(
    val dayOffset: Int, // 0 = today, -1 = yesterday, ..., -29 = 29 days ago
    val dateLabel: String, // e.g. "29. Sep"
    val fullDateFormatted: String, // e.g. "29. September 2026"
    val price: Double
)

data class StationPriceHistory(
    val stationId: String,
    val stationName: String,
    val fuelType: FuelType,
    val points: List<PriceHistoryPoint>,
    val currentPrice: Double,
    val minPrice: Double,
    val maxPrice: Double,
    val avgPrice: Double,
    val minPricePoint: PriceHistoryPoint?,
    val maxPricePoint: PriceHistoryPoint?,
    val priceDiff30Days: Double // today - 30 days ago
)

data class Station(
    val id: String,
    val name: String,
    val brand: String,
    val street: String,
    val houseNumber: String,
    val postCode: String,
    val place: String,
    val lat: Double,
    val lng: Double,
    val dist: Double,
    val isOpen: Boolean,
    val price: Double?,
    val diesel: Double?,
    val e5: Double?,
    val e10: Double?,
    val isFavorite: Boolean = false
) {
    val fullAddress: String
        get() {
            val streetPart = listOf(street, houseNumber).filter { it.isNotBlank() }.joinToString(" ")
            val cityPart = listOf(postCode, place).filter { it.isNotBlank() }.joinToString(" ")
            return listOf(streetPart, cityPart).filter { it.isNotBlank() }.joinToString(", ")
        }

    fun getPriceFor(fuelType: FuelType): Double? {
        return when (fuelType) {
            FuelType.DIESEL -> diesel ?: price
            FuelType.E5 -> e5 ?: price
            FuelType.E10 -> e10 ?: price
            FuelType.ALL -> price ?: e5 ?: diesel ?: e10
        }
    }
}

@JsonClass(generateAdapter = true)
data class TankerkoenigResponse(
    @Json(name = "ok") val ok: Boolean = false,
    @Json(name = "status") val status: String? = null,
    @Json(name = "message") val message: String? = null,
    @Json(name = "stations") val stations: List<StationDto>? = null
)

@JsonClass(generateAdapter = true)
data class TankerkoenigPricesResponse(
    @Json(name = "ok") val ok: Boolean = false,
    @Json(name = "status") val status: String? = null,
    @Json(name = "message") val message: String? = null,
    @Json(name = "prices") val prices: Map<String, Any?>? = null
)

@JsonClass(generateAdapter = true)
data class StationDto(
    @Json(name = "id") val id: String? = null,
    @Json(name = "name") val name: String? = null,
    @Json(name = "brand") val brand: String? = null,
    @Json(name = "street") val street: String? = null,
    @Json(name = "houseNumber") val houseNumber: String? = null,
    @Json(name = "postCode") val postCode: Any? = null,
    @Json(name = "place") val place: String? = null,
    @Json(name = "lat") val lat: Double? = null,
    @Json(name = "lng") val lng: Double? = null,
    @Json(name = "dist") val dist: Double? = null,
    @Json(name = "isOpen") val isOpen: Boolean? = null,
    @Json(name = "price") val price: Any? = null,
    @Json(name = "diesel") val diesel: Any? = null,
    @Json(name = "e5") val e5: Any? = null,
    @Json(name = "e10") val e10: Any? = null
) {
    private fun parsePrice(value: Any?): Double? {
        return when (value) {
            is Number -> value.toDouble()
            is String -> value.toDoubleOrNull()
            else -> null
        }
    }

    fun toDomain(isFavorite: Boolean = false): Station {
        return Station(
            id = id ?: "",
            name = name ?: "Unbenannte Tankstelle",
            brand = (brand ?: name ?: "Freie Tankstelle").trim(),
            street = street ?: "",
            houseNumber = houseNumber ?: "",
            postCode = postCode?.toString() ?: "",
            place = place ?: "",
            lat = lat ?: 0.0,
            lng = lng ?: 0.0,
            dist = dist ?: 0.0,
            isOpen = isOpen ?: false,
            price = parsePrice(price),
            diesel = parsePrice(diesel),
            e5 = parsePrice(e5),
            e10 = parsePrice(e10),
            isFavorite = isFavorite
        )
    }
}
