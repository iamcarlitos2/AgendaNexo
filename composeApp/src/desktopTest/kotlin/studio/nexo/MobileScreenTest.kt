@file:OptIn(androidx.compose.ui.test.ExperimentalTestApi::class)

package studio.nexo

import androidx.compose.ui.graphics.asSkiaBitmap
import androidx.compose.ui.test.*
import java.io.File
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import org.jetbrains.skia.Image

class MobileScreenTest {
    @Test fun phoneAgendaEditorAndChatAreUsable()=runDesktopComposeUiTest(width=390,height=844) {
        val state=AgendaState()
        setContent {App(state)}
        fun screenshot(name: String) {
            waitForIdle()
            val target=File(System.getProperty("nexo.captureDir"),"mobile-$name.png")
            target.parentFile.mkdirs()
            Image.makeFromBitmap(onRoot().captureToImage().asSkiaBitmap()).encodeToData()!!.use {target.writeBytes(it.bytes)}
        }
        screenshot("login")
        onNodeWithText("Entrar a mi agenda").performScrollTo().performClick()
        onNodeWithText("Mi agenda").assertExists()
        screenshot("mes")
        onNodeWithText("Semana").performClick()
        screenshot("semana")
        onNodeWithText("Día").performClick()
        screenshot("dia")
        onNodeWithText("+  Nueva cita").performScrollTo().performClick()
        onNodeWithContentDescription("Título de la cita").performTextInput("Cita móvil")
        screenshot("nueva-cita")
        onNodeWithText("Guardar cita").performScrollTo().performClick()
        runOnIdle {assertTrue(state.appointments.any{it.title=="Cita móvil"});state.toast=null}
        onNodeWithText("Equipo",substring=false).performClick()
        screenshot("salas")
        onNodeWithText("Aceptar invitación").performScrollTo().performClick()
        onNodeWithText("Lanzamiento de noviembre").assertExists()
        onNodeWithContentDescription("Escribe al equipo…").performTextInput("Mensaje de prueba móvil")
        onNodeWithText("Enviar").performClick()
        onNodeWithText("Mensaje de prueba móvil").assertExists()
        screenshot("chat")
    }

    @Test fun phoneSettingsAndPasswordWorkOnSmallScreens()=runDesktopComposeUiTest(width=360,height=640) {
        val state=AgendaState().apply {loggedIn=true;page=Page.SETTINGS}
        setContent {App(state)}
        onAllNodes(isToggleable()).onFirst().performClick()
        runOnIdle {assertTrue(state.dark)}
        onNodeWithText("Cambiar contraseña →").performScrollTo().performClick()
        onNodeWithText("Tu cuenta, segura").assertExists()
        onNodeWithContentDescription("Contraseña actual").performTextInput("Demo2026!")
        onNodeWithContentDescription("Nueva contraseña").performTextInput("Nueva2026!")
        onNodeWithContentDescription("Repite la nueva contraseña").performScrollTo().performTextInput("Nueva2026!")
        onNodeWithText("Cambiar contraseña",substring=false).performScrollTo().performClick()
        runOnIdle {assertEquals("Nueva2026!",state.password)}
        onNodeWithText("A tu manera").assertExists()
    }
}
