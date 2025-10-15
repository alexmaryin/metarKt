package alexmaryin.metarkt.helpers

import alexmaryin.metarkt.models.PressureQFE
import kotlin.math.exp
import kotlin.math.floor
import kotlin.math.pow

private const val T0 = 288.15             // K, ISA sea level temp
private const val L = 0.0065              // K/m, lapse rate
private const val g = 9.80665             // m/s^2
private const val R = 287.053             // J/(kg*K)
private const val EXPONENT = g / (R * L)  // ≈ 5.255877

/**
 * @return QNH pressure converted from QFE for ISA (Standard Atmosphere)
 * @param elevationMeters elevation in meters above sea level
 *
 * ICAO DOC 8896: "In local routine reports and METAR, atmospheric pressure is given in hectopascals,
 * rounded down to the nearest whole hectopascal." OR "Any observed value which does not fit
 * the reporting scale in use shall be rounded down to the nearest lower whole hectopascal."
 */
public fun PressureQFE.toIsaQnh(elevationMeters: Int): Int {
    val factor = 1.0 - (L * elevationMeters) / T0
    return floor(milliBar * factor.pow(-EXPONENT)).toInt()
}

/**
 * @return QNH pressure converted from QFE for actual atmosphere
 * @param elevationMeters elevation in meters above sea level
 * @param celsius local temperature in Celsius
 *
 * ICAO DOC 8896: "In local routine reports and METAR, atmospheric pressure is given in hectopascals,
 * rounded down to the nearest whole hectopascal." OR "Any observed value which does not fit
 * the reporting scale in use shall be rounded down to the nearest lower whole hectopascal."
 */
public fun PressureQFE.toCorrectedQnh(elevationMeters: Int, celsius: Int): Int {
    val tmK = celsius + 273.15
    return floor(milliBar * exp((g * elevationMeters) / (R * tmK))).toInt()
}