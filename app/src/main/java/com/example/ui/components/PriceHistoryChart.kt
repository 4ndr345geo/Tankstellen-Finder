package com.example.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
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
import androidx.compose.material.icons.filled.ShowChart
import androidx.compose.material.icons.filled.TrendingDown
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.FuelType
import com.example.data.model.PriceHistoryPoint
import com.example.data.model.StationPriceHistory
import com.example.ui.theme.FuelDiesel
import com.example.ui.theme.FuelE10
import com.example.ui.theme.FuelE5
import com.example.ui.theme.PriceCheapest
import com.example.ui.theme.StatusClosed
import com.example.ui.theme.StatusOpen
import java.util.Locale
import kotlin.math.abs

@Composable
fun PriceHistoryChart(
    history: StationPriceHistory,
    selectedFuel: FuelType,
    onFuelChange: (FuelType) -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedPointIndex by remember(history) { mutableStateOf<Int?>(null) }
    val points = history.points
    if (points.isEmpty()) return

    val fuelColor = when (selectedFuel) {
        FuelType.DIESEL -> FuelDiesel
        FuelType.E5 -> FuelE5
        FuelType.E10 -> FuelE10
        FuelType.ALL -> MaterialTheme.colorScheme.primary
    }

    val activePoint = selectedPointIndex?.let { points.getOrNull(it) } ?: points.last()

    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("price_history_card"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // Header with title and 30-day tag
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.ShowChart,
                        contentDescription = "Preishistorie",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(20.dp)
                    )
                    Text(
                        text = "Preisentwicklung (30 Tage)",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                }

                // Trend badge
                val diff = history.priceDiff30Days
                val isDrop = diff <= 0
                val trendColor = if (isDrop) StatusOpen else StatusClosed
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = trendColor.copy(alpha = 0.15f)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(2.dp)
                    ) {
                        Icon(
                            imageVector = if (isDrop) Icons.Default.TrendingDown else Icons.Default.TrendingUp,
                            contentDescription = null,
                            tint = trendColor,
                            modifier = Modifier.size(14.dp)
                        )
                        Text(
                            text = String.format(Locale.GERMANY, "%+.1f ct", diff * 100),
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = trendColor,
                            fontSize = 11.sp
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Fuel Selector chips: [Super E5] [Super E10] [Diesel]
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                listOf(FuelType.E5, FuelType.E10, FuelType.DIESEL).forEach { fuel ->
                    val isSelected = selectedFuel == fuel
                    FilterChip(
                        selected = isSelected,
                        onClick = { onFuelChange(fuel) },
                        label = { Text(fuel.displayName, fontSize = 12.sp) },
                        modifier = Modifier
                            .weight(1f)
                            .testTag("chart_fuel_${fuel.apiParam}"),
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = when (fuel) {
                                FuelType.E5 -> FuelE5
                                FuelType.E10 -> FuelE10
                                FuelType.DIESEL -> FuelDiesel
                                else -> MaterialTheme.colorScheme.primary
                            },
                            selectedLabelColor = Color.White
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Active / Hovered Point Details Banner
            Surface(
                shape = RoundedCornerShape(10.dp),
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = activePoint.fullDateFormatted,
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = if (activePoint.dayOffset == 0) "Aktueller Preis" else "Tagesdurchschnitt",
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.Medium
                        )
                    }

                    Text(
                        text = String.format(Locale.GERMANY, "%.3f €", activePoint.price),
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.ExtraBold,
                        color = fuelColor
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Canvas Chart
            val minP = history.minPrice - 0.015
            val maxP = history.maxPrice + 0.015
            val rangeP = (maxP - minP).coerceAtLeast(0.04)

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(180.dp)
                    .pointerInput(points) {
                        detectTapGestures { offset ->
                            val fraction = (offset.x / size.width).coerceIn(0f, 1f)
                            val index = ((points.size - 1) * fraction).toInt()
                            selectedPointIndex = index.coerceIn(0, points.size - 1)
                        }
                    }
                    .pointerInput(points) {
                        detectDragGestures { change, _ ->
                            change.consume()
                            val fraction = (change.position.x / size.width).coerceIn(0f, 1f)
                            val index = ((points.size - 1) * fraction).toInt()
                            selectedPointIndex = index.coerceIn(0, points.size - 1)
                        }
                    }
                    .testTag("chart_canvas")
            ) {
                Canvas(modifier = Modifier.matchParentSize()) {
                    val w = size.width
                    val h = size.height
                    val paddingBottom = 20.dp.toPx()
                    val chartHeight = h - paddingBottom

                    // Draw 3 subtle horizontal gridlines
                    val gridSteps = 3
                    for (step in 0..gridSteps) {
                        val y = (chartHeight / gridSteps) * step
                        drawLine(
                            color = Color.Gray.copy(alpha = 0.2f),
                            start = Offset(0f, y),
                            end = Offset(w, y),
                            strokeWidth = 1.dp.toPx(),
                            pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 10f), 0f)
                        )
                    }

                    // Build line and fill path
                    val path = Path()
                    val fillPath = Path()

                    val stepX = w / (points.size - 1).toFloat()

                    fun getY(price: Double): Float {
                        val normalized = ((price - minP) / rangeP).coerceIn(0.0, 1.0)
                        return (chartHeight * (1.0 - normalized)).toFloat()
                    }

                    points.forEachIndexed { i, pt ->
                        val x = i * stepX
                        val y = getY(pt.price)

                        if (i == 0) {
                            path.moveTo(x, y)
                            fillPath.moveTo(x, chartHeight)
                            fillPath.lineTo(x, y)
                        } else {
                            val prevX = (i - 1) * stepX
                            val prevY = getY(points[i - 1].price)
                            val cx = (prevX + x) / 2f
                            path.cubicTo(cx, prevY, cx, y, x, y)
                            fillPath.cubicTo(cx, prevY, cx, y, x, y)
                        }
                    }

                    // Close fill path
                    fillPath.lineTo(w, chartHeight)
                    fillPath.close()

                    // Draw area gradient under the curve
                    drawPath(
                        path = fillPath,
                        brush = Brush.verticalGradient(
                            colors = listOf(fuelColor.copy(alpha = 0.35f), fuelColor.copy(alpha = 0.02f)),
                            startY = 0f,
                            endY = chartHeight
                        )
                    )

                    // Draw curve line
                    drawPath(
                        path = path,
                        color = fuelColor,
                        style = Stroke(
                            width = 3.dp.toPx(),
                            cap = StrokeCap.Round
                        )
                    )

                    // Highlight min and max points
                    history.minPricePoint?.let { minPt ->
                        val idx = points.indexOf(minPt)
                        if (idx >= 0) {
                            val x = idx * stepX
                            val y = getY(minPt.price)
                            drawCircle(color = StatusOpen, radius = 5.dp.toPx(), center = Offset(x, y))
                            drawCircle(color = Color.White, radius = 2.dp.toPx(), center = Offset(x, y))
                        }
                    }

                    history.maxPricePoint?.let { maxPt ->
                        val idx = points.indexOf(maxPt)
                        if (idx >= 0) {
                            val x = idx * stepX
                            val y = getY(maxPt.price)
                            drawCircle(color = StatusClosed, radius = 5.dp.toPx(), center = Offset(x, y))
                            drawCircle(color = Color.White, radius = 2.dp.toPx(), center = Offset(x, y))
                        }
                    }

                    // Draw selected vertical scrubber indicator if active
                    val activeIdx = selectedPointIndex ?: (points.size - 1)
                    val activeX = activeIdx * stepX
                    val activeY = getY(points[activeIdx].price)

                    drawLine(
                        color = fuelColor.copy(alpha = 0.8f),
                        start = Offset(activeX, 0f),
                        end = Offset(activeX, chartHeight),
                        strokeWidth = 1.5.dp.toPx(),
                        pathEffect = PathEffect.dashPathEffect(floatArrayOf(6f, 6f), 0f)
                    )

                    drawCircle(
                        color = Color.White,
                        radius = 6.dp.toPx(),
                        center = Offset(activeX, activeY)
                    )
                    drawCircle(
                        color = fuelColor,
                        radius = 4.dp.toPx(),
                        center = Offset(activeX, activeY)
                    )
                }
            }

            // X-Axis date labels (4 ticks across the 30-day window)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(points.first().dateLabel, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text(points[9].dateLabel, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text(points[19].dateLabel, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text(points.last().dateLabel, style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
            }

            Spacer(modifier = Modifier.height(14.dp))

            // 3-Metric Summary below chart: Min, Avg, Max
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                SummaryMetric(
                    title = "30T-Tiefstpreis",
                    value = String.format(Locale.GERMANY, "%.3f €", history.minPrice),
                    date = history.minPricePoint?.dateLabel ?: "",
                    color = StatusOpen
                )

                SummaryMetric(
                    title = "30T-Durchschnitt",
                    value = String.format(Locale.GERMANY, "%.3f €", history.avgPrice),
                    date = "30 Tage",
                    color = MaterialTheme.colorScheme.onSurface
                )

                SummaryMetric(
                    title = "30T-Höchstpreis",
                    value = String.format(Locale.GERMANY, "%.3f €", history.maxPrice),
                    date = history.maxPricePoint?.dateLabel ?: "",
                    color = StatusClosed
                )
            }
        }
    }
}

@Composable
private fun SummaryMetric(
    title: String,
    value: String,
    date: String,
    color: Color
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = title,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            fontSize = 10.sp
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Bold,
            color = color
        )
        if (date.isNotBlank()) {
            Text(
                text = date,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                fontSize = 10.sp
            )
        }
    }
}
