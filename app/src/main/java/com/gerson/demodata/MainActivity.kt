package com.gerson.demodata


import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.lifecycle.viewmodel.compose.viewModel
import com.gerson.demodata.ui.navigation.Navigation
import com.gerson.demodata.ui.theme.DemoDataTheme
import com.gerson.demodata.ui.viewmodel.SessionViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            val app = applicationContext as DemoDataApp
            val sessionVm: SessionViewModel = viewModel(
                factory = SessionViewModel.Factory(app.sessionManager)
            )

            val isDarkModePref by sessionVm.isDarkMode.collectAsState()
            val darkTheme      = isDarkModePref ?: isSystemInDarkTheme()

            DemoDataTheme(darkTheme = darkTheme) {
                Navigation()
            }
        }
    }
}