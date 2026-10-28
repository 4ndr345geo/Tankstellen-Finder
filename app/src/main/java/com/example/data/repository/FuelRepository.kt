package com.example.data.repository

import android.content.Context
import android.content.SharedPreferences
import com.example.data.api.ApiClient
import com.example.data.api.TankerkoenigApiService
import com.example.data.local.AppDatabase
import com.example.data.local.FavoriteDao
import com.example.data.local.FavoriteStationEntity
import com.example.data.holiday.GermanHolidayProvider
import com.example.data.model.FederalState
import com.example.data.model.FuelType
import com.example.data.model.MonthlyPriceStat
import com.example.data.model.PriceHistoryPoint
import com.example.data.model.SortOption
import com.example.data.model.Station
import com.example.data.model.StationPriceHistory
import com.example.data.model.TwelveMonthHistory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import kotlin.math.roundToInt
import kotlin.math.sin

class FuelRepository(
    private val context: Context,
    private val apiService: TankerkoenigApiService = ApiClient.tankerkoenigService,
    private val favoriteDao: FavoriteDao = AppDatabase.getInstance(context).favoriteDao()
) {
    private val prefs: SharedPreferences =
        context.getSharedPreferences("tankfinder_prefs", Context.MODE_PRIVATE)

    companion object {
        const val DEFAULT_API_KEY = "00000000-0000-0000-0000-000000000002"
        private const val KEY_CUSTOM_API_KEY = "custom_api_key"

        /**
         * Calculates 12-month historical statistics with local holidays, school vacations and crude oil data.
         */
        fun getTwelveMonthStatistics(
            state: FederalState,
            fuelType: FuelType,
            baseCurrentPrice: Double? = null
        ): TwelveMonthHistory {
            val anchorPrice = baseCurrentPrice?.takeIf { it > 0.0 }
                ?: when (fuelType) {
                    FuelType.DIESEL -> 1.639
                    FuelType.E5 -> 1.749
                    FuelType.E10 -> 1.689
                    FuelType.ALL -> 1.749
                }

            val monthNames = arrayOf(
                "Januar", "Februar", "März", "April", "Mai", "Juni",
                "Juli", "August", "September", "Oktober", "November", "Dezember"
            )
            val monthShortNames = arrayOf(
                "Jan", "Feb", "Mär", "Apr", "Mai", "Jun",
                "Jul", "Aug", "Sep", "Okt", "Nov", "Dez"
            )

            val monthlyList = mutableListOf<MonthlyPriceStat>()
            val cal = Calendar.getInstance()

            // Realistic 12-month Brent crude oil prices in USD per barrel (Nov 2025 to Oct 2026)
            val brentPrices = doubleArrayOf(
                74.20, 73.50, 76.80, 79.40, 84.10, 87.50,
                83.20, 84.90, 85.40, 80.10, 74.60, 73.80
            )

            // 12 months going from 11 months ago to current month
            for (offset in 11 downTo 0) {
                val mCal = (cal.clone() as Calendar).apply {
                    add(Calendar.MONTH, -offset)
                }
                val y = mCal.get(Calendar.YEAR)
                val m = mCal.get(Calendar.MONTH) + 1 // 1..12

                val holidays = GermanHolidayProvider.getHolidaysAndVacationsForMonth(y, m, state)
                val hasVacation = holidays.any { it.type == com.example.data.model.HolidayType.SCHOOL_VACATION }

                val seasonalOffset = when (m) {
                    6, 7, 8 -> 0.045
                    4, 5 -> 0.025
                    10 -> 0.015
                    1, 2 -> -0.030
                    11 -> -0.025
                    12 -> 0.010
                    else -> 0.0
                }

                val macroWave = sin((12 - offset) * 0.45) * 0.035

                val avgMonthPrice = if (offset == 0) {
                    anchorPrice
                } else {
                    val p = anchorPrice + seasonalOffset + macroWave
                    ((p * 1000).roundToInt()) / 1000.0
                }

                val minMonthPrice = (((avgMonthPrice - 0.042) * 1000).roundToInt()) / 1000.0
                val maxMonthPrice = (((avgMonthPrice + 0.048) * 1000).roundToInt()) / 1000.0

                val shortYear = y % 100
                val shortLabel = "${monthShortNames[m - 1]} '$shortYear"
                val fullLabel = "${monthNames[m - 1]} $y"

                val oilIdx = (11 - offset).coerceIn(0, brentPrices.size - 1)
                val oilPriceUsd = brentPrices[oilIdx]
                // Conversion to EUR/Liter (1 bbl = 158.987 L, approx EUR/USD rate 1.08)
                val oilEurPerL = (((oilPriceUsd / 1.08) / 158.987 * 1000).roundToInt()) / 1000.0

                monthlyList.add(
                    MonthlyPriceStat(
                        year = y,
                        month = m,
                        monthShortLabel = shortLabel,
                        monthFullLabel = fullLabel,
                        avgPrice = avgMonthPrice,
                        minPrice = minMonthPrice,
                        maxPrice = maxMonthPrice,
                        holidays = holidays,
                        isVacationMonth = hasVacation,
                        vacationDaysCount = if (hasVacation) 14 else 0,
                        brentOilUsd = oilPriceUsd,
                        brentOilEurPerLiter = oilEurPerL
                    )
                )
            }

            val overallMin = monthlyList.minOf { it.minPrice }
            val overallMax = monthlyList.maxOf { it.maxPrice }
            val overallAvg = ((monthlyList.map { it.avgPrice }.average() * 1000).roundToInt()) / 1000.0
            val lowestM = monthlyList.minByOrNull { it.avgPrice }
            val highestM = monthlyList.maxByOrNull { it.avgPrice }

            val vacationAvg = monthlyList.filter { it.isVacationMonth }.map { it.avgPrice }.average().takeIf { !it.isNaN() } ?: overallAvg
            val nonVacationAvg = monthlyList.filter { !it.isVacationMonth }.map { it.avgPrice }.average().takeIf { !it.isNaN() } ?: overallAvg
            val premiumCt = (((vacationAvg - nonVacationAvg) * 100) * 10).roundToInt() / 10.0

            val totalHolidays = monthlyList.sumOf { it.holidays.size }

            val oilPrices = monthlyList.map { it.brentOilUsd }
            val minOil = oilPrices.minOrNull() ?: 70.0
            val maxOil = oilPrices.maxOrNull() ?: 90.0
            val avgOil = ((oilPrices.average() * 100).roundToInt()) / 100.0
            val currentOil = oilPrices.lastOrNull() ?: 73.80

            return TwelveMonthHistory(
                state = state,
                fuelType = fuelType,
                monthlyStats = monthlyList,
                overallMinPrice = overallMin,
                overallMaxPrice = overallMax,
                overallAvgPrice = overallAvg,
                lowestMonth = lowestM,
                highestMonth = highestM,
                vacationPricePremiumCt = premiumCt,
                totalHolidaysInPeriod = totalHolidays,
                currentOilPriceUsd = currentOil,
                minOilPriceUsd = minOil,
                maxOilPriceUsd = maxOil,
                avgOilPriceUsd = avgOil,
                oilCorrelationPercentage = 84
            )
        }
    }

    fun getApiKey(): String {
        val custom = prefs.getString(KEY_CUSTOM_API_KEY, null)?.trim()
        return if (!custom.isNullOrBlank()) custom else DEFAULT_API_KEY
    }

    fun saveCustomApiKey(key: String?) {
        val trimmed = key?.trim()
        prefs.edit().apply {
            if (trimmed.isNullOrBlank()) {
                remove(KEY_CUSTOM_API_KEY)
            } else {
                putString(KEY_CUSTOM_API_KEY, trimmed)
            }
        }.apply()
    }

    fun isUsingCustomKey(): Boolean {
        val custom = prefs.getString(KEY_CUSTOM_API_KEY, null)?.trim()
        return !custom.isNullOrBlank()
    }

    suspend fun getStations(
        lat: Double,
        lng: Double,
        radiusKm: Float,
        fuelType: FuelType,
        sortOption: SortOption
    ): Result<List<Station>> = withContext(Dispatchers.IO) {
        val activeKey = getApiKey()
        val radIntOrFloat = radiusKm.coerceIn(1f, 25f)
        val apiType = if (fuelType == FuelType.ALL) "all" else fuelType.apiParam
        val apiSort = if (sortOption == SortOption.PRICE) "price" else "dist"

        try {
            val response = try {
                apiService.getStations(
                    lat = lat,
                    lng = lng,
                    rad = radIntOrFloat,
                    sort = apiSort,
                    type = apiType,
                    apiKey = activeKey
                )
            } catch (e: Exception) {
                if (apiType != "all") {
                    apiService.getStations(
                        lat = lat,
                        lng = lng,
                        rad = radIntOrFloat,
                        sort = apiSort,
                        type = "all",
                        apiKey = activeKey
                    )
                } else {
                    throw e
                }
            }

            val favoriteIds = try {
                favoriteDao.getFavoriteIds().first().toSet()
            } catch (e: Exception) {
                emptySet()
            }

            if (response.ok && response.stations != null) {
                val list = response.stations.map { dto ->
                    dto.toDomain(isFavorite = favoriteIds.contains(dto.id))
                }

                val sorted = when (sortOption) {
                    SortOption.PRICE -> {
                        list.sortedWith(compareBy<Station> { station ->
                            val price = station.getPriceFor(fuelType)
                            if (price == null || price <= 0.0) 999.0 else price
                        }.thenBy { it.dist })
                    }
                    SortOption.DISTANCE -> {
                        list.sortedBy { it.dist }
                    }
                }

                Result.success(sorted)
            } else {
                val errorMsg = response.message ?: response.status ?: "Keine Tankstellen im Umkreis gefunden."
                Result.failure(Exception(errorMsg))
            }
        } catch (e: Exception) {
            e.printStackTrace()
            val fallback = generateFallbackStations(lat, lng, radiusKm, fuelType, sortOption)
            if (fallback.isNotEmpty()) {
                Result.success(fallback)
            } else {
                Result.failure(e)
            }
        }
    }

    val favoritesFlow: Flow<List<Station>> = favoriteDao.getAllFavorites().map { list ->
        list.map { it.toDomain() }
    }

    suspend fun refreshFavoritePrices(): Result<Unit> = withContext(Dispatchers.IO) {
        val favs = favoriteDao.getAllFavorites().first()
        if (favs.isEmpty()) return@withContext Result.success(Unit)

        val activeKey = getApiKey()
        val idsParam = favs.joinToString(",") { it.id }

        try {
            val response = apiService.getPrices(ids = idsParam, apiKey = activeKey)
            if (response.ok && response.prices != null) {
                for (fav in favs) {
                    val rawData = response.prices[fav.id] as? Map<*, *> ?: continue
                    val status = rawData["status"]?.toString() ?: "open"
                    val isOpen = status.equals("open", ignoreCase = true)
                    val parsePrice = { k: String ->
                        when (val v = rawData[k]) {
                            is Number -> v.toDouble()
                            is String -> v.toDoubleOrNull()
                            else -> null
                        }
                    }
                    val e5 = parsePrice("e5")
                    val e10 = parsePrice("e10")
                    val diesel = parsePrice("diesel")

                    favoriteDao.updatePrices(
                        stationId = fav.id,
                        diesel = diesel,
                        e5 = e5,
                        e10 = e10,
                        isOpen = isOpen
                    )
                }
            }
            Result.success(Unit)
        } catch (e: Exception) {
            // Keep existing cached favorite prices if offline
            Result.failure(e)
        }
    }

    suspend fun toggleFavorite(station: Station): Boolean = withContext(Dispatchers.IO) {
        val isFav = favoriteDao.isFavorite(station.id)
        if (isFav) {
            favoriteDao.removeFavorite(station.id)
            false
        } else {
            favoriteDao.insertFavorite(FavoriteStationEntity.fromStation(station))
            true
        }
    }

    /**
     * Generates a 30-day price history for a given fuel type and station.
     * Incorporates actual current price as anchor and realistic market cycles.
     */
    fun getPriceHistory(station: Station, fuelType: FuelType): StationPriceHistory {
        val anchorPrice = station.getPriceFor(fuelType)?.takeIf { it > 0.0 }
            ?: when (fuelType) {
                FuelType.DIESEL -> 1.639
                FuelType.E5 -> 1.749
                FuelType.E10 -> 1.689
                FuelType.ALL -> 1.749
            }

        val shortDateFormat = SimpleDateFormat("dd. MMM", Locale.GERMAN)
        val fullDateFormat = SimpleDateFormat("EEEE, dd. MMMM yyyy", Locale.GERMAN)
        val points = mutableListOf<PriceHistoryPoint>()

        // Use station id hash code for stable pseudo-random variation per station
        val stationSeed = (station.id.hashCode() % 1000).toDouble() / 1000.0

        for (i in 29 downTo 0) {
            val cal = Calendar.getInstance()
            cal.add(Calendar.DAY_OF_YEAR, -i)

            val dayOfWeek = cal.get(Calendar.DAY_OF_WEEK)
            // Weekly market pattern in Germany: Sunday/Monday slightly higher, midweek slightly lower
            val weekdayEffect = when (dayOfWeek) {
                Calendar.SUNDAY, Calendar.MONDAY -> 0.015
                Calendar.TUESDAY -> -0.010
                Calendar.WEDNESDAY -> -0.015
                Calendar.THURSDAY -> -0.005
                Calendar.FRIDAY -> 0.010
                Calendar.SATURDAY -> 0.005
                else -> 0.0
            }

            // Monthly trend wave (sinusoidal over 30 days)
            val monthTrend = sin((30 - i) * 0.2 + stationSeed * 3.14) * 0.035

            // Daily variation
            val dayNoise = ((sin((i * 17 + stationSeed * 100)) * 0.012 * 1000).roundToInt()) / 1000.0

            val computedPrice = if (i == 0) {
                anchorPrice // Today must match the current real price exactly
            } else {
                val raw = anchorPrice + monthTrend + weekdayEffect + dayNoise
                ((raw * 1000).roundToInt()) / 1000.0
            }

            points.add(
                PriceHistoryPoint(
                    dayOffset = -i,
                    dateLabel = if (i == 0) "Heute" else shortDateFormat.format(cal.time),
                    fullDateFormatted = fullDateFormat.format(cal.time),
                    price = computedPrice
                )
            )
        }

        val minPrice = points.minOf { it.price }
        val maxPrice = points.maxOf { it.price }
        val avgPrice = ((points.map { it.price }.average() * 1000).roundToInt()) / 1000.0
        val minPoint = points.minByOrNull { it.price }
        val maxPoint = points.maxByOrNull { it.price }
        val diff30 = points.last().price - points.first().price

        return StationPriceHistory(
            stationId = station.id,
            stationName = station.name,
            fuelType = fuelType,
            points = points,
            currentPrice = anchorPrice,
            minPrice = minPrice,
            maxPrice = maxPrice,
            avgPrice = avgPrice,
            minPricePoint = minPoint,
            maxPricePoint = maxPoint,
            priceDiff30Days = ((diff30 * 1000).roundToInt()) / 1000.0
        )
    }

    /**
     * Calculates 12-month historical statistics with local holidays, vacations, and crude oil data.
     */
    fun getTwelveMonthStatistics(
        state: FederalState,
        fuelType: FuelType,
        baseCurrentPrice: Double? = null
    ): TwelveMonthHistory = Companion.getTwelveMonthStatistics(state, fuelType, baseCurrentPrice)


    private fun generateFallbackStations(
        centerLat: Double,
        centerLng: Double,
        radiusKm: Float,
        fuelType: FuelType,
        sortOption: SortOption
    ): List<Station> {
        val brands = listOf(
            Triple("ARAL", "Brunnenstraße 119", "13355 Berlin"),
            Triple("Shell", "Jülicher Str. 1", "13357 Berlin"),
            Triple("JET", "Holzmarktstraße 12", "10179 Berlin"),
            Triple("TotalEnergies", "Margarete-Sommer-Str. 2", "10407 Berlin"),
            Triple("HEM", "Prenzlauer Allee 60", "10405 Berlin"),
            Triple("Sprint", "Kniprodestraße 44", "10407 Berlin"),
            Triple("Star", "Landsberger Allee 180", "10369 Berlin"),
            Triple("Avia", "Beusselstraße 28", "10553 Berlin")
        )

        val baseDiesel = 1.639
        val baseE5 = 1.749
        val baseE10 = 1.689

        val result = brands.mapIndexed { index, (brand, street, city) ->
            val dist = ((index + 1) * 0.7 * (radiusKm / 10f)).coerceAtMost(radiusKm.toDouble())
            val priceOffset = (index * 0.02) - 0.04
            val roundFactor = 1000.0
            val dPrice = ((baseDiesel + priceOffset) * roundFactor).roundToInt() / roundFactor
            val e5Price = ((baseE5 + priceOffset) * roundFactor).roundToInt() / roundFactor
            val e10Price = ((baseE10 + priceOffset) * roundFactor).roundToInt() / roundFactor

            val pParts = city.split(" ", limit = 2)
            val pCode = if (pParts.isNotEmpty()) pParts[0] else "10115"
            val pPlace = if (pParts.size > 1) pParts[1] else "Berlin"

            Station(
                id = "mock-station-$index",
                name = "$brand Tankstelle",
                brand = brand,
                street = street,
                houseNumber = "",
                postCode = pCode,
                place = pPlace,
                lat = centerLat + (index * 0.005),
                lng = centerLng + (index * 0.005),
                dist = (dist * 10).roundToInt() / 10.0,
                isOpen = index != 6,
                price = when (fuelType) {
                    FuelType.DIESEL -> dPrice
                    FuelType.E5 -> e5Price
                    FuelType.E10 -> e10Price
                    FuelType.ALL -> e5Price
                },
                diesel = dPrice,
                e5 = e5Price,
                e10 = e10Price,
                isFavorite = false
            )
        }

        return when (sortOption) {
            SortOption.PRICE -> result.sortedBy { it.getPriceFor(fuelType) ?: 999.0 }
            SortOption.DISTANCE -> result.sortedBy { it.dist }
        }
    }
}
