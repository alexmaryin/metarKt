package alexmaryin.metarkt.models

import kotlin.math.roundToInt

public const val ONE_INCH_HG: Double = 33.863886666667

public data class PressureQNH(
    val hPa: Int,
    val inHg: Float
) {
    public companion object {
        public fun fromHpa(hPa: Int): PressureQNH = PressureQNH(hPa, (hPa / ONE_INCH_HG).toFloat())
        public fun fromInHg(inHg: Float): PressureQNH = PressureQNH((inHg * ONE_INCH_HG).roundToInt(), inHg)

        public fun standard(): PressureQNH = PressureQNH(1013, 29.92f)
    }
}