package com.example.data.holiday

import com.example.data.model.FederalState
import com.example.data.model.HolidayEvent
import com.example.data.model.HolidayType
import java.util.Calendar

object GermanHolidayProvider {

    /**
     * Calculates public holidays (Gesetzliche Feiertage) for the given state and year.
     */
    fun getPublicHolidays(year: Int, state: FederalState): List<HolidayEvent> {
        val holidays = mutableListOf<HolidayEvent>()

        // 1. Fixed nationwide holidays
        holidays.add(HolidayEvent("Neujahr", HolidayType.PUBLIC_HOLIDAY, "01.01.$year", null, 1, year))
        holidays.add(HolidayEvent("Tag der Arbeit", HolidayType.PUBLIC_HOLIDAY, "01.05.$year", null, 5, year))
        holidays.add(HolidayEvent("Tag der Deutschen Einheit", HolidayType.PUBLIC_HOLIDAY, "03.10.$year", null, 10, year))
        holidays.add(HolidayEvent("1. Weihnachtstag", HolidayType.PUBLIC_HOLIDAY, "25.12.$year", null, 12, year))
        holidays.add(HolidayEvent("2. Weihnachtstag", HolidayType.PUBLIC_HOLIDAY, "26.12.$year", null, 12, year))

        // 2. Fixed state-specific holidays
        when (state) {
            FederalState.BW, FederalState.BY, FederalState.ST -> {
                holidays.add(HolidayEvent("Heilige Drei Könige", HolidayType.PUBLIC_HOLIDAY, "06.01.$year", null, 1, year, isFederalStateSpecific = true))
            }
            else -> {}
        }

        when (state) {
            FederalState.BE, FederalState.MV -> {
                holidays.add(HolidayEvent("Internationaler Frauentag", HolidayType.PUBLIC_HOLIDAY, "08.03.$year", null, 3, year, isFederalStateSpecific = true))
            }
            else -> {}
        }

        when (state) {
            FederalState.BY, FederalState.SL -> {
                holidays.add(HolidayEvent("Mariä Himmelfahrt", HolidayType.PUBLIC_HOLIDAY, "15.08.$year", null, 8, year, isFederalStateSpecific = true))
            }
            else -> {}
        }

        when (state) {
            FederalState.BB, FederalState.HB, FederalState.HH, FederalState.MV,
            FederalState.NI, FederalState.SN, FederalState.ST, FederalState.SH, FederalState.TH -> {
                holidays.add(HolidayEvent("Reformationstag", HolidayType.PUBLIC_HOLIDAY, "31.10.$year", null, 10, year, isFederalStateSpecific = true))
            }
            else -> {}
        }

        when (state) {
            FederalState.BW, FederalState.BY, FederalState.NW, FederalState.RP, FederalState.SL -> {
                holidays.add(HolidayEvent("Allerheiligen", HolidayType.PUBLIC_HOLIDAY, "01.11.$year", null, 11, year, isFederalStateSpecific = true))
            }
            else -> {}
        }

        if (state == FederalState.SN) {
            // Buß- und Bettag (Wednesday before Nov 23)
            val bussTagCal = Calendar.getInstance().apply {
                set(year, Calendar.NOVEMBER, 22)
                while (get(Calendar.DAY_OF_WEEK) != Calendar.WEDNESDAY) {
                    add(Calendar.DAY_OF_MONTH, -1)
                }
            }
            val day = bussTagCal.get(Calendar.DAY_OF_MONTH)
            holidays.add(HolidayEvent("Buß- und Bettag", HolidayType.PUBLIC_HOLIDAY, String.format("%02d.11.%d", day, year), null, 11, year, isFederalStateSpecific = true))
        }

        // 3. Movable holidays based on Easter Sunday
        val easter = calculateEasterSunday(year)

        // Karfreitag (Easter - 2)
        val goodFriday = (easter.clone() as Calendar).apply { add(Calendar.DAY_OF_YEAR, -2) }
        holidays.add(formatHoliday("Karfreitag", goodFriday, isSpecific = false))

        // Ostersonntag (Easter)
        holidays.add(formatHoliday("Ostersonntag", easter, isSpecific = false))

        // Ostermontag (Easter + 1)
        val easterMonday = (easter.clone() as Calendar).apply { add(Calendar.DAY_OF_YEAR, 1) }
        holidays.add(formatHoliday("Ostermontag", easterMonday, isSpecific = false))

        // Christi Himmelfahrt (Easter + 39)
        val ascension = (easter.clone() as Calendar).apply { add(Calendar.DAY_OF_YEAR, 39) }
        holidays.add(formatHoliday("Christi Himmelfahrt", ascension, isSpecific = false))

        // Pfingstsonntag (Easter + 49)
        val pentecostSunday = (easter.clone() as Calendar).apply { add(Calendar.DAY_OF_YEAR, 49) }
        holidays.add(formatHoliday("Pfingstsonntag", pentecostSunday, isSpecific = false))

        // Pfingstmontag (Easter + 50)
        val pentecostMonday = (easter.clone() as Calendar).apply { add(Calendar.DAY_OF_YEAR, 50) }
        holidays.add(formatHoliday("Pfingstmontag", pentecostMonday, isSpecific = false))

        // Fronleichnam (Easter + 60) - BW, BY, HE, NW, RP, SL
        when (state) {
            FederalState.BW, FederalState.BY, FederalState.HE, FederalState.NW, FederalState.RP, FederalState.SL -> {
                val corpusChristi = (easter.clone() as Calendar).apply { add(Calendar.DAY_OF_YEAR, 60) }
                holidays.add(formatHoliday("Fronleichnam", corpusChristi, isSpecific = true))
            }
            else -> {}
        }

        return holidays.sortedWith(compareBy({ it.year }, { it.month }, { it.startDateFormatted }))
    }

    /**
     * Returns the school holidays (Schulferien) for a specific state and year.
     */
    fun getSchoolVacations(year: Int, state: FederalState): List<HolidayEvent> {
        val vacations = mutableListOf<HolidayEvent>()

        when (state) {
            FederalState.BY -> {
                vacations.add(HolidayEvent("Weihnachtsferien", HolidayType.SCHOOL_VACATION, "23.12.${year-1}", "05.01.$year", 1, year))
                vacations.add(HolidayEvent("Frühjahrsferien", HolidayType.SCHOOL_VACATION, "16.02.$year", "20.02.$year", 2, year))
                vacations.add(HolidayEvent("Osterferien", HolidayType.SCHOOL_VACATION, "30.03.$year", "10.04.$year", 4, year))
                vacations.add(HolidayEvent("Pfingstferien", HolidayType.SCHOOL_VACATION, "26.05.$year", "05.06.$year", 6, year))
                vacations.add(HolidayEvent("Sommerferien", HolidayType.SCHOOL_VACATION, "03.08.$year", "14.09.$year", 8, year))
                vacations.add(HolidayEvent("Herbstferien", HolidayType.SCHOOL_VACATION, "02.11.$year", "06.11.$year", 11, year))
                vacations.add(HolidayEvent("Weihnachtsferien", HolidayType.SCHOOL_VACATION, "24.12.$year", "08.01.${year+1}", 12, year))
            }
            FederalState.BW -> {
                vacations.add(HolidayEvent("Weihnachtsferien", HolidayType.SCHOOL_VACATION, "23.12.${year-1}", "05.01.$year", 1, year))
                vacations.add(HolidayEvent("Faschingsferien", HolidayType.SCHOOL_VACATION, "16.02.$year", "20.02.$year", 2, year))
                vacations.add(HolidayEvent("Osterferien", HolidayType.SCHOOL_VACATION, "30.03.$year", "11.04.$year", 4, year))
                vacations.add(HolidayEvent("Pfingstferien", HolidayType.SCHOOL_VACATION, "26.05.$year", "05.06.$year", 6, year))
                vacations.add(HolidayEvent("Sommerferien", HolidayType.SCHOOL_VACATION, "30.07.$year", "12.09.$year", 8, year))
                vacations.add(HolidayEvent("Herbstferien", HolidayType.SCHOOL_VACATION, "26.10.$year", "30.10.$year", 10, year))
                vacations.add(HolidayEvent("Weihnachtsferien", HolidayType.SCHOOL_VACATION, "23.12.$year", "09.01.${year+1}", 12, year))
            }
            FederalState.NW -> {
                vacations.add(HolidayEvent("Weihnachtsferien", HolidayType.SCHOOL_VACATION, "23.12.${year-1}", "06.01.$year", 1, year))
                vacations.add(HolidayEvent("Osterferien", HolidayType.SCHOOL_VACATION, "30.03.$year", "11.04.$year", 4, year))
                vacations.add(HolidayEvent("Pfingstferien", HolidayType.SCHOOL_VACATION, "26.05.$year", "26.05.$year", 5, year))
                vacations.add(HolidayEvent("Sommerferien", HolidayType.SCHOOL_VACATION, "20.07.$year", "01.09.$year", 7, year))
                vacations.add(HolidayEvent("Herbstferien", HolidayType.SCHOOL_VACATION, "12.10.$year", "24.10.$year", 10, year))
                vacations.add(HolidayEvent("Weihnachtsferien", HolidayType.SCHOOL_VACATION, "23.12.$year", "06.01.${year+1}", 12, year))
            }
            FederalState.NI -> {
                vacations.add(HolidayEvent("Weihnachtsferien", HolidayType.SCHOOL_VACATION, "23.12.${year-1}", "05.01.$year", 1, year))
                vacations.add(HolidayEvent("Halbjahresferien", HolidayType.SCHOOL_VACATION, "02.02.$year", "03.02.$year", 2, year))
                vacations.add(HolidayEvent("Osterferien", HolidayType.SCHOOL_VACATION, "23.03.$year", "07.04.$year", 4, year))
                vacations.add(HolidayEvent("Himmelfahrt/Pfingsten", HolidayType.SCHOOL_VACATION, "15.05.$year", "26.05.$year", 5, year))
                vacations.add(HolidayEvent("Sommerferien", HolidayType.SCHOOL_VACATION, "02.07.$year", "12.08.$year", 7, year))
                vacations.add(HolidayEvent("Herbstferien", HolidayType.SCHOOL_VACATION, "12.10.$year", "24.10.$year", 10, year))
                vacations.add(HolidayEvent("Weihnachtsferien", HolidayType.SCHOOL_VACATION, "23.12.$year", "08.01.${year+1}", 12, year))
            }
            FederalState.HE -> {
                vacations.add(HolidayEvent("Weihnachtsferien", HolidayType.SCHOOL_VACATION, "22.12.${year-1}", "10.01.$year", 1, year))
                vacations.add(HolidayEvent("Osterferien", HolidayType.SCHOOL_VACATION, "07.04.$year", "18.04.$year", 4, year))
                vacations.add(HolidayEvent("Sommerferien", HolidayType.SCHOOL_VACATION, "29.06.$year", "07.08.$year", 7, year))
                vacations.add(HolidayEvent("Herbstferien", HolidayType.SCHOOL_VACATION, "05.10.$year", "17.10.$year", 10, year))
                vacations.add(HolidayEvent("Weihnachtsferien", HolidayType.SCHOOL_VACATION, "21.12.$year", "09.01.${year+1}", 12, year))
            }
            else -> { // Default Berlin / general northern & eastern states
                vacations.add(HolidayEvent("Weihnachtsferien", HolidayType.SCHOOL_VACATION, "22.12.${year-1}", "03.01.$year", 1, year))
                vacations.add(HolidayEvent("Winterferien", HolidayType.SCHOOL_VACATION, "02.02.$year", "07.02.$year", 2, year))
                vacations.add(HolidayEvent("Osterferien", HolidayType.SCHOOL_VACATION, "30.03.$year", "10.04.$year", 4, year))
                vacations.add(HolidayEvent("Pfingstferien", HolidayType.SCHOOL_VACATION, "15.05.$year", "15.05.$year", 5, year))
                vacations.add(HolidayEvent("Sommerferien", HolidayType.SCHOOL_VACATION, "09.07.$year", "21.08.$year", 7, year))
                vacations.add(HolidayEvent("Herbstferien", HolidayType.SCHOOL_VACATION, "19.10.$year", "31.10.$year", 10, year))
                vacations.add(HolidayEvent("Weihnachtsferien", HolidayType.SCHOOL_VACATION, "23.12.$year", "02.01.${year+1}", 12, year))
            }
        }

        return vacations
    }

    /**
     * Determines whether a given month has active school holidays in the state.
     */
    fun getHolidaysAndVacationsForMonth(year: Int, month: Int, state: FederalState): List<HolidayEvent> {
        val pubHolidays = getPublicHolidays(year, state).filter { it.month == month }
        val vacations = getSchoolVacations(year, state).filter { vac ->
            // Check if vacation falls in or touches this month
            vac.month == month || isVacationInMonth(vac, year, month)
        }
        return (pubHolidays + vacations)
    }

    private fun isVacationInMonth(vac: HolidayEvent, targetYear: Int, targetMonth: Int): Boolean {
        // e.g. Sommerferien spanning July and August
        if (vac.month == targetMonth && vac.year == targetYear) return true
        if (vac.name.contains("Sommer") && (targetMonth == 7 || targetMonth == 8)) return true
        if (vac.name.contains("Herbst") && (targetMonth == 10 || targetMonth == 11)) return true
        if (vac.name.contains("Weihnacht") && (targetMonth == 12 || targetMonth == 1)) return true
        if (vac.name.contains("Oster") && (targetMonth == 3 || targetMonth == 4)) return true
        if (vac.name.contains("Pfingst") && (targetMonth == 5 || targetMonth == 6)) return true
        return false
    }

    /**
     * Butcher's Algorithm to calculate Easter Sunday.
     */
    private fun calculateEasterSunday(year: Int): Calendar {
        val a = year % 19
        val b = year / 100
        val c = year % 100
        val d = b / 4
        val e = b % 4
        val f = (b + 8) / 25
        val g = (b - f + 1) / 3
        val h = (19 * a + b - d - g + 15) % 30
        val i = c / 4
        val k = c % 4
        val l = (32 + 2 * e + 2 * i - h - k) % 7
        val m = (a + 11 * h + 22 * l) / 451
        val month = (h + l - 7 * m + 114) / 31 // 3 = March, 4 = April
        val day = ((h + l - 7 * m + 114) % 31) + 1

        return Calendar.getInstance().apply {
            set(year, month - 1, day, 0, 0, 0)
        }
    }

    private fun formatHoliday(name: String, cal: Calendar, isSpecific: Boolean): HolidayEvent {
        val day = cal.get(Calendar.DAY_OF_MONTH)
        val month = cal.get(Calendar.MONTH) + 1
        val year = cal.get(Calendar.YEAR)
        val dateStr = String.format("%02d.%02d.%d", day, month, year)
        return HolidayEvent(
            name = name,
            type = HolidayType.PUBLIC_HOLIDAY,
            startDateFormatted = dateStr,
            endDateFormatted = null,
            month = month,
            year = year,
            isFederalStateSpecific = isSpecific
        )
    }
}
