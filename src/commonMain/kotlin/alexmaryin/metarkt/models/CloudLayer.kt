package alexmaryin.metarkt.models

public enum class CloudsType(public val code: String) {
    CLEAR("SKC"), NIL_SIGNIFICANT("NSC"), FEW("FEW"), SCATTERED("SCT"), BROKEN("BKN"), OVERCAST("OVC")
}

public enum class CumulusType { CUMULONIMBUS, TOWERING_CUMULUS }

public data class CloudLayer(
    val type: CloudsType,
    val lowMarginFt: Int,
    val cumulusType: CumulusType? = null
)