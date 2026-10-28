package com.example.ui.components

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
import androidx.compose.material.icons.filled.LocalGasStation
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.FuelType
import com.example.ui.theme.FuelDiesel
import com.example.ui.theme.FuelE10
import com.example.ui.theme.FuelE5

@Composable
fun FuelTypeSelector(
    selectedFuel: FuelType,
    onFuelSelected: (FuelType) -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.LocalGasStation,
                    contentDescription = "Spritsorte",
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(18.dp)
                )
                Text(
                    text = "Spritsorte wählen",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                FuelTabButton(
                    fuel = FuelType.E5,
                    color = FuelE5,
                    isSelected = selectedFuel == FuelType.E5,
                    onClick = { onFuelSelected(FuelType.E5) },
                    modifier = Modifier.weight(1f)
                )

                FuelTabButton(
                    fuel = FuelType.E10,
                    color = FuelE10,
                    isSelected = selectedFuel == FuelType.E10,
                    onClick = { onFuelSelected(FuelType.E10) },
                    modifier = Modifier.weight(1f)
                )

                FuelTabButton(
                    fuel = FuelType.DIESEL,
                    color = FuelDiesel,
                    isSelected = selectedFuel == FuelType.DIESEL,
                    onClick = { onFuelSelected(FuelType.DIESEL) },
                    modifier = Modifier.weight(1f)
                )

                FuelTabButton(
                    fuel = FuelType.ALL,
                    color = MaterialTheme.colorScheme.secondary,
                    isSelected = selectedFuel == FuelType.ALL,
                    onClick = { onFuelSelected(FuelType.ALL) },
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = selectedFuel.subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontSize = 11.sp
            )
        }
    }
}

@Composable
private fun FuelTabButton(
    fuel: FuelType,
    color: Color,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val shape = RoundedCornerShape(12.dp)
    val bg = if (isSelected) color else MaterialTheme.colorScheme.surface
    val contentColor = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurface

    Box(
        modifier = modifier
            .clip(shape)
            .background(bg)
            .border(
                width = if (isSelected) 0.dp else 1.dp,
                color = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f),
                shape = shape
            )
            .clickable(onClick = onClick)
            .padding(vertical = 10.dp, horizontal = 4.dp)
            .testTag("fuel_tab_${fuel.apiParam}"),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(6.dp)
                    .background(if (isSelected) Color.White else color, shape = CircleShape)
            )
            Text(
                text = fuel.shortName,
                style = MaterialTheme.typography.labelMedium,
                fontWeight = if (isSelected) FontWeight.ExtraBold else FontWeight.Bold,
                color = contentColor,
                fontSize = 13.sp
            )
        }
    }
}
