package com.example.model

import java.util.Locale
import kotlin.math.abs
import kotlin.math.floor
import kotlin.math.roundToInt

enum class ConversionDirection {
    INCHES_TO_FEET,
    FEET_TO_INCHES
}

data class ConversionResult(
    val id: Long = System.currentTimeMillis(),
    val inches: Double,
    val feet: Double,
    val wholeFeet: Long,
    val remainingInches: Double,
    val fractionLabel: String,
    val formulaExplanation: String,
    val metricCm: Double,
    val metricMeters: Double,
    val formattedResult: String
)

object MeasurementCalculator {

    fun convertInchesToFeet(totalInches: Double, precision: Int = 3): ConversionResult {
        val safeInches = if (totalInches.isNaN() || totalInches < 0.0) 0.0 else totalInches
        val totalFeet = safeInches / 12.0
        val wholeFt = floor(totalFeet).toLong()
        val remInches = safeInches - (wholeFt * 12.0)

        val fractionString = formatInchesWithFraction(remInches)
        val compoundLabel = if (wholeFt > 0) {
            if (remInches > 0.001) "$wholeFt ft $fractionString" else "$wholeFt ft"
        } else {
            fractionString
        }

        val metricCm = safeInches * 2.54
        val metricM = metricCm / 100.0

        val decimalFeetStr = String.format(Locale.US, "%.${precision}f", totalFeet)
        val formula = "${String.format(Locale.US, "%.2f", safeInches)} in ÷ 12 = $decimalFeetStr ft"

        return ConversionResult(
            inches = safeInches,
            feet = totalFeet,
            wholeFeet = wholeFt,
            remainingInches = remInches,
            fractionLabel = compoundLabel,
            formulaExplanation = formula,
            metricCm = metricCm,
            metricMeters = metricM,
            formattedResult = "$decimalFeetStr ft"
        )
    }

    fun convertFeetToInches(totalFeet: Double, precision: Int = 3): ConversionResult {
        val safeFeet = if (totalFeet.isNaN() || totalFeet < 0.0) 0.0 else totalFeet
        val totalInches = safeFeet * 12.0
        val wholeFt = floor(safeFeet).toLong()
        val remInches = totalInches - (wholeFt * 12.0)

        val fractionString = formatInchesWithFraction(remInches)
        val compoundLabel = if (wholeFt > 0) {
            if (remInches > 0.001) "$wholeFt ft $fractionString" else "$wholeFt ft"
        } else {
            fractionString
        }

        val metricCm = totalInches * 2.54
        val metricM = metricCm / 100.0

        val decimalInchesStr = String.format(Locale.US, "%.${precision}f", totalInches)
        val formula = "${String.format(Locale.US, "%.2f", safeFeet)} ft × 12 = $decimalInchesStr in"

        return ConversionResult(
            inches = totalInches,
            feet = safeFeet,
            wholeFeet = wholeFt,
            remainingInches = remInches,
            fractionLabel = compoundLabel,
            formulaExplanation = formula,
            metricCm = metricCm,
            metricMeters = metricM,
            formattedResult = "$decimalInchesStr in"
        )
    }

    fun convertCompoundToInches(feet: Double, inches: Double, precision: Int = 3): ConversionResult {
        val safeFeet = if (feet.isNaN() || feet < 0.0) 0.0 else feet
        val safeInches = if (inches.isNaN() || inches < 0.0) 0.0 else inches
        val totalInches = (safeFeet * 12.0) + safeInches
        val totalFeet = totalInches / 12.0
        val wholeFt = floor(totalFeet).toLong()
        val remInches = totalInches - (wholeFt * 12.0)

        val fractionString = formatInchesWithFraction(remInches)
        val compoundLabel = if (wholeFt > 0) {
            if (remInches > 0.001) "$wholeFt ft $fractionString" else "$wholeFt ft"
        } else {
            fractionString
        }

        val metricCm = totalInches * 2.54
        val metricM = metricCm / 100.0

        val decimalInchesStr = String.format(Locale.US, "%.${precision}f", totalInches)
        val formula = "($safeFeet ft × 12) + $safeInches in = $decimalInchesStr in"

        return ConversionResult(
            inches = totalInches,
            feet = totalFeet,
            wholeFeet = wholeFt,
            remainingInches = remInches,
            fractionLabel = compoundLabel,
            formulaExplanation = formula,
            metricCm = metricCm,
            metricMeters = metricM,
            formattedResult = "$decimalInchesStr in"
        )
    }

    /**
     * Converts a decimal inch value to nearest 1/16 fractional string
     * e.g. 8.5 -> 8 ½ in, 8.25 -> 8 ¼ in, 8.125 -> 8 ⅛ in
     */
    fun formatInchesWithFraction(inches: Double): String {
        val wholePart = floor(inches).toLong()
        val fractionalPart = inches - wholePart

        if (fractionalPart < 0.03125) {
            return if (wholePart == 0L && inches <= 0.001) "0 in" else "$wholePart in"
        }

        val sixteenths = (fractionalPart * 16.0).roundToInt()
        return when (sixteenths) {
            0 -> "$wholePart in"
            16 -> "${wholePart + 1} in"
            8 -> if (wholePart > 0) "$wholePart ½ in" else "½ in"
            4 -> if (wholePart > 0) "$wholePart ¼ in" else "¼ in"
            12 -> if (wholePart > 0) "$wholePart ¾ in" else "¾ in"
            2 -> if (wholePart > 0) "$wholePart ⅛ in" else "⅛ in"
            6 -> if (wholePart > 0) "$wholePart ⅜ in" else "⅜ in"
            10 -> if (wholePart > 0) "$wholePart ⅝ in" else "⅝ in"
            14 -> if (wholePart > 0) "$wholePart ⅞ in" else "⅞ in"
            else -> {
                val fraction = "$sixteenths/16"
                if (wholePart > 0) "$wholePart $fraction in" else "$fraction in"
            }
        }
    }
}
