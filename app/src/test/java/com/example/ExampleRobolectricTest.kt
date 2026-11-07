package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.data.model.AppUsageInfo
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class ExampleRobolectricTest {

  @Test
  fun `read string from context`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val appName = context.getString(R.string.app_name)
    assertEquals("Force Stop", appName)
  }

  @Test
  fun `verify app usage info calculations`() {
    val app = AppUsageInfo(
        packageName = "com.google.android.youtube",
        appName = "YouTube",
        isRestricted = true,
        isEntertainment = true,
        dailyLimitMinutes = 30,
        usedTodayMinutes = 35,
        extraTimeMinutesGrantedToday = 0
    )
    assertTrue(app.isLimitReached)
    assertEquals(0, app.remainingMinutes)
  }
}
