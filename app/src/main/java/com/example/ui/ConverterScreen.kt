package com.example.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.RestartAlt
import androidx.compose.material.icons.filled.Straighten
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material.icons.filled.SwapVert
import androidx.compose.material.icons.outlined.HelpOutline
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.model.ConversionDirection
import com.example.ui.components.HistorySection
import com.example.ui.components.PresetsAndFractions
import com.example.ui.components.ResultCard
import com.example.ui.components.TapeMeasureCanvas
import com.example.viewmodel.ConverterViewModel
import kotlinx.coroutines.launch
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ConverterScreen(
    viewModel: ConverterViewModel,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()
    val scrollState = rememberScrollState()
    val focusManager = LocalFocusManager.current
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    LaunchedEffect(uiState.copyFeedbackTrigger) {
        if (uiState.copyFeedbackTrigger > 0L) {
            scope.launch {
                snackbarHostState.showSnackbar("Copied result to clipboard!")
            }
        }
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        contentWindowInsets = WindowInsets.safeDrawing,
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Box(
                            modifier = Modifier
                                .size(34.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(MaterialTheme.colorScheme.primaryContainer),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Straighten,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onPrimaryContainer,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = stringResource(R.string.title_converter),
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 0.2.sp
                            )
                        )
                    }
                },
                actions = {
                    IconButton(
                        onClick = { viewModel.clearInput() },
                        modifier = Modifier.testTag("app_bar_reset_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.RestartAlt,
                            contentDescription = "Reset input",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(MaterialTheme.colorScheme.background),
            contentAlignment = Alignment.TopCenter
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .widthIn(max = 680.dp)
                    .verticalScroll(scrollState)
                    .imePadding()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Direction Toggle Tabs
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Inches to Feet Tab
                        val inToFtSelected = uiState.direction == ConversionDirection.INCHES_TO_FEET
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = if (inToFtSelected) MaterialTheme.colorScheme.surface else MaterialTheme.colorScheme.surfaceVariant,
                            shadowElevation = if (inToFtSelected) 2.dp else 0.dp,
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(12.dp))
                                .testTag("tab_in_to_ft")
                        ) {
                            IconButton(
                                onClick = { viewModel.setDirection(ConversionDirection.INCHES_TO_FEET) },
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(
                                    text = "Inches ➔ Feet",
                                    style = MaterialTheme.typography.labelLarge.copy(
                                        fontWeight = if (inToFtSelected) FontWeight.Bold else FontWeight.Normal
                                    ),
                                    color = if (inToFtSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        // Swap Button in Center
                        FilledIconButton(
                            onClick = { viewModel.toggleDirection() },
                            colors = IconButtonDefaults.filledIconButtonColors(
                                containerColor = MaterialTheme.colorScheme.primary,
                                contentColor = MaterialTheme.colorScheme.onPrimary
                            ),
                            modifier = Modifier
                                .size(40.dp)
                                .testTag("swap_direction_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.SwapHoriz,
                                contentDescription = stringResource(R.string.btn_swap),
                                modifier = Modifier.size(22.dp)
                            )
                        }

                        // Feet to Inches Tab
                        val ftToInSelected = uiState.direction == ConversionDirection.FEET_TO_INCHES
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = if (ftToInSelected) MaterialTheme.colorScheme.surface else MaterialTheme.colorScheme.surfaceVariant,
                            shadowElevation = if (ftToInSelected) 2.dp else 0.dp,
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(12.dp))
                                .testTag("tab_ft_to_in")
                        ) {
                            IconButton(
                                onClick = { viewModel.setDirection(ConversionDirection.FEET_TO_INCHES) },
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(
                                    text = "Feet ➔ Inches",
                                    style = MaterialTheme.typography.labelLarge.copy(
                                        fontWeight = if (ftToInSelected) FontWeight.Bold else FontWeight.Normal
                                    ),
                                    color = if (ftToInSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }

                // Input Card
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("input_section_card"),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        if (uiState.direction == ConversionDirection.INCHES_TO_FEET) {
                            // Single Inches input field
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "ENTER INCHES",
                                    style = MaterialTheme.typography.labelMedium.copy(
                                        fontWeight = FontWeight.Bold,
                                        letterSpacing = 1.sp
                                    ),
                                    color = MaterialTheme.colorScheme.primary
                                )
                                Text(
                                    text = "1 ft = 12 in",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.outline
                                )
                            }

                            OutlinedTextField(
                                value = uiState.inchInput,
                                onValueChange = { viewModel.onInchInputChanged(it) },
                                label = { Text(stringResource(R.string.input_inches_label)) },
                                placeholder = { Text("e.g. 68 or 72.5") },
                                trailingIcon = {
                                    if (uiState.inchInput.isNotEmpty()) {
                                        IconButton(
                                            onClick = { viewModel.clearInput() },
                                            modifier = Modifier.testTag("clear_inch_input_button")
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Clear,
                                                contentDescription = stringResource(R.string.btn_clear)
                                            )
                                        }
                                    }
                                },
                                prefix = {
                                    Text(
                                        text = "″ ",
                                        style = MaterialTheme.typography.bodyLarge.copy(
                                            fontWeight = FontWeight.Bold,
                                            fontFamily = FontFamily.Monospace
                                        ),
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                },
                                singleLine = true,
                                keyboardOptions = KeyboardOptions(
                                    keyboardType = KeyboardType.Decimal,
                                    imeAction = ImeAction.Done
                                ),
                                keyboardActions = KeyboardActions(
                                    onDone = { focusManager.clearFocus() }
                                ),
                                shape = RoundedCornerShape(14.dp),
                                textStyle = MaterialTheme.typography.headlineSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    fontFamily = FontFamily.Monospace
                                ),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                                    unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)
                                ),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("inch_input_field")
                            )
                        } else {
                            // FEET_TO_INCHES mode: Choice between Pure Decimal Feet or Compound Feet + Inches
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "ENTER FEET",
                                    style = MaterialTheme.typography.labelMedium.copy(
                                        fontWeight = FontWeight.Bold,
                                        letterSpacing = 1.sp
                                    ),
                                    color = MaterialTheme.colorScheme.primary
                                )

                                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                    FilterChip(
                                        selected = !uiState.isCompoundFeetInput,
                                        onClick = { viewModel.setCompoundFeetMode(false) },
                                        label = { Text("Decimal Ft", style = MaterialTheme.typography.labelSmall) },
                                        shape = RoundedCornerShape(8.dp),
                                        modifier = Modifier.testTag("chip_decimal_feet_mode")
                                    )
                                    FilterChip(
                                        selected = uiState.isCompoundFeetInput,
                                        onClick = { viewModel.setCompoundFeetMode(true) },
                                        label = { Text("Ft + In", style = MaterialTheme.typography.labelSmall) },
                                        shape = RoundedCornerShape(8.dp),
                                        modifier = Modifier.testTag("chip_compound_feet_mode")
                                    )
                                }
                            }

                            if (!uiState.isCompoundFeetInput) {
                                OutlinedTextField(
                                    value = uiState.feetInput,
                                    onValueChange = { viewModel.onFeetInputChanged(it) },
                                    label = { Text(stringResource(R.string.input_feet_label)) },
                                    placeholder = { Text("e.g. 5.67 or 6.25") },
                                    prefix = {
                                        Text(
                                            text = "′ ",
                                            style = MaterialTheme.typography.bodyLarge.copy(
                                                fontWeight = FontWeight.Bold,
                                                fontFamily = FontFamily.Monospace
                                            ),
                                            color = MaterialTheme.colorScheme.primary
                                        )
                                    },
                                    trailingIcon = {
                                        if (uiState.feetInput.isNotEmpty()) {
                                            IconButton(onClick = { viewModel.clearInput() }) {
                                                Icon(
                                                    imageVector = Icons.Default.Clear,
                                                    contentDescription = stringResource(R.string.btn_clear)
                                                )
                                            }
                                        }
                                    },
                                    singleLine = true,
                                    keyboardOptions = KeyboardOptions(
                                        keyboardType = KeyboardType.Decimal,
                                        imeAction = ImeAction.Done
                                    ),
                                    keyboardActions = KeyboardActions(
                                        onDone = { focusManager.clearFocus() }
                                    ),
                                    shape = RoundedCornerShape(14.dp),
                                    textStyle = MaterialTheme.typography.headlineSmall.copy(
                                        fontWeight = FontWeight.Bold,
                                        fontFamily = FontFamily.Monospace
                                    ),
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                                        unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)
                                    ),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .testTag("feet_input_field")
                                )
                            } else {
                                // Side-by-side compound inputs: Feet + Inches
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                                ) {
                                    OutlinedTextField(
                                        value = uiState.feetInput,
                                        onValueChange = { viewModel.onFeetInputChanged(it) },
                                        label = { Text("Feet (ft)") },
                                        placeholder = { Text("5") },
                                        prefix = { Text("ft ") },
                                        singleLine = true,
                                        keyboardOptions = KeyboardOptions(
                                            keyboardType = KeyboardType.Number,
                                            imeAction = ImeAction.Next
                                        ),
                                        shape = RoundedCornerShape(14.dp),
                                        textStyle = MaterialTheme.typography.titleLarge.copy(
                                            fontWeight = FontWeight.Bold,
                                            fontFamily = FontFamily.Monospace
                                        ),
                                        modifier = Modifier
                                            .weight(1f)
                                            .testTag("compound_feet_field")
                                    )

                                    OutlinedTextField(
                                        value = uiState.compoundInchesInput,
                                        onValueChange = { viewModel.onCompoundInchesChanged(it) },
                                        label = { Text("Inches (in)") },
                                        placeholder = { Text("8.5") },
                                        prefix = { Text("in ") },
                                        singleLine = true,
                                        keyboardOptions = KeyboardOptions(
                                            keyboardType = KeyboardType.Decimal,
                                            imeAction = ImeAction.Done
                                        ),
                                        keyboardActions = KeyboardActions(
                                            onDone = { focusManager.clearFocus() }
                                        ),
                                        shape = RoundedCornerShape(14.dp),
                                        textStyle = MaterialTheme.typography.titleLarge.copy(
                                            fontWeight = FontWeight.Bold,
                                            fontFamily = FontFamily.Monospace
                                        ),
                                        modifier = Modifier
                                            .weight(1f)
                                            .testTag("compound_inches_field")
                                    )
                                }
                            }
                        }
                    }
                }

                // Main Result Display Card
                ResultCard(
                    result = uiState.currentResult,
                    direction = uiState.direction,
                    onCopied = { viewModel.triggerCopyFeedback() },
                    modifier = Modifier.testTag("result_card_section")
                )

                // Interactive Canvas Tape Measure
                TapeMeasureCanvas(
                    currentInches = uiState.currentResult.inches,
                    onInchesChange = { newInches ->
                        viewModel.applyPreset(newInches)
                    }
                )

                // Quick Fractions & Common Dimension Presets
                PresetsAndFractions(
                    direction = uiState.direction,
                    precision = uiState.precision,
                    onFractionAdd = { fraction -> viewModel.addFraction(fraction) },
                    onPresetSelect = { inches -> viewModel.applyPreset(inches) },
                    onPrecisionChange = { p -> viewModel.setPrecision(p) }
                )

                // History Section
                HistorySection(
                    history = uiState.history,
                    onSelect = { item -> viewModel.selectHistoryItem(item) },
                    onClear = { viewModel.clearHistory() }
                )

                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }
}
