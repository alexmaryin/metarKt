package alexmaryin.metarkt.parser

import alexmaryin.metarkt.MetarParser
import alexmaryin.metarkt.models.*
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import kotlin.math.pow
import kotlin.math.round
import kotlin.time.Clock
import kotlin.time.ExperimentalTime

fun Double.formatToFloat(decimals: Int): Float {
    val multiplier = 10f.pow(decimals)
    return (this * multiplier).toInt() / multiplier
}

class MetarParserKt : MetarParser {
    private val parts = mutableListOf<String>()

    private fun parseStation(): String? {
        parts.forEach { part ->
            MetarGroups.STATION.find(part)?.let {
                return it.value
            }
        }
        return null
    }

    @OptIn(ExperimentalTime::class)
    private fun parseReportTime(): LocalDateTime? {
        parts.forEach { part ->
            MetarGroups.REPORT_TIME.find(part)?.let {
                val stamp = Clock.System.now().toLocalDateTime(TimeZone.UTC)
                return LocalDateTime(
                    stamp.year, stamp.month, it.groupValues[1].toInt(), it.groupValues[2].toInt(), it.groupValues[3].toInt()
                )
            }
        }
        return null
    }

    private fun parseWind(): Wind? {
        parts.forEach { part ->
            MetarGroups.WIND.find(part)?.let {
                return Wind(
                    direction = it.groupValues[1].toIntOrNull() ?: 0,
                    variable = it.groupValues[1] == "VRB",
                    speed = it.groupValues[2].toIntOrNull() ?: 0,
                    speedUnits = when (it.groupValues[4]) {
                        "MPS" -> WindUnit.MPS
                        "KPH" -> WindUnit.KPH
                        else -> WindUnit.KT
                    },
                    gusts = it.groupValues[3].toIntOrNull() ?: 0
                )
            }
        }
        return null
    }

    private fun parseVisibility(): Visibility? {

        val byDirs: MutableList<VisibilityByDir> = mutableListOf()
        val byRunways: MutableList<VisibilityByRunway> = mutableListOf()

        parts.forEach { part ->
            MetarGroups.VISIBILITY.find(part)?.let {
                when {
                    it.groupValues[2].isBlank() && it.groupValues[6].isBlank() -> {
                        return Visibility(
                            distAll = it.groupValues[1].toIntOrNull() ?: it.groupValues[3].toIntOrNull()
                            ?: if (it.groupValues[5] == "CAVOK") 9999 else throw RuntimeException("Visibility undefined in part ${it.groupValues[0]}!"),
                            distUnits = if (it.groupValues[4] == "SM") VisibilityUnit.SM else VisibilityUnit.METERS,
                        )
                    }
                    it.groupValues[2].isNotBlank() -> {
                        byDirs += VisibilityByDir(
                            dist = it.groupValues[1].toIntOrNull() ?: throw RuntimeException("Visibility for direction ${it.groupValues[2]} undefined!"),
                            direction = when (it.groupValues[2]) {
                                "N" -> VisibilityDirection.NORTH
                                "NE" -> VisibilityDirection.NORTH_EAST
                                "E" -> VisibilityDirection.EAST
                                "SE" -> VisibilityDirection.SOUTH_EAST
                                "S" -> VisibilityDirection.SOUTH
                                "SW" -> VisibilityDirection.SOUTH_WEST
                                "W" -> VisibilityDirection.WEST
                                "NW" -> VisibilityDirection.NORTH_WEST
                                else -> throw RuntimeException("Unknown visibility direction ${it.groupValues[2]}!")
                            }
                        )
                    }
                    it.groupValues[6].isNotBlank() -> {
                        byRunways += VisibilityByRunway(
                            dist = it.groupValues[7].toIntOrNull() ?: throw RuntimeException("Visibility for runway ${it.groupValues[6]} undefined!"),
                            runway = it.groupValues[6]
                        )
                    }
                }
            }
        }
        return if (byDirs.isNotEmpty() || byRunways.isNotEmpty()) Visibility(byDirections = byDirs, byRunways = byRunways) else null
    }

    private fun parsePhenomenons(): List<WeatherPhenomenon> {

        val items = mutableListOf<WeatherPhenomenon>()

        parts.forEach { part ->
            MetarGroups.PHENOMENONS.find(part)?.let { match ->
                val intensity = if (match.groupValues[1].isNotBlank()) {
                    when (match.groupValues[1]) {
                        "+" -> PhenomenonIntensity.HIGH
                        "-" -> PhenomenonIntensity.LIGHT
                        else -> PhenomenonIntensity.NONE
                    }
                } else PhenomenonIntensity.NONE

                val group = mutableSetOf<Phenomenons>()
                if (match.groupValues[2].isNotBlank()) {
                    match.groupValues[2].windowed(2, 2) { code ->
                        group += Phenomenons.entries.first { it.code == code }
                    }
                }
                items += WeatherPhenomenon(group, intensity)
            }
        }
        return items
    }

    private fun parseClouds(): List<CloudLayer> {

        val items = mutableListOf<CloudLayer>()

        parts.forEach { part ->
            MetarGroups.CLOUDS.find(part)?.let { match ->
                val type = CloudsType.entries.first { it.code == match.groupValues[1] }
                val cumulus = when (match.groupValues[3]) {
                    "CB" -> CumulusType.CUMULONIMBUS
                    "TCU" -> CumulusType.TOWERING_CUMULUS
                    else -> null
                }
                items += CloudLayer(
                    type = type,
                    lowMarginFt = match.groupValues[2].toIntOrNull() ?: throw RuntimeException("No margin for cloud layer in ${match.groupValues[0]}"),
                    cumulusType = cumulus
                )
            }
        }
        return items.sortedBy { it.lowMarginFt }
    }

    private fun parseTemperature(): Temperature? {
        parts.forEach { part ->
            MetarGroups.TEMPERATURE_DEW.find(part)?.let { match ->
                return Temperature(
                    air = match.groupValues[1].replace("M", "-").toInt(),
                    dewPoint = match.groupValues[2].replace("M", "-").toInt()
                )
            }
        }
        return null
    }

    private fun parsePressure(): PressureQNH? {
        parts.forEach { part ->
            MetarGroups.PRESSURE.find(part)?.let { match ->
                if (match.groupValues[1].isNotBlank()) {
                    return PressureQNH(
                        inHg = match.groupValues[1].substringAfter('A').toInt() / 100f,
                        hPa = round(match.groupValues[1].substringAfter('A').toInt() * ONE_INCH_HG / 100).toInt()
                    )
                }
                if (match.groupValues[2].isNotBlank()) {
                    return PressureQNH(
                        hPa = match.groupValues[2].substringAfter('Q').toInt(),
                        inHg = (match.groupValues[2].substringAfter('Q').toInt() / ONE_INCH_HG).formatToFloat(2)
                    )
                }
            }
        }
        return null
    }

    override fun parse(rawMetar: String): Metar {

        parts.clear()
        parts.addAll(rawMetar.split(" ", "\t"))

        return Metar(
            station = parseStation(),
            reportTime = parseReportTime(),
            wind = parseWind(),
            visibility = parseVisibility(),
            phenomenons = parsePhenomenons(),
            clouds = parseClouds(),
            temperature = parseTemperature(),
            pressureQNH = parsePressure(),
            raw = rawMetar
        )
    }
}
