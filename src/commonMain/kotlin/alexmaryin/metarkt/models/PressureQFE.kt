package alexmaryin.metarkt.models

import kotlin.math.roundToInt

public const val ONE_MM_HG: Double = 1.33322

public data class PressureQFE(
    val mmHg: Int,
    val milliBar: Int
) {

    public constructor(mmHg: Int) : this(mmHg, (mmHg * ONE_MM_HG).roundToInt())

    public companion object {
        public fun standard(): PressureQFE = PressureQFE(760, 1013)
    }
}
