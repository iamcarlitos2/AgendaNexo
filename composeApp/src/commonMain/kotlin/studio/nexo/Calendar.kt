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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.datetime.*

@Composable fun AgendaScreen(state: AgendaState, onNew: (LocalDate) -> Unit, onEvent: (Appointment) -> Unit) {
    BoxWithConstraints(Modifier.fillMaxSize().padding(horizontal=28.dp,vertical=26.dp)) {
        val showAside = maxWidth > 1050.dp
        Column {
            PageHeading("NEXO STUDIO", "Mi agenda", "Espacio para lo que importa.") {Action("Nueva cita",{onNew(state.date)},symbol=Symbol.PLUS)}
            Spacer(Modifier.height(25.dp))
            Row(Modifier.weight(1f),horizontalArrangement=Arrangement.spacedBy(23.dp)) {
                Column(Modifier.weight(1f)) {
                    CalendarToolbar(state)
                    Spacer(Modifier.height(18.dp))
                    Card(Modifier.weight(1f).fillMaxWidth()) {
                        when(state.view) {
                            CalendarView.MONTH -> MonthCalendar(state,onNew,onEvent)
                            CalendarView.WEEK -> TimeCalendar(state,onNew,onEvent)
                            CalendarView.DAY -> DayAgenda(state,onNew,onEvent)
                        }
                    }
                    Spacer(Modifier.height(14.dp))
                    Row(Modifier.fillMaxWidth(),verticalAlignment=Alignment.CenterVertically) {
                        Row(horizontalArrangement=Arrangement.spacedBy(17.dp)) {Category.entries.forEach {Row(verticalAlignment=Alignment.CenterVertically,horizontalArrangement=Arrangement.spacedBy(6.dp)){Dot(it.strongColor(),5.dp);Caption(it.label)}}}
                        Spacer(Modifier.weight(1f));Caption(TimeZone.currentSystemDefault().id.let{if(it=="SYSTEM")"Hora del dispositivo"else"Hora local · $it"})
                    }
                }
                if(showAside) TodayPanel(state,onNew,onEvent)
            }
        }
    }
}

@Composable fun ColumnScope.DayAgenda(state: AgendaState,onNew: (LocalDate)->Unit,onEvent: (Appointment)->Unit) {
    Column(Modifier.fillMaxWidth().weight(1f).verticalScroll(rememberScrollState()).padding(24.dp),verticalArrangement=Arrangement.spacedBy(18.dp)) {
        Txt(state.date.fullLabel(),18,weight=FontWeight.SemiBold)
        val events=state.visible(state.date)
        if(events.isEmpty()) {Caption("No tienes citas para este día.")}
        events.forEach {event ->
            Row(Modifier.fillMaxWidth().clip(RoundedCornerShape(16.dp)).border(1.dp,C.line,RoundedCornerShape(16.dp)).clickable{onEvent(event)}.padding(18.dp),horizontalArrangement=Arrangement.spacedBy(16.dp)) {
                Box(Modifier.width(4.dp).height(75.dp).background(event.category.strongColor(),RoundedCornerShape(2.dp)))
                Column(verticalArrangement=Arrangement.spacedBy(7.dp)) {
                    Txt("${timeLabel(event.start)} — ${timeLabel(event.end)} · ${event.category.label}",12,event.category.strongColor(),FontWeight.Medium)
                    Txt(event.title,19,weight=FontWeight.SemiBold)
                    Caption(event.place.ifBlank{"Sin ubicación"})
                }
            }
        }
        Action("Nueva cita",{onNew(state.date)},symbol=Symbol.PLUS)
    }
}

@Composable fun CalendarToolbar(state: AgendaState) {
    Row(Modifier.fillMaxWidth().height(42.dp),verticalAlignment=Alignment.CenterVertically) {
        Txt(if(state.view==CalendarView.DAY)"${state.date.day} de ${monthNames[state.date.month.number-1].lowercase()}" else state.date.monthLabel(),21,weight=FontWeight.Medium)
        Spacer(Modifier.width(15.dp));IconAction(Symbol.LEFT,"Periodo anterior",{state.move(-1)});IconAction(Symbol.RIGHT,"Periodo siguiente",{state.move(1)})
        Spacer(Modifier.width(6.dp));OutlinedButton(onClick={state.date=today()},shape=RoundedCornerShape(8.dp),border=androidx.compose.foundation.BorderStroke(1.dp,C.line),contentPadding=PaddingValues(horizontal=13.dp,vertical=0.dp),modifier=Modifier.height(32.dp)){Txt("Hoy",12)}
        Spacer(Modifier.weight(1f))
        Row(Modifier.height(37.dp).background(C.soft,RoundedCornerShape(9.dp)).padding(3.dp),verticalAlignment=Alignment.CenterVertically) {
            CalendarView.entries.forEach {view->
                Box(Modifier.fillMaxHeight().clip(RoundedCornerShape(7.dp)).background(if(state.view==view)C.surface else Color.Transparent).clickable {state.view=view}.padding(horizontal=14.dp),contentAlignment=Alignment.Center){Txt(view.label,12,if(state.view==view)C.ink else C.muted,if(state.view==view)FontWeight.Medium else FontWeight.Normal)}
            }
        }
    }
}

@Composable fun ColumnScope.MonthCalendar(state: AgendaState, onNew: (LocalDate) -> Unit, onEvent: (Appointment) -> Unit) {
    val first = state.date.monthStart()
    val start = first.weekStart()
    val daysInMonth = (first.plus(1,DateTimeUnit.MONTH).toEpochDays()-first.toEpochDays()).toInt()
    val rowCount = (daysInMonth+first.dayOfWeek.isoDayNumber-2)/7+1
    Row(Modifier.fillMaxWidth().height(43.dp),verticalAlignment=Alignment.CenterVertically) {
        dayNames.forEach { day-> Text(day.take(3).uppercase(),Modifier.weight(1f),fontSize=10.sp,color=C.muted,textAlign=TextAlign.Center,fontWeight=FontWeight.Medium) }
    }
    Rule()
    repeat(rowCount) {week->
        Row(Modifier.fillMaxWidth().weight(1f)) {
            repeat(7){day->
                val date = start.plusDays(week*7+day)
                val inMonth = date.month==state.date.month
                val isToday = date==today()
                val events = state.visible(date)
                val selected = date==state.date
                Column(Modifier.weight(1f).fillMaxHeight().background(when{selected->C.accentSoft.copy(alpha=.45f);!inMonth->C.soft.copy(alpha=.4f);else->Color.Transparent}).clickable {state.date=date}.padding(7.dp)) {
                    Row(Modifier.fillMaxWidth(),verticalAlignment=Alignment.CenterVertically) {
                        Box(Modifier.size(25.dp).background(if(isToday)C.accent else Color.Transparent,CircleShape),contentAlignment=Alignment.Center) {Txt(date.day.toString(),11,if(isToday){if(C.dark)C.bg else Color.White}else if(inMonth)C.ink else C.muted.copy(alpha=.55f),if(isToday)FontWeight.SemiBold else FontWeight.Normal)}
                        Spacer(Modifier.weight(1f))
                        Box(Modifier.size(21.dp).clip(RoundedCornerShape(5.dp)).clickable {onNew(date)},contentAlignment=Alignment.Center){Glyph(Symbol.PLUS,size=12.dp,color=C.muted.copy(alpha=.6f))}
                    }
                    Spacer(Modifier.height(4.dp))
                    // Keep the month readable at shorter desktop window heights.
                    BoxWithConstraints(Modifier.weight(1f).fillMaxWidth()) {
                        var capacity = ((maxHeight.value+4f)/27f).toInt().coerceIn(1,3)
                        if(events.size>capacity && capacity*27f+13f>maxHeight.value)capacity=(capacity-1).coerceAtLeast(1)
                        Column(verticalArrangement=Arrangement.spacedBy(4.dp)) {
                            events.take(capacity).forEach {event->
                                Row(Modifier.fillMaxWidth().height(23.dp).clip(RoundedCornerShape(4.dp)).background(if(C.dark)event.category.strongColor().copy(alpha=.2f)else event.category.lightColor()).clickable {onEvent(event)}.padding(horizontal=5.dp),verticalAlignment=Alignment.CenterVertically) {
                                    Box(Modifier.width(2.dp).height(12.dp).background(event.category.strongColor(),RoundedCornerShape(1.dp)))
                                    Spacer(Modifier.width(5.dp));Txt("${timeLabel(event.start)} ${event.title}",10,if(C.dark)C.ink else event.category.strongColor(),FontWeight.Medium,lines=1)
                                }
                            }
                            if(events.size>capacity)Txt("+${events.size-capacity} más",9,C.muted,modifier=Modifier.clickable {state.date=date;state.view=CalendarView.DAY}.padding(start=4.dp))
                        }
                    }
                }
                if(day<6)Box(Modifier.width(1.dp).fillMaxHeight().background(C.line))
            }
        }
        if(week<rowCount-1)Rule()
    }
}

@Composable fun ColumnScope.TimeCalendar(state: AgendaState, onNew: (LocalDate) -> Unit, onEvent: (Appointment) -> Unit) {
    val dates = if(state.view==CalendarView.DAY) listOf(state.date) else (0..6).map {state.date.weekStart().plusDays(it)}
    val earliest = dates.flatMap{state.visible(it)}.minOfOrNull{it.start/60}?:8
    val latest = dates.flatMap{state.visible(it)}.maxOfOrNull{(it.end+59)/60}?:18
    val startHour = minOf(8,earliest)
    val endHour = maxOf(19,latest)
    Row(Modifier.fillMaxWidth().height(66.dp),verticalAlignment=Alignment.CenterVertically) {
        Spacer(Modifier.width(56.dp))
        dates.forEach {date->Column(Modifier.weight(1f).clickable {state.date=date},horizontalAlignment=Alignment.CenterHorizontally) {
            Txt(dayNames[date.dayOfWeek.isoDayNumber-1].take(3).uppercase(),10,C.muted);Spacer(Modifier.height(3.dp));Box(Modifier.size(28.dp).background(if(date==today())C.accent else Color.Transparent,CircleShape),contentAlignment=Alignment.Center){Txt(date.day.toString(),16,if(date==today()){if(C.dark)C.bg else Color.White}else C.ink,FontWeight.Medium)}
        }}
    }
    Rule()
    Row(Modifier.fillMaxWidth().weight(1f).verticalScroll(rememberScrollState())) {
        Column(Modifier.width(56.dp)) { for(hour in startHour until endHour)Box(Modifier.height(72.dp).fillMaxWidth().padding(top=8.dp),contentAlignment=Alignment.TopCenter){Caption("${hour.toString().padStart(2,'0')}:00")} }
        dates.forEach {date->
            val allEvents=state.visible(date)
            // A group reserves horizontal lanes whenever appointments overlap.
            val lanes=layoutEvents(allEvents)
            BoxWithConstraints(Modifier.weight(1f).height(((endHour-startHour)*72).dp).border(.5.dp,C.line)) {
                val width=maxWidth
                Column {for(hour in startHour until endHour)Box(Modifier.fillMaxWidth().height(72.dp).clickable {onNew(date)}.border(.5.dp,C.line))}
                lanes.forEach {placement->
                    val event=placement.event
                    Column(Modifier.offset(x=width/placement.total*placement.lane+3.dp,y=((event.start-startHour*60)*1.2f).dp).width(width/placement.total-6.dp).height(maxOf(32f,(event.end-event.start)*1.2f-3f).dp).clip(RoundedCornerShape(6.dp)).background(if(C.dark)event.category.strongColor().copy(alpha=.25f)else event.category.lightColor()).clickable {onEvent(event)}.padding(7.dp)) {
                        Txt(event.title,11,if(C.dark)C.ink else event.category.strongColor(),FontWeight.Medium,lines=1)
                        if(event.end-event.start>=45){Spacer(Modifier.height(3.dp));Txt("${timeLabel(event.start)} – ${timeLabel(event.end)}",10,if(C.dark)C.muted else event.category.strongColor(),lines=1)}
                    }
                }
            }
        }
    }
}

data class EventLane(val event: Appointment,val lane: Int,val total: Int)
fun layoutEvents(events: List<Appointment>): List<EventLane> {
    val result=mutableListOf<EventLane>()
    val group=mutableListOf<Appointment>()
    var groupEnd=-1
    fun flush() {
        if(group.isEmpty())return
        val ends=mutableListOf<Int>()
        val assignments=group.map {event->
            var lane=ends.indexOfFirst {it<=event.start}
            if(lane<0){lane=ends.size;ends.add(event.end)}else ends[lane]=event.end
            event to lane
        }
        assignments.forEach{(event,lane)->result.add(EventLane(event,lane,ends.size))};group.clear()
    }
    events.sortedBy {it.start}.forEach {event->if(event.start>=groupEnd){flush();groupEnd=event.end}else groupEnd=maxOf(groupEnd,event.end);group.add(event)}
    flush();return result
}

@Composable fun TodayPanel(state: AgendaState, onNew: (LocalDate) -> Unit, onEvent: (Appointment) -> Unit) {
    val date=state.date
    val events=state.visible(date)
    Column(Modifier.width(256.dp).fillMaxHeight().verticalScroll(rememberScrollState())) {
        Row(Modifier.height(42.dp),verticalAlignment=Alignment.CenterVertically){Txt(if(date==today())"Un vistazo a hoy"else"Tu día, de un vistazo",15,weight=FontWeight.Medium);Spacer(Modifier.weight(1f));Dot(Color(0xFF91AC73),6.dp)}
        Spacer(Modifier.height(18.dp))
        Card(Modifier.fillMaxWidth()) {
            Column(Modifier.padding(20.dp)) {
                Caption(dayNames[date.dayOfWeek.isoDayNumber-1].uppercase());Spacer(Modifier.height(3.dp))
                Row(verticalAlignment=Alignment.Bottom,horizontalArrangement=Arrangement.spacedBy(9.dp)){Txt(date.day.toString(),42,C.accent,FontWeight.Normal);Column(Modifier.padding(bottom=9.dp)){Txt(monthNames[date.month.number-1].lowercase(),14);Caption(date.year.toString())}}
                Spacer(Modifier.height(9.dp));Rule();Spacer(Modifier.height(15.dp))
                Row(verticalAlignment=Alignment.CenterVertically,horizontalArrangement=Arrangement.spacedBy(7.dp)){Glyph(Symbol.CALENDAR,size=15.dp);Caption("${events.size} ${if(events.size==1)"cita"else"citas"} en tu agenda")}
            }
        }
        Spacer(Modifier.height(25.dp));Txt("EL PLAN DEL DÍA",10,C.muted,FontWeight.Medium);Spacer(Modifier.height(15.dp))
        if(events.isEmpty()) {
            Column(Modifier.fillMaxWidth().padding(vertical=20.dp),horizontalAlignment=Alignment.CenterHorizontally) {Glyph(Symbol.SUN,size=30.dp);Spacer(Modifier.height(12.dp));Txt("Un día por escribir.",13,weight=FontWeight.Medium);Spacer(Modifier.height(4.dp));Caption("Añade tu primera cita.");Spacer(Modifier.height(14.dp));Action("Guardar cita",{onNew(date)},symbol=Symbol.PLUS,primary=false)}
        } else events.forEach {event->
            Row(Modifier.fillMaxWidth().clip(RoundedCornerShape(9.dp)).clickable{onEvent(event)}.padding(vertical=9.dp),horizontalArrangement=Arrangement.spacedBy(13.dp)) {
                Column(horizontalAlignment=Alignment.CenterHorizontally){Dot(event.category.strongColor(),7.dp);Spacer(Modifier.height(6.dp));Box(Modifier.width(1.dp).height(57.dp).background(C.line))}
                Column {Txt(timeLabel(event.start),11,C.muted);Spacer(Modifier.height(4.dp));Txt(event.title,13,weight=FontWeight.Medium,lines=1);Spacer(Modifier.height(5.dp));Row(verticalAlignment=Alignment.CenterVertically,horizontalArrangement=Arrangement.spacedBy(5.dp)){Glyph(Symbol.PIN,size=12.dp);Txt(event.place.ifBlank{"Sin ubicación"},11,C.muted,lines=1)}}
            }
        }
        Spacer(Modifier.height(18.dp));Rule();Spacer(Modifier.height(20.dp))
        Column(Modifier.fillMaxWidth().background(C.accentSoft,RoundedCornerShape(13.dp)).padding(18.dp)) {
            Txt("También hay tiempo\npara una pausa.",17,C.accent,FontWeight.Medium);Spacer(Modifier.height(8.dp));Txt("Los mejores días tienen\nespacio para respirar.",12,C.muted);Spacer(Modifier.height(17.dp));Row(verticalAlignment=Alignment.CenterVertically,horizontalArrangement=Arrangement.spacedBy(6.dp)){Glyph(Symbol.SUN,size=15.dp,color=C.accent);Txt("A tu ritmo, siempre.",11,C.accent)}
        }
        Spacer(Modifier.height(22.dp));Row(verticalAlignment=Alignment.CenterVertically){Avatar("AM",1,26.dp);Spacer(Modifier.width(4.dp));Avatar("LT",2,26.dp);Spacer(Modifier.width(4.dp));Avatar("PR",3,26.dp);Spacer(Modifier.width(10.dp));Caption("Tu equipo, cerca.")}
    }
}

@Composable fun EventEditor(initial: Appointment?, initialDate: LocalDate, onClose: () -> Unit, onSave: (Appointment) -> Unit) {
    var title by remember {mutableStateOf(initial?.title?:"")}
    var date by remember {mutableStateOf((initial?.date?:initialDate).let{"${it.day.toString().padStart(2,'0')}/${it.month.number.toString().padStart(2,'0')}/${it.year}"})}
    var start by remember {mutableStateOf(timeLabel(initial?.start?:720))}
    var duration by remember {mutableStateOf((initial?.let{it.end-it.start}?:45).toString())}
    var category by remember {mutableStateOf(initial?.category?:Category.TEAM)}
    var place by remember {mutableStateOf(initial?.place?:"")}
    var description by remember {mutableStateOf(initial?.description?:"")}
    var attendees by remember {mutableStateOf(initial?.attendees?:"Ana")}
    var reminder by remember {mutableStateOf(initial?.reminder?:true)}
    var error by remember {mutableStateOf<String?>(null)}
    fun save() {
        val parsedDate=runCatching{val p=date.split('/');if(p.size==3)LocalDate(p[2].toInt(),p[1].toInt(),p[0].toInt())else LocalDate.parse(date)}.getOrNull()
        val parsedStart=parseTime(start)
        val minutes=duration.toIntOrNull()
        val parsedEnd=if(parsedStart!=null&&minutes!=null)parsedStart+minutes else null
        error=when{title.isBlank()->"Ponle un título a tu cita.";parsedDate==null->"Escribe una fecha válida, por ejemplo 16/11/2026.";parsedStart==null->"Usa el formato HH:MM para la hora.";minutes==null||minutes<=0||minutes>1440||parsedEnd==null||parsedEnd>1440->"La duración debe ser positiva y la cita debe terminar el mismo día.";else->null}
        if(error==null)onSave(Appointment(initial?.id?:nextEventId(),title.trim(),parsedDate!!,parsedStart!!,parsedEnd!!,category,place.trim(),description.trim(),reminder,attendees.trim()))
    }
    Modal(if(initial==null)"Nueva cita"else"Editar cita", "Haz espacio para tu próximo paso.",onClose,width=570.dp,footer={
        error?.let{Txt(it,12,MaterialTheme.colorScheme.error);Spacer(Modifier.height(12.dp))}
        Row(Modifier.fillMaxWidth(),horizontalArrangement=Arrangement.End){Action("Cancelar",onClose,primary=false);Spacer(Modifier.width(10.dp));Action(if(initial==null)"Guardar cita"else"Guardar cambios",::save,symbol=Symbol.CHECK)}
    }) {
        Field("Título de la cita",title,{title=it},placeholder="¿Qué tienes en mente?")
        Row(horizontalArrangement=Arrangement.spacedBy(12.dp)){Field("Fecha · DD/MM/AAAA",date,{date=it},Modifier.weight(1.5f));Field("Hora · HH:MM",start,{start=it},Modifier.weight(1f));Field("Duración (minutos)",duration,{duration=it},Modifier.weight(1f))}
        Column(verticalArrangement=Arrangement.spacedBy(9.dp)) {Txt("Calendario",12,weight=FontWeight.Medium);Row(horizontalArrangement=Arrangement.spacedBy(9.dp)){Category.entries.forEach{c->FilterChip(selected=category==c,onClick={category=c},label={Txt(c.label,12)},leadingIcon={Dot(c.strongColor(),6.dp)},border=FilterChipDefaults.filterChipBorder(enabled=true,selected=category==c,borderColor=C.line,selectedBorderColor=c.strongColor()),colors=FilterChipDefaults.filterChipColors(selectedContainerColor=if(C.dark)C.soft else c.lightColor()))}}}
        Field("Ubicación o enlace",place,{place=it},placeholder="Sala Horizonte o enlace de videollamada")
        Field("Participantes",attendees,{attendees=it},placeholder="Nombres separados por comas")
        Field("Notas",description,{description=it},placeholder="Añade un poco de contexto…",singleLine=false)
        Row(Modifier.fillMaxWidth().background(C.soft,RoundedCornerShape(10.dp)).padding(horizontal=14.dp,vertical=5.dp),verticalAlignment=Alignment.CenterVertically){Glyph(Symbol.BELL,size=19.dp);Spacer(Modifier.width(12.dp));Column(Modifier.weight(1f)){Txt("Avisarme antes de la cita",12,weight=FontWeight.Medium);Caption("Usa la antelación de tus ajustes")};Switch(reminder,{reminder=it})}
    }
}
private var eventId=1000
private fun nextEventId()=eventId++

@Composable fun EventDetails(item: Appointment, onClose: () -> Unit, onEdit: () -> Unit) {
    Modal(item.title,"Un momento en tu agenda",onClose) {
        CategoryBadge(item.category)
        DetailRow(Symbol.CALENDAR,item.date.fullLabel()+" de ${item.date.year}")
        DetailRow(Symbol.CLOCK,"${timeLabel(item.start)} – ${timeLabel(item.end)}")
        DetailRow(Symbol.PIN,item.place.ifBlank{"Sin ubicación"})
        DetailRow(Symbol.USERS,item.attendees.ifBlank{"Sin participantes"})
        if(item.description.isNotBlank()) {Rule();Txt(item.description,14,C.muted)}
        DetailRow(Symbol.BELL,if(item.reminder)"Recordatorio activado"else"Sin recordatorio")
        Rule();Row(Modifier.fillMaxWidth(),horizontalArrangement=Arrangement.End){Action("Cerrar",onClose,primary=false);Spacer(Modifier.width(10.dp));Action("Editar cita",onEdit,symbol=Symbol.EDIT)}
    }
}
@Composable fun DetailRow(symbol: Symbol,text: String) {Row(verticalAlignment=Alignment.CenterVertically,horizontalArrangement=Arrangement.spacedBy(13.dp)){Glyph(symbol,size=18.dp);Txt(text,13)}}

@Composable fun AlertPopup(item: Appointment, minutes: Int, onClose: () -> Unit) {
    Modal("Tu próxima cita","Un pequeño aviso para llegar con calma.",onClose,width=450.dp){
        Row(Modifier.fillMaxWidth().background(C.accentSoft,RoundedCornerShape(12.dp)).padding(18.dp),horizontalArrangement=Arrangement.spacedBy(14.dp)){Glyph(Symbol.BELL,color=C.accent,size=25.dp);Column{Txt(item.title,17,weight=FontWeight.Medium);Spacer(Modifier.height(5.dp));Caption("${timeLabel(item.start)} · ${item.place.ifBlank{"Sin ubicación"}}");Spacer(Modifier.height(6.dp));Txt("Empieza en $minutes minutos o menos.",12,C.accent)}}
        Action("Entendido",onClose,Modifier.fillMaxWidth(),Symbol.CHECK)
    }
}
@Composable fun NotificationsDialog(state: AgendaState,onClose: () -> Unit) {
    Modal("Tus notificaciones","Todo lo que conviene tener a mano.",onClose,width=460.dp){
        val pending=state.rooms.count{!it.accepted}
        if(pending>0)Row(Modifier.fillMaxWidth().background(C.accentSoft,RoundedCornerShape(10.dp)).clickable{state.page=Page.CHAT;onClose()}.padding(16.dp),horizontalArrangement=Arrangement.spacedBy(12.dp)){Glyph(Symbol.CHAT);Column{Txt("Tienes $pending invitación de equipo",14,weight=FontWeight.Medium);Caption("Abre el chat para aceptarla.")}}
        DetailRow(Symbol.BELL,if(state.alerts)"Recordatorios activos · ${state.reminderMinutes} minutos antes"else"Tus recordatorios están desactivados")
        Caption("Los avisos de las citas aparecen mientras la app está abierta.")
        Action("Configurar alertas",{state.page=Page.SETTINGS;onClose()},Modifier.fillMaxWidth(),primary=false)
    }
}
