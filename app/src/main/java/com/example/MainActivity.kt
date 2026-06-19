package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ui.BoutiqueAppScreen
import com.example.ui.BoutiqueViewModel
import com.example.ui.theme.MyApplicationTheme

class MainActivity : ComponentActivity() {
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    enableEdgeToEdge()
    setContent {
      val viewModel: BoutiqueViewModel = viewModel()
      val activeLayoutId by viewModel.invoiceLayoutId.collectAsStateWithLifecycle()
      val selectedFontType by viewModel.selectedFontType.collectAsStateWithLifecycle()
      val themeMode by viewModel.themeMode.collectAsStateWithLifecycle()

      val systemInDark = androidx.compose.foundation.isSystemInDarkTheme()
      val resolvedDarkTheme = when (themeMode) {
        "Light" -> false
        "Dark" -> true
        else -> systemInDark
      }

      MyApplicationTheme(
        activeLayoutId = activeLayoutId,
        selectedFontType = selectedFontType,
        darkTheme = resolvedDarkTheme
      ) {
        Surface(
          modifier = Modifier.fillMaxSize()
        ) {
          BoutiqueAppScreen(viewModel = viewModel)
        }
      }
    }
  }
}
