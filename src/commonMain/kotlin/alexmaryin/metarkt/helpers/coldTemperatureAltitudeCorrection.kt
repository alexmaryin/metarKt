package alexmaryin.metarkt.helpers

import kotlin.math.roundToInt

private object ColdTemperatureCorrectionTable {
    private val heightsFt = intArrayOf(0, 100, 200, 300, 400, 500, 600, 700, 800, 900, 1000, 1500, 2000, 3000, 4000, 5000)
    private val temperaturesC = intArrayOf(-50, -40, -30, -20, -10, 0, 10)
    private val correctionsFt = arrayOf(
        intArrayOf(0, 60, 90, 120, 150, 180, 210, 240, 270, 300, 300, 450, 590, 890, 1190, 1500),
        intArrayOf(0, 50, 80, 100, 120, 150, 170, 190, 220, 240, 240, 360, 480, 720, 970, 1210),
        intArrayOf(0, 40, 60, 80, 100, 120, 140, 150, 170, 190, 190, 280, 380, 570, 760, 950),
        intArrayOf(0, 30, 50, 60, 70, 90, 100, 120, 130, 140, 140, 210, 280, 420, 570, 710),
        intArrayOf(0, 20, 30, 40, 50, 60, 70, 80, 90, 100, 100, 150, 200, 290, 390, 490),
        intArrayOf(0, 20, 20, 30, 30, 40, 40, 50, 50, 60, 60, 90, 120, 170, 230, 280),
        intArrayOf(0, 10, 10, 10, 10, 20, 20, 20, 20, 20, 20, 30, 40, 60, 80, 90)
    )

    fun correctionFor(heightAboveAirportFt: Int, reportedTemperatureC: Int): Int {
        val height = heightAboveAirportFt.coerceIn(heightsFt.first(), heightsFt.last())
        val temperature = reportedTemperatureC.coerceIn(temperaturesC.first(), temperaturesC.last())

        val (heightLow, heightHigh) = heightsFt.bounds(height)
        val (tempLow, tempHigh) = temperaturesC.bounds(temperature)

        val lowTempRow = interpolate(
            x = height,
            x0 = heightsFt[heightLow],
            x1 = heightsFt[heightHigh],
            y0 = correctionsFt[tempLow][heightLow],
            y1 = correctionsFt[tempLow][heightHigh]
        )
        val highTempRow = interpolate(
            x = height,
            x0 = heightsFt[heightLow],
            x1 = heightsFt[heightHigh],
            y0 = correctionsFt[tempHigh][heightLow],
            y1 = correctionsFt[tempHigh][heightHigh]
        )

        return interpolate(
            x = temperature,
            x0 = temperaturesC[tempLow],
            x1 = temperaturesC[tempHigh],
            y0 = lowTempRow,
            y1 = highTempRow
        ).roundToInt()
    }

    private fun IntArray.bounds(value: Int): Pair<Int, Int> {
        val upper = indexOfFirst { it >= value }.let { if (it == -1) lastIndex else it }
        return (upper - 1).coerceAtLeast(0) to upper
    }

    private fun interpolate(x: Int, x0: Int, x1: Int, y0: Int, y1: Int): Double =
        interpolate(x = x, x0 = x0, x1 = x1, y0 = y0.toDouble(), y1 = y1.toDouble())

    private fun interpolate(x: Int, x0: Int, x1: Int, y0: Double, y1: Double): Double =
        if (x0 == x1) y0
        else y0 + (y1 - y0) * (x - x0).toDouble() / (x1 - x0)
}

/**
 * Applies the FAA/ICAO cold-temperature correction table to an indicated height above airport.
 *
 * The input height is expected to be in feet above airport/reporting station elevation,
 * as described in AIM 7-3-1 / TBL 7-3-1.
 */
public fun coldTemperatureCorrectedAltitude(heightAboveAirportFt: Int, reportedTemperatureC: Int): Int =
    heightAboveAirportFt + ColdTemperatureCorrectionTable.correctionFor(heightAboveAirportFt, reportedTemperatureC)
