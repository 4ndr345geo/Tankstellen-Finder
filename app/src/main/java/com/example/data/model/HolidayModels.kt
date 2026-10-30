package com.example.data.model

enum class FederalState(val shortCode: String, val fullName: String) {
    BW("BW", "Baden-Württemberg"),
    BY("BY", "Bayern"),
    BE("BE", "Berlin"),
    BB("BB", "Brandenburg"),
    HB("HB", "Bremen"),
    HH("HH", "Hamburg"),
    HE("HE", "Hessen"),
    MV("MV", "Mecklenburg-Vorpommern"),
    NI("NI", "Niedersachsen"),
    NW("NW", "Nordrhein-Westfalen"),
    RP("RP", "Rheinland-Pfalz"),
    SL("SL", "Saarland"),
    SN("SN", "Sachsen"),
    ST("ST", "Sachsen-Anhalt"),
    SH("SH", "Schleswig-Holstein"),
    TH("TH", "Thüringen");

    companion object {
        fun fromString(text: String?): FederalState {
            if (text == null) return BE
            val upper = text.uppercase()
            return entries.find {
                upper.contains(it.name) ||
                upper.contains(it.fullName.uppercase()) ||
                upper.contains(it.shortCode)
            } ?: when {
                upper.contains("WEIMAR") || upper.contains("ERFURT") || upper.contains("JENA") || upper.contains("GERA") -> TH
                upper.contains("MÜNCHEN") || upper.contains("NÜRNBERG") || upper.contains("AUGSBURG") -> BY
                upper.contains("KÖLN") || upper.contains("DÜSSELDORF") || upper.contains("DORTMUND") || upper.contains("ESSEN") -> NW
                upper.contains("FRANKFURT") || upper.contains("WIESBADEN") || upper.contains("KASSEL") -> HE
                upper.contains("STUTTGART") || upper.contains("KARLSRUHE") || upper.contains("MANNHEIM") -> BW
                upper.contains("HANNOVER") || upper.contains("BRAUNSCHWEIG") || upper.contains("PEINE") || upper.contains("WOLFSBURG") -> NI
                upper.contains("HAMBURG") -> HH
                upper.contains("LEIPZIG") || upper.contains("DRESDEN") || upper.contains("CHEMNITZ") -> SN
                upper.contains("BERLIN") -> BE
                else -> TH
            }
        }
    }
}

enum class HolidayType {
    PUBLIC_HOLIDAY, // Gesetzlicher Feiertag
    SCHOOL_VACATION // Schulferien
}

data class HolidayEvent(
    val name: String,
    val type: HolidayType,
    val startDateFormatted: String,
    val endDateFormatted: String? = null,
    val month: Int,
    val year: Int,
    val isFederalStateSpecific: Boolean = false
) {
    val dateDisplay: String
        get() = if (endDateFormatted != null && endDateFormatted != startDateFormatted) {
            "$startDateFormatted – $endDateFormatted"
        } else {
            startDateFormatted
        }
}

data class MonthlyPriceStat(
    val year: Int,
    val month: Int, // 1..12
    val monthShortLabel: String, // e.g. "Nov 25", "Aug 26"
    val monthFullLabel: String, // e.g. "November 2025"
    val avgPrice: Double,
    val minPrice: Double,
    val maxPrice: Double,
    val holidays: List<HolidayEvent>,
    val isVacationMonth: Boolean,
    val vacationDaysCount: Int,
    val brentOilUsd: Double, // Crude Oil Brent in USD per Barrel
    val brentOilEurPerLiter: Double // Calculated equivalent in EUR per Liter of crude
)

data class TwelveMonthHistory(
    val state: FederalState,
    val fuelType: FuelType,
    val monthlyStats: List<MonthlyPriceStat>,
    val overallMinPrice: Double,
    val overallMaxPrice: Double,
    val overallAvgPrice: Double,
    val lowestMonth: MonthlyPriceStat?,
    val highestMonth: MonthlyPriceStat?,
    val vacationPricePremiumCt: Double, // price difference in cent between vacation months and non-vacation months
    val totalHolidaysInPeriod: Int,
    val currentOilPriceUsd: Double,
    val minOilPriceUsd: Double,
    val maxOilPriceUsd: Double,
    val avgOilPriceUsd: Double,
    val oilCorrelationPercentage: Int // e.g. 84%
)
