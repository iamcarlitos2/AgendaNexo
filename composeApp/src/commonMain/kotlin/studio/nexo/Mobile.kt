package studio.nexo

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
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
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import kotlinx.datetime.*
import kotlin.time.Clock

@Composable fun MobileApp(state: AgendaState) {
    var editor by remember { mutableStateOf(false) }
    var editing by remember { mutableStateOf<Appointment?>(null) }
    var draftDate by remember { mutableStateOf(state.date) }
    var details by remember { mutableStateOf<Appointment?>(null) }
    var passwordScreen by remember { mutableStateOf(false) }
    var conversation by remember { mutableStateOf(false) }
    var createRoom by remember { mutableStateOf(false) }
    var invite by remember { mutableStateOf(false) }
    fun newEvent(date: LocalDate) { draftDate=date; editing=null; editor=true }
    fun navigate(page: Page) { state.page=page; editor=false; passwordScreen=false; conversation=false }
    LaunchedEffect(Unit) { while(true) { state.checkAlerts(); delay(1000) } }
    LaunchedEffect(state.toast) { if(state.toast!=null) { delay(3500);state.toast=null } }
    NexoTheme(state.dark,state.accent) {
        Surface(Modifier.fillMaxSize(),color=C.bg) {
            Box(Modifier.fillMaxSize()) {
                if(!state.loggedIn) MobileLogin(state) else Column(Modifier.fillMaxSize()) {
                    val room=state.rooms.first { it.id==state.selectedRoom }
                    val title=when {
                        editor -> if(editing==null) "Nueva cita" else "Editar cita"
                        passwordScreen -> "Tu cuenta, segura"
                        state.page==Page.AGENDA -> "Mi agenda"
                        state.page==Page.CHAT -> if(conversation) room.name else "Tu equipo"
                        else -> "A tu manera"
                    }
                    val subtitle=when {
                        editor -> "Haz espacio para tu próximo paso."
                        passwordScreen -> "Actualiza tu contraseña cuando lo necesites."
                        state.page==Page.AGENDA -> "Espacio para lo que importa."
                        state.page==Page.CHAT -> if(conversation) "${room.members.size} participantes" else "Conversaciones que nos acercan."
                        else -> "Tu espacio, tus preferencias."
                    }
                    MobileHeader(state,title,subtitle) {navigate(Page.SETTINGS)}
                    Box(Modifier.weight(1f).fillMaxWidth()) {
                        when {
                            editor -> MobileEventForm(state,editing,draftDate,{editor=false}) {state.save(it);editor=false}
                            passwordScreen -> MobilePassword(state,{passwordScreen=false})
                            state.page==Page.AGENDA -> MobileAgenda(state,::newEvent) {details=it}
                            state.page==Page.CHAT && conversation -> MobileConversation(state,room,{conversation=false},{invite=true})
                            state.page==Page.CHAT -> MobileRooms(state,{createRoom=true}) {state.selectedRoom=it;conversation=true}
                            else -> MobileSettings(state) {passwordScreen=true}
                        }
                    }
                    Rule()
                    Row(Modifier.fillMaxWidth().height(62.dp).background(C.surface),verticalAlignment=Alignment.CenterVertically) {
                        Page.entries.forEach {page ->
                            TextButton(onClick={navigate(page)},modifier=Modifier.weight(1f).fillMaxHeight()) {
                                Txt(page.label,14,if(page==state.page)C.accent else C.muted,if(page==state.page)FontWeight.SemiBold else FontWeight.Normal)
                            }
                        }
                    }
                }
                state.toast?.let {message ->
                    Box(Modifier.align(Alignment.BottomCenter).padding(start=18.dp,end=18.dp,bottom=76.dp).background(C.ink,RoundedCornerShape(12.dp)).padding(14.dp)) {Txt(message,12,C.surface)}
                }
            }
        }
        details?.let {item -> EventDetails(item,{details=null}) {details=null;editing=item;editor=true} }
        state.activeAlert?.let {item -> AlertPopup(item,state.reminderMinutes) {state.activeAlert=null} }
        if(createRoom) RoomDialog({createRoom=false}) {name,members ->
            val room=Room(state.id(),name,"Un espacio para compartir con el equipo",listOf(state.name))
            room.pending.addAll(members);state.rooms.add(room);state.selectedRoom=room.id
            createRoom=false;conversation=true;state.toast="Sala creada con ${members.size} invitaciones pendientes."
        }
        if(invite) {
            val room=state.rooms.first {it.id==state.selectedRoom}
            InviteDialog(room,{invite=false}) {members ->room.pending.addAll(members);invite=false;state.toast="Invitaciones añadidas a ${room.name}."}
        }
    }
}

@Composable private fun MobileHeader(state: AgendaState,title: String,subtitle: String,onProfile: ()->Unit) {
    Column(Modifier.fillMaxWidth().padding(start=24.dp,end=24.dp,top=16.dp,bottom=20.dp)) {
        Row(Modifier.fillMaxWidth(),verticalAlignment=Alignment.CenterVertically) {
            Brand();Spacer(Modifier.weight(1f))
            Box(Modifier.size(44.dp).clip(RoundedCornerShape(14.dp)).background(C.accentSoft).clickable(onClick=onProfile),contentAlignment=Alignment.Center) {
                Txt(state.name.split(" ").take(2).joinToString(""){it.take(1)},14,C.accent,FontWeight.SemiBold)
            }
        }
        Spacer(Modifier.height(12.dp));Caption("Demo local · ${state.name}")
        Spacer(Modifier.height(20.dp));Txt(title,27,weight=FontWeight.Bold,lines=2)
        Spacer(Modifier.height(4.dp));Txt(subtitle,13,C.muted,lines=2)
    }
}

@Composable private fun MobileLogin(state: AgendaState) {
    var email by remember {mutableStateOf("ana@nexo.test")}
    var password by remember {mutableStateOf("Demo2026!")}
    var visible by remember {mutableStateOf(false)}
    var error by remember {mutableStateOf<String?>(null)}
    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(24.dp)) {
        Spacer(Modifier.height(28.dp))
        Box(Modifier.size(64.dp).background(C.accent,RoundedCornerShape(16.dp)),contentAlignment=Alignment.Center) {Txt("N",42,if(C.dark)C.bg else Color.White,FontWeight.Bold)}
        Spacer(Modifier.height(20.dp));Caption("NEXO STUDIO")
        Spacer(Modifier.height(28.dp));Text("Tu equipo.\nTu tiempo.",fontSize=38.sp,lineHeight=47.sp,fontWeight=FontWeight.Bold,color=C.ink)
        Spacer(Modifier.height(14.dp));Txt("La agenda que conecta a todo el equipo.",14,C.muted)
        Spacer(Modifier.height(28.dp))
        Field("Correo de empresa",email,{email=it;error=null})
        Spacer(Modifier.height(20.dp))
        Field("Contraseña",password,{password=it;error=null},visualTransformation=if(visible)VisualTransformation.None else PasswordVisualTransformation(),trailing={IconAction(Symbol.EYE,"Mostrar contraseña",{visible=!visible})})
        Spacer(Modifier.height(10.dp));Caption("Mínimo 8 caracteres, 1 número y 1 símbolo.")
        error?.let {Spacer(Modifier.height(10.dp));Txt(it,12,MaterialTheme.colorScheme.error)}
        Spacer(Modifier.height(24.dp))
        Action("Entrar a mi agenda",{
            error=when {
                !email.matches(Regex("[^@\\s]+@[^@\\s]+\\.[^@\\s]+")) -> "Introduce un correo válido."
                !validPassword(password) -> "La contraseña necesita 8 caracteres, un número y un símbolo, sin espacios."
                email.trim().lowercase()!="ana@nexo.test" || password!=state.password -> "El correo o la contraseña no son correctos."
                else -> null
            }
            if(error==null)state.loggedIn=true
        },Modifier.fillMaxWidth().height(50.dp))
        Spacer(Modifier.height(24.dp))
        Column(Modifier.fillMaxWidth().background(C.accentSoft,RoundedCornerShape(18.dp)).padding(16.dp)) {
            Txt("PRUEBA LA DEMO",11,C.accent,FontWeight.Medium)
            Spacer(Modifier.height(8.dp));Txt("ana@nexo.test · Demo2026!",13,weight=FontWeight.SemiBold)
            Spacer(Modifier.height(6.dp));Caption("Datos de demostración durante esta sesión.")
        }
        Spacer(Modifier.height(30.dp));Text("Organiza. Comparte. Sigue adelante.",Modifier.fillMaxWidth(),color=C.muted,fontSize=12.sp,textAlign=TextAlign.Center)
        Spacer(Modifier.height(12.dp))
    }
}

@Composable private fun MobileAgenda(state: AgendaState,onNew: (LocalDate)->Unit,onEvent: (Appointment)->Unit) {
    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(horizontal=24.dp),verticalArrangement=Arrangement.spacedBy(18.dp)) {
        Row(Modifier.fillMaxWidth(),verticalAlignment=Alignment.CenterVertically) {
            IconAction(Symbol.LEFT,"Periodo anterior",{state.move(-1)})
            val label=when(state.view) {
                CalendarView.MONTH -> state.date.monthLabel()
                CalendarView.WEEK -> state.date.weekStart().let{"${it.day} – ${it.plusDays(6).day} ${monthNames[it.month.number-1].lowercase()}"}
                CalendarView.DAY -> "${dayNames[state.date.dayOfWeek.isoDayNumber-1]}, ${state.date.day} ${monthNames[state.date.month.number-1].lowercase()}"
            }
            Text(label,Modifier.weight(1f),color=C.ink,fontSize=15.sp,fontWeight=FontWeight.SemiBold,textAlign=TextAlign.Center)
            IconAction(Symbol.RIGHT,"Periodo siguiente",{state.move(1)})
        }
        Row(Modifier.fillMaxWidth().height(46.dp).background(C.accentSoft,RoundedCornerShape(14.dp)).padding(4.dp)) {
            CalendarView.entries.forEach {view ->
                Box(Modifier.weight(1f).fillMaxHeight().clip(RoundedCornerShape(10.dp)).background(if(view==state.view)C.surface else Color.Transparent).clickable {state.view=view},contentAlignment=Alignment.Center) {
                    Txt(view.label,13,if(view==state.view)C.accent else C.muted,if(view==state.view)FontWeight.SemiBold else FontWeight.Normal)
                }
            }
        }
        when(state.view) {
            CalendarView.MONTH -> {
                MobileMonth(state)
                Action("+  Nueva cita",{onNew(state.date)},Modifier.fillMaxWidth().height(50.dp))
                Caption(state.date.fullLabel().uppercase())
                val events=state.visible(state.date)
                if(events.isEmpty())Caption("No hay citas para este día.")
                events.forEach {MobileAppointment(it,onEvent)}
            }
            CalendarView.WEEK -> {
                MobileWeek(state,onEvent)
                Action("+  Nueva cita",{onNew(state.date)},Modifier.fillMaxWidth().height(50.dp))
                Caption("Desliza para explorar los días de la semana.")
            }
            CalendarView.DAY -> {
                val events=state.visible(state.date)
                if(events.isEmpty())Caption("No hay citas para este día.")
                events.forEach {MobileAppointment(it,onEvent)}
                Action("+  Nueva cita",{onNew(state.date)},Modifier.fillMaxWidth().height(50.dp))
            }
        }
        Spacer(Modifier.height(6.dp))
    }
}

@Composable private fun MobileMonth(state: AgendaState) {
    val first=state.date.monthStart();val start=first.weekStart()
    val days=(first.plus(1,DateTimeUnit.MONTH).toEpochDays()-first.toEpochDays()).toInt()
    val rows=(days+first.dayOfWeek.isoDayNumber-2)/7+1
    Column(Modifier.fillMaxWidth(),verticalArrangement=Arrangement.spacedBy(2.dp)) {
        Row(Modifier.fillMaxWidth().height(24.dp)) {listOf("L","M","X","J","V","S","D").forEach {Text(it,Modifier.weight(1f),color=C.muted,fontSize=12.sp,textAlign=TextAlign.Center)}}
        repeat(rows) {week ->
            Row(Modifier.fillMaxWidth()) {
                repeat(7) {day ->
                    val date=start.plusDays(week*7+day)
                    Box(Modifier.weight(1f).height(39.dp),contentAlignment=Alignment.Center) {
                        if(date.month==first.month) {
                            Column(Modifier.fillMaxSize().clip(RoundedCornerShape(13.dp)).background(if(date==state.date)C.accent else Color.Transparent).clickable {state.date=date},horizontalAlignment=Alignment.CenterHorizontally,verticalArrangement=Arrangement.Center) {
                                Txt(date.day.toString(),12,if(date==state.date){if(C.dark)C.bg else Color.White}else C.ink)
                                if(state.visible(date).isNotEmpty()){Spacer(Modifier.height(3.dp));Dot(if(date==state.date){if(C.dark)C.bg else Color.White}else C.accent,3.dp)}
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable private fun MobileAppointment(item: Appointment,onEvent: (Appointment)->Unit) {
    Row(Modifier.fillMaxWidth().clip(RoundedCornerShape(20.dp)).background(C.surface).border(1.dp,C.line,RoundedCornerShape(20.dp)).clickable {onEvent(item)}.padding(vertical=15.dp,horizontal=12.dp),horizontalArrangement=Arrangement.spacedBy(12.dp)) {
        Box(Modifier.width(4.dp).height(72.dp).background(item.category.strongColor(),RoundedCornerShape(3.dp)))
        Column(verticalArrangement=Arrangement.spacedBy(7.dp)) {
            Txt("${timeLabel(item.start)} — ${timeLabel(item.end)} · ${item.category.label}",11,item.category.strongColor(),FontWeight.Medium)
            Txt(item.title,17,weight=FontWeight.Bold)
            Caption(item.place.ifBlank{"Sin ubicación"})
        }
    }
}

@Composable private fun MobileWeek(state: AgendaState,onEvent: (Appointment)->Unit) {
    val dates=(0..6).map {state.date.weekStart().plusDays(it)}
    val start=minOf(9,dates.flatMap{state.visible(it)}.minOfOrNull{it.start/60}?:9)
    val end=maxOf(18,dates.flatMap{state.visible(it)}.maxOfOrNull{(it.end+59)/60}?:18)
    Column(Modifier.fillMaxWidth().horizontalScroll(rememberScrollState())) {
        Row(Modifier.width(704.dp).height(42.dp),verticalAlignment=Alignment.CenterVertically) {
            Spacer(Modifier.width(46.dp))
            dates.forEach {date ->Box(Modifier.width(94.dp).height(34.dp).clip(RoundedCornerShape(10.dp)).background(if(date==state.date)C.accent else Color.Transparent).clickable{state.date=date},contentAlignment=Alignment.Center) {Txt("${dayNames[date.dayOfWeek.isoDayNumber-1].take(1)} ${date.day}",11,if(date==state.date){if(C.dark)C.bg else Color.White}else C.ink)}}
        }
        Row(Modifier.width(704.dp).height(((end-start)*60).dp)) {
            Column(Modifier.width(46.dp)) {for(hour in start until end)Box(Modifier.height(60.dp).padding(top=6.dp)){Txt(timeLabel(hour*60),10,C.muted)}}
            dates.forEach {date ->
                Box(Modifier.width(94.dp).fillMaxHeight()) {
                    Column {repeat(end-start){Box(Modifier.width(94.dp).height(60.dp).border(.5.dp,C.line))}}
                    layoutEvents(state.visible(date)).forEach {placement ->
                        val item=placement.event
                        Box(Modifier.offset(x=(94f/placement.total*placement.lane+2).dp,y=(item.start-start*60).dp).width((94f/placement.total-4).dp).height(maxOf(30,item.end-item.start-2).dp).clip(RoundedCornerShape(9.dp)).background(item.category.strongColor()).clickable {onEvent(item)}.padding(7.dp)) {Txt(item.title,10,Color.White,FontWeight.Medium,lines=2)}
                    }
                }
            }
        }
    }
}

@Composable private fun MobileEventForm(state: AgendaState,initial: Appointment?,date: LocalDate,onCancel: ()->Unit,onSave: (Appointment)->Unit) {
    var title by remember {mutableStateOf(initial?.title?:"")}
    var place by remember {mutableStateOf(initial?.place?:"Sala Nexo")}
    var dateText by remember {mutableStateOf((initial?.date?:date).let{"${it.day.toString().padStart(2,'0')}/${it.month.number.toString().padStart(2,'0')}/${it.year}"})}
    var time by remember {mutableStateOf(timeLabel(initial?.start?:720))}
    var duration by remember {mutableStateOf((initial?.let{it.end-it.start}?:45).toString())}
    var category by remember {mutableStateOf(initial?.category?:Category.PROJECT)}
    var reminder by remember {mutableStateOf(initial?.reminder?:true)}
    var expanded by remember {mutableStateOf(false)}
    var error by remember {mutableStateOf<String?>(null)}
    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(horizontal=24.dp),verticalArrangement=Arrangement.spacedBy(20.dp)) {
        Field("Título de la cita",title,{title=it},placeholder="Revisión de propuestas")
        Field("Ubicación o enlace",place,{place=it},placeholder="Sala Nexo")
        Row(horizontalArrangement=Arrangement.spacedBy(16.dp)) {
            Field("Fecha",dateText,{dateText=it},Modifier.weight(1f))
            Field("Hora",time,{time=it},Modifier.weight(1f))
        }
        Row(horizontalArrangement=Arrangement.spacedBy(16.dp)) {
            Field("Duración (minutos)",duration,{duration=it},Modifier.weight(1f))
            Column(Modifier.weight(1f),verticalArrangement=Arrangement.spacedBy(7.dp)) {
                Txt("Categoría",12,weight=FontWeight.Medium)
                Box {
                    OutlinedButton({expanded=true},Modifier.fillMaxWidth().height(56.dp),shape=RoundedCornerShape(10.dp),border=androidx.compose.foundation.BorderStroke(1.dp,C.line)){Txt("${category.label} ▾",12)}
                    DropdownMenu(expanded,{expanded=false}) {Category.entries.forEach {item->DropdownMenuItem(text={Txt(item.label,13)},onClick={category=item;expanded=false})}}
                }
            }
        }
        SettingToggle("Avisarme antes de la cita","",reminder,{reminder=it})
        error?.let {Txt(it,12,MaterialTheme.colorScheme.error)}
        Action("Guardar cita",{
            val parsedDate=runCatching {val p=dateText.split('/');LocalDate(p[2].toInt(),p[1].toInt(),p[0].toInt())}.getOrNull()
            val start=parseTime(time);val minutes=duration.toIntOrNull()
            error=when {
                title.isBlank()->"Ponle un título a tu cita."
                parsedDate==null->"Introduce una fecha válida: DD/MM/AAAA."
                start==null->"Introduce una hora válida: HH:MM."
                minutes==null || minutes<=0 || minutes>1440 || start+minutes>1440 ->"La duración debe ser positiva y terminar el mismo día."
                else->null
            }
            if(error==null)onSave(Appointment(initial?.id?:state.id(),title.trim(),parsedDate!!,start!!,start+minutes!!,category,place.trim(),initial?.description?:"",reminder,initial?.attendees?:state.name))
        },Modifier.fillMaxWidth().height(50.dp))
        Action("Cancelar",onCancel,Modifier.fillMaxWidth(),primary=false)
        Spacer(Modifier.height(4.dp))
    }
}

@Composable private fun MobileRooms(state: AgendaState,onCreate: ()->Unit,onOpen: (Int)->Unit) {
    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(horizontal=24.dp),verticalArrangement=Arrangement.spacedBy(20.dp)) {
        Action("+  Nueva sala",onCreate,Modifier.fillMaxWidth().height(50.dp))
        Spacer(Modifier.height(2.dp));Caption("TUS CONVERSACIONES")
        state.rooms.filter{it.accepted}.forEach {room ->
            Card(Modifier.fillMaxWidth().clickable{onOpen(room.id)}) {
                Column(Modifier.padding(18.dp),verticalArrangement=Arrangement.spacedBy(8.dp)) {
                    Txt(room.name,17,weight=FontWeight.Bold)
                    Txt(room.messages.lastOrNull()?.let{"${if(it.mine)"Tú" else it.author.substringBefore(' ')}: ${it.text}"}?:"El comienzo de algo bueno.",12,C.muted,lines=2)
                    Txt("${room.members.size} participantes · abrir conversación",11,C.accent)
                }
            }
        }
        state.rooms.filter{!it.accepted}.forEach {room ->
            Column(Modifier.fillMaxWidth().background(C.accentSoft,RoundedCornerShape(22.dp)).padding(18.dp),verticalArrangement=Arrangement.spacedBy(12.dp)) {
                Txt("INVITACIÓN PENDIENTE",10,C.accent,FontWeight.Medium)
                Txt(room.name,17,weight=FontWeight.Bold);Caption(room.description)
                Action("Aceptar invitación",{room.accepted=true;if(state.name !in room.members)room.members.add(state.name);onOpen(room.id)},Modifier.fillMaxWidth().height(50.dp))
            }
        }
        Caption("Las salas y el historial se conservan durante la sesión.");Spacer(Modifier.height(4.dp))
    }
}

@Composable private fun MobileConversation(state: AgendaState,room: Room,onBack: ()->Unit,onInvite: ()->Unit) {
    var text by remember(room.id) {mutableStateOf("")}
    val list=rememberLazyListState()
    LaunchedEffect(room.id,room.messages.size) {if(room.messages.isNotEmpty())list.animateScrollToItem(room.messages.lastIndex)}
    Column(Modifier.fillMaxSize().padding(horizontal=24.dp)) {
        Row(Modifier.fillMaxWidth(),verticalAlignment=Alignment.CenterVertically) {TextButton(onBack){Txt("‹ Salas",13,C.accent,FontWeight.SemiBold)};Spacer(Modifier.weight(1f));TextButton(onInvite){Txt("Invitar",13,C.accent,FontWeight.SemiBold)}}
        LazyColumn(Modifier.weight(1f).fillMaxWidth(),state=list,contentPadding=PaddingValues(vertical=20.dp),verticalArrangement=Arrangement.spacedBy(20.dp)) {
            itemsIndexed(room.messages) {_,message ->
                Box(Modifier.fillMaxWidth(),contentAlignment=if(message.mine)Alignment.CenterEnd else Alignment.CenterStart) {
                    Column(Modifier.fillMaxWidth(.87f).background(if(message.mine)C.accentSoft else C.surface,RoundedCornerShape(20.dp)).border(1.dp,if(message.mine)C.accentSoft else C.line,RoundedCornerShape(20.dp)).padding(16.dp),verticalArrangement=Arrangement.spacedBy(12.dp)) {
                        Txt("${if(message.mine)"Tú"else message.author} · ${message.time}",11,if(message.mine)C.accent else C.muted)
                        Txt(message.text,14)
                    }
                }
            }
        }
        Row(Modifier.fillMaxWidth().padding(bottom=14.dp),horizontalArrangement=Arrangement.spacedBy(10.dp),verticalAlignment=Alignment.CenterVertically) {
            Field("",text,{text=it},Modifier.weight(1f),placeholder="Escribe al equipo…")
            Action("Enviar",{
                if(text.isNotBlank()) {
                    val now=Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault())
                    room.messages.add(ChatMessage(state.name,text.trim(),timeLabel(now.hour*60+now.minute),true));text=""
                }
            },enabled=text.isNotBlank())
        }
    }
}

@Composable private fun MobileSettings(state: AgendaState,onPassword: ()->Unit) {
    var expanded by remember {mutableStateOf(false)}
    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(horizontal=24.dp),verticalArrangement=Arrangement.spacedBy(20.dp)) {
        Caption("APARIENCIA")
        SettingToggle("Modo noche","",state.dark,{state.dark=it})
        Txt("Color de las citas",14)
        Row(horizontalArrangement=Arrangement.spacedBy(14.dp)) {accents.forEachIndexed {index,color->Box(Modifier.size(44.dp,34.dp).clip(RoundedCornerShape(13.dp)).background(color).clickable{state.accent=index},contentAlignment=Alignment.Center){if(index==state.accent)Glyph(Symbol.CHECK,size=18.dp,color=Color.White)}}}
        Spacer(Modifier.height(8.dp));Caption("RECORDATORIOS")
        SettingToggle("Activar avisos","",state.alerts,{state.alerts=it})
        SettingToggle("Sonido del aviso","",state.sound,{state.sound=it},state.alerts)
        SettingToggle("Ventana emergente","",state.modalAlerts,{state.modalAlerts=it},state.alerts)
        Box {
            OutlinedButton({expanded=true},Modifier.fillMaxWidth().height(54.dp),shape=RoundedCornerShape(14.dp),border=androidx.compose.foundation.BorderStroke(1.dp,C.line)) {Txt("${state.reminderMinutes} minutos antes",14);Spacer(Modifier.weight(1f));Txt("▾",14)}
            DropdownMenu(expanded,{expanded=false}) {listOf(5,10,15,30).forEach {minutes ->DropdownMenuItem(text={Txt("$minutes minutos antes")},onClick={state.reminderMinutes=minutes;expanded=false})}}
        }
        Action("Guardar preferencias",{state.toast="Preferencias guardadas para esta sesión."},Modifier.fillMaxWidth().height(50.dp))
        Action("Probar aviso",{
            val now=Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault())
            val sample=Appointment(-1,"Revisión de propuestas",now.date,now.hour*60+now.minute,now.hour*60+now.minute,Category.PROJECT,"Sala Nexo")
            if(state.sound)playAlertSound();if(state.modalAlerts)state.activeAlert=sample else state.toast="Tu cita empieza en ${state.reminderMinutes} minutos."
        },Modifier.fillMaxWidth(),primary=false,enabled=state.alerts)
        TextButton(onPassword){Txt("Cambiar contraseña →",13,C.accent,FontWeight.SemiBold)}
        TextButton({state.loggedIn=false}){Txt("Cerrar sesión",13,C.muted)}
        Spacer(Modifier.height(4.dp))
    }
}

@Composable private fun MobilePassword(state: AgendaState,onBack: ()->Unit) {
    var current by remember{mutableStateOf("")};var password by remember{mutableStateOf("")};var repeat by remember{mutableStateOf("")}
    var error by remember{mutableStateOf<String?>(null)}
    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(horizontal=24.dp),verticalArrangement=Arrangement.spacedBy(24.dp)) {
        Field("Contraseña actual",current,{current=it},visualTransformation=PasswordVisualTransformation())
        Field("Nueva contraseña",password,{password=it},visualTransformation=PasswordVisualTransformation())
        Field("Repite la nueva contraseña",repeat,{repeat=it},visualTransformation=PasswordVisualTransformation())
        Column(Modifier.fillMaxWidth().background(C.accentSoft,RoundedCornerShape(15.dp)).padding(16.dp),verticalArrangement=Arrangement.spacedBy(7.dp)) {
            Txt("8 o más caracteres · 1 número · 1 símbolo",11,C.accent)
            Caption("Ejemplo válido para la demo: Demo2026!")
        }
        error?.let{Txt(it,12,MaterialTheme.colorScheme.error)}
        Action("Cambiar contraseña",{
            error=when{current!=state.password->"La contraseña actual no es correcta.";!validPassword(password)->"La contraseña necesita 8 caracteres, un número y un símbolo, sin espacios.";password!=repeat->"Las nuevas contraseñas no coinciden.";password==current->"Elige una contraseña distinta a la actual.";else->null}
            if(error==null){state.password=password;state.toast="Contraseña actualizada para esta sesión.";onBack()}
        },Modifier.fillMaxWidth().height(50.dp))
        Action("Volver a ajustes",onBack,Modifier.fillMaxWidth(),primary=false)
        Spacer(Modifier.height(4.dp))
    }
}
