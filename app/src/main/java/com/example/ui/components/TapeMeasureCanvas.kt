package com.example.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Straighten
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.TapeMeasureYellow
import kotlin.math.floor
import kotlin.math.max
import kotlin.math.roundToInt

@Composable
fun TapeMeasureCanvas(
    currentInches: Double,
    onInchesChange: (Double) -> Unit,
    modifier: Modifier = Modifier
) {
    val textMeasurer = rememberTextMeasurer()
    val isDark = MaterialTheme.colorScheme.background.red < 0.2f

    val tapeBackground = if (isDark) Color(0xFF1E293B) else TapeMeasureYellow
    val tickColor = if (isDark) Color(0xFFF1F5F9) else Color(0xFF0F172A)
    val footBadgeBg = if (isDark) Color(0xFFF59E0B) else Color(0xFF0F172A)
    val footBadgeText = if (isDark) Color(0xFF0F172A) else Color(0xFFFACC15)
    val pointerColor = Color(0xFFEF4444) // Bright red carpenter cursor

    // Animated inches for smooth visual motion
    val animatedInches by animateFloatAsState(
        targetValue = currentInches.toFloat().coerceAtLeast(0f),
        animationSpec = spring(stiffness = 500f),
        label = "inches_tape_anim"
    )

    // Pixels per inch scale
    val pixelsPerInch = 36f

    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .padding(12.dp)
            .testTag("tape_measure_container")
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.Straighten,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = "Interactive Tape Measure",
                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.weight(1f))
            Text(
                text = "Drag or tap ruler to adjust",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.outline
            )
        }

        Spacer(modifier = Modifier.height(10.dp))

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(86.dp)
                .clip(RoundedCornerShape(12.dp))
                .shadow(2.dp, RoundedCornerShape(12.dp))
                .background(tapeBackground)
        ) {
            Canvas(
                modifier = Modifier
                    .matchParentSize()
                    .testTag("tape_measure_canvas")
                    .pointerInput(Unit) {
                        detectTapGestures { offset ->
                            val centerX = size.width / 2f
                            val deltaPx = offset.x - centerX
                            val deltaInches = deltaPx / pixelsPerInch
                            val newInches = max(0.0, ((currentInches + deltaInches) * 2).roundToInt() / 2.0)
                            onInchesChange(newInches)
                        }
                    }
                    .pointerInput(Unit) {
                        detectDragGestures { change, dragAmount ->
                            change.consume()
                            val deltaInches = -dragAmount.x / pixelsPerInch
                            val newInches = max(0.0, ((currentInches + deltaInches) * 4).roundToInt() / 4.0)
                            onInchesChange(newInches)
                        }
                    }
            ) {
                val canvasWidth = size.width
                val canvasHeight = size.height
                val centerX = canvasWidth / 2f

                // Range of visible inches
                val visibleHalfInches = (canvasWidth / 2f) / pixelsPerInch
                val startInch = max(0, floor(animatedInches - visibleHalfInches - 1).toInt())
                val endInch = (animatedInches + visibleHalfInches + 2).toInt()

                // Subtle edge shadows on the tape measure
                drawRect(
                    brush = Brush.verticalGradient(
                        colors = listOf(Color(0x20000000), Color.Transparent, Color(0x30000000))
                    ),
                    size = size
                )

                // Top guide line
                drawLine(
                    color = tickColor.copy(alpha = 0.3f),
                    start = Offset(0f, 0f),
                    end = Offset(canvasWidth, 0f),
                    strokeWidth = 2f
                )

                // Draw tick marks for each inch and subdivisions (1/4, 1/2)
                for (inch in startInch..endInch) {
                    val inchX = centerX + (inch - animatedInches) * pixelsPerInch

                    // Major 1-inch tick
                    if (inchX in -50f..(canvasWidth + 50f)) {
                        val isFoot = (inch > 0 && inch % 12 == 0)
                        val tickHeight = if (isFoot) 44f else 32f

                        drawLine(
                            color = tickColor,
                            start = Offset(inchX, 0f),
                            end = Offset(inchX, tickHeight),
                            strokeWidth = if (isFoot) 3f else 2f
                        )

                        // If it's a foot marker (12", 24", 36", ...)
                        if (isFoot) {
                            val footNum = inch / 12
                            val footText = "${footNum}′"

                            // Draw badge background
                            val badgeWidth = 36f
                            val badgeHeight = 24f
                            val badgeX = inchX - badgeWidth / 2f
                            val badgeY = 48f

                            drawRoundRect(
                                color = footBadgeBg,
                                topLeft = Offset(badgeX, badgeY),
                                size = androidx.compose.ui.geometry.Size(badgeWidth, badgeHeight),
                                cornerRadius = androidx.compose.ui.geometry.CornerRadius(6f, 6f)
                            )

                            val measuredFoot = textMeasurer.measure(
                                text = footText,
                                style = TextStyle(
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = footBadgeText,
                                    fontFamily = FontFamily.Monospace
                                )
                            )
                            drawText(
                                textLayoutResult = measuredFoot,
                                topLeft = Offset(
                                    badgeX + (badgeWidth - measuredFoot.size.width) / 2f,
                                    badgeY + (badgeHeight - measuredFoot.size.height) / 2f
                                )
                            )
                        } else if (inch >= 0) {
                            // Inch number label
                            val inchText = "$inch"
                            val measuredInch = textMeasurer.measure(
                                text = inchText,
                                style = TextStyle(
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = tickColor.copy(alpha = 0.85f),
                                    fontFamily = FontFamily.Monospace
                                )
                            )
                            drawText(
                                textLayoutResult = measuredInch,
                                topLeft = Offset(
                                    inchX - (measuredInch.size.width / 2f),
                                    36f
                                )
                            )
                        }

                        // Half-inch tick
                        val halfX = inchX + (pixelsPerInch / 2f)
                        if (halfX in 0f..canvasWidth) {
                            drawLine(
                                color = tickColor.copy(alpha = 0.7f),
                                start = Offset(halfX, 0f),
                                end = Offset(halfX, 22f),
                                strokeWidth = 1.5f
                            )
                        }

                        // Quarter-inch ticks
                        val quarter1X = inchX + (pixelsPerInch / 4f)
                        if (quarter1X in 0f..canvasWidth) {
                            drawLine(
                                color = tickColor.copy(alpha = 0.5f),
                                start = Offset(quarter1X, 0f),
                                end = Offset(quarter1X, 14f),
                                strokeWidth = 1f
                            )
                        }
                        val quarter3X = inchX + (3 * pixelsPerInch / 4f)
                        if (quarter3X in 0f..canvasWidth) {
                            drawLine(
                                color = tickColor.copy(alpha = 0.5f),
                                start = Offset(quarter3X, 0f),
                                end = Offset(quarter3X, 14f),
                                strokeWidth = 1f
                            )
                        }
                    }
                }

                // Center cursor / needle indicating current reading
                drawLine(
                    color = pointerColor,
                    start = Offset(centerX, 0f),
                    end = Offset(centerX, canvasHeight),
                    strokeWidth = 3f
                )

                // Cursor pointer triangle top
                val topTriangle = Path().apply {
                    moveTo(centerX - 8f, 0f)
                    lineTo(centerX + 8f, 0f)
                    lineTo(centerX, 12f)
                    close()
                }
                drawPath(topTriangle, color = pointerColor)

                // Cursor pointer triangle bottom
                val bottomTriangle = Path().apply {
                    moveTo(centerX - 8f, canvasHeight)
                    lineTo(centerX + 8f, canvasHeight)
                    lineTo(centerX, canvasHeight - 12f)
                    close()
                }
                drawPath(bottomTriangle, color = pointerColor)
            }
        }
    }
}
