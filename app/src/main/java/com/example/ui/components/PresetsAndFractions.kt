package com.example.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SuggestionChip
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.ConversionDirection

data class PresetSize(val label: String, val inches: Double)

val COMMON_PRESETS = listOf(
    PresetSize("1 ft (12\")", 12.0),
    PresetSize("2 ft (24\")", 24.0),
    PresetSize("3 ft / Yard", 36.0),
    PresetSize("4 ft (48\")", 48.0),
    PresetSize("5 ft (60\")", 60.0),
    PresetSize("6 ft (72\")", 72.0),
    PresetSize("Door (80\")", 80.0),
    PresetSize("8 ft Sheet", 96.0)
)

val FRACTION_CHIPS = listOf(
    Pair("+ ⅛\"", 0.125),
    Pair("+ ¼\"", 0.25),
    Pair("+ ⅜\"", 0.375),
    Pair("+ ½\"", 0.5),
    Pair("+ ⅝\"", 0.625),
    Pair("+ ¾\"", 0.75),
    Pair("+ 1\"", 1.0)
)

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun PresetsAndFractions(
    direction: ConversionDirection,
    precision: Int,
    onFractionAdd: (Double) -> Unit,
    onPresetSelect: (Double) -> Unit,
    onPrecisionChange: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .testTag("presets_and_fractions")
    ) {
        // Only show fractional increment buttons when converting from inches
        if (direction == ConversionDirection.INCHES_TO_FEET) {
            Text(
                text = "Add Inch Fractions",
                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(6.dp))
            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                FRACTION_CHIPS.forEach { (label, value) ->
                    AssistChip(
                        onClick = { onFractionAdd(value) },
                        label = { Text(label, style = MaterialTheme.typography.labelMedium) },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.Add,
                                contentDescription = null,
                                modifier = Modifier.size(14.dp)
                            )
                        },
                        colors = AssistChipDefaults.assistChipColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant,
                            labelColor = MaterialTheme.colorScheme.onSurfaceVariant
                        ),
                        border = null,
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.testTag("fraction_chip_${label.replace(" ", "_")}")
                    )
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
        }

        // Common Presets
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = Icons.Default.BookmarkBorder,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(16.dp)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = "Quick Presets",
                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Spacer(modifier = Modifier.height(6.dp))
        FlowRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            COMMON_PRESETS.forEach { preset ->
                SuggestionChip(
                    onClick = { onPresetSelect(preset.inches) },
                    label = { Text(preset.label, style = MaterialTheme.typography.labelMedium) },
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.testTag("preset_chip_${preset.inches.toInt()}")
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Precision selector row
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = "Decimal Precision:",
                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                listOf(2, 3, 4).forEach { p ->
                    FilterChip(
                        selected = (precision == p),
                        onClick = { onPrecisionChange(p) },
                        label = { Text("$p dec") },
                        shape = RoundedCornerShape(8.dp),
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = MaterialTheme.colorScheme.primary,
                            selectedLabelColor = MaterialTheme.colorScheme.onPrimary
                        ),
                        modifier = Modifier.testTag("precision_chip_$p")
                    )
                }
            }
        }
    }
}
