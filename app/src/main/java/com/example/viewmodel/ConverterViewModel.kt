package com.example.viewmodel

import androidx.lifecycle.ViewModel
import com.example.model.ConversionDirection
import com.example.model.ConversionResult
import com.example.model.MeasurementCalculator
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.Locale

data class ConverterUiState(
    val direction: ConversionDirection = ConversionDirection.INCHES_TO_FEET,
    val isCompoundFeetInput: Boolean = false,
    val inchInput: String = "36",
    val feetInput: String = "3",
    val compoundInchesInput: String = "0",
    val precision: Int = 3,
    val currentResult: ConversionResult = MeasurementCalculator.convertInchesToFeet(36.0, 3),
    val history: List<ConversionResult> = emptyList(),
    val copyFeedbackTrigger: Long = 0L
)

class ConverterViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(ConverterUiState())
    val uiState: StateFlow<ConverterUiState> = _uiState.asStateFlow()

    init {
        recalculate()
    }

    fun setDirection(direction: ConversionDirection) {
        if (_uiState.value.direction == direction) return
        _uiState.value = _uiState.value.copy(direction = direction)
        syncInputForNewDirection()
    }

    fun toggleDirection() {
        val newDir = if (_uiState.value.direction == ConversionDirection.INCHES_TO_FEET) {
            ConversionDirection.FEET_TO_INCHES
        } else {
            ConversionDirection.INCHES_TO_FEET
        }
        setDirection(newDir)
    }

    private fun syncInputForNewDirection() {
        val current = _uiState.value.currentResult
        if (_uiState.value.direction == ConversionDirection.INCHES_TO_FEET) {
            val formattedInches = if (current.inches % 1.0 == 0.0) {
                current.inches.toLong().toString()
            } else {
                String.format(Locale.US, "%.${_uiState.value.precision}f", current.inches)
            }
            _uiState.value = _uiState.value.copy(inchInput = formattedInches)
        } else {
            val formattedFeet = if (current.feet % 1.0 == 0.0) {
                current.feet.toLong().toString()
            } else {
                String.format(Locale.US, "%.${_uiState.value.precision}f", current.feet)
            }
            _uiState.value = _uiState.value.copy(
                feetInput = formattedFeet,
                compoundInchesInput = if (current.remainingInches % 1.0 == 0.0) {
                    current.remainingInches.toLong().toString()
                } else {
                    String.format(Locale.US, "%.2f", current.remainingInches)
                }
            )
        }
        recalculate()
    }

    fun setCompoundFeetMode(enabled: Boolean) {
        _uiState.value = _uiState.value.copy(isCompoundFeetInput = enabled)
        recalculate()
    }

    fun onInchInputChanged(input: String) {
        val filtered = filterDecimalInput(input)
        _uiState.value = _uiState.value.copy(inchInput = filtered)
        recalculate()
    }

    fun onFeetInputChanged(input: String) {
        val filtered = filterDecimalInput(input)
        _uiState.value = _uiState.value.copy(feetInput = filtered)
        recalculate()
    }

    fun onCompoundInchesChanged(input: String) {
        val filtered = filterDecimalInput(input)
        _uiState.value = _uiState.value.copy(compoundInchesInput = filtered)
        recalculate()
    }

    fun addFraction(fraction: Double) {
        val currentVal = _uiState.value.inchInput.toDoubleOrNull() ?: 0.0
        val newVal = currentVal + fraction
        val formatted = if (newVal % 1.0 == 0.0) {
            newVal.toLong().toString()
        } else {
            String.format(Locale.US, "%.4f", newVal).trimEnd('0').trimEnd('.')
        }
        _uiState.value = _uiState.value.copy(inchInput = formatted)
        recalculate()
    }

    fun applyPreset(inches: Double) {
        if (_uiState.value.direction == ConversionDirection.INCHES_TO_FEET) {
            val formatted = if (inches % 1.0 == 0.0) inches.toLong().toString() else inches.toString()
            _uiState.value = _uiState.value.copy(inchInput = formatted)
        } else {
            val feet = inches / 12.0
            val formattedFeet = if (feet % 1.0 == 0.0) feet.toLong().toString() else String.format(Locale.US, "%.3f", feet)
            val wholeFeet = (inches / 12).toLong()
            val remInches = inches - (wholeFeet * 12)
            _uiState.value = _uiState.value.copy(
                feetInput = formattedFeet,
                compoundInchesInput = if (remInches % 1.0 == 0.0) remInches.toLong().toString() else remInches.toString()
            )
        }
        recalculate()
    }

    fun setPrecision(precision: Int) {
        _uiState.value = _uiState.value.copy(precision = precision.coerceIn(1, 6))
        recalculate()
    }

    fun clearInput() {
        if (_uiState.value.direction == ConversionDirection.INCHES_TO_FEET) {
            _uiState.value = _uiState.value.copy(inchInput = "")
        } else {
            _uiState.value = _uiState.value.copy(feetInput = "", compoundInchesInput = "")
        }
        recalculate()
    }

    fun clearHistory() {
        _uiState.value = _uiState.value.copy(history = emptyList())
    }

    fun selectHistoryItem(item: ConversionResult) {
        if (_uiState.value.direction == ConversionDirection.INCHES_TO_FEET) {
            val formatted = if (item.inches % 1.0 == 0.0) item.inches.toLong().toString() else item.inches.toString()
            _uiState.value = _uiState.value.copy(inchInput = formatted)
        } else {
            val formatted = if (item.feet % 1.0 == 0.0) item.feet.toLong().toString() else String.format(Locale.US, "%.3f", item.feet)
            _uiState.value = _uiState.value.copy(
                feetInput = formatted,
                compoundInchesInput = if (item.remainingInches % 1.0 == 0.0) item.remainingInches.toLong().toString() else item.remainingInches.toString()
            )
        }
        recalculate()
    }

    fun triggerCopyFeedback() {
        _uiState.value = _uiState.value.copy(copyFeedbackTrigger = System.currentTimeMillis())
    }

    private fun recalculate() {
        val state = _uiState.value
        val result = when (state.direction) {
            ConversionDirection.INCHES_TO_FEET -> {
                val inches = state.inchInput.toDoubleOrNull() ?: 0.0
                MeasurementCalculator.convertInchesToFeet(inches, state.precision)
            }
            ConversionDirection.FEET_TO_INCHES -> {
                if (state.isCompoundFeetInput) {
                    val ft = state.feetInput.toDoubleOrNull() ?: 0.0
                    val inch = state.compoundInchesInput.toDoubleOrNull() ?: 0.0
                    MeasurementCalculator.convertCompoundToInches(ft, inch, state.precision)
                } else {
                    val ft = state.feetInput.toDoubleOrNull() ?: 0.0
                    MeasurementCalculator.convertFeetToInches(ft, state.precision)
                }
            }
        }

        // Add to history if value > 0 and not identical to last entry
        val updatedHistory = state.history.toMutableList()
        val isMeaningful = (result.inches > 0.001)
        if (isMeaningful) {
            val last = updatedHistory.firstOrNull()
            if (last == null || Math.abs(last.inches - result.inches) > 0.001) {
                updatedHistory.add(0, result)
                if (updatedHistory.size > 8) {
                    updatedHistory.removeAt(updatedHistory.lastIndex)
                }
            }
        }

        _uiState.value = state.copy(
            currentResult = result,
            history = updatedHistory
        )
    }

    private fun filterDecimalInput(input: String): String {
        // Keep digits and at most one decimal point
        val clean = StringBuilder()
        var hasDecimal = false
        for (ch in input) {
            if (ch.isDigit()) {
                clean.append(ch)
            } else if ((ch == '.' || ch == ',') && !hasDecimal) {
                clean.append('.')
                hasDecimal = true
            }
        }
        return clean.toString()
    }
}
