package com.example.ui.dialogs

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.Navigation
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.FuelType
import com.example.data.model.Station
import com.example.data.model.StationPriceHistory
import com.example.ui.components.BrandBadge
import com.example.ui.components.PriceHistoryChart
import com.example.ui.components.StatusIndicator
import com.example.ui.theme.FuelDiesel
import com.example.ui.theme.FuelE10
import com.example.ui.theme.FuelE5
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StationDetailSheet(
    station: Station,
    history: StationPriceHistory?,
    initialFuelType: FuelType,
    onFuelChange: (FuelType) -> Unit,
    onToggleFavorite: (Station) -> Unit,
    onViewTwelveMonthStats: () -> Unit = {},
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var selectedChartFuel by remember(initialFuelType) {
        mutableStateOf(if (initialFuelType == FuelType.ALL) FuelType.E5 else initialFuelType)
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
        containerColor = MaterialTheme.colorScheme.surface,
        modifier = Modifier.testTag("station_detail_sheet")
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .padding(bottom = 32.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Header Row: Brand, Name, Favorite Button
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                BrandBadge(brand = station.brand)
                Spacer(modifier = Modifier.width(12.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = station.brand.ifBlank { station.name },
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Black
                    )
                    Text(
                        text = station.name,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                // Favorite toggle
                FilledTonalButton(
                    onClick = { onToggleFavorite(station) },
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.testTag("detail_fav_btn")
                ) {
                    Icon(
                        imageVector = if (station.isFavorite) Icons.Filled.Favorite else Icons.Outlined.FavoriteBorder,
                        contentDescription = null,
                        tint = if (station.isFavorite) androidx.compose.ui.graphics.Color(0xFFEF4444) else MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(if (station.isFavorite) "Favorit" else "Merken")
                }
            }

            // Status and distance row
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                StatusIndicator(isOpen = station.isOpen)

                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.DirectionsCar,
                            contentDescription = null,
                            modifier = Modifier.size(14.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = String.format(Locale.GERMANY, "%.1f km entfernt", station.dist),
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }

            // Navigation Card with pre-filled address and direct app chooser
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("navigation_card"),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Navigation,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.secondary,
                            modifier = Modifier.size(20.dp)
                        )
                        Text(
                            text = "Navigation & Zieladresse",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    // Pre-filled address display
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = MaterialTheme.colorScheme.surface,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "Vorausgefüllte Adresse:",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(
                                    text = station.fullAddress,
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }

                            IconButton(
                                onClick = {
                                    copyAddressToClipboard(context, station.fullAddress)
                                },
                                modifier = Modifier.testTag("copy_address_btn")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.ContentCopy,
                                    contentDescription = "Adresse kopieren",
                                    tint = MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                    }

                    // Navigation buttons
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        // Open Google Maps / Default Navigation App
                        Button(
                            onClick = { openNavigationApp(context, station) },
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier
                                .weight(1f)
                                .testTag("open_maps_btn"),
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary)
                        ) {
                            Icon(Icons.Default.Navigation, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Google Maps", style = MaterialTheme.typography.labelLarge)
                        }

                        // Open with map chooser / general map apps
                        OutlinedButton(
                            onClick = { openMapChooser(context, station) },
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.testTag("open_map_chooser_btn")
                        ) {
                            Icon(Icons.Default.Map, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Karten-App")
                        }
                    }
                }
            }

            // Current Prices Overview Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text(
                        text = "Aktuelle Kraftstoffpreise",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        PriceDetailColumn("Super E5", station.e5, FuelE5)
                        PriceDetailColumn("Super E10", station.e10, FuelE10)
                        PriceDetailColumn("Diesel", station.diesel, FuelDiesel)
                    }
                }
            }

            // 30-Day Price History Chart Section
            if (history != null) {
                PriceHistoryChart(
                    history = history,
                    selectedFuel = selectedChartFuel,
                    onFuelChange = { newFuel ->
                        selectedChartFuel = newFuel
                        onFuelChange(newFuel)
                    }
                )
            }

            // Quick Link to 12-Month Statistics with Holidays
            OutlinedButton(
                onClick = {
                    onViewTwelveMonthStats()
                    onDismiss()
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("detail_view_12m_btn"),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.DateRange,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp),
                    tint = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("12-Monats-Statistik & Ferien anzeigen")
            }
        }
    }
}

@Composable
private fun PriceDetailColumn(
    fuelName: String,
    price: Double?,
    color: androidx.compose.ui.graphics.Color
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = fuelName,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = if (price != null && price > 0.0) {
                String.format(Locale.GERMANY, "%.3f €", price)
            } else {
                "—"
            },
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.ExtraBold,
            color = color
        )
    }
}

private fun openNavigationApp(context: Context, station: Station) {
    // Direct Google Maps navigation intent with coordinates and mode
    val uri = if (station.lat != 0.0 && station.lng != 0.0) {
        Uri.parse("google.navigation:q=${station.lat},${station.lng}&mode=d")
    } else {
        Uri.parse("google.navigation:q=" + Uri.encode("${station.name}, ${station.fullAddress}"))
    }

    val intent = Intent(Intent.ACTION_VIEW, uri).apply {
        setPackage("com.google.android.apps.maps")
        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    }

    try {
        context.startActivity(intent)
    } catch (e: Exception) {
        // Fallback to general map chooser
        openMapChooser(context, station)
    }
}

private fun openMapChooser(context: Context, station: Station) {
    // Geo URI with pre-filled label/address
    val encodedAddress = Uri.encode("${station.name}, ${station.fullAddress}")
    val geoUri = if (station.lat != 0.0 && station.lng != 0.0) {
        Uri.parse("geo:${station.lat},${station.lng}?q=${station.lat},${station.lng}($encodedAddress)")
    } else {
        Uri.parse("geo:0,0?q=$encodedAddress")
    }

    val mapIntent = Intent(Intent.ACTION_VIEW, geoUri).apply {
        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    }

    val chooser = Intent.createChooser(mapIntent, "Navigations-App auswählen").apply {
        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    }

    try {
        context.startActivity(chooser)
    } catch (e: Exception) {
        // Web fallback
        val webUri = Uri.parse("https://www.google.com/maps/dir/?api=1&destination=" + Uri.encode("${station.name}, ${station.fullAddress}"))
        context.startActivity(Intent(Intent.ACTION_VIEW, webUri).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        })
    }
}

private fun copyAddressToClipboard(context: Context, address: String) {
    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as? ClipboardManager
    val clip = ClipData.newPlainText("Tankstellen-Adresse", address)
    clipboard?.setPrimaryClip(clip)
    Toast.makeText(context, "Adresse in Zwischenablage kopiert", Toast.LENGTH_SHORT).show()
}
