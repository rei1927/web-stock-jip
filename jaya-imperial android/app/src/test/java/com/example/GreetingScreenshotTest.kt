package com.example

import android.content.Context
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onRoot
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.example.data.PropertyDatabase
import com.example.data.PropertyRepository
import com.example.ui.LoginScreen
import com.example.ui.PropertyViewModel
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

    @get:Rule 
    val composeTestRule = createComposeRule()

    @Test
    fun greeting_screenshot() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        
        // Setup in-memory SQLite db for clean, side-effect free screenshot tests
        val db = Room.inMemoryDatabaseBuilder(context, PropertyDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        val repository = PropertyRepository(db.propertyDao())
        val viewModel = PropertyViewModel(repository)

        composeTestRule.setContent { 
            MyApplicationTheme { 
                LoginScreen(
                    viewModel = viewModel,
                    onLoginSuccess = {}
                ) 
            } 
        }

        composeTestRule.onRoot().captureRoboImage(filePath = "src/test/screenshots/greeting.png")
        
        db.close()
    }
}
