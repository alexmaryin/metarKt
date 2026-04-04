package alexmaryin.metarkt.helpers

import kotlin.test.Test
import kotlin.test.assertEquals

class ColdTemperatureAltitudeCorrectionTest {

    private val missoElevationFt = 3206

    @Test
    fun `should return exact table value on grid point`() {
        assertEquals(1100, coldTemperatureCorrectedAltitude(heightAboveAirportFt = 1000, reportedTemperatureC = -10))
    }

    @Test
    fun `should interpolate for height and temperature between grid points - Missoula outside FAF example`() {
        // Official example: 2994 ft at -12°C should give 320 ft correction
        val correction = ColdTemperatureCorrectionTable.correctionFor(2994, -12)
        assertEquals(320, correction)
    }

    @Test
    fun `should interpolate for height and temperature - Missoula inside FAF example`() {
        // Official example: 1314 ft at -12°C
        // Official manual calculation rounds up to 1500 ft for safety, giving 170 ft
        // Our interpolation gives a more precise value
        val correction = ColdTemperatureCorrectionTable.correctionFor(1314, -12)
        // Interpolated: between 1000 ft (-10°C=100, -20°C=140) and 1500 ft (-10°C=150, -20°C=210)
        // At 1314 ft, -12°C: approximately 140 ft
        assertEquals(140, correction)
    }

    @Test
    fun `should interpolate temperature between -10 and -20 at 3000 ft`() {
        // At 3000 ft: -10°C = 290, -20°C = 420
        // At -12°C: should be 290 + 0.2 * (420 - 290) = 290 + 26 = 316, rounds to 320
        val correction = ColdTemperatureCorrectionTable.correctionFor(3000, -12)
        assertEquals(320, correction)
    }

    @Test
    fun `should interpolate height between 1000 and 1500 at -10C`() {
        // At -10°C: 1000 ft = 100, 1500 ft = 150
        // At 1314 ft: should be 100 + (314/500) * (150 - 100) = 100 + 31.4 = 131.4, rounds to 130
        val correction = ColdTemperatureCorrectionTable.correctionFor(1314, -10)
        assertEquals(130, correction)
    }

    @Test
    fun `should handle exact grid point without interpolation`() {
        assertEquals(290, ColdTemperatureCorrectionTable.correctionFor(3000, -10))
        assertEquals(420, ColdTemperatureCorrectionTable.correctionFor(3000, -20))
        assertEquals(150, ColdTemperatureCorrectionTable.correctionFor(1500, -10))
        assertEquals(210, ColdTemperatureCorrectionTable.correctionFor(1500, -20))
    }

    @Test
    fun `should clamp values outside table bounds`() {
        // Height above max (5000 ft) should use max height
        // Temperature below min (-50°C) should use min temperature
        val correction = ColdTemperatureCorrectionTable.correctionFor(6000, -60)
        assertEquals(1500, correction)
    }

    @Test
    fun `should correct Missoula FAF altitude at -20C`() {
        // SUPPY (FAF): 6200 ft, airport elevation 3206 ft
        // Height above airport: 2994 ft
        val heightAboveAirport = 6200 - missoElevationFt
        val correction = ColdTemperatureCorrectionTable.correctionFor(heightAboveAirport, -20)
        val correctedAltitude = 6200 + correction
        assertEquals(6620, correctedAltitude) // 6200 + 420 = 6620
    }

    @Test
    fun `should correct Missoula IAF altitude at -20C`() {
        // LANNY/CHARL/ODIRE (IAF): 9400 ft, airport elevation 3206 ft
        // Height above airport: 6194 ft (exceeds table max 5000 ft, uses 5000 ft)
        val heightAboveAirport = 9400 - missoElevationFt
        val correction = ColdTemperatureCorrectionTable.correctionFor(heightAboveAirport, -20)
        val correctedAltitude = 9400 + correction
        // At 5000 ft, -20°C: correction is 710 ft
        assertEquals(10110, correctedAltitude) // 9400 + 710 = 10110
    }

    @Test
    fun `should correct Missoula LP MDA at -20C`() {
        // LP MDA: 4520 ft, airport elevation 3206 ft
        // Height above airport: 1314 ft
        val heightAboveAirport = 4520 - missoElevationFt
        val correction = ColdTemperatureCorrectionTable.correctionFor(heightAboveAirport, -20)
        val correctedAltitude = 4520 + correction
        // Interpolated correction at 1314 ft, -20°C: approximately 180 ft
        assertEquals(4700, correctedAltitude)
    }
}
