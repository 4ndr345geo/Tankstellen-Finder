package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.location.CityPreset
import com.example.data.location.LocationInfo
import com.example.data.location.LocationProvider
import com.example.data.model.FuelType
import com.example.data.model.SortOption
import com.example.data.model.Station
import com.example.data.model.StationPriceHistory
import com.example.data.repository.FuelRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

data class FuelUiState(
    val location: LocationInfo = LocationProvider.DEFAULT_LOCATION,
    val radiusKm: Float = 5.0f,
    val selectedFuel: FuelType = FuelType.E5,
    val sortOption: SortOption = SortOption.PRICE,
    val onlyOpen: Boolean = false,
    val searchQuery: String = "",
    val stations: List<Station> = emptyList(),
    val favorites: List<Station> = emptyList(),
    val isLoading: Boolean = false,
    val isRefreshingFavorites: Boolean = false,
    val isGpsLocating: Boolean = false,
    val errorMessage: String? = null,
    val selectedTab: Int = 0,
    val customApiKey: String = "",
    val isUsingCustomKey: Boolean = false,
    val showApiKeyDialog: Boolean = false,
    val showCityPickerDialog: Boolean = false,
    val showSavingsCalculator: Boolean = false,
    val selectedStationForDetail: Station? = null,
    val detailPriceHistory: StationPriceHistory? = null,
    val detailHistoryFuelType: FuelType = FuelType.E5,
    val selectedStatState: com.example.data.model.FederalState = com.example.data.model.FederalState.BE,
    val selectedStatFuel: FuelType = FuelType.E5,
    val twelveMonthHistory: com.example.data.model.TwelveMonthHistory? = null,
    val showWebviewDialog: Boolean = false
) {
    val filteredStations: List<Station>
        get() {
            var list = stations
            if (onlyOpen) {
                list = list.filter { it.isOpen }
            }
            if (searchQuery.isNotBlank()) {
                val q = searchQuery.trim().lowercase()
                list = list.filter {
                    it.name.lowercase().contains(q) ||
                    it.brand.lowercase().contains(q) ||
                    it.street.lowercase().contains(q) ||
                    it.place.lowercase().contains(q)
                }
            }
            return list
        }

    val cheapestPrice: Double?
        get() = getCheapestPriceFor(selectedFuel)

    val averagePrice: Double?
        get() = getAveragePriceFor(selectedFuel)

    val highestPrice: Double?
        get() {
            val prices = filteredStations.mapNotNull { it.getPriceFor(selectedFuel) }.filter { it in 1.25..2.60 }
            return prices.maxOrNull()
        }

    fun getAveragePriceFor(fuel: FuelType): Double? {
        val prices = filteredStations.mapNotNull { it.getPriceFor(fuel) }.filter { it in 1.25..2.60 }
        return if (prices.isNotEmpty()) {
            ((prices.average() * 1000).roundToInt()) / 1000.0
        } else null
    }

    fun getCheapestPriceFor(fuel: FuelType): Double? {
        val prices = filteredStations.mapNotNull { it.getPriceFor(fuel) }.filter { it in 1.25..2.60 }
        return prices.minOrNull()
    }

    val maxSavings50L: Double?
        get() {
            val min = cheapestPrice ?: return null
            val max = highestPrice ?: return null
            val diff = (max - min) * 50.0
            return (diff * 100).roundToInt() / 100.0
        }
}

class FuelViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = FuelRepository(application)
    private val locationProvider = LocationProvider(application)

    private val _uiState = MutableStateFlow(
        FuelUiState(
            customApiKey = repository.getApiKey(),
            isUsingCustomKey = repository.isUsingCustomKey()
        )
    )
    val uiState: StateFlow<FuelUiState> = _uiState.asStateFlow()

    init {
        // Observe favorites from Room DB
        viewModelScope.launch {
            repository.favoritesFlow.collect { favs ->
                _uiState.update { state ->
                    val favIds = favs.map { it.id }.toSet()
                    state.copy(
                        favorites = favs,
                        stations = state.stations.map { s ->
                            s.copy(isFavorite = favIds.contains(s.id))
                        },
                        selectedStationForDetail = state.selectedStationForDetail?.let { current ->
                            current.copy(isFavorite = favIds.contains(current.id))
                        }
                    )
                }
            }
        }

        // Initial load
        refreshLocationAndStations(autoGps = true)
    }

    fun refreshLocationAndStations(autoGps: Boolean = false) {
        viewModelScope.launch {
            if (autoGps && locationProvider.hasLocationPermission()) {
                _uiState.update { it.copy(isGpsLocating = true) }
                val gpsLoc = locationProvider.getDeviceLocation()
                if (gpsLoc != null) {
                    _uiState.update { it.copy(location = gpsLoc, isGpsLocating = false) }
                } else {
                    _uiState.update { it.copy(isGpsLocating = false) }
                }
            }

            fetchStations()
        }
    }

    fun triggerGpsLocate() {
        viewModelScope.launch {
            _uiState.update { it.copy(isGpsLocating = true) }
            val gpsLoc = locationProvider.getDeviceLocation()
            if (gpsLoc != null) {
                _uiState.update {
                    it.copy(location = gpsLoc, isGpsLocating = false)
                }
                fetchStations()
            } else {
                _uiState.update {
                    it.copy(
                        isGpsLocating = false,
                        errorMessage = "Standort konnte nicht ermittelt werden. Bitte prüfe GPS-Freigabe oder wähle eine Stadt."
                    )
                }
            }
        }
    }

    fun selectCity(city: CityPreset) {
        _uiState.update {
            it.copy(
                location = LocationInfo(
                    latitude = city.latitude,
                    longitude = city.longitude,
                    label = "${city.name} (${city.state})",
                    isRealGps = false
                ),
                showCityPickerDialog = false
            )
        }
        fetchStations()
    }

    fun setRadius(radius: Float) {
        val rounded = ((radius * 10).roundToInt() / 10f).coerceIn(1f, 25f)
        if (_uiState.value.radiusKm != rounded) {
            _uiState.update { it.copy(radiusKm = rounded) }
            fetchStations()
        }
    }

    fun setSelectedFuel(fuel: FuelType) {
        if (_uiState.value.selectedFuel != fuel) {
            _uiState.update { it.copy(selectedFuel = fuel) }
            fetchStations()
        }
    }

    fun setSortOption(sort: SortOption) {
        _uiState.update { it.copy(sortOption = sort) }
        fetchStations()
    }

    fun setOnlyOpen(onlyOpen: Boolean) {
        _uiState.update { it.copy(onlyOpen = onlyOpen) }
    }

    fun setSearchQuery(query: String) {
        _uiState.update { it.copy(searchQuery = query) }
    }

    fun setSelectedTab(tab: Int) {
        _uiState.update { it.copy(selectedTab = tab) }
        if (tab == 1) {
            refreshFavoritePrices()
        }
    }

    fun setShowApiKeyDialog(show: Boolean) {
        _uiState.update { it.copy(showApiKeyDialog = show) }
    }

    fun setShowCityPickerDialog(show: Boolean) {
        _uiState.update { it.copy(showCityPickerDialog = show) }
    }

    fun setShowWebviewDialog(show: Boolean) {
        _uiState.update { it.copy(showWebviewDialog = show) }
    }

    fun saveApiKey(apiKey: String) {
        repository.saveCustomApiKey(apiKey)
        _uiState.update {
            it.copy(
                customApiKey = repository.getApiKey(),
                isUsingCustomKey = repository.isUsingCustomKey(),
                showApiKeyDialog = false
            )
        }
        fetchStations()
    }

    fun toggleFavorite(station: Station) {
        viewModelScope.launch {
            repository.toggleFavorite(station)
        }
    }

    fun selectStationForDetail(station: Station) {
        val fuelToUse = if (_uiState.value.selectedFuel == FuelType.ALL) FuelType.E5 else _uiState.value.selectedFuel
        val history = repository.getPriceHistory(station, fuelToUse)
        _uiState.update {
            it.copy(
                selectedStationForDetail = station,
                detailPriceHistory = history,
                detailHistoryFuelType = fuelToUse
            )
        }
    }

    fun clearSelectedStation() {
        _uiState.update {
            it.copy(
                selectedStationForDetail = null,
                detailPriceHistory = null
            )
        }
    }

    fun setDetailHistoryFuel(fuel: FuelType) {
        val station = _uiState.value.selectedStationForDetail ?: return
        val history = repository.getPriceHistory(station, fuel)
        _uiState.update {
            it.copy(
                detailPriceHistory = history,
                detailHistoryFuelType = fuel
            )
        }
    }

    fun refreshFavoritePrices() {
        viewModelScope.launch {
            _uiState.update { it.copy(isRefreshingFavorites = true) }
            repository.refreshFavoritePrices()
            _uiState.update { it.copy(isRefreshingFavorites = false) }
        }
    }

    fun dismissError() {
        _uiState.update { it.copy(errorMessage = null) }
    }

    fun fetchStations() {
        viewModelScope.launch {
            val currentState = _uiState.value
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }

            val result = repository.getStations(
                lat = currentState.location.latitude,
                lng = currentState.location.longitude,
                radiusKm = currentState.radiusKm,
                fuelType = currentState.selectedFuel,
                sortOption = currentState.sortOption
            )

            result.onSuccess { list ->
                _uiState.update {
                    it.copy(
                        stations = list,
                        isLoading = false
                    )
                }
                refreshTwelveMonthStats()
            }.onFailure { err ->
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = err.localizedMessage ?: "Fehler beim Abrufen der Tankstellenpreise"
                    )
                }
                refreshTwelveMonthStats()
            }
        }
    }

    fun setStatState(state: com.example.data.model.FederalState) {
        val currentFuel = _uiState.value.selectedStatFuel
        val anchor = _uiState.value.getAveragePriceFor(currentFuel) ?: _uiState.value.getCheapestPriceFor(currentFuel)
        val stats = repository.getTwelveMonthStatistics(state, currentFuel, anchor)
        _uiState.update {
            it.copy(selectedStatState = state, twelveMonthHistory = stats)
        }
    }

    fun setStatFuel(fuel: FuelType) {
        val currentState = _uiState.value.selectedStatState
        val anchor = _uiState.value.getAveragePriceFor(fuel) ?: _uiState.value.getCheapestPriceFor(fuel)
        val stats = repository.getTwelveMonthStatistics(currentState, fuel, anchor)
        _uiState.update {
            it.copy(selectedStatFuel = fuel, twelveMonthHistory = stats)
        }
    }

    fun refreshTwelveMonthStats() {
        val st = _uiState.value
        val derivedState = com.example.data.model.FederalState.fromString(st.location.label)
        val targetState = if (st.selectedStatState == com.example.data.model.FederalState.BE && derivedState != com.example.data.model.FederalState.BE) {
            derivedState
        } else {
            st.selectedStatState
        }
        val targetFuel = st.selectedStatFuel
        val anchor = st.getAveragePriceFor(targetFuel) ?: st.getCheapestPriceFor(targetFuel)
        val stats = repository.getTwelveMonthStatistics(targetState, targetFuel, anchor)
        _uiState.update {
            it.copy(
                selectedStatState = targetState,
                twelveMonthHistory = stats
            )
        }
    }
}
