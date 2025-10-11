package alexmaryin.metarkt.models

import kotlin.math.roundToInt

private const val MPS_TO_KNOT = 1.94384
private const val KPH_TO_KNOT = 0.539957
private const val KPH_TO_MPS = 0.277778 // 1000 / 3600

public enum class WindUnit { KT, MPS, KPH }

public data class Wind(
    val direction: Int = 0,
    val variable: Boolean = false,
    val speed: Int = 0,
    val speedUnits: WindUnit = WindUnit.KT,
    val gusts: Int = 0
) {
    val isCalm: Boolean get() = speed == 0 && direction == 0

    val speedKt: Int get() = toKnots(speed)

    val gustsKt: Int get() = toKnots(gusts)

    val speedMps: Int get() = toMps(speed)

    val gustsMps: Int get() = toMps(gusts)

    private fun toKnots(value: Int): Int = when (speedUnits) {
        WindUnit.KT -> value
        WindUnit.MPS -> (value * MPS_TO_KNOT).roundToInt()
        WindUnit.KPH -> (value * KPH_TO_KNOT).roundToInt()
    }

    private fun toMps(value: Int): Int = when (speedUnits) {
        WindUnit.KT -> (value / MPS_TO_KNOT).roundToInt()
        WindUnit.MPS -> value
        WindUnit.KPH -> (value * KPH_TO_MPS).roundToInt()
    }

//     KPH conversion can be derived from the other two, but a direct factor is cleaner.
     private fun toKph(value: Int): Int = when (speedUnits) {
         WindUnit.KT -> (value / KPH_TO_KNOT).roundToInt()
         WindUnit.MPS -> (value / KPH_TO_MPS).roundToInt()
         WindUnit.KPH -> value
     }
}