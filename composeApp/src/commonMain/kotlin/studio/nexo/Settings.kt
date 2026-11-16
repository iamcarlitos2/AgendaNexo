package studio.nexo

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import kotlinx.datetime.*
import kotlin.time.Clock

@Composable fun SettingsScreen(state: AgendaState) {
    var passwordOpen by remember{mutableStateOf(false)}
    if(passwordOpen) { ChangePasswordScreen(state){passwordOpen=false}; return }
    var name by remember{mutableStateOf(state.name)}
    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(horizontal=28.dp,vertical=26.dp)) {
        PageHeading("NEXO STUDIO","A tu manera","Tu espacio, tus preferencias.")
        Spacer(Modifier.height(25.dp))
        Column(Modifier.widthIn(max=1050.dp),verticalArrangement=Arrangement.spacedBy(20.dp)) {
            SettingsCard("Tu perfil","Así te ve el equipo.",Symbol.USERS) {
                Row(verticalAlignment=Alignment.CenterVertically,horizontalArrangement=Arrangement.spacedBy(22.dp)) {
                    Avatar(state.name.split(" ").take(2).joinToString(""){it.take(1).uppercase()},size=58.dp)
                    Field("Nombre y apellidos",name,{name=it},Modifier.weight(1f))
                    Column(Modifier.weight(1f)){Txt("Correo de empresa",12,weight=FontWeight.Medium);Spacer(Modifier.height(16.dp));Txt("ana@nexo.test",14,C.muted);Spacer(Modifier.height(16.dp))}
                    Action("Guardar perfil",{
                        if(name.trim().length<2)state.toast="Introduce un nombre de al menos 2 caracteres."else{
                            val oldName=state.name;state.name=name.trim()
                            state.rooms.forEach{room->val i=room.members.indexOf(oldName);if(i>=0)room.members[i]=state.name}
                            state.toast="Tu perfil se ha actualizado."
                        }
                    },primary=false)
                }
            }
            SettingsCard("Apariencia","Elige cómo quieres ver tu espacio.",Symbol.SUN) {
                Row(horizontalArrangement=Arrangement.spacedBy(16.dp)) {
                    AppearanceTile(false,!state.dark,{state.dark=false},Modifier.weight(1f))
                    AppearanceTile(true,state.dark,{state.dark=true},Modifier.weight(1f))
                    Column(Modifier.weight(1f).padding(start=14.dp)) {
                        Txt("Color de las citas",12,weight=FontWeight.Medium);Spacer(Modifier.height(14.dp))
                        Row(horizontalArrangement=Arrangement.spacedBy(12.dp)) {accents.forEachIndexed{index,color->Box(Modifier.size(32.dp).clip(CircleShape).background(color).clickable{state.accent=index}.border(if(state.accent==index)3.dp else 0.dp,if(state.accent==index)C.muted else Color.Transparent,CircleShape),contentAlignment=Alignment.Center){if(state.accent==index)Glyph(Symbol.CHECK,color=Color.White,size=16.dp)}}}
                        Spacer(Modifier.height(12.dp));Caption(listOf("Violeta","Verde","Coral")[state.accent])
                    }
                }
            }
            SettingsCard("Alertas y recordatorios","Un pequeño aviso para llegar a tiempo.",Symbol.BELL) {
                SettingToggle("Activar avisos","Recibe avisos de las citas que tengan recordatorio.",state.alerts,{state.alerts=it})
                Rule();Spacer(Modifier.height(15.dp));Txt("Antelación del aviso",12,weight=FontWeight.Medium);Spacer(Modifier.height(8.dp))
                Row(horizontalArrangement=Arrangement.spacedBy(9.dp)){listOf(5,10,15,30).forEach{minutes->FilterChip(selected=state.reminderMinutes==minutes,onClick={state.reminderMinutes=minutes},label={Txt("$minutes minutos",12)},enabled=state.alerts)}}
                Spacer(Modifier.height(10.dp));Rule()
                SettingToggle("Ventana emergente","Muestra una ventana con los detalles de la cita.",state.modalAlerts,{state.modalAlerts=it},state.alerts)
                Rule();SettingToggle("Sonido del aviso","Una señal breve cuando se acerque una cita.",state.sound,{state.sound=it},state.alerts)
                Row(Modifier.fillMaxWidth(),verticalAlignment=Alignment.CenterVertically){Caption("Los avisos funcionan mientras la aplicación está abierta.",Modifier.weight(1f));Action("Probar aviso",{
                    val now=Clock.System.now().plus(kotlin.time.Duration.parse("${state.reminderMinutes}m")).toLocalDateTime(TimeZone.currentSystemDefault())
                    val sample=Appointment(-1,"Revisión de prototipos",now.date,now.hour*60+now.minute,now.hour*60+now.minute+30,Category.PROJECT,"Sala Nexo")
                    if(state.sound)playAlertSound();if(state.modalAlerts)state.activeAlert=sample else state.toast="En ${state.reminderMinutes} min: ${sample.title}"
                },symbol=Symbol.BELL,primary=false,enabled=state.alerts)}
            }
            Row(horizontalArrangement=Arrangement.spacedBy(12.dp)) {
                Action("Guardar preferencias",{state.toast="Preferencias guardadas para esta sesión."},symbol=Symbol.CHECK)
                Action("Cambiar contraseña →",{passwordOpen=true},primary=false)
            }
            Row(Modifier.fillMaxWidth().padding(bottom=8.dp),verticalAlignment=Alignment.CenterVertically){Glyph(Symbol.INFO,size=15.dp);Spacer(Modifier.width(8.dp));Caption("Tus cambios se aplican al momento y se conservan durante esta sesión.")}
        }
    }
}

@Composable fun ChangePasswordScreen(state: AgendaState,onBack: ()->Unit) {
    var current by remember{mutableStateOf("")}
    var password by remember{mutableStateOf("")}
    var repeat by remember{mutableStateOf("")}
    var error by remember{mutableStateOf<String?>(null)}
    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(28.dp)) {
        PageHeading("NEXO STUDIO","Tu cuenta, segura","Actualiza tu contraseña cuando lo necesites.")
        Spacer(Modifier.height(22.dp))
        Action("Volver a ajustes",onBack,primary=false,symbol=Symbol.LEFT)
        Spacer(Modifier.height(22.dp))
        Card(Modifier.widthIn(max=620.dp).fillMaxWidth()) {
            Column(Modifier.padding(24.dp),verticalArrangement=Arrangement.spacedBy(22.dp)) {
                Field("Contraseña actual",current,{current=it},visualTransformation=PasswordVisualTransformation())
                Field("Nueva contraseña",password,{password=it},visualTransformation=PasswordVisualTransformation())
                Field("Repite la nueva contraseña",repeat,{repeat=it},visualTransformation=PasswordVisualTransformation())
                Column(Modifier.fillMaxWidth().background(C.accentSoft,RoundedCornerShape(12.dp)).padding(16.dp)) {
                    Txt("8 o más caracteres · 1 número · 1 símbolo",12,C.accent)
                    Spacer(Modifier.height(6.dp));Caption("Ejemplo válido para la demo: Demo2026!")
                }
                error?.let{Txt(it,12,MaterialTheme.colorScheme.error)}
                Action("Cambiar contraseña",{
                    error=when{current!=state.password->"La contraseña actual no es correcta.";!validPassword(password)->"La contraseña necesita 8 caracteres, un número y un símbolo, sin espacios.";password!=repeat->"Las contraseñas nuevas no coinciden.";password==current->"Elige una contraseña distinta a la actual.";else->null}
                    if(error==null){state.password=password;state.toast="Contraseña actualizada para esta sesión.";onBack()}
                },Modifier.fillMaxWidth(),symbol=Symbol.LOCK)
            }
        }
    }
}

@Composable fun SettingsCard(title: String,subtitle: String,symbol: Symbol,content: @Composable ColumnScope.() -> Unit) {
    Card(Modifier.fillMaxWidth()) {
        Row(Modifier.padding(horizontal=23.dp,vertical=18.dp),horizontalArrangement=Arrangement.spacedBy(13.dp),verticalAlignment=Alignment.CenterVertically){Box(Modifier.size(36.dp).background(C.soft,RoundedCornerShape(9.dp)),contentAlignment=Alignment.Center){Glyph(symbol,color=C.accent,size=18.dp)};Column{Txt(title,15,weight=FontWeight.Medium);Spacer(Modifier.height(3.dp));Caption(subtitle)}}
        Rule();Column(Modifier.padding(horizontal=23.dp,vertical=20.dp),content=content)
    }
}
@Composable fun AppearanceTile(dark: Boolean, selected: Boolean, onSelect: () -> Unit, modifier: Modifier) {
    val bg=if(dark)Color(0xFF1D2A24)else Color(0xFFF6F8F2)
    Column(modifier.clip(RoundedCornerShape(10.dp)).border(if(selected)1.5.dp else 1.dp,if(selected)C.accent else C.line,RoundedCornerShape(10.dp)).clickable(onClick=onSelect)) {
        Row(Modifier.fillMaxWidth().height(60.dp).padding(8.dp).background(bg,RoundedCornerShape(5.dp))) {
            Column(Modifier.width(25.dp).fillMaxHeight().padding(5.dp),verticalArrangement=Arrangement.spacedBy(4.dp)){repeat(3){Box(Modifier.fillMaxWidth().height(3.dp).background(if(dark)Color(0xFF5C705F)else Color(0xFFCDD7C6),RoundedCornerShape(2.dp)))}}
            Box(Modifier.weight(1f).fillMaxHeight().padding(5.dp).background(if(dark)Color(0xFF304335)else Color.White,RoundedCornerShape(4.dp)))
        }
        Row(Modifier.fillMaxWidth().padding(start=12.dp,end=8.dp,bottom=7.dp),verticalAlignment=Alignment.CenterVertically){Glyph(if(dark)Symbol.MOON else Symbol.SUN,size=14.dp);Spacer(Modifier.width(7.dp));Txt(if(dark)"Modo noche"else"Modo día",12);Spacer(Modifier.weight(1f));RadioButton(selected,onClick=onSelect,modifier=Modifier.size(25.dp))}
    }
}
@Composable fun SettingToggle(title: String,subtitle: String,checked: Boolean,onChange: (Boolean) -> Unit,enabled: Boolean=true) {
    Row(Modifier.fillMaxWidth().padding(vertical=10.dp),verticalAlignment=Alignment.CenterVertically){Column(Modifier.weight(1f)){Txt(title,13,weight=FontWeight.Medium);Spacer(Modifier.height(3.dp));Caption(subtitle)};Switch(checked,onChange,enabled=enabled)}
}
