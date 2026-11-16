@file:OptIn(androidx.compose.ui.test.ExperimentalTestApi::class)

package studio.nexo

import androidx.compose.ui.graphics.asSkiaBitmap
import androidx.compose.ui.test.*
import java.io.File
import kotlin.test.Test
import org.jetbrains.skia.Image

class ScreenTest {
    @Test fun navigateAndRenderTheMainScreens() = runDesktopComposeUiTest(width=1440,height=960) {
        val state=AgendaState()
        setContent { App(state) }
        fun screenshot(name: String, node: SemanticsNodeInteraction = onRoot()) {
            waitForIdle()
            val bitmap=node.captureToImage().asSkiaBitmap()
            val target=File(System.getProperty("nexo.captureDir"),"$name.png")
            target.parentFile.mkdirs()
            Image.makeFromBitmap(bitmap).encodeToData()!!.use {target.writeBytes(it.bytes)}
        }
        onNodeWithText("Entrar a mi agenda").assertExists()
        screenshot("01-login")
        onNodeWithText("Entrar a mi agenda").performClick()
        onAllNodesWithText("Nueva cita").onFirst().assertExists()
        screenshot("02-agenda-mes")
        onNodeWithText("Nueva cita").performClick()
        onAllNodesWithText("Nueva cita").onFirst().assertExists()
        screenshot("05-nueva-cita",onAllNodes(isRoot()).onLast())
        onNodeWithText("Guardar cita").performClick()
        onNodeWithText("Ponle un título a tu cita.").assertExists()
        onNodeWithContentDescription("Título de la cita").performTextInput("Cita de comprobación")
        onNodeWithText("Guardar cita").performClick()
        runOnIdle {kotlin.test.assertTrue(state.appointments.any{it.title=="Cita de comprobación"});state.toast=null}
        onNodeWithText("Semana",useUnmergedTree=true).performClick()
        screenshot("03-agenda-semana")
        onNodeWithText("Día",useUnmergedTree=true).performClick()
        screenshot("04-agenda-dia")
        runOnIdle {state.page=Page.CHAT}
        onNodeWithText("Aceptar invitación").assertExists().performClick()
        onNodeWithText("# Lanzamiento de noviembre").assertExists()
        runOnIdle {state.selectedRoom=1;state.toast=null}
        screenshot("06-chat")
        runOnIdle {state.page=Page.SETTINGS}
        onNodeWithText("Tu perfil").assertExists()
        screenshot("07-configuracion")
        runOnIdle {state.activeAlert=state.appointments.first()}
        onNodeWithText("Tu próxima cita").assertExists()
        screenshot("08-alerta",onAllNodes(isRoot()).onLast())
        onNodeWithText("Entendido").performClick()
        runOnIdle {state.page=Page.AGENDA;state.view=CalendarView.MONTH;state.dark=true}
        screenshot("09-modo-noche")
    }

    @Test fun compactDesktopRetainsCalendarControls() = runDesktopComposeUiTest(width=960,height=720) {
        val state=AgendaState().apply{loggedIn=true}
        setContent{App(state)}
        onNodeWithText("Nueva cita").assertExists()
        onNodeWithText("Semana",useUnmergedTree=true).performClick()
        onNodeWithText("Hoy").assertExists()
        onNodeWithContentDescription("Cambiar tema").performClick()
        kotlin.test.assertTrue(state.dark)
    }
}
