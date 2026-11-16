package studio.nexo

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import kotlinx.datetime.*
import kotlin.time.Clock

@Composable fun ChatScreen(state: AgendaState) {
    var createRoom by remember {mutableStateOf(false)}
    var invite by remember {mutableStateOf(false)}
    val room=state.rooms.first {it.id==state.selectedRoom}
    var message by remember(room.id) {mutableStateOf("")}
    val listState=rememberLazyListState()
    LaunchedEffect(room.id,room.messages.size) {if(room.messages.isNotEmpty())listState.animateScrollToItem(room.messages.lastIndex)}
    Column(Modifier.fillMaxSize().padding(horizontal=28.dp,vertical=26.dp)) {
        PageHeading("NEXO STUDIO","Tu equipo","Conversaciones que nos acercan."){Action("Nueva sala",{createRoom=true},symbol=Symbol.PLUS)}
        Spacer(Modifier.height(25.dp))
        Card(Modifier.fillMaxWidth().weight(1f)) {
            Row(Modifier.fillMaxSize()) {
                Column(Modifier.width(245.dp).fillMaxHeight().background(C.surface)) {
                    Row(Modifier.fillMaxWidth().padding(21.dp),verticalAlignment=Alignment.CenterVertically){Txt("Tus conversaciones",13,weight=FontWeight.Medium);Spacer(Modifier.weight(1f));Txt(state.rooms.count{it.accepted}.toString(),11,C.muted)}
                    Rule()
                    Column(Modifier.padding(10.dp),verticalArrangement=Arrangement.spacedBy(5.dp)) {
                        state.rooms.filter{it.accepted}.forEach {r->
                            Row(Modifier.fillMaxWidth().clip(RoundedCornerShape(10.dp)).background(if(room.id==r.id)C.accentSoft else Color.Transparent).clickable{state.selectedRoom=r.id}.padding(12.dp),verticalAlignment=Alignment.CenterVertically,horizontalArrangement=Arrangement.spacedBy(10.dp)) {
                                Box(Modifier.size(34.dp).background(if(room.id==r.id)C.accent else C.soft,RoundedCornerShape(9.dp)),contentAlignment=Alignment.Center){Txt("#",20,if(room.id==r.id){if(C.dark)C.bg else Color.White}else C.muted)}
                                Column(Modifier.weight(1f)){Txt(r.name,12,weight=FontWeight.Medium,lines=1);Spacer(Modifier.height(3.dp));Txt(r.messages.lastOrNull()?.text?:"El comienzo de algo bueno.",10,C.muted,lines=1)}
                            }
                        }
                    }
                    if(state.rooms.any{!it.accepted}) {
                        Spacer(Modifier.height(23.dp));Txt("INVITACIONES",10,C.muted,FontWeight.Medium,Modifier.padding(horizontal=21.dp));Spacer(Modifier.height(12.dp))
                        state.rooms.filter{!it.accepted}.forEach {r->
                            Column(Modifier.padding(horizontal=14.dp).fillMaxWidth().background(C.soft,RoundedCornerShape(10.dp)).padding(13.dp)) {Row(verticalAlignment=Alignment.CenterVertically){Glyph(Symbol.MAIL,size=16.dp);Spacer(Modifier.width(8.dp));Txt(r.name,12,weight=FontWeight.Medium)};Spacer(Modifier.height(7.dp));Txt(r.description,11,C.muted);Spacer(Modifier.height(11.dp));Action("Aceptar invitación",{r.accepted=true;r.members.add(state.name);state.selectedRoom=r.id;state.toast="Te has unido a ${r.name}."},Modifier.fillMaxWidth(),symbol=Symbol.CHECK)}
                        }
                    }
                    Spacer(Modifier.weight(1f));Rule();Row(Modifier.padding(18.dp),horizontalArrangement=Arrangement.spacedBy(8.dp)){Glyph(Symbol.LOCK,size=14.dp);Txt("Un espacio para cuidar\nlas conversaciones del equipo.",10,C.muted)}
                }
                Box(Modifier.width(1.dp).fillMaxHeight().background(C.line))
                Column(Modifier.weight(1f).fillMaxHeight()) {
                    Row(Modifier.fillMaxWidth().height(79.dp).padding(horizontal=22.dp),verticalAlignment=Alignment.CenterVertically) {
                        Column(Modifier.weight(1f)){Txt("# ${room.name}",17,weight=FontWeight.Medium);Spacer(Modifier.height(3.dp));Caption("${room.members.size} miembros${if(room.pending.isNotEmpty())" · ${room.pending.size} invitación pendiente"else""}")}
                        Action("Invitar",{invite=true},primary=false,symbol=Symbol.USERS)
                    }
                    Rule()
                    Row(Modifier.fillMaxWidth().background(C.bg).padding(vertical=11.dp),horizontalArrangement=Arrangement.Center){Caption("El principio de una buena conversación")}
                    if(room.messages.isEmpty()) Box(Modifier.weight(1f).fillMaxWidth(),contentAlignment=Alignment.Center) {
                        Column(horizontalAlignment=Alignment.CenterHorizontally){Box(Modifier.size(62.dp).background(C.accentSoft,CircleShape),contentAlignment=Alignment.Center){Glyph(Symbol.CHAT,color=C.accent,size=27.dp)};Spacer(Modifier.height(18.dp));Txt("Este espacio empieza contigo.",20,weight=FontWeight.Medium);Spacer(Modifier.height(7.dp));Caption("Saluda al equipo y comparte tu primera idea.")}
                    } else LazyColumn(Modifier.weight(1f).fillMaxWidth().padding(horizontal=23.dp),state=listState,contentPadding=PaddingValues(vertical=22.dp),verticalArrangement=Arrangement.spacedBy(22.dp)) {
                        itemsIndexed(room.messages) {index,item->
                            Row(Modifier.fillMaxWidth(),horizontalArrangement=if(item.mine)Arrangement.End else Arrangement.Start,verticalAlignment=Alignment.Top) {
                                if(!item.mine){Avatar(item.author.split(" ").take(2).joinToString(""){it.take(1)},index+1,30.dp);Spacer(Modifier.width(10.dp))}
                                Column(Modifier.widthIn(max=420.dp),horizontalAlignment=if(item.mine)Alignment.End else Alignment.Start) {
                                    Row(horizontalArrangement=Arrangement.spacedBy(8.dp),verticalAlignment=Alignment.CenterVertically){Txt(if(item.mine)"Tú"else item.author,11,weight=FontWeight.Medium);Txt(item.time,10,C.muted)}
                                    Spacer(Modifier.height(7.dp));Box(Modifier.background(if(item.mine)C.accentSoft else C.soft,RoundedCornerShape(0.dp,12.dp,12.dp,12.dp)).padding(horizontal=15.dp,vertical=12.dp)){Txt(item.text,13)}
                                }
                            }
                        }
                    }
                    Rule()
                    Column(Modifier.fillMaxWidth().padding(horizontal=22.dp,vertical=15.dp)) {
                        Row(Modifier.fillMaxWidth(),verticalAlignment=Alignment.CenterVertically,horizontalArrangement=Arrangement.spacedBy(10.dp)) {
                            Field("",message,{message=it},Modifier.weight(1f),placeholder="Escribe a #${room.name.lowercase()}…")
                            Action("Enviar",{
                                if(message.isNotBlank()){
                                    val now=Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault())
                                    room.messages.add(ChatMessage(state.name,message.trim(),timeLabel(now.hour*60+now.minute),true));message=""
                                }
                            },symbol=Symbol.SEND,enabled=message.isNotBlank())
                        }
                        Spacer(Modifier.height(8.dp));Caption("El historial se mantiene durante esta sesión. Los mensajes y las salas no se pueden borrar.")
                    }
                }
            }
        }
    }
    if(createRoom) RoomDialog(onClose={createRoom=false}) {name,selected->
        val newRoom=Room(state.id(),name,"Un espacio para compartir con el equipo",listOf(state.name))
        newRoom.pending.addAll(selected)
        state.rooms.add(newRoom);state.selectedRoom=newRoom.id;createRoom=false;state.toast="Sala creada con ${selected.size} invitaciones pendientes."
    }
    if(invite) InviteDialog(room,{invite=false}) {selected->room.pending.addAll(selected);invite=false;state.toast="${selected.size} invitaciones añadidas a ${room.name}."}
}

@Composable fun ColleaguePicker(selected: List<String>, onToggle: (String) -> Unit, available: List<Colleague> = colleagues) {
    Column(verticalArrangement=Arrangement.spacedBy(4.dp)) {
        available.forEachIndexed {index,person->Row(Modifier.fillMaxWidth().clip(RoundedCornerShape(9.dp)).clickable{onToggle(person.name)}.padding(10.dp),verticalAlignment=Alignment.CenterVertically,horizontalArrangement=Arrangement.spacedBy(11.dp)) {
            Avatar(person.initials,index+1);Column(Modifier.weight(1f)){Txt(person.name,13,weight=FontWeight.Medium);Caption(person.role)};Checkbox(person.name in selected,{onToggle(person.name)})
        }}
    }
}
@Composable fun RoomDialog(onClose: () -> Unit,onCreate: (String,List<String>) -> Unit) {
    var name by remember {mutableStateOf("")}
    val selected=remember{mutableStateListOf<String>()}
    var error by remember{mutableStateOf<String?>(null)}
    Modal("Un espacio para tu equipo.","Crea una sala e invita a tus compañeros.",onClose,width=480.dp) {
        Field("Nombre de la sala",name,{name=it},placeholder="Por ejemplo, Proyecto Horizonte")
        Txt("Invitar a compañeros",12,weight=FontWeight.Medium)
        ColleaguePicker(selected,{if(it in selected)selected.remove(it)else selected.add(it)})
        Caption("Las invitaciones quedan pendientes de aceptación. Las salas y su historial se conservan durante la sesión.")
        error?.let{Txt(it,12,MaterialTheme.colorScheme.error)}
        Action("Crear sala",{if(name.trim().length<3)error="Escribe un nombre de al menos 3 caracteres."else onCreate(name.trim(),selected.toList())},Modifier.fillMaxWidth(),Symbol.PLUS)
    }
}
@Composable fun InviteDialog(room: Room,onClose: () -> Unit,onInvite: (List<String>) -> Unit) {
    val selected=remember{mutableStateListOf<String>()}
    val available=colleagues.filter{it.name !in room.members && it.name !in room.pending}
    Modal("Las ideas crecen en compañía.","Invitar a #${room.name}",onClose,width=480.dp){
        if(available.isEmpty())Txt("Todo el equipo ya está en esta sala o tiene una invitación pendiente.",14,C.muted)else ColleaguePicker(selected,{if(it in selected)selected.remove(it)else selected.add(it)},available)
        if(room.pending.isNotEmpty()){Rule();Txt("Pendientes de aceptación",12,weight=FontWeight.Medium);Caption(room.pending.joinToString(", "))}
        Action("Enviar invitaciones",{onInvite(selected.toList())},Modifier.fillMaxWidth(),Symbol.MAIL,enabled=selected.isNotEmpty())
    }
}
