package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ui.FamilyTreeViewModel
import com.example.ui.ShajraMainScreen
import com.example.ui.components.AppSplashScreen
import com.example.ui.theme.EmeraldDark
import com.example.ui.theme.MyApplicationTheme

class MainActivity : ComponentActivity() {
  private var currentViewModel: FamilyTreeViewModel? = null

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    com.example.data.FirestoreSyncManager.getInstance(applicationContext)
    com.example.data.FirebaseManager.initialize(applicationContext)
    enableEdgeToEdge()
    setContent {
      val viewModel: FamilyTreeViewModel = viewModel()
      currentViewModel = viewModel
      val isDarkMode by viewModel.isDarkMode.collectAsState()

      androidx.compose.runtime.LaunchedEffect(intent) {
        processIntent(intent, viewModel)
      }

      MyApplicationTheme(darkTheme = isDarkMode) {
        Surface(
          modifier = Modifier.fillMaxSize(),
          color = if (isDarkMode) MaterialTheme.colorScheme.background else EmeraldDark
        ) {
          var showSplash by remember { mutableStateOf(true) }

          AnimatedContent(
            targetState = showSplash,
            transitionSpec = {
              fadeIn() togetherWith fadeOut()
            },
            label = "SplashTransition"
          ) { isSplash ->
            if (isSplash) {
              AppSplashScreen(
                onDismiss = { showSplash = false }
              )
            } else {
              ShajraMainScreen(viewModel = viewModel)
            }
          }
        }
      }
    }
  }

  override fun onNewIntent(intent: android.content.Intent) {
    super.onNewIntent(intent)
    setIntent(intent)
    currentViewModel?.let { processIntent(intent, it) }
  }

  private fun processIntent(inIntent: android.content.Intent?, viewModel: FamilyTreeViewModel) {
    if (inIntent == null) return
    if (inIntent.getBooleanExtra(com.example.util.AnnouncementNotificationHelper.EXTRA_OPEN_ANNOUNCEMENTS, false)) {
      viewModel.openAnnouncementsDialog()
    }
    if (inIntent.getBooleanExtra(com.example.util.AnnouncementNotificationHelper.EXTRA_OPEN_USER_APPROVALS, false)) {
      val targetUser = inIntent.getStringExtra(com.example.util.AnnouncementNotificationHelper.EXTRA_TARGET_USER_NAME)
      viewModel.openUserManagementDialog(targetUser)
    }
  }
}

