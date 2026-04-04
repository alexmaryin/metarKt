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

        return correction.roundToNearest10()
    }

    private fun interpolate(value: Int, v0: Int, v1: Int, c0: Int, c1: Int): Int {
        if (v0 == v1) return c0
        val fraction = (value - v0).toDouble() / (v1 - v0)
        return (c0 + fraction * (c1 - c0)).toInt()
    }

    private fun Int.roundToNearest10(): Int {
        val remainder = this % 10
        return if (remainder >= 5) this + (10 - remainder) else this - remainder
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
