package studio.nexo

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import kotlinx.coroutines.delay
import kotlinx.datetime.LocalDate

@Composable fun App(state: AgendaState = remember { AgendaState() }) {
    BoxWithConstraints(Modifier.fillMaxSize()) {
        if(maxWidth < 600.dp) MobileApp(state) else DesktopApp(state)
    }
}

@Composable fun DesktopApp(state: AgendaState) {
    var editor by remember { mutableStateOf(false) }
    var editing by remember { mutableStateOf<Appointment?>(null) }
    var draftDate by remember { mutableStateOf(state.date) }
    var details by remember { mutableStateOf<Appointment?>(null) }
    var notifications by remember { mutableStateOf(false) }
    fun newEvent(date: LocalDate) { editing = null; draftDate = date; editor = true }
    LaunchedEffect(Unit) { while(true) { state.checkAlerts();delay(1000) } }
    LaunchedEffect(state.toast) { if(state.toast != null) { delay(4000);state.toast = null } }
    NexoTheme(state.dark, state.accent) {
        Surface(Modifier.fillMaxSize(), color = C.bg) {
            if(!state.loggedIn) LoginScreen(state) else BoxWithConstraints(Modifier.fillMaxSize()) {
                val compact = maxWidth < 1050.dp
                Row(Modifier.fillMaxSize()) {
                    Sidebar(state, compact)
                    Column(Modifier.weight(1f)) {
                        Topbar(state, compact) { notifications = true }
                        when(state.page) {
                            Page.AGENDA -> AgendaScreen(state, ::newEvent) { details = it }
                            Page.CHAT -> ChatScreen(state)
                            Page.SETTINGS -> SettingsScreen(state)
                        }
                    }
                }
                state.toast?.let { message ->
                    Row(Modifier.align(Alignment.BottomCenter).padding(24.dp).clip(RoundedCornerShape(12.dp)).background(C.ink).padding(horizontal = 18.dp, vertical = 12.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        Glyph(Symbol.CHECK,color = C.surface,size = 16.dp);Txt(message,13,C.surface)
                    }
                }
            }
        }
        if(editor) EventEditor(editing, draftDate, {editor=false}) { state.save(it); editor=false }
        details?.let { item -> EventDetails(item, {details=null}) { details=null;editing=item;editor=true } }
        state.activeAlert?.let { item -> AlertPopup(item, state.reminderMinutes) {state.activeAlert=null} }
        if(notifications) NotificationsDialog(state) {notifications=false}
    }
}

@Composable fun LoginScreen(state: AgendaState) {
    var email by remember { mutableStateOf("ana@nexo.test") }
    var password by remember { mutableStateOf("Demo2026!") }
    var showPassword by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }
    BoxWithConstraints(Modifier.fillMaxSize()) {
        val wide = maxWidth > 950.dp
        Row(Modifier.fillMaxSize()) {
            if(wide) Column(Modifier.weight(1.06f).fillMaxHeight().background(Color(0xFFEDEDFE)).padding(54.dp)) {
                Brand()
                Spacer(Modifier.weight(1f))
                Txt("NEXO STUDIO",11, Color(0xFF626D84),FontWeight.SemiBold)
                Spacer(Modifier.height(18.dp))
                Text("Tu equipo.\nTu tiempo.",color=Color(0xFF19233C),fontFamily=FontFamily.SansSerif,fontWeight=FontWeight.Bold,fontSize=46.sp,lineHeight=52.sp)
                Spacer(Modifier.height(20.dp))
                Txt("La agenda que conecta a todo el equipo.",15,Color(0xFF626D84))
                Spacer(Modifier.height(34.dp))
                Spacer(Modifier.weight(1f))
                Caption("Organiza. Comparte. Sigue adelante.")
            }
            Box(Modifier.weight(1f).fillMaxHeight().background(C.surface).padding(40.dp),contentAlignment=Alignment.Center) {
                Column(Modifier.widthIn(max=380.dp).verticalScroll(rememberScrollState())) {
                    if(!wide) { Brand();Spacer(Modifier.height(42.dp)) }
                    Row(horizontalArrangement=Arrangement.spacedBy(7.dp),verticalAlignment=Alignment.CenterVertically) {Dot(Color(0xFF8EAA73));Caption("TU ESPACIO DE TRABAJO")}
                    Spacer(Modifier.height(15.dp));Txt("Entrar a mi agenda.",30,weight=FontWeight.SemiBold)
                    Spacer(Modifier.height(9.dp));Txt("Organiza. Comparte. Sigue adelante.",14,C.muted)
                    Spacer(Modifier.height(34.dp))
                    Field("Correo de empresa",email,{email=it;error=null},placeholder="nombre@nexo.studio")
                    Spacer(Modifier.height(18.dp))
                    Field("Contraseña",password,{password=it;error=null},visualTransformation=if(showPassword)VisualTransformation.None else PasswordVisualTransformation(),trailing={IconAction(Symbol.EYE,if(showPassword)"Ocultar contraseña" else "Mostrar contraseña",{showPassword=!showPassword})})
                    Spacer(Modifier.height(8.dp));Caption("Mínimo 8 caracteres, un número y un símbolo.")
                    error?.let {Spacer(Modifier.height(12.dp));Txt(it,12,MaterialTheme.colorScheme.error)}
                    Spacer(Modifier.height(27.dp))
                    Action("Entrar a mi agenda",{
                        error = when {
                            !email.matches(Regex("[^@\\s]+@[^@\\s]+\\.[^@\\s]+")) -> "Introduce un correo electrónico válido."
                            !validPassword(password) -> "La contraseña necesita 8 caracteres, un número y un símbolo, sin espacios."
                            email.lowercase().trim() != "ana@nexo.test" || password != state.password -> "El correo o la contraseña no coinciden con la cuenta de demostración."
                            else -> null
                        }
                        if(error == null) state.loggedIn=true
                    },Modifier.fillMaxWidth(),Symbol.ARROW)
                    Spacer(Modifier.height(30.dp));Rule();Spacer(Modifier.height(22.dp))
                    Row(Modifier.fillMaxWidth().background(C.soft,RoundedCornerShape(10.dp)).padding(15.dp),horizontalArrangement=Arrangement.spacedBy(10.dp)) {
                        Glyph(Symbol.INFO,size=17.dp)
                        Column {Txt("PRUEBA LA DEMO",12,weight=FontWeight.Medium);Spacer(Modifier.height(5.dp));Caption("ana@nexo.test  ·  Demo2026!");Spacer(Modifier.height(3.dp));Caption("Prototipo local · datos de demostración")}
                    }
                }
            }
        }
    }
}

@Composable fun Sidebar(state: AgendaState, compact: Boolean) {
    Column(Modifier.width(if(compact)76.dp else 228.dp).fillMaxHeight().background(C.surface).border(androidx.compose.foundation.BorderStroke(1.dp,C.line)).padding(horizontal=if(compact)14.dp else 22.dp,vertical=28.dp)) {
        Box(Modifier.padding(start=if(compact)6.dp else 4.dp)) {Brand(compact)}
        Spacer(Modifier.height(43.dp))
        if(!compact) {Txt("ESPACIO PERSONAL",10,C.muted,FontWeight.Medium);Spacer(Modifier.height(12.dp))}
        Page.entries.forEach { page ->
            val selected = state.page == page
            Row(Modifier.fillMaxWidth().height(46.dp).semantics { contentDescription = page.label }.clip(RoundedCornerShape(10.dp)).background(if(selected)C.accentSoft else Color.Transparent).clickable {state.page=page}.padding(horizontal=if(compact)13.dp else 12.dp),verticalAlignment=Alignment.CenterVertically,horizontalArrangement=Arrangement.spacedBy(11.dp)) {
                Glyph(when(page){Page.AGENDA->Symbol.CALENDAR;Page.CHAT->Symbol.CHAT;Page.SETTINGS->Symbol.SETTINGS},color=if(selected)C.accent else C.muted)
                if(!compact) {Txt(page.label,13,if(selected)C.accent else C.muted,if(selected)FontWeight.SemiBold else FontWeight.Normal);if(page==Page.CHAT){Spacer(Modifier.weight(1f));Dot(Color(0xFFB1C69A),5.dp)}}
            }
            Spacer(Modifier.height(6.dp))
        }
        if(!compact) {
            Spacer(Modifier.height(34.dp));Rule();Spacer(Modifier.height(26.dp));Txt("MIS CALENDARIOS",10,C.muted,FontWeight.Medium);Spacer(Modifier.height(12.dp))
            Category.entries.forEach { category ->
                Row(Modifier.fillMaxWidth().clip(RoundedCornerShape(8.dp)).clickable {if(category in state.categories)state.categories.remove(category) else state.categories.add(category)}.padding(vertical=10.dp,horizontal=5.dp),verticalAlignment=Alignment.CenterVertically,horizontalArrangement=Arrangement.spacedBy(10.dp)) {
                    Box(Modifier.size(15.dp).background(if(category in state.categories)category.strongColor()else Color.Transparent,RoundedCornerShape(4.dp)).border(1.dp,category.strongColor(),RoundedCornerShape(4.dp)),contentAlignment=Alignment.Center) {if(category in state.categories)Glyph(Symbol.CHECK,size=11.dp,color=Color.White)}
                    Txt(category.label,12,C.muted)
                }
            }
        }
        Spacer(Modifier.weight(1f))
        if(!compact) {
            Column(Modifier.fillMaxWidth().background(C.accentSoft,RoundedCornerShape(12.dp)).padding(15.dp)) {
                Glyph(Symbol.USERS,color=C.accent,size=21.dp);Spacer(Modifier.height(10.dp));Txt("Mejor, en equipo.",13,C.accent,FontWeight.Medium);Spacer(Modifier.height(6.dp));Txt("Un espacio para coordinarte\ny compartir ideas.",11,C.muted);Spacer(Modifier.height(11.dp));Txt("Ir al chat  ↗",11,C.accent,FontWeight.SemiBold,Modifier.clickable {state.page=Page.CHAT})
            }
            Spacer(Modifier.height(25.dp));Caption("NEXO STUDIO");Spacer(Modifier.height(4.dp));Caption("Agenda de equipo · v1.0");Spacer(Modifier.height(20.dp));Rule();Spacer(Modifier.height(12.dp))
        }
        Row(Modifier.fillMaxWidth().semantics { contentDescription = "Cerrar sesión" }.clip(RoundedCornerShape(8.dp)).clickable {state.loggedIn=false;state.query=""}.padding(vertical=10.dp,horizontal=if(compact)13.dp else 5.dp),verticalAlignment=Alignment.CenterVertically,horizontalArrangement=Arrangement.spacedBy(12.dp)) {Glyph(Symbol.LOGOUT,size=18.dp);if(!compact)Txt("Cerrar sesión",12,C.muted)}
    }
}

@Composable fun Topbar(state: AgendaState, compact: Boolean, onNotifications: () -> Unit) {
    Row(Modifier.fillMaxWidth().height(76.dp).background(C.surface).padding(horizontal=28.dp),verticalAlignment=Alignment.CenterVertically) {
        if(!compact) {Caption("Espacio de trabajo");Spacer(Modifier.width(12.dp));Caption("/");Spacer(Modifier.width(12.dp))};Txt(state.page.label,12,weight=FontWeight.Medium)
        Spacer(Modifier.weight(1f))
        if(state.page==Page.AGENDA) {
            Row(Modifier.width(if(compact)190.dp else 250.dp).height(37.dp).background(C.bg,RoundedCornerShape(8.dp)).padding(horizontal=11.dp),verticalAlignment=Alignment.CenterVertically) {
                Glyph(Symbol.SEARCH,size=16.dp);Spacer(Modifier.width(8.dp));androidx.compose.foundation.text.BasicTextField(state.query,{state.query=it},Modifier.weight(1f),singleLine=true,textStyle=androidx.compose.ui.text.TextStyle(color=C.ink,fontSize=12.sp),decorationBox={inner->if(state.query.isEmpty())Txt("Buscar en mi agenda…",12,C.muted);inner()})
                if(state.query.isNotEmpty())IconAction(Symbol.CLOSE,"Limpiar búsqueda",{state.query=""},Modifier.size(22.dp))
            }
            Spacer(Modifier.width(15.dp))
        }
        IconAction(if(state.dark)Symbol.SUN else Symbol.MOON,"Cambiar tema",{state.dark=!state.dark})
        IconAction(Symbol.BELL,"Ver notificaciones",onNotifications)
        Spacer(Modifier.width(15.dp));Box(Modifier.width(1.dp).height(26.dp).background(C.line));Spacer(Modifier.width(19.dp))
        Row(Modifier.clickable{state.page=Page.SETTINGS},verticalAlignment=Alignment.CenterVertically,horizontalArrangement=Arrangement.spacedBy(10.dp)) {
            Avatar(state.name.split(" ").take(2).joinToString(""){it.take(1).uppercase()})
            if(!compact)Column {Txt(state.name,12,weight=FontWeight.Medium);Txt("Demo local · Ana Martín",10,C.muted)}
        }
    }
    Rule()
}

@Composable fun PageHeading(eyebrow: String,title: String,subtitle: String,action: @Composable () -> Unit = {}) {
    Row(Modifier.fillMaxWidth(),verticalAlignment=Alignment.CenterVertically) {
        Column(Modifier.weight(1f)) {Txt(eyebrow,10,C.muted,FontWeight.Medium);Spacer(Modifier.height(5.dp));Txt(title,29,weight=FontWeight.SemiBold);Spacer(Modifier.height(5.dp));Txt(subtitle,13,C.muted)}
        action()
    }
}

@Composable fun Modal(title: String, subtitle: String, onClose: () -> Unit, width: androidx.compose.ui.unit.Dp = 550.dp, footer: (@Composable () -> Unit)? = null, content: @Composable ColumnScope.() -> Unit) {
    Dialog(onDismissRequest=onClose) {
        Card(Modifier.width(width).heightIn(max=780.dp).padding(4.dp)) {
            Row(Modifier.fillMaxWidth().padding(start=24.dp,top=22.dp,end=15.dp,bottom=18.dp),verticalAlignment=Alignment.Top) {
                Column(Modifier.weight(1f)) {Txt(title,23,weight=FontWeight.SemiBold);if(subtitle.isNotEmpty()){Spacer(Modifier.height(5.dp));Caption(subtitle)}}
                IconAction(Symbol.CLOSE,"Cerrar ventana",onClose)
            }
            Rule()
            Column(Modifier.weight(1f,fill=false).verticalScroll(rememberScrollState()).padding(24.dp),verticalArrangement=Arrangement.spacedBy(18.dp),content=content)
            if(footer!=null) {Rule();Column(Modifier.fillMaxWidth().padding(horizontal=24.dp,vertical=16.dp)){footer()}}
        }
    }
}
