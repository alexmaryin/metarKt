package alexmaryin.metarkt.helpers

import kotlin.test.Test
import kotlin.test.assertEquals

class ColdTemperatureAltitudeCorrectionTest {

    private val missoElevationFt = 3206

    @Test
    fun `should return exact table value on grid point`() {
        assertEquals(1100, coldTemperatureCorrectedAltitude(heightAboveAirportFt = 1000, reportedTemperatureC = -10))
    }

    @Test
    fun `should interpolate for height and temperature between grid points - Missoula outside FAF example`() {
        // Official example: 2994 ft at -12°C should give 320 ft correction
        val correction = ColdTemperatureCorrectionTable.correctionFor(2994, -12)
        assertEquals(320, correction)
    }

    @Test
    fun `should interpolate for height and temperature - Missoula inside FAF example`() {
        // Official example: 1314 ft at -12°C
        // Official manual calculation rounds up to 1500 ft for safety, giving 170 ft
        // Our interpolation gives a more precise value, but we round UP for safety
        val correction = ColdTemperatureCorrectionTable.correctionFor(1314, -12)
        // Interpolated: between 1000 ft (-10°C=100, -20°C=140) and 1500 ft (-10°C=150, -20°C=210)
        // At 1314 ft, -12°C: approximately 141 ft, rounds UP to 150 ft
        assertEquals(150, correction)
    }

    @Test
    fun `should interpolate temperature between -10 and -20 at 3000 ft`() {
        // At 3000 ft: -10°C = 290, -20°C = 420
        // At -12°C: should be 290 + 0.2 * (420 - 290) = 290 + 26 = 316, rounds UP to 320
        val correction = ColdTemperatureCorrectionTable.correctionFor(3000, -12)
        assertEquals(320, correction)
    }

    @Test
    fun `should interpolate height between 1000 and 1500 at -10C`() {
        // At -10°C: 1000 ft = 100, 1500 ft = 150
        // At 1314 ft: should be 100 + (314/500) * (150 - 100) = 100 + 31.4 = 131.4, rounds UP to 140
        val correction = ColdTemperatureCorrectionTable.correctionFor(1314, -10)
        assertEquals(140, correction)
    }

    @Test
    fun `should handle exact grid point without interpolation`() {
        assertEquals(290, ColdTemperatureCorrectionTable.correctionFor(3000, -10))
        assertEquals(420, ColdTemperatureCorrectionTable.correctionFor(3000, -20))
        assertEquals(150, ColdTemperatureCorrectionTable.correctionFor(1500, -10))
        assertEquals(210, ColdTemperatureCorrectionTable.correctionFor(1500, -20))
    }

    @Test
    fun `should clamp values outside table bounds`() {
        // Height above max (5000 ft) should use max height
        // Temperature below min (-50°C) should use min temperature
        val correction = ColdTemperatureCorrectionTable.correctionFor(6000, -60)
        assertEquals(1500, correction)
    }

    @Test
    fun `should correct Missoula FAF altitude at -20C`() {
        // SUPPY (FAF): 6200 ft, airport elevation 3206 ft
        // Height above airport: 2994 ft
        val heightAboveAirport = 6200 - missoElevationFt
        val correction = ColdTemperatureCorrectionTable.correctionFor(heightAboveAirport, -20)
        val correctedAltitude = 6200 + correction
        assertEquals(6620, correctedAltitude) // 6200 + 420 = 6620
    }

    @Test
    fun `should correct Missoula IAF altitude at -20C`() {
        // LANNY/CHARL/ODIRE (IAF): 9400 ft, airport elevation 3206 ft
        // Height above airport: 6194 ft (exceeds table max 5000 ft, uses 5000 ft)
        val heightAboveAirport = 9400 - missoElevationFt
        val correction = ColdTemperatureCorrectionTable.correctionFor(heightAboveAirport, -20)
        val correctedAltitude = 9400 + correction
        // At 5000 ft, -20°C: correction is 710 ft
        assertEquals(10110, correctedAltitude) // 9400 + 710 = 10110
    }

    @Test
    fun `should correct Missoula LP MDA at -20C`() {
        // LP MDA: 4520 ft, airport elevation 3206 ft
        // Height above airport: 1314 ft
        val heightAboveAirport = 4520 - missoElevationFt
        val correction = ColdTemperatureCorrectionTable.correctionFor(heightAboveAirport, -20)
        val correctedAltitude = 4520 + correction
        // Interpolated correction at 1314 ft, -20°C: approximately 181 ft, rounds UP to 190 ft
        assertEquals(4710, correctedAltitude)
    }

    @Test
    fun `debug Missoula calculations at -12C`() {
        // LANNY: 9400 ft
        val lannyHeightAbove = 9400 - missoElevationFt
        val lannyCorrection = ColdTemperatureCorrectionTable.correctionFor(lannyHeightAbove, -12)
        val lannyCorrected = 9400 + lannyCorrection
        println("LANNY: heightAbove=$lannyHeightAbove, correction=$lannyCorrection, corrected=$lannyCorrected")
        
        // SUPPY: 6200 ft
        val suppyHeightAbove = 6200 - missoElevationFt
        val suppyCorrection = ColdTemperatureCorrectionTable.correctionFor(suppyHeightAbove, -12)
        val suppyCorrected = 6200 + suppyCorrection
        println("SUPPY: heightAbove=$suppyHeightAbove, correction=$suppyCorrection, corrected=$suppyCorrected")
        
        // MDA: 4520 ft
        val mdaHeightAbove = 4520 - missoElevationFt
        val mdaCorrection = ColdTemperatureCorrectionTable.correctionFor(mdaHeightAbove, -12)
        val mdaCorrected = 4520 + mdaCorrection
        println("MDA: heightAbove=$mdaHeightAbove, correction=$mdaCorrection, corrected=$mdaCorrected")
    }

    @Test
    fun `should calculate Missoula corrections using FAA segment-based method at -12C`() {
        // FAA AIM 7-3-6 Example: Missoula RNAV (GPS) Y RWY 12
        // Airport elevation: 3,206 ft, Temperature: -12°C
        // FAF (SUPPY): 6,200 ft, MDA: 4,520 ft
        
        val corrections = calculateColdTemperatureCorrections(
            fafAltitude = 6200,
            mdaAltitude = 4520,
            airportElevation = missoElevationFt,
            reportedTemperatureC = -12,
            roundUpForSafety = true // FAA recommended
        )
        
        // Intermediate segment correction based on FAF height (2,994 ft above airport)
        // Should be 320 ft (as per FAA example calculation)
        assertEquals(320, corrections.intermediateSegmentCorrection)
        
        // Final segment correction based on MDA height (1,314 ft above airport)
        // FAA recommends rounding UP to 1,500 ft for safety
        // At 1,500 ft and -12°C: interpolation between -10°C (150 ft) and -20°C (210 ft)
        // = 150 + 0.2 * (210 - 150) = 150 + 12 = 162, rounds to 170 ft
        assertEquals(170, corrections.finalSegmentCorrection)
        assertEquals(1500, corrections.mdaHeightAboveAirportRounded)
        
        // Test intermediate segment fixes (IAFs and stepdown fixes)
        assertEquals(9720, corrections.correctIntermediateSegment(9400)) // LANNY/CHARL/ODIRE
        assertEquals(7320, corrections.correctIntermediateSegment(7000)) // CALIP
        assertEquals(6520, corrections.correctIntermediateSegment(6200)) // SUPPY (FAF)
        
        // Test final segment (MDA) - with round up for safety: 4520 + 170 = 4690
        assertEquals(4690, corrections.correctFinalSegment(4520)) // MDA
    }

    @Test
    fun `should demonstrate segment-based vs individual correction difference`() {
        // This test shows why segment-based method is correct
        
        // WRONG WAY (individual corrections - what the app was doing):
        // Each fix gets its own correction based on its height above airport
        val lannyHeightAbove = 9400 - missoElevationFt // 6194 ft
        val lannyCorrection = ColdTemperatureCorrectionTable.correctionFor(lannyHeightAbove, -12)
        val lannyWrong = 9400 + lannyCorrection // 9400 + 530 = 9930
        
        val suppyHeightAbove = 6200 - missoElevationFt // 2994 ft
        val suppyCorrection = ColdTemperatureCorrectionTable.correctionFor(suppyHeightAbove, -12)
        val suppyWrong = 6200 + suppyCorrection // 6200 + 320 = 6520
        
        val mdaHeightAbove = 4520 - missoElevationFt // 1314 ft
        val mdaCorrection = ColdTemperatureCorrectionTable.correctionFor(mdaHeightAbove, -12)
        val mdaWrong = 4520 + mdaCorrection // 4520 + 140 = 4660
        
        // CORRECT WAY (FAA segment-based method):
        // Use FAF height for all intermediate segment fixes
        val corrections = calculateColdTemperatureCorrections(
            fafAltitude = 6200,
            mdaAltitude = 4520,
            airportElevation = missoElevationFt,
            reportedTemperatureC = -12
        )
        
        val lannyCorrect = corrections.correctIntermediateSegment(9400) // 9400 + 320 = 9720
        val suppyCorrect = corrections.correctIntermediateSegment(6200) // 6200 + 320 = 6520
        val mdaCorrect = corrections.correctFinalSegment(4520) // 4520 + 140 = 4660
        
        // Show the difference
        println("=== Segment-based vs Individual Correction ===")
        println("LANNY: Individual=$lannyWrong (WRONG - too high), Segment-based=$lannyCorrect (CORRECT)")
        println("SUPPY: Individual=$suppyWrong, Segment-based=$suppyCorrect")
        println("MDA: Individual=$mdaWrong, Segment-based=$mdaCorrect")
        
        // LANNY: 9940 ft (wrong - too high!), Segment-based: 9720 ft (correct)
        assertEquals(9940, lannyWrong)
        assertEquals(9720, lannyCorrect)
        
        // SUPPY: 6520 ft (same for both in this case)
        assertEquals(6520, suppyWrong)
        assertEquals(6520, suppyCorrect)
        
        // MDA: 4670 ft (individual), Segment-based: 4690 ft (with round up for safety)
        assertEquals(4670, mdaWrong)
        assertEquals(4690, mdaCorrect)
    }

    @Test
    fun `should demonstrate round up for safety vs exact interpolation`() {
        // FAA recommends rounding UP height above airport to next table value for safety
        
        // Without rounding (exact interpolation):
        val correctionsExact = calculateColdTemperatureCorrections(
            fafAltitude = 6200,
            mdaAltitude = 4520,
            airportElevation = missoElevationFt,
            reportedTemperatureC = -12,
            roundUpForSafety = false
        )
        
        // With rounding up for safety (FAA recommended):
        val correctionsRounded = calculateColdTemperatureCorrections(
            fafAltitude = 6200,
            mdaAltitude = 4520,
            airportElevation = missoElevationFt,
            reportedTemperatureC = -12,
            roundUpForSafety = true
        )
        
        println("=== Round Up for Safety Comparison ===")
        println("MDA Height Above Airport: ${correctionsExact.mdaHeightAboveAirport} ft")
        println("Without rounding: correction=${correctionsExact.finalSegmentCorrection}, MDA=${correctionsExact.correctFinalSegment(4520)}")
        println("With rounding up: correction=${correctionsRounded.finalSegmentCorrection}, MDA=${correctionsRounded.correctFinalSegment(4520)}")
        
        // Without rounding: 1,314 ft at -12°C = 150 ft correction (rounds UP from ~141)
        assertEquals(150, correctionsExact.finalSegmentCorrection)
        assertEquals(4670, correctionsExact.correctFinalSegment(4520))
        
        // With rounding up: 1,500 ft at -12°C = 170 ft correction (FAA recommended for safety)
        assertEquals(170, correctionsRounded.finalSegmentCorrection)
        assertEquals(4690, correctionsRounded.correctFinalSegment(4520))
    }

    @Test
    fun `should round up height to next table value correctly`() {
        assertEquals(200, ColdTemperatureCorrectionTable.roundUpToNextTableValue(150))
        assertEquals(200, ColdTemperatureCorrectionTable.roundUpToNextTableValue(200))
        assertEquals(300, ColdTemperatureCorrectionTable.roundUpToNextTableValue(201))
        assertEquals(1500, ColdTemperatureCorrectionTable.roundUpToNextTableValue(1314))
        assertEquals(1500, ColdTemperatureCorrectionTable.roundUpToNextTableValue(1500))
        assertEquals(2000, ColdTemperatureCorrectionTable.roundUpToNextTableValue(1501))
        assertEquals(5000, ColdTemperatureCorrectionTable.roundUpToNextTableValue(4500))
        assertEquals(5000, ColdTemperatureCorrectionTable.roundUpToNextTableValue(5000))
        assertEquals(5000, ColdTemperatureCorrectionTable.roundUpToNextTableValue(6000)) // Caps at max
    }
}
