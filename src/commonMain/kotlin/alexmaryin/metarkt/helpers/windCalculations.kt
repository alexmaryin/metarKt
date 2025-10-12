package alexmaryin.metarkt.helpers

import alexmaryin.metarkt.models.Wind
import kotlin.math.*

/**
 * @param runwayHeadingTrue true course of runway in degrees.
 * @return [WindComponent] which contains
 * headwind and crosswind component in Knots for given runway heading.
 * Also, boolean flag fromLeft defines crosswind direction.
 */
public fun Wind.componentForRunwayTrue(runwayHeadingTrue: Int): WindComponent {
    val angleDeg = (direction - runwayHeadingTrue).toDouble()
    val angleRad = angleDeg * PI / 180.0

    val headwind = speed * cos(angleRad)
    val crosswindRaw = speed * sin(angleRad)

    return WindComponent(
        headwind = headwind,
        crosswind = abs(crosswindRaw),
        fromLeft = crosswindRaw > 0
    )
}