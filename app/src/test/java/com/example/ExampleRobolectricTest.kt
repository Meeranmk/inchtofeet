package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.model.MeasurementCalculator
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

  @Test
  fun `read string from context`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val appName = context.getString(R.string.app_name)
    assertEquals("Inch to Feet", appName)
  }

  @Test
  fun `convert 12 inches to feet`() {
    val result = MeasurementCalculator.convertInchesToFeet(12.0, 2)
    assertEquals(1.0, result.feet, 0.001)
    assertEquals(1L, result.wholeFeet)
    assertEquals(0.0, result.remainingInches, 0.001)
    assertEquals("1 ft", result.fractionLabel)
  }

  @Test
  fun `convert 68 inches to compound feet and inches`() {
    val result = MeasurementCalculator.convertInchesToFeet(68.0, 3)
    assertEquals(5.667, result.feet, 0.001)
    assertEquals(5L, result.wholeFeet)
    assertEquals(8.0, result.remainingInches, 0.001)
    assertEquals("5 ft 8 in", result.fractionLabel)
  }

  @Test
  fun `convert 5 feet 8 inches to total inches`() {
    val result = MeasurementCalculator.convertCompoundToInches(5.0, 8.0, 2)
    assertEquals(68.0, result.inches, 0.001)
    assertEquals("68.00 in", result.formattedResult)
  }
}

