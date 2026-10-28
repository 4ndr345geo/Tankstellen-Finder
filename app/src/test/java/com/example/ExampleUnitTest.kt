package com.example

import com.example.data.model.FederalState
import com.example.data.model.FuelType
import com.example.data.repository.FuelRepository
import org.junit.Assert.*
import org.junit.Test

class ExampleUnitTest {
  @Test
  fun addition_isCorrect() {
    assertEquals(4, 2 + 2)
  }

  @Test
  fun testTwelveMonthStatistics_includesBrentCrudeOilData() {
    val stats = FuelRepository.getTwelveMonthStatistics(
      state = FederalState.BY,
      fuelType = FuelType.E5
    )

    assertEquals(12, stats.monthlyStats.size)
    assertTrue("Current oil price should be realistic", stats.currentOilPriceUsd in 50.0..120.0)
    assertTrue("Min oil price should be <= max oil price", stats.minOilPriceUsd <= stats.maxOilPriceUsd)
    assertTrue("Oil correlation should be positive", stats.oilCorrelationPercentage > 50)

    stats.monthlyStats.forEach { month ->
      assertTrue("Brent USD price should be > 0", month.brentOilUsd > 0)
      assertTrue("Brent EUR per liter should be > 0", month.brentOilEurPerLiter > 0)
      assertTrue("Fuel price should be > 0", month.avgPrice > 0)
    }
  }
}

