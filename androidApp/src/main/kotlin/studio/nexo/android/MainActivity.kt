package studio.nexo.android

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.background
import androidx.compose.runtime.remember
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.Modifier
import studio.nexo.App
import studio.nexo.AgendaState

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val state=remember {AgendaState()}
            SideEffect {
                if(android.os.Build.VERSION.SDK_INT>=30) {
                    val flags=android.view.WindowInsetsController.APPEARANCE_LIGHT_STATUS_BARS or android.view.WindowInsetsController.APPEARANCE_LIGHT_NAVIGATION_BARS
                    window.insetsController?.setSystemBarsAppearance(if(state.dark)0 else flags,flags)
                }
            }
            Box(Modifier.fillMaxSize().background(if(state.dark)Color(0xFF101522)else Color(0xFFF6F7FB)).safeDrawingPadding().imePadding()) {
                App(state)
            }
        }
    }
}
