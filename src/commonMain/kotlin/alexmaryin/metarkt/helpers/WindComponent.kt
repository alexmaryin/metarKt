package alexmaryin.metarkt.helpers

/**
 * wind components for given runway heading.
 */
public data class WindComponent(
    val headwind: Double,   // positive = headwind, negative = tailwind
    val crosswind: Double,  // always positive magnitude
    val fromLeft: Boolean   // true if crosswind comes from the left
)
