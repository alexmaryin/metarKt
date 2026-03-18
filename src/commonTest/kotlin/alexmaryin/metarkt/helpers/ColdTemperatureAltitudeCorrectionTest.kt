package alexmaryin.metarkt.helpers

import kotlin.test.Test
import kotlin.test.assertEquals

class ColdTemperatureAltitudeCorrectionTest {

    @Test
    fun `should return exact table value on grid point`() {
        assertEquals(1100, coldTemperatureCorrectedAltitude(heightAboveAirportFt = 1000, reportedTemperatureC = -10))
    }

    @Test
    fun `should interpolate by height between grid points`() {
        assertEquals(65, coldTemperatureCorrectedAltitude(heightAboveAirportFt = 50, reportedTemperatureC = -20))
    }

    @Test
    fun `should interpolate by height and temperature`() {
        assertEquals(1350, coldTemperatureCorrectedAltitude(heightAboveAirportFt = 1250, reportedTemperatureC = -5))
    }

    @Test
    fun `should clamp values outside table bounds`() {
        assertEquals(7500, coldTemperatureCorrectedAltitude(heightAboveAirportFt = 6000, reportedTemperatureC = -60))
    }
}
