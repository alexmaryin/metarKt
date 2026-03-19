package alexmaryin.metarkt.helpers

import kotlin.test.Test
import kotlin.test.assertEquals

class ColdTemperatureAltitudeCorrectionTest {

    private val kjacElevationFt = 6451

    @Test
    fun `should return exact table value on grid point`() {
        assertEquals(1100, coldTemperatureCorrectedAltitude(heightAboveAirportFt = 1000, reportedTemperatureC = -10))
    }

    @Test
    fun `should use next higher table column for height between grid points`() {
        assertEquals(300, coldTemperatureCorrectedAltitude(heightAboveAirportFt = 250, reportedTemperatureC = -20))
    }

    @Test
    fun `should use next colder table row for temperature between grid points`() {
        assertEquals(1140, coldTemperatureCorrectedAltitude(heightAboveAirportFt = 1000, reportedTemperatureC = -15))
    }

    @Test
    fun `should clamp values outside table bounds`() {
        assertEquals(7500, coldTemperatureCorrectedAltitude(heightAboveAirportFt = 6000, reportedTemperatureC = -60))
    }

    @Test
    fun `should correct HOMVA altitude for KJAC approach at minus 20`() {
        assertEquals(12410, correctedMslAltitude(11700))
    }

    @Test
    fun `should correct VALCU altitude for KJAC approach at minus 20`() {
        assertEquals(11710, correctedMslAltitude(11000))
    }

    @Test
    fun `should correct DECEV altitude for KJAC approach at minus 20`() {
        assertEquals(10970, correctedMslAltitude(10400))
    }

    @Test
    fun `should correct ZUGEN altitude for KJAC approach at minus 20`() {
        assertEquals(10070, correctedMslAltitude(9500))
    }

    @Test
    fun `should correct COVGI altitude for KJAC approach at minus 20`() {
        assertEquals(8460, correctedMslAltitude(8180))
    }

    @Test
    fun `should correct DA altitude for KJAC approach at minus 20`() {
        assertEquals(6681, correctedMslAltitude(6651))
    }

    private fun correctedMslAltitude(publishedAltitudeFt: Int): Int {
        val heightAboveAirportFt = publishedAltitudeFt - kjacElevationFt
        return coldTemperatureCorrectedAltitude(heightAboveAirportFt, reportedTemperatureC = -20) + kjacElevationFt
    }
}
