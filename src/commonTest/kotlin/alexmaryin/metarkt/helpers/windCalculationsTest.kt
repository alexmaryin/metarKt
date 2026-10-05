package alexmaryin.metarkt.helpers

import alexmaryin.metarkt.models.Wind
import alexmaryin.metarkt.models.WindUnit
import kotlin.math.roundToInt
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class WindCalculationsTest {

    @Test
    fun `test direct headwind`() {
        val wind = Wind(direction = 180, speed = 10, speedUnits = WindUnit.KT)
        val runway = 180
        val component = wind.componentForRunwayTrue(runway)
        assertEquals(10.0, component.headwind, 0.01)
        assertEquals(0.0, component.crosswind, 0.01)
    }

    @Test
    fun `test direct tailwind`() {
        val wind = Wind(direction = 360, speed = 10, speedUnits = WindUnit.KT)
        val runway = 180
        val component = wind.componentForRunwayTrue(runway)
        assertEquals(-10.0, component.headwind, 0.01)
        assertEquals(0.0, component.crosswind, 0.01)
    }

    @Test
    fun `test direct crosswind from left`() {
        // on a southbound runway (180), wind from the east (090) hits the pilot's left side
        val wind = Wind(direction = 90, speed = 10, speedUnits = WindUnit.KT)
        val runway = 180
        val component = wind.componentForRunwayTrue(runway)
        assertEquals(0.0, component.headwind, 0.01)
        assertEquals(10.0, component.crosswind, 0.01)
        assertTrue(component.fromLeft)
    }

    @Test
    fun `test direct crosswind from right`() {
        // on a southbound runway (180), wind from the west (270) hits the pilot's right side
        val wind = Wind(direction = 270, speed = 10, speedUnits = WindUnit.KT)
        val runway = 180
        val component = wind.componentForRunwayTrue(runway)
        assertEquals(0.0, component.headwind, 0.01)
        assertEquals(10.0, component.crosswind, 0.01)
        assertFalse(component.fromLeft)
    }

    @Test
    fun `test angled headwind from left`() {
        val wind = Wind(direction = 135, speed = 10, speedUnits = WindUnit.KT)
        val runway = 180
        val component = wind.componentForRunwayTrue(runway)
        // angle is -45 deg. cos(45) = sin(45) = 0.707
        assertEquals(10 * 0.707, component.headwind, 0.01)
        assertEquals(10 * 0.707, component.crosswind, 0.01)
        assertTrue(component.fromLeft)
    }

    @Test
    fun `test angled tailwind from right`() {
        val wind = Wind(direction = 315, speed = 15, speedUnits = WindUnit.KT)
        val runway = 180
        val component = wind.componentForRunwayTrue(runway)
        // angle is 135 deg. cos(135) = -0.707, sin(135) = 0.707
        assertEquals(-15 * 0.707, component.headwind, 0.01)
        assertEquals(15 * 0.707, component.crosswind, 0.01)
        assertFalse(component.fromLeft)
    }

    @Test
    fun `test rwy 18 wind 150 05KT is left cross headwind`() {
        val wind = Wind(direction = 150, speed = 5, speedUnits = WindUnit.KT)
        val component = wind.componentForRunwayTrue(180)
        assertTrue(component.fromLeft)
        assertTrue(component.headwind > 0)
        assertEquals(2.5, component.crosswind, 0.01)
    }

    @Test
    fun `test rwy 36 wind 150 05KT is right tailwind with same crosswind magnitude`() {
        val wind = Wind(direction = 150, speed = 5, speedUnits = WindUnit.KT)
        val rwy18 = wind.componentForRunwayTrue(180)
        val rwy36 = wind.componentForRunwayTrue(360)
        assertFalse(rwy36.fromLeft)
        assertTrue(rwy36.headwind < 0)
        assertEquals(2.5, rwy36.crosswind, 0.01)
        // opposite runway ends of the same physical wind must display the same rounded crosswind
        assertEquals(rwy18.crosswind, rwy36.crosswind, 1e-9)
        assertEquals(rwy18.crosswind.roundToInt(), rwy36.crosswind.roundToInt())
    }

    @Test
    fun `test rwy 04 wind 030 12KT is from left`() {
        val wind = Wind(direction = 30, speed = 12, speedUnits = WindUnit.KT)
        val component = wind.componentForRunwayTrue(35)
        assertTrue(component.fromLeft)
        assertTrue(component.headwind > 0)
    }

    @Test
    fun `test rwy 22 wind 030 12KT is from right`() {
        val wind = Wind(direction = 30, speed = 12, speedUnits = WindUnit.KT)
        val component = wind.componentForRunwayTrue(215)
        assertFalse(component.fromLeft)
        assertTrue(component.headwind < 0)
    }

    @Test
    fun `test rwy 04 and 22 report same rounded crosswind`() {
        val wind = Wind(direction = 30, speed = 12, speedUnits = WindUnit.KT)
        val rwy04 = wind.componentForRunwayTrue(35)
        val rwy22 = wind.componentForRunwayTrue(215)
        assertEquals(rwy04.crosswind, rwy22.crosswind, 1e-9)
        assertEquals(rwy04.crosswind.roundToInt(), rwy22.crosswind.roundToInt())
    }

    @Test
    fun `test wind aligned with runway has no crosswind`() {
        val wind = Wind(direction = 270, speed = 8, speedUnits = WindUnit.KT)
        val component = wind.componentForRunwayTrue(270)
        assertEquals(8.0, component.headwind, 0.01)
        assertEquals(0.0, component.crosswind, 0.01)
    }

    @Test
    fun `test calm wind`() {
        val wind = Wind(direction = 0, speed = 0, speedUnits = WindUnit.KT)
        val component = wind.componentForRunwayTrue(180)
        assertEquals(0.0, component.headwind, 0.01)
        assertEquals(0.0, component.crosswind, 0.01)
        assertFalse(component.fromLeft)
    }

    @Test
    fun `test variable direction wind is handled as its parsed direction`() {
        val wind = Wind(direction = 0, variable = true, speed = 8, speedUnits = WindUnit.KT)
        val component = wind.componentForRunwayTrue(180)
        assertEquals(-8.0, component.headwind, 0.01)
        assertEquals(0.0, component.crosswind, 0.01)
    }

    @Test
    fun `test angle wraps across north`() {
        // wind 350 on runway 02 (heading 020): relative angle -30, source 30 deg left of the nose
        val wind = Wind(direction = 350, speed = 10, speedUnits = WindUnit.KT)
        val component = wind.componentForRunwayTrue(20)
        assertTrue(component.fromLeft)
        assertTrue(component.headwind > 0)
        assertEquals(5.0, component.crosswind, 0.01)
    }
}
