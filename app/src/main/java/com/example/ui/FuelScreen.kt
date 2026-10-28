package com.example.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.LocalGasStation
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Sort
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.FuelType
import com.example.data.model.SortOption
import com.example.data.model.Station
import androidx.compose.material3.FilledTonalButton
import com.example.ui.components.FuelTypeSelector
import com.example.ui.components.PriceStatsBanner
import com.example.ui.components.RadiusSelector
import com.example.ui.components.StationCard
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Language
import com.example.ui.dialogs.ApiKeyDialog
import com.example.ui.dialogs.CityPickerDialog
import com.example.ui.dialogs.StationDetailSheet
import com.example.ui.dialogs.WebviewDialog
import com.example.ui.tabs.TwelveMonthStatsTab
import com.example.ui.theme.PriceCheapest
import com.example.ui.theme.StatusOpen
import java.util.Locale
import kotlin.math.roundToInt

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FuelScreen(
    viewModel: FuelViewModel,
    onRequestGpsPermission: () -> Unit
) {
    val state by viewModel.uiState.collectAsState()

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        contentWindowInsets = WindowInsets.statusBars,
        topBar = {
            TopAppBar(
                title = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(34.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(MaterialTheme.colorScheme.primary),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.LocalGasStation,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onPrimary,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Column {
                            Text(
                                text = "TankFinder",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Black
                            )
                            Text(
                                text = "Spritpreise MTS-K",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                fontSize = 11.sp
                            )
                        }
                    }
                },
                actions = {
                    // Web-Version action
                    IconButton(
                        onClick = { viewModel.setShowWebviewDialog(true) },
                        modifier = Modifier.testTag("webview_btn")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Language,
                            contentDescription = "Web-Version anzeigen",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }

                    // API Key Settings action
                    IconButton(
                        onClick = { viewModel.setShowApiKeyDialog(true) },
                        modifier = Modifier.testTag("api_key_btn")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Key,
                            contentDescription = "API-Schlüssel / Info",
                            tint = if (state.isUsingCustomKey) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    // Refresh action
                    IconButton(
                        onClick = { viewModel.fetchStations() },
                        modifier = Modifier.testTag("refresh_btn")
                    ) {
                        if (state.isLoading) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(20.dp),
                                strokeWidth = 2.dp,
                                color = MaterialTheme.colorScheme.primary
                            )
                        } else {
                            Icon(
                                imageVector = Icons.Default.Refresh,
                                contentDescription = "Preise aktualisieren"
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        },
        bottomBar = {
            NavigationBar(
                modifier = Modifier.windowInsetsPadding(WindowInsets.navigationBars),
                containerColor = MaterialTheme.colorScheme.surface
            ) {
                NavigationBarItem(
                    selected = state.selectedTab == 0,
                    onClick = { viewModel.setSelectedTab(0) },
                    icon = { Icon(Icons.Default.LocalGasStation, contentDescription = "Preise") },
                    label = { Text("Preise") },
                    modifier = Modifier.testTag("tab_stations")
                )
                NavigationBarItem(
                    selected = state.selectedTab == 1,
                    onClick = { viewModel.setSelectedTab(1) },
                    icon = {
                        Icon(Icons.Default.Favorite, contentDescription = "Favoriten")
                    },
                    label = {
                        Text(if (state.favorites.isNotEmpty()) "Favoriten (${state.favorites.size})" else "Favoriten")
                    },
                    modifier = Modifier.testTag("tab_favorites")
                )
                NavigationBarItem(
                    selected = state.selectedTab == 2,
                    onClick = { viewModel.setSelectedTab(2) },
                    icon = { Icon(Icons.Default.CalendarMonth, contentDescription = "12M-Statistik") },
                    label = { Text("12M-Statistik") },
                    modifier = Modifier.testTag("tab_12m_stats")
                )
                NavigationBarItem(
                    selected = state.selectedTab == 3,
                    onClick = { viewModel.setSelectedTab(3) },
                    icon = { Icon(Icons.Default.Calculate, contentDescription = "Spar-Rechner & Info") },
                    label = { Text("Rechner") },
                    modifier = Modifier.testTag("tab_calculator")
                )
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when (state.selectedTab) {
                0 -> StationsTabContent(
                    state = state,
                    viewModel = viewModel,
                    onRequestGpsPermission = onRequestGpsPermission
                )
                1 -> FavoritesTabContent(
                    state = state,
                    viewModel = viewModel
                )
                2 -> TwelveMonthStatsTab(
                    state = state,
                    viewModel = viewModel
                )
                3 -> CalculatorAndInfoTabContent(
                    state = state,
                    viewModel = viewModel
                )
            }
        }
    }

    // Dialogs
    if (state.showCityPickerDialog) {
        CityPickerDialog(
            onDismiss = { viewModel.setShowCityPickerDialog(false) },
            onUseGps = {
                onRequestGpsPermission()
                viewModel.triggerGpsLocate()
            },
            onSelectCity = { city ->
                viewModel.selectCity(city)
            }
        )
    }

    if (state.showApiKeyDialog) {
        ApiKeyDialog(
            currentKey = state.customApiKey,
            isUsingCustomKey = state.isUsingCustomKey,
            onSave = { key -> viewModel.saveApiKey(key) },
            onDismiss = { viewModel.setShowApiKeyDialog(false) }
        )
    }

    if (state.showWebviewDialog) {
        WebviewDialog(
            onDismissRequest = { viewModel.setShowWebviewDialog(false) }
        )
    }

    state.selectedStationForDetail?.let { station ->
        StationDetailSheet(
            station = station,
            history = state.detailPriceHistory,
            initialFuelType = state.detailHistoryFuelType,
            onFuelChange = { fuel -> viewModel.setDetailHistoryFuel(fuel) },
            onToggleFavorite = { viewModel.toggleFavorite(it) },
            onViewTwelveMonthStats = { viewModel.setSelectedTab(2) },
            onDismiss = { viewModel.clearSelectedStation() }
        )
    }
}

@Composable
private fun StationsTabContent(
    state: FuelUiState,
    viewModel: FuelViewModel,
    onRequestGpsPermission: () -> Unit
) {
    val filteredList = state.filteredStations
    val cheapestPrice = state.cheapestPrice

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Location Selector Card
        item {
            LocationCard(
                locationLabel = state.location.label,
                isRealGps = state.location.isRealGps,
                isLocating = state.isGpsLocating,
                onLocateClick = {
                    onRequestGpsPermission()
                    viewModel.triggerGpsLocate()
                },
                onChangeCityClick = {
                    viewModel.setShowCityPickerDialog(true)
                }
            )
        }

        // Fuel Type Selector
        item {
            FuelTypeSelector(
                selectedFuel = state.selectedFuel,
                onFuelSelected = { viewModel.setSelectedFuel(it) }
            )
        }

        // Radius Selector
        item {
            RadiusSelector(
                currentRadiusKm = state.radiusKm,
                onRadiusChange = { viewModel.setRadius(it) }
            )
        }

        // Price comparison stats summary banner
        item {
            PriceStatsBanner(
                cheapestPrice = state.cheapestPrice,
                averagePrice = state.averagePrice,
                highestPrice = state.highestPrice,
                maxSavings50L = state.maxSavings50L,
                stationCount = filteredList.size
            )
        }

        // Search & Filter Bar
        item {
            SearchAndFilterSection(
                searchQuery = state.searchQuery,
                onSearchChange = { viewModel.setSearchQuery(it) },
                sortOption = state.sortOption,
                onSortChange = { viewModel.setSortOption(it) },
                onlyOpen = state.onlyOpen,
                onOnlyOpenChange = { viewModel.setOnlyOpen(it) }
            )
        }

        // Error message if any
        if (state.errorMessage != null) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.ErrorOutline,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.error
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = state.errorMessage ?: "",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onErrorContainer,
                            modifier = Modifier.weight(1f)
                        )
                        IconButton(onClick = { viewModel.dismissError() }) {
                            Icon(Icons.Default.Clear, contentDescription = "Schließen", tint = MaterialTheme.colorScheme.error)
                        }
                    }
                }
            }
        }

        // Stations Count and Status Header
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "${filteredList.size} Tankstellen gefunden",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )

                if (state.isLoading) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(14.dp),
                            strokeWidth = 2.dp,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = "Aktualisiere...",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }

        // Empty state
        if (!state.isLoading && filteredList.isEmpty()) {
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 24.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.LocalGasStation,
                            contentDescription = null,
                            modifier = Modifier.size(48.dp),
                            tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.6f)
                        )
                        Text(
                            text = "Keine Tankstellen gefunden",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Vergrößere den Suchradius (z.B. auf 10 oder 15 km) oder deaktiviere den Filter 'Nur geöffnete'.",
                            style = MaterialTheme.typography.bodySmall,
                            textAlign = TextAlign.Center,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Button(
                            onClick = {
                                viewModel.setRadius(15f)
                                viewModel.setOnlyOpen(false)
                            }
                        ) {
                            Text("Suchradius auf 15 km erweitern")
                        }
                    }
                }
            }
        }

        // List of Stations
        items(filteredList, key = { it.id }) { station ->
            val isCheapest = cheapestPrice != null &&
                    station.getPriceFor(state.selectedFuel) == cheapestPrice &&
                    cheapestPrice > 0.0

            StationCard(
                station = station,
                selectedFuel = state.selectedFuel,
                isCheapest = isCheapest,
                onToggleFavorite = { viewModel.toggleFavorite(it) },
                onSelectStation = { viewModel.selectStationForDetail(it) }
            )
        }

        // MTS-K & CC BY 4.0 Attribution Footer
        item {
            AttributionFooter(onOpenSettings = { viewModel.setShowApiKeyDialog(true) })
        }
    }
}

@Composable
private fun FavoritesTabContent(
    state: FuelUiState,
    viewModel: FuelViewModel
) {
    val favorites = state.favorites

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Favorite,
                        contentDescription = null,
                        tint = Color(0xFFEF4444)
                    )
                    Text(
                        text = "Gespeicherte Favoriten (${favorites.size})",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                }

                if (favorites.isNotEmpty()) {
                    FilledTonalButton(
                        onClick = { viewModel.refreshFavoritePrices() },
                        enabled = !state.isRefreshingFavorites,
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        if (state.isRefreshingFavorites) {
                            CircularProgressIndicator(modifier = Modifier.size(14.dp), strokeWidth = 2.dp)
                        } else {
                            Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Preise live prüfen", style = MaterialTheme.typography.labelSmall)
                        }
                    }
                }
            }
        }

        if (favorites.isEmpty()) {
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 32.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Favorite,
                            contentDescription = null,
                            modifier = Modifier.size(48.dp),
                            tint = Color(0xFFEF4444).copy(alpha = 0.4f)
                        )
                        Text(
                            text = "Noch keine Favoriten",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Tippe auf das Herz-Symbol bei einer Tankstelle in der Liste, um sie hier für den schnellen Zugriff zu speichern.",
                            style = MaterialTheme.typography.bodySmall,
                            textAlign = TextAlign.Center,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Button(
                            onClick = { viewModel.setSelectedTab(0) }
                        ) {
                            Text("Tankstellen ansehen")
                        }
                    }
                }
            }
        } else {
            // Find live station information if already fetched
            items(favorites, key = { it.id }) { fav ->
                val liveStation = state.stations.find { it.id == fav.id } ?: fav
                StationCard(
                    station = liveStation.copy(isFavorite = true),
                    selectedFuel = state.selectedFuel,
                    isCheapest = false,
                    onToggleFavorite = { viewModel.toggleFavorite(it) },
                    onSelectStation = { viewModel.selectStationForDetail(it) }
                )
            }
        }
    }
}

@Composable
private fun CalculatorAndInfoTabContent(
    state: FuelUiState,
    viewModel: FuelViewModel
) {
    var tankVolume by remember { mutableFloatStateOf(50f) }
    val cheapest = state.cheapestPrice ?: 1.639
    val highest = state.highestPrice ?: 1.769
    val totalCostMin = (cheapest * tankVolume * 100).roundToInt() / 100.0
    val totalCostMax = (highest * tankVolume * 100).roundToInt() / 100.0
    val savings = (totalCostMax - totalCostMin * 100).roundToInt() / 100.0

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(2.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Calculate,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = "Tankfüllung & Ersparnis-Rechner",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Text(
                        text = "Tankvolumen deines Autos: ${tankVolume.toInt()} Liter",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold
                    )

                    Slider(
                        value = tankVolume,
                        onValueChange = { tankVolume = (it / 5).roundToInt() * 5f },
                        valueRange = 20f..100f,
                        steps = 15,
                        modifier = Modifier.testTag("tank_volume_slider")
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text("Günstigste Füllung", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text(String.format(Locale.GERMANY, "%.2f €", totalCostMin), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = PriceCheapest)
                        }

                        Column(horizontalAlignment = Alignment.End) {
                            Text("Teuerste Füllung", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text(String.format(Locale.GERMANY, "%.2f €", totalCostMax), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = Color(0xFF10B981).copy(alpha = 0.15f),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text(
                                text = "Mögliche Ersparnis beim Tanken:",
                                style = MaterialTheme.typography.labelMedium,
                                color = Color(0xFF10B981)
                            )
                            Text(
                                text = String.format(Locale.GERMANY, "bis zu %.2f €", totalCostMax - totalCostMin),
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.ExtraBold,
                                color = Color(0xFF10B981)
                            )
                            Text(
                                text = "bei einer einzigen Tankfüllung von ${tankVolume.toInt()}L für ${state.selectedFuel.displayName}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                fontSize = 11.sp
                            )
                        }
                    }
                }
            }
        }

        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Info,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = "Tipps zum günstigen Tanken",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Text(
                        text = "• Zeitfenster beachten: Sprit ist in Deutschland statistisch zwischen 18:00 und 22:00 Uhr am günstigsten.\n• Morgens teurer: Zwischen 6:00 und 9:00 Uhr sind die Preise oft um bis zu 10–15 Cent pro Liter höher.\n• Autobahntankstellen meiden: Tankstellen an Autobahnen sind oft 20–30 Cent pro Liter teurer als Stationen an der nächsten Ausfahrt.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        lineHeight = 20.sp
                    )
                }
            }
        }

        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { viewModel.setShowWebviewDialog(true) },
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f)
                )
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        modifier = Modifier.weight(1f),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Surface(
                            shape = CircleShape,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(40.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Default.Language,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.onPrimary,
                                    modifier = Modifier.size(22.dp)
                                )
                            }
                        }
                        Column {
                            Text(
                                text = "Aufrufbare Web-Version",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "Interaktives Web-Dashboard mit Live-Preisen & Rohöl-Charts",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    FilledTonalButton(
                        onClick = { viewModel.setShowWebviewDialog(true) }
                    ) {
                        Text("Öffnen")
                    }
                }
            }
        }

        item {
            AttributionFooter(onOpenSettings = { viewModel.setShowApiKeyDialog(true) })
        }
    }
}

@Composable
private fun LocationCard(
    locationLabel: String,
    isRealGps: Boolean,
    isLocating: Boolean,
    onLocateClick: () -> Unit,
    onChangeCityClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(1.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                modifier = Modifier.weight(1f),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .background(
                            if (isRealGps) StatusOpen.copy(alpha = 0.15f) else MaterialTheme.colorScheme.primary.copy(alpha = 0.15f),
                            shape = CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = if (isRealGps) Icons.Default.MyLocation else Icons.Default.LocationOn,
                        contentDescription = null,
                        tint = if (isRealGps) StatusOpen else MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(20.dp)
                    )
                }

                Column {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(
                            text = if (isRealGps) "Mein Handy-Standort" else "Ausgewählter Ort",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        if (isRealGps) {
                            Box(
                                modifier = Modifier
                                    .size(6.dp)
                                    .background(StatusOpen, shape = CircleShape)
                            )
                        }
                    }

                    Text(
                        text = locationLabel,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                // GPS locate button
                IconButton(
                    onClick = onLocateClick,
                    modifier = Modifier.testTag("gps_locate_btn")
                ) {
                    if (isLocating) {
                        CircularProgressIndicator(modifier = Modifier.size(18.dp), strokeWidth = 2.dp)
                    } else {
                        Icon(
                            imageVector = Icons.Default.MyLocation,
                            contentDescription = "GPS aktualisieren",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                }

                // Change City button
                Button(
                    onClick = onChangeCityClick,
                    shape = RoundedCornerShape(8.dp),
                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
                    modifier = Modifier.testTag("change_city_btn")
                ) {
                    Text("Ort wählen", style = MaterialTheme.typography.labelSmall)
                }
            }
        }
    }
}

@Composable
private fun SearchAndFilterSection(
    searchQuery: String,
    onSearchChange: (String) -> Unit,
    sortOption: SortOption,
    onSortChange: (SortOption) -> Unit,
    onlyOpen: Boolean,
    onOnlyOpenChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        OutlinedTextField(
            value = searchQuery,
            onValueChange = onSearchChange,
            placeholder = { Text("Tankstelle, Marke oder Straße filtern...") },
            leadingIcon = {
                Icon(Icons.Default.Search, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
            },
            trailingIcon = {
                if (searchQuery.isNotEmpty()) {
                    IconButton(onClick = { onSearchChange("") }) {
                        Icon(Icons.Default.Clear, contentDescription = "Löschen")
                    }
                }
            },
            singleLine = true,
            modifier = Modifier
                .fillMaxWidth()
                .testTag("station_search_input"),
            shape = RoundedCornerShape(12.dp),
            colors = OutlinedTextFieldDefaults.colors(
                unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)
            )
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Sort chip: Price vs Distance
            FilterChip(
                selected = sortOption == SortOption.PRICE,
                onClick = {
                    val next = if (sortOption == SortOption.PRICE) SortOption.DISTANCE else SortOption.PRICE
                    onSortChange(next)
                },
                label = {
                    Text(if (sortOption == SortOption.PRICE) "Günstigste" else "Nächste")
                },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Sort,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                },
                modifier = Modifier.testTag("sort_chip"),
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = MaterialTheme.colorScheme.secondaryContainer,
                    selectedLabelColor = MaterialTheme.colorScheme.onSecondaryContainer
                )
            )

            // Open now filter
            FilterChip(
                selected = onlyOpen,
                onClick = { onOnlyOpenChange(!onlyOpen) },
                label = { Text("Nur geöffnet") },
                leadingIcon = {
                    if (onlyOpen) {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                },
                modifier = Modifier.testTag("filter_only_open_chip"),
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = StatusOpen.copy(alpha = 0.15f),
                    selectedLabelColor = StatusOpen
                )
            )
        }
    }
}

@Composable
private fun AttributionFooter(
    onOpenSettings: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable { onOpenSettings() },
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f)
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Datenquelle: Tankerkönig (CC BY 4.0) & MTS-K",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontWeight = FontWeight.SemiBold
            )
            Text(
                text = "Markttransparenzstelle für Kraftstoffe des Bundeskartellamtes • Tippen für API-Status",
                style = MaterialTheme.typography.bodySmall,
                fontSize = 10.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f)
            )
        }
    }
}
