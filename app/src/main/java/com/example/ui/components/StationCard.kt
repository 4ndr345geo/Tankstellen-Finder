package com.example.ui.components

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Navigation
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.ShowChart
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.BaselineShift
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.FuelType
import com.example.data.model.Station
import com.example.ui.theme.FuelDiesel
import com.example.ui.theme.FuelE10
import com.example.ui.theme.FuelE5
import com.example.ui.theme.PriceCheapest
import com.example.ui.theme.StatusClosed
import com.example.ui.theme.StatusOpen
import java.util.Locale

@Composable
fun StationCard(
    station: Station,
    selectedFuel: FuelType,
    isCheapest: Boolean,
    onToggleFavorite: (Station) -> Unit,
    onSelectStation: (Station) -> Unit = {},
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val currentPrice = station.getPriceFor(selectedFuel)

    val cardBorderColor by animateColorAsState(
        targetValue = if (isCheapest) PriceCheapest else MaterialTheme.colorScheme.outline.copy(alpha = 0.5f),
        label = "cardBorderColor"
    )

    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("station_card_${station.id}")
            .border(
                width = if (isCheapest) 1.5.dp else 0.5.dp,
                color = cardBorderColor,
                shape = RoundedCornerShape(16.dp)
            )
            .clickable { onSelectStation(station) },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = if (isCheapest) 3.dp else 1.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // Header Row: Brand badge, Name, Cheapest Tag, Favorite
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Brand pill
                BrandBadge(brand = station.brand)

                Spacer(modifier = Modifier.width(10.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(
                            text = station.brand.ifBlank { station.name },
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )

                        if (isCheapest && currentPrice != null && currentPrice > 0.0) {
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = PriceCheapest.copy(alpha = 0.18f),
                                border = androidx.compose.foundation.BorderStroke(1.dp, PriceCheapest)
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(2.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Star,
                                        contentDescription = "Günstigste Tankstelle",
                                        tint = PriceCheapest,
                                        modifier = Modifier.size(12.dp)
                                    )
                                    Text(
                                        text = "Bester Preis",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = PriceCheapest,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 10.sp
                                    )
                                }
                            }
                        }
                    }

                    Text(
                        text = station.name,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                // Favorite button
                IconButton(
                    onClick = { onToggleFavorite(station) },
                    modifier = Modifier.testTag("fav_btn_${station.id}")
                ) {
                    Icon(
                        imageVector = if (station.isFavorite) Icons.Filled.Favorite else Icons.Outlined.FavoriteBorder,
                        contentDescription = if (station.isFavorite) "Aus Favoriten entfernen" else "Zu Favoriten hinzufügen",
                        tint = if (station.isFavorite) Color(0xFFEF4444) else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Middle section: Price highlight + Open/Distance Status
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Address & Status
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = station.fullAddress,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        StatusIndicator(isOpen = station.isOpen)

                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.DirectionsCar,
                                    contentDescription = "Entfernung",
                                    modifier = Modifier.size(12.dp),
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(
                                    text = String.format(Locale.GERMANY, "%.1f km", station.dist),
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.width(12.dp))

                // Price display for selected fuel
                PriceTag(
                    price = currentPrice,
                    isOpen = station.isOpen,
                    fuelName = selectedFuel.shortName
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Other fuel prices breakdown bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                        shape = RoundedCornerShape(8.dp)
                    )
                    .padding(horizontal = 10.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                MiniPriceBadge("Diesel", station.diesel, FuelDiesel, selectedFuel == FuelType.DIESEL)
                Box(
                    modifier = Modifier
                        .width(1.dp)
                        .height(16.dp)
                        .background(MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))
                )
                MiniPriceBadge("Super E5", station.e5, FuelE5, selectedFuel == FuelType.E5)
                Box(
                    modifier = Modifier
                        .width(1.dp)
                        .height(16.dp)
                        .background(MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))
                )
                MiniPriceBadge("Super E10", station.e10, FuelE10, selectedFuel == FuelType.E10)
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Actions: Navigation, Details & 30T-Historie, Share
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Navigation button
                Button(
                    onClick = { launchNavigation(context, station) },
                    modifier = Modifier
                        .weight(1.2f)
                        .testTag("nav_btn_${station.id}"),
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.secondary
                    )
                ) {
                    Icon(
                        imageVector = Icons.Default.Navigation,
                        contentDescription = "Navigation",
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Route", style = MaterialTheme.typography.labelLarge)
                }

                // 30-Day History & Details button
                FilledTonalButton(
                    onClick = { onSelectStation(station) },
                    modifier = Modifier
                        .weight(1.4f)
                        .testTag("history_btn_${station.id}"),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.ShowChart,
                        contentDescription = "30-Tage Historie",
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("30T-Historie", style = MaterialTheme.typography.labelMedium)
                }

                FilledTonalIconButton(
                    onClick = { shareStation(context, station, selectedFuel) },
                    modifier = Modifier.testTag("share_btn_${station.id}"),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Share,
                        contentDescription = "Preise teilen",
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun PriceTag(
    price: Double?,
    isOpen: Boolean,
    fuelName: String,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        color = if (isOpen && price != null && price > 0.0) {
            MaterialTheme.colorScheme.primaryContainer
        } else {
            MaterialTheme.colorScheme.surfaceVariant
        }
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
            horizontalAlignment = Alignment.End
        ) {
            Text(
                text = fuelName,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f),
                fontWeight = FontWeight.SemiBold
            )

            if (!isOpen) {
                Text(
                    text = "Geschlossen",
                    style = MaterialTheme.typography.bodyMedium,
                    color = StatusClosed,
                    fontWeight = FontWeight.Bold
                )
            } else if (price == null || price <= 0.0) {
                Text(
                    text = "k.A.",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontWeight = FontWeight.Bold
                )
            } else {
                val fullFormatted = String.format(Locale.US, "%.3f", price)
                val basePart = fullFormatted.substring(0, fullFormatted.length - 1)
                val lastDigit = fullFormatted.last().toString()

                val annotatedPrice = buildAnnotatedString {
                    append(basePart.replace('.', ','))
                    withStyle(
                        SpanStyle(
                            fontSize = 13.sp,
                            baselineShift = BaselineShift.Superscript,
                            fontWeight = FontWeight.Bold
                        )
                    ) {
                        append(lastDigit)
                    }
                    append(" €")
                }

                Text(
                    text = annotatedPrice,
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 22.sp
                )
            }
        }
    }
}

@Composable
fun MiniPriceBadge(
    label: String,
    price: Double?,
    accentColor: Color,
    isSelected: Boolean
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = if (isSelected) accentColor else MaterialTheme.colorScheme.onSurfaceVariant,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
            fontSize = 11.sp
        )
        Text(
            text = if (price != null && price > 0.0) {
                String.format(Locale.GERMANY, "%.2f⁹ €", price)
            } else {
                "—"
            },
            style = MaterialTheme.typography.bodySmall,
            color = if (isSelected) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant,
            fontWeight = if (isSelected) FontWeight.ExtraBold else FontWeight.Medium,
            fontSize = 12.sp
        )
    }
}

@Composable
fun BrandBadge(brand: String) {
    val upper = brand.uppercase()
    val (bg, fg) = when {
        upper.contains("ARAL") -> Pair(Color(0xFF003399), Color.White)
        upper.contains("SHELL") -> Pair(Color(0xFFFFD100), Color(0xFFDD1D21))
        upper.contains("JET") -> Pair(Color(0xFFFFEE00), Color(0xFF003087))
        upper.contains("TOTAL") -> Pair(Color(0xFFE2001A), Color.White)
        upper.contains("ESSO") -> Pair(Color(0xFFEE1D23), Color.White)
        upper.contains("HEM") -> Pair(Color(0xFF0072BC), Color(0xFFFFB612))
        upper.contains("AVIA") -> Pair(Color(0xFFE30613), Color.White)
        upper.contains("STAR") -> Pair(Color(0xFFD60812), Color.White)
        upper.contains("AGIP") || upper.contains("ENI") -> Pair(Color(0xFFFFDD00), Color.Black)
        else -> Pair(MaterialTheme.colorScheme.secondaryContainer, MaterialTheme.colorScheme.onSecondaryContainer)
    }

    Box(
        modifier = Modifier
            .size(38.dp)
            .clip(RoundedCornerShape(10.dp))
            .background(bg),
        contentAlignment = Alignment.Center
    ) {
        val initials = brand.take(2).uppercase().ifBlank { "TS" }
        Text(
            text = initials,
            color = fg,
            fontWeight = FontWeight.Black,
            fontSize = 14.sp
        )
    }
}

@Composable
fun StatusIndicator(isOpen: Boolean) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Box(
            modifier = Modifier
                .size(8.dp)
                .background(if (isOpen) StatusOpen else StatusClosed, shape = CircleShape)
        )
        Text(
            text = if (isOpen) "Geöffnet" else "Geschlossen",
            style = MaterialTheme.typography.labelSmall,
            color = if (isOpen) StatusOpen else StatusClosed,
            fontWeight = FontWeight.SemiBold
        )
    }
}

private fun launchNavigation(context: Context, station: Station) {
    val encodedAddress = Uri.encode("${station.name}, ${station.fullAddress}")
    val geoUri = if (station.lat != 0.0 && station.lng != 0.0) {
        Uri.parse("geo:${station.lat},${station.lng}?q=${station.lat},${station.lng}($encodedAddress)")
    } else {
        Uri.parse("geo:0,0?q=$encodedAddress")
    }

    val intent = Intent(Intent.ACTION_VIEW, geoUri).apply {
        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    }

    val chooser = Intent.createChooser(intent, "Navigations-App auswählen").apply {
        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    }

    try {
        context.startActivity(chooser)
    } catch (e: Exception) {
        val webUri = Uri.parse("https://www.google.com/maps/dir/?api=1&destination=" + Uri.encode("${station.name}, ${station.fullAddress}"))
        context.startActivity(Intent(Intent.ACTION_VIEW, webUri).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        })
    }
}

private fun shareStation(context: Context, station: Station, fuel: FuelType) {
    val priceStr = station.getPriceFor(fuel)?.let { String.format(Locale.GERMANY, "%.3f €", it) } ?: "k.A."
    val text = """
        Gefunden mit TankFinder:
        ${station.brand} Tankstelle
        ${station.fullAddress}
        Aktueller Preis für ${fuel.displayName}: $priceStr
        Entfernung: ${String.format(Locale.GERMANY, "%.1f km", station.dist)}
    """.trimIndent()

    val sendIntent = Intent().apply {
        action = Intent.ACTION_SEND
        putExtra(Intent.EXTRA_TEXT, text)
        type = "text/plain"
    }
    context.startActivity(Intent.createChooser(sendIntent, "Tankstelle teilen").apply {
        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    })
}
