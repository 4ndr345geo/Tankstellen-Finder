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
import androidx.compose.material.icons.filled.BeachAccess
import androidx.compose.material.icons.filled.Celebration
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Event
import androidx.compose.material.icons.filled.OilBarrel
import androidx.compose.material.icons.filled.ShowChart
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
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.FuelType
import com.example.data.model.MonthlyPriceStat
import com.example.data.model.TwelveMonthHistory
import com.example.ui.theme.FuelDiesel
import com.example.ui.theme.FuelE10
import com.example.ui.theme.FuelE5
import com.example.ui.theme.PriceCheapest
import com.example.ui.theme.StatusClosed
import com.example.ui.theme.StatusOpen
import java.util.Locale

@Composable
fun TwelveMonthChart(
    history: TwelveMonthHistory,
    selectedMonthIndex: Int,
    onSelectMonth: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val months = history.monthlyStats
    if (months.isEmpty()) return

    var showOilCurve by remember { mutableStateOf(true) }

    val primaryColor = MaterialTheme.colorScheme.primary
    val fuelColor = when (history.fuelType) {
        FuelType.DIESEL -> FuelDiesel
        FuelType.E5 -> FuelE5
        FuelType.E10 -> FuelE10
        FuelType.ALL -> primaryColor
    }

    val oilColor = Color(0xFFD97706) // Warm amber-copper for crude oil

    val minP = (history.overallMinPrice - 0.02).coerceAtLeast(1.0)
    val maxP = history.overallMaxPrice + 0.02
    val rangeP = (maxP - minP).coerceAtLeast(0.08)

    val minOil = (history.minOilPriceUsd - 3.0).coerceAtLeast(50.0)
    val maxOil = history.maxOilPriceUsd + 3.0
    val rangeOil = (maxOil - minOil).coerceAtLeast(10.0)

    val activeMonth = months.getOrNull(selectedMonthIndex) ?: months.last()

    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("twelve_month_chart_card"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // Header
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
                        imageVector = Icons.Default.ShowChart,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(20.dp)
                    )
                    Text(
                        text = "12M: Spritpreis vs. Rohöl",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                }

                // Oil curve toggle chip
                FilterChip(
                    selected = showOilCurve,
                    onClick = { showOilCurve = !showOilCurve },
                    label = { Text("Rohöl Brent ($)", fontSize = 11.sp) },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.OilBarrel,
                            contentDescription = null,
                            modifier = Modifier.size(14.dp)
                        )
                    },
                    modifier = Modifier.testTag("toggle_oil_curve_chip"),
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = oilColor.copy(alpha = 0.2f),
                        selectedLabelColor = oilColor,
                        selectedLeadingIconColor = oilColor
                    )
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Active Point Detail Banner (Fuel price + Crude oil price)
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
                            text = activeMonth.monthFullLabel,
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = if (activeMonth.isVacationMonth) "Ferienmonat (${history.state.shortCode})" else "Nebensaison",
                            style = MaterialTheme.typography.labelSmall,
                            color = if (activeMonth.isVacationMonth) Color(0xFFD97706) else MaterialTheme.colorScheme.onSurfaceVariant,
                            fontSize = 11.sp
                        )
                    }

                    Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                        Column(horizontalAlignment = Alignment.End) {
                            Text(
                                text = "Spritpreis",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                fontSize = 10.sp
                            )
                            Text(
                                text = String.format(Locale.GERMANY, "%.3f €/L", activeMonth.avgPrice),
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Black,
                                color = fuelColor
                            )
                        }

                        Column(horizontalAlignment = Alignment.End) {
                            Text(
                                text = "Rohöl Brent",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                fontSize = 10.sp
                            )
                            Text(
                                text = String.format(Locale.GERMANY, "%.1f $/bbl", activeMonth.brentOilUsd),
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Black,
                                color = oilColor
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Legend indicators row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    Box(modifier = Modifier.size(10.dp, 3.dp).background(fuelColor, RoundedCornerShape(1.dp)))
                    Text("Spritpreis (€/L)", style = MaterialTheme.typography.labelSmall, fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }

                if (showOilCurve) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        Box(modifier = Modifier.size(10.dp, 3.dp).background(oilColor, RoundedCornerShape(1.dp)))
                        Text("Rohöl Brent ($/bbl)", style = MaterialTheme.typography.labelSmall, fontSize = 10.sp, color = oilColor)
                    }
                }

                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    Box(modifier = Modifier.size(6.dp).background(Color(0xFFF59E0B), CircleShape))
                    Text("Ferien", style = MaterialTheme.typography.labelSmall, fontSize = 10.sp, color = Color(0xFFD97706))
                }

                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    Box(modifier = Modifier.size(6.dp).background(Color(0xFF0F766E), CircleShape))
                    Text("Feiertag", style = MaterialTheme.typography.labelSmall, fontSize = 10.sp, color = Color(0xFF0F766E))
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Canvas Chart
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
                    .pointerInput(months) {
                        detectTapGestures { offset ->
                            val colW = size.width / months.size
                            val idx = (offset.x / colW).toInt().coerceIn(0, months.size - 1)
                            onSelectMonth(idx)
                        }
                    }
                    .pointerInput(months) {
                        detectDragGestures { change, _ ->
                            change.consume()
                            val colW = size.width / months.size
                            val idx = (change.position.x / colW).toInt().coerceIn(0, months.size - 1)
                            onSelectMonth(idx)
                        }
                    }
                    .testTag("twelve_month_canvas")
            ) {
                Canvas(modifier = Modifier.matchParentSize()) {
                    val w = size.width
                    val h = size.height
                    val colWidth = w / months.size
                    val paddingBottom = 22.dp.toPx()
                    val chartHeight = h - paddingBottom

                    fun getFuelY(price: Double): Float {
                        val norm = ((price - minP) / rangeP).coerceIn(0.0, 1.0)
                        return (chartHeight * (1.0 - norm)).toFloat()
                    }

                    fun getOilY(usd: Double): Float {
                        val norm = ((usd - minOil) / rangeOil).coerceIn(0.0, 1.0)
                        return (chartHeight * (1.0 - norm)).toFloat()
                    }

                    // 1. Vacation background columns
                    months.forEachIndexed { i, stat ->
                        val colLeft = i * colWidth
                        if (stat.isVacationMonth) {
                            drawRect(
                                color = Color(0xFFF59E0B).copy(alpha = 0.12f),
                                topLeft = Offset(colLeft + 2.dp.toPx(), 0f),
                                size = Size(colWidth - 4.dp.toPx(), chartHeight)
                            )
                        }

                        // Selected column highlight
                        if (i == selectedMonthIndex) {
                            drawRect(
                                color = primaryColor.copy(alpha = 0.16f),
                                topLeft = Offset(colLeft, 0f),
                                size = Size(colWidth, chartHeight)
                            )
                            drawRect(
                                color = primaryColor,
                                topLeft = Offset(colLeft, chartHeight - 2.dp.toPx()),
                                size = Size(colWidth, 2.dp.toPx())
                            )
                        }
                    }

                    // 2. Horizontal dashed grid lines
                    val steps = 3
                    for (s in 0..steps) {
                        val gy = (chartHeight / steps) * s
                        drawLine(
                            color = Color.Gray.copy(alpha = 0.2f),
                            start = Offset(0f, gy),
                            end = Offset(w, gy),
                            strokeWidth = 1.dp.toPx(),
                            pathEffect = PathEffect.dashPathEffect(floatArrayOf(8f, 8f), 0f)
                        )
                    }

                    // 3. Min/Max fuel range vertical bars
                    months.forEachIndexed { i, stat ->
                        val cx = (i * colWidth) + (colWidth / 2f)
                        val yMin = getFuelY(stat.minPrice)
                        val yMax = getFuelY(stat.maxPrice)

                        drawLine(
                            color = fuelColor.copy(alpha = 0.25f),
                            start = Offset(cx, yMax),
                            end = Offset(cx, yMin),
                            strokeWidth = 3.dp.toPx(),
                            cap = StrokeCap.Round
                        )
                    }

                    // 4. Draw Crude Oil curve if enabled
                    if (showOilCurve) {
                        val oilPath = Path()
                        months.forEachIndexed { i, stat ->
                            val cx = (i * colWidth) + (colWidth / 2f)
                            val cy = getOilY(stat.brentOilUsd)
                            if (i == 0) {
                                oilPath.moveTo(cx, cy)
                            } else {
                                val prevX = ((i - 1) * colWidth) + (colWidth / 2f)
                                val prevY = getOilY(months[i - 1].brentOilUsd)
                                val midX = (prevX + cx) / 2f
                                oilPath.cubicTo(midX, prevY, midX, cy, cx, cy)
                            }
                        }

                        drawPath(
                            path = oilPath,
                            color = oilColor,
                            style = Stroke(
                                width = 2.dp.toPx(),
                                cap = StrokeCap.Round,
                                pathEffect = PathEffect.dashPathEffect(floatArrayOf(12f, 6f), 0f)
                            )
                        )

                        // Oil point dots
                        months.forEachIndexed { i, stat ->
                            val cx = (i * colWidth) + (colWidth / 2f)
                            val cy = getOilY(stat.brentOilUsd)
                            drawCircle(
                                color = oilColor,
                                radius = 2.5.dp.toPx(),
                                center = Offset(cx, cy)
                            )
                        }
                    }

                    // 5. Fuel price filled path & smooth curve
                    val fuelPath = Path()
                    val fillPath = Path()

                    months.forEachIndexed { i, stat ->
                        val cx = (i * colWidth) + (colWidth / 2f)
                        val cy = getFuelY(stat.avgPrice)

                        if (i == 0) {
                            fuelPath.moveTo(cx, cy)
                            fillPath.moveTo(cx, chartHeight)
                            fillPath.lineTo(cx, cy)
                        } else {
                            val prevX = ((i - 1) * colWidth) + (colWidth / 2f)
                            val prevY = getFuelY(months[i - 1].avgPrice)
                            val midX = (prevX + cx) / 2f
                            fuelPath.cubicTo(midX, prevY, midX, cy, cx, cy)
                            fillPath.cubicTo(midX, prevY, midX, cy, cx, cy)
                        }
                    }

                    val lastCx = ((months.size - 1) * colWidth) + (colWidth / 2f)
                    fillPath.lineTo(lastCx, chartHeight)
                    fillPath.close()

                    drawPath(
                        path = fillPath,
                        brush = Brush.verticalGradient(
                            colors = listOf(fuelColor.copy(alpha = 0.3f), fuelColor.copy(alpha = 0.02f)),
                            startY = 0f,
                            endY = chartHeight
                        )
                    )

                    drawPath(
                        path = fuelPath,
                        color = fuelColor,
                        style = Stroke(width = 3.dp.toPx(), cap = StrokeCap.Round)
                    )

                    // 6. Draw dots for fuel price + Holiday indicators
                    months.forEachIndexed { i, stat ->
                        val cx = (i * colWidth) + (colWidth / 2f)
                        val cy = getFuelY(stat.avgPrice)
                        val isSel = i == selectedMonthIndex

                        drawCircle(
                            color = if (isSel) Color.White else fuelColor,
                            radius = if (isSel) 6.dp.toPx() else 4.dp.toPx(),
                            center = Offset(cx, cy)
                        )
                        if (isSel) {
                            drawCircle(
                                color = fuelColor,
                                radius = 4.dp.toPx(),
                                center = Offset(cx, cy)
                            )
                        }

                        // Holiday dot
                        val holidayCount = stat.holidays.count { it.type == com.example.data.model.HolidayType.PUBLIC_HOLIDAY }
                        if (holidayCount > 0) {
                            drawCircle(
                                color = Color(0xFF0F766E),
                                radius = 2.5.dp.toPx(),
                                center = Offset(cx, 8.dp.toPx())
                            )
                        }
                    }
                }
            }

            // Month labels row (12 columns)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                months.forEachIndexed { idx, stat ->
                    val isSel = idx == selectedMonthIndex
                    val label = stat.monthShortLabel.split(" ")[0]
                    Text(
                        text = label,
                        style = MaterialTheme.typography.labelSmall,
                        fontSize = 9.sp,
                        fontWeight = if (isSel) FontWeight.ExtraBold else FontWeight.Normal,
                        color = if (isSel) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
    }
}
