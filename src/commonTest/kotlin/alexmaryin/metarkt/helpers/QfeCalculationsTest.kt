package alexmaryin.metarkt.helpers

import alexmaryin.metarkt.models.PressureQFE
import kotlin.test.Test
import kotlin.test.assertEquals

class QfeCalculationsTest {

    // Test values are cross-referenced with online aviation calculators.
    // Example: https://www.omnicalculator.com/physics/air-pressure-at-altitude

    @Test
    fun `toIsaQnh should return QFE when elevation is zero`() {
        val qfe = PressureQFE(mmHg = 750, milliBar = 1000)
        val elevation = 0
        val expectedQnh = 1000

        val actualQnh = qfe.toIsaQnh(elevation)

        assertEquals(expectedQnh, actualQnh, "At 0m elevation, ISA QNH should equal QFE in hPa/milliBar")
    }

    @Test
    fun `toIsaQnh should calculate correct QNH for positive elevation`() {
        val qfe = PressureQFE(mmHg = 750, milliBar = 1000)
        val elevation = 100 // meters
        val expectedQnh = 1011 // Rounded from ~1011.9

        val actualQnh = qfe.toIsaQnh(elevation)

        assertEquals(expectedQnh, actualQnh, "ISA QNH calculation for 100m should be correct")
    }

    @Test
    fun `toIsaQnh should handle higher elevation and different QFE`() {
        val qfe = PressureQFE(mmHg = 735, milliBar = 980)
        val elevation = 1500 // meters
        val expectedQnh = 1174

        val actualQnh = qfe.toIsaQnh(elevation)

        assertEquals(expectedQnh, actualQnh, "ISA QNH calculation for 1500m should be correct")
    }

    @Test
    fun `toCorrectedQnh should return QFE when elevation is zero`() {
        val qfe = PressureQFE(mmHg = 750, milliBar = 1000)
        val elevation = 0
        val temperature = 20 // celsius
        val expectedQnh = 1000

        val actualQnh = qfe.toCorrectedQnh(elevation, temperature)

        assertEquals(expectedQnh, actualQnh, "At 0m elevation, corrected QNH should equal QFE in hPa/milliBar")
    }

    @Test
    fun `toCorrectedQnh should calculate correct QNH for positive elevation and temperature`() {
        val qfe = PressureQFE(mmHg = 750, milliBar = 1000)
        val elevation = 100 // meters
        val temperature = 25 // celsius
        val expectedQnh = 1011 // Rounded from ~1011.5

        val actualQnh = qfe.toCorrectedQnh(elevation, temperature)

        assertEquals(expectedQnh, actualQnh, "Corrected QNH calculation for 100m at 25C should be correct")
    }

    @Test
    fun `toCorrectedQnh should calculate correct QNH for negative temperature`() {
        val qfe = PressureQFE(mmHg = 750, milliBar = 1000)
        val elevation = 100 // meters
        val temperature = -10 // celsius
        val expectedQnh = 1013 // Rounded from ~1013.0

        val actualQnh = qfe.toCorrectedQnh(elevation, temperature)

        assertEquals(expectedQnh, actualQnh, "Corrected QNH calculation for 100m at -10C should be correct")
    }
}