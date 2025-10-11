package alexmaryin.metarkt.models

public enum class VisibilityUnit { METERS, SM }

public enum class VisibilityDirection { NORTH, NORTH_EAST, EAST, SOUTH_EAST, SOUTH, SOUTH_WEST, WEST, NORTH_WEST }

public data class Visibility(
    val distAll: Int? = null,
    val byDirections: List<VisibilityByDir> = emptyList(),
    val byRunways: List<VisibilityByRunway> = emptyList(),
    val distUnits: VisibilityUnit = VisibilityUnit.METERS,
    )

public data class VisibilityByDir(
    val dist: Int,
    val direction: VisibilityDirection
)

public data class VisibilityByRunway(
    val dist: Int,
    val runway: String
)