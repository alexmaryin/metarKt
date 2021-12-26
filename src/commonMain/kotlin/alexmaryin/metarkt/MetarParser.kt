package alexmaryin.metarkt

import alexmaryin.metarkt.models.Metar
import alexmaryin.metarkt.parser.MetarParserKt

/**
 * MetarParser includes the implementation to parse raw METAR string to data class
 * with structured weather information. To get actual implementation invoke as follows:
 * @constructor MetarParser.current()
 *
 * @author Alex Maryin, 2021
 */

fun interface MetarParser {
    /**
     * Returns parsed data class with METAR information.
     * @param rawMetar [String] - string with METAR information, i.e.:
     *
     * LOWI 231120Z VRB02KT CAVOK 00/M07 Q1018 NOSIG
     *
     * @return [Metar] - data class with structured weather information.
     */
    fun parse(rawMetar: String): Metar

    /**
     * MetarParser includes the implementation to parse raw METAR string to data class
     * with structured weather information. To get actual implementation invoke as follows:
     * @constructor MetarParser.current()
     *
     * @author Alex Maryin, 2021
     */
    companion object {
        fun current() = MetarParserKt()
    }
}