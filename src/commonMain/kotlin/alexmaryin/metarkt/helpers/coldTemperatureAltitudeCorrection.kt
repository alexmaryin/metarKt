package alexmaryin.metarkt.helpers

internal object ColdTemperatureCorrectionTable {
    private val heightsFt = intArrayOf(200, 300, 400, 500, 600, 700, 800, 900, 1000, 1500, 2000, 3000, 4000, 5000)
    private val temperaturesC = intArrayOf(10, 0, -10, -20, -30, -40, -50)
    private val correctionsFt = arrayOf(
        intArrayOf(10, 10, 10, 10, 20, 20, 20, 20, 20, 30, 40, 60, 80, 90),
        intArrayOf(20, 20, 30, 30, 40, 40, 50, 50, 60, 90, 120, 170, 230, 280),
        intArrayOf(20, 30, 40, 50, 60, 70, 80, 90, 100, 150, 200, 290, 390, 490),
        intArrayOf(30, 50, 60, 70, 90, 100, 120, 130, 140, 210, 280, 420, 570, 710),
        intArrayOf(40, 60, 80, 100, 120, 140, 150, 170, 190, 280, 380, 570, 760, 950),
        intArrayOf(50, 80, 100, 120, 150, 170, 190, 220, 240, 360, 480, 720, 970, 1210),
        intArrayOf(60, 90, 120, 150, 180, 210, 240, 270, 300, 450, 590, 890, 1190, 1500),
    )

    /**
     * Rounds up the height to the next table value for safety, per FAA guidance.
     * For example, 1,314 ft rounds up to 1,500 ft.
     */
    fun roundUpToNextTableValue(heightAboveAirportFt: Int): Int {
        val index = heightsFt.indexOfFirst { heightAboveAirportFt <= it }
        return if (index == -1) heightsFt.last() else heightsFt[index]
    }

    fun correctionFor(heightAboveAirportFt: Int, reportedTemperatureC: Int): Int {
        val heightIndices = heightsFt.boundingIndices(heightAboveAirportFt)
        val temperatureIndices = temperaturesC.boundingIndicesDescending(reportedTemperatureC)

        val (hLower, hUpper) = heightIndices
        val (tLower, tUpper) = temperatureIndices

        val h0 = heightsFt[hLower]
        val h1 = heightsFt[hUpper]
        val t0 = temperaturesC[tLower]
        val t1 = temperaturesC[tUpper]

        val c00 = correctionsFt[tLower][hLower]
        val c01 = correctionsFt[tLower][hUpper]
        val c10 = correctionsFt[tUpper][hLower]
        val c11 = correctionsFt[tUpper][hUpper]

        val c0 = interpolate(heightAboveAirportFt, h0, h1, c00, c01)
        val c1 = interpolate(heightAboveAirportFt, h0, h1, c10, c11)
        val correction = interpolate(reportedTemperatureC, t0, t1, c0, c1)

        return correction.roundUpToNearest10()
    }

    private fun interpolate(value: Int, v0: Int, v1: Int, c0: Int, c1: Int): Int {
        if (v0 == v1) return c0
        val fraction = (value - v0).toDouble() / (v1 - v0)
        return (c0 + fraction * (c1 - c0)).toInt()
    }

    /**
     * Rounds UP to the nearest 10 for safety, per FAA guidance.
     * For example: 162 → 170, 160 → 160, 161 → 170
     */
    private fun Int.roundUpToNearest10(): Int {
        val remainder = this % 10
        return if (remainder == 0) this else this + (10 - remainder)
    }

    private fun IntArray.boundingIndices(value: Int): Pair<Int, Int> {
        val upperIndex = indexOfFirst { value <= it }.let { if (it == -1) lastIndex else it }
        val lowerIndex = if (upperIndex > 0 && value < this[upperIndex]) upperIndex - 1 else upperIndex
        return lowerIndex to upperIndex
    }

    private fun IntArray.boundingIndicesDescending(value: Int): Pair<Int, Int> {
        val upperIndex = indexOfFirst { value >= it }.let { if (it == -1) lastIndex else it }
        val lowerIndex = if (upperIndex > 0 && value > this[upperIndex]) upperIndex - 1 else upperIndex
        return lowerIndex to upperIndex
    }
}

/**
 * Applies the FAA/ICAO cold-temperature correction table to an indicated height above airport.
 *
 * The input height is expected to be in feet above airport/reporting station elevation,
 * as described in AIM 7-3-1 / TBL 7-3-1.
 */
public fun coldTemperatureCorrectedAltitude(heightAboveAirportFt: Int, reportedTemperatureC: Int): Int =
    heightAboveAirportFt + ColdTemperatureCorrectionTable.correctionFor(heightAboveAirportFt, reportedTemperatureC)

/**
 * Calculates cold temperature corrections using the FAA segment-based method (AIM 7-3-6).
 *
 * According to FAA guidance, corrections should be applied by segment:
 * - **Intermediate Segment** (IAF through FAF): Use ONE correction based on FAF height above airport,
 *   applied to all fixes in the segment (IAFs, stepdown fixes, FAF)
 * - **Final Segment** (MDA/DA): Use correction based on MDA/DA height above airport
 * - **Missed Approach Segment**: Use correction based on final missed approach holding altitude
 *
 * **Important**: For the final segment (MDA/DA), the FAA recommends rounding UP the height above airport
 * to the next table value for added safety (e.g., 1,314 ft rounds up to 1,500 ft).
 *
 * @param fafAltitude The published altitude of the Final Approach Fix
 * @param mdaAltitude The published Minimum Descent Altitude (or Decision Altitude)
 * @param airportElevation The airport/reporting station elevation
 * @param reportedTemperatureC The reported temperature at the airport
 * @param roundUpForSafety If true (recommended), rounds up height above airport to next table value
 *        for final segment corrections per FAA guidance. Default is true.
 * @return A [ColdTemperatureCorrections] object containing corrections for each segment
 */
public fun calculateColdTemperatureCorrections(
    fafAltitude: Int,
    mdaAltitude: Int,
    airportElevation: Int,
    reportedTemperatureC: Int,
    roundUpForSafety: Boolean = true
): ColdTemperatureCorrections {
    val fafHeightAboveAirport = fafAltitude - airportElevation
    val mdaHeightAboveAirport = mdaAltitude - airportElevation
    
    // Intermediate segment: use exact FAF height
    val intermediateCorrection = ColdTemperatureCorrectionTable.correctionFor(fafHeightAboveAirport, reportedTemperatureC)
    
    // Final segment: optionally round up for safety per FAA guidance
    val finalCorrection = if (roundUpForSafety) {
        val roundedHeight = ColdTemperatureCorrectionTable.roundUpToNextTableValue(mdaHeightAboveAirport)
        ColdTemperatureCorrectionTable.correctionFor(roundedHeight, reportedTemperatureC)
    } else {
        ColdTemperatureCorrectionTable.correctionFor(mdaHeightAboveAirport, reportedTemperatureC)
    }
    
    return ColdTemperatureCorrections(
        intermediateSegmentCorrection = intermediateCorrection,
        finalSegmentCorrection = finalCorrection,
        fafHeightAboveAirport = fafHeightAboveAirport,
        mdaHeightAboveAirport = mdaHeightAboveAirport,
        mdaHeightAboveAirportRounded = if (roundUpForSafety) 
            ColdTemperatureCorrectionTable.roundUpToNextTableValue(mdaHeightAboveAirport) 
        else mdaHeightAboveAirport
    )
}

/**
 * Holds cold temperature corrections for different approach segments.
 *
 * @property intermediateSegmentCorrection Correction to apply to all fixes in the intermediate segment (IAF through FAF)
 * @property finalSegmentCorrection Correction to apply to MDA/DA and final segment stepdown fixes
 * @property fafHeightAboveAirport Height of FAF above airport elevation
 * @property mdaHeightAboveAirport Height of MDA above airport elevation (actual)
 * @property mdaHeightAboveAirportRounded Height of MDA above airport elevation (rounded up for safety, if used)
 */
public data class ColdTemperatureCorrections(
    public val intermediateSegmentCorrection: Int,
    public val finalSegmentCorrection: Int,
    public val fafHeightAboveAirport: Int,
    public val mdaHeightAboveAirport: Int,
    public val mdaHeightAboveAirportRounded: Int = mdaHeightAboveAirport
) {
    /**
     * Calculates the corrected altitude for a fix in the intermediate segment (IAF through FAF).
     *
     * @param publishedAltitude The published altitude of the fix
     * @return The corrected altitude
     */
    public fun correctIntermediateSegment(publishedAltitude: Int): Int =
        publishedAltitude + intermediateSegmentCorrection
    
    /**
     * Calculates the corrected altitude for the MDA/DA or final segment stepdown fixes.
     *
     * @param publishedAltitude The published MDA/DA or stepdown fix altitude
     * @return The corrected altitude
     */
    public fun correctFinalSegment(publishedAltitude: Int): Int =
        publishedAltitude + finalSegmentCorrection
}
