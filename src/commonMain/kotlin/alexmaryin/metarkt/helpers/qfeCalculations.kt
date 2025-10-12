package alexmaryin.metarkt.helpers

import alexmaryin.metarkt.models.PressureQFE
import kotlin.math.exp
import kotlin.math.pow
import kotlin.math.roundToInt

private const val T0 = 288.15             // K, ISA sea level temp
private const val L = 0.0065              // K/m, lapse rate
private const val g = 9.80665             // m/s^2
private const val R = 287.053             // J/(kg*K)
private const val EXPONENT = g / (R * L)  // ≈ 5.255877

public fun PressureQFE.toIsaQnh(elevationMeters: Int): Int {
    val factor = 1.0 - (L * elevationMeters) / T0
    return (milliBar * factor.pow(-EXPONENT)).roundToInt()
}

public fun PressureQFE.toCorrectedQnh(elevationMeters: Int, celsius: Int): Int {
    val tmK = celsius + 273.15
    return (milliBar * exp((g * elevationMeters) / (R * tmK))).roundToInt()
}