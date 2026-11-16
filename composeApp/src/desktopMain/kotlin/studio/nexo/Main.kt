package studio.nexo

import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import androidx.compose.ui.window.rememberWindowState
import java.awt.Dimension
import java.awt.Toolkit

fun main() = application {
    Window(onCloseRequest = ::exitApplication, title = "Nexo · Agenda de equipo",
        state = rememberWindowState(width = 1440.dp, height = 920.dp)) {
        window.minimumSize = Dimension(900, 680)
        App()
    }
}

actual fun playAlertSound() { Toolkit.getDefaultToolkit().beep() }
