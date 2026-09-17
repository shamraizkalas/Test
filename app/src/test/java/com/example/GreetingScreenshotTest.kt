package com.example

import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onRoot
import com.example.data.FamilyMember
import com.example.data.Gender
import com.example.ui.components.MemberCard
import com.example.ui.theme.MyApplicationTheme
import com.github.takahirom.roborazzi.RobolectricDeviceQualifiers
import com.github.takahirom.roborazzi.captureRoboImage
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(qualifiers = RobolectricDeviceQualifiers.Pixel8, sdk = [36])
class GreetingScreenshotTest {

  @get:Rule val composeTestRule = createComposeRule()

  @Test
  fun member_card_screenshot() {
    val sampleMember = FamilyMember(
      id = 1,
      name = "محمد علی",
      fatherId = null,
      fatherName = null,
      gender = Gender.MALE,
      generation = 1,
      spouse = null,
      location = null,
      isDeceased = true,
      deathNote = "بانیٔ شجرہ",
      notes = null
    )
    composeTestRule.setContent {
      MyApplicationTheme {
        MemberCard(
          member = sampleMember,
          childrenCount = 4,
          isAdmin = false,
          onClick = {}
        )
      }
    }

    composeTestRule.waitForIdle()
    composeTestRule.onRoot().captureRoboImage(filePath = "src/test/screenshots/member_card.png")
  }
}

