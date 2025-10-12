package alexmaryin.metarkt.helpers

import alexmaryin.metarkt.models.Wind
import alexmaryin.metarkt.models.WindUnit
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
        val wind = Wind(direction = 270, speed = 10, speedUnits = WindUnit.KT)
        val runway = 180
        val component = wind.componentForRunwayTrue(runway)
        assertEquals(0.0, component.headwind, 0.01)
        assertEquals(10.0, component.crosswind, 0.01)
        assertTrue(component.fromLeft)
    }

    @Test
    fun `test direct crosswind from right`() {
        val wind = Wind(direction = 90, speed = 10, speedUnits = WindUnit.KT)
        val runway = 180
        val component = wind.componentForRunwayTrue(runway)
        assertEquals(0.0, component.headwind, 0.01)
        assertEquals(10.0, component.crosswind, 0.01)
        assertFalse(component.fromLeft)
    }

    @Test
    fun `test angled headwind from left`() {
        val wind = Wind(direction = 225, speed = 10, speedUnits = WindUnit.KT)
        val runway = 180
        val component = wind.componentForRunwayTrue(runway)
        // angle is 45 deg. cos(45) = sin(45) = 0.707
        assertEquals(10 * 0.707, component.headwind, 0.01)
        assertEquals(10 * 0.707, component.crosswind, 0.01)
        assertTrue(component.fromLeft)
    }

    @Test
    fun `test angled tailwind from right`() {
        val wind = Wind(direction = 45, speed = 15, speedUnits = WindUnit.KT)
        val runway = 180
        val component = wind.componentForRunwayTrue(runway)
        // angle is -135 deg. cos(-135) = -0.707, sin(-135) = -0.707
        assertEquals(-15 * 0.707, component.headwind, 0.01)
        assertEquals(15 * 0.707, component.crosswind, 0.01)
        assertFalse(component.fromLeft)
    }
}
