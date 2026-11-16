package studio.nexo

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import kotlinx.datetime.*
import kotlin.time.Clock

expect fun playAlertSound()

val monthNames = listOf("Enero", "Febrero", "Marzo", "Abril", "Mayo", "Junio", "Julio", "Agosto", "Septiembre", "Octubre", "Noviembre", "Diciembre")
val dayNames = listOf("Lunes", "Martes", "Miércoles", "Jueves", "Viernes", "Sábado", "Domingo")
fun today(): LocalDate = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).date
fun LocalDate.plusDays(days: Int): LocalDate = plus(days, DateTimeUnit.DAY)
fun LocalDate.monthStart(): LocalDate = LocalDate(year, month, 1)
fun LocalDate.weekStart(): LocalDate = plusDays(1 - dayOfWeek.isoDayNumber)
fun LocalDate.fullLabel() = "${dayNames[dayOfWeek.isoDayNumber - 1]}, $day ${monthNames[month.number - 1].lowercase()}"
fun LocalDate.monthLabel() = "${monthNames[month.number - 1]} $year"
fun timeLabel(minutes: Int) = "${(minutes / 60).toString().padStart(2, '0')}:${(minutes % 60).toString().padStart(2, '0')}"
fun parseTime(value: String): Int? {
    val parts = value.split(":")
    if (parts.size != 2) return null
    val hour = parts[0].toIntOrNull() ?: return null
    val minute = parts[1].toIntOrNull() ?: return null
    return if (hour in 0..23 && minute in 0..59) hour * 60 + minute else null
}
fun validPassword(value: String) = value.length >= 8 && value.any(Char::isDigit) && value.any { !it.isLetterOrDigit() && !it.isWhitespace() } && value.none(Char::isWhitespace)

enum class Page(val label: String) { AGENDA("Agenda"), CHAT("Equipo"), SETTINGS("Ajustes") }
enum class CalendarView(val label: String) { MONTH("Mes"), WEEK("Semana"), DAY("Día") }
enum class Category(val label: String) { TEAM("Equipo"), PROJECT("Proyecto"), PERSONAL("Personal") }
data class Appointment(
    val id: Int, val title: String, val date: LocalDate,
    val start: Int, val end: Int, val category: Category = Category.TEAM,
    val place: String = "", val description: String = "", val reminder: Boolean = true,
    val attendees: String = "Ana, Bruno, Carla"
)
data class Colleague(val name: String, val initials: String, val role: String)
val colleagues = listOf(Colleague("Bruno Ruiz", "BR", "Desarrollo"), Colleague("Carla Soler", "CS", "Diseño"), Colleague("Diego Vidal", "DV", "Producto"), Colleague("Elena Gil", "EG", "Marketing"))
data class ChatMessage(val author: String, val text: String, val time: String, val mine: Boolean = false)
class Room(val id: Int, val name: String, val description: String, members: List<String>, var invited: Boolean = false) {
    val members = mutableStateListOf<String>().apply { addAll(members) }
    val pending = mutableStateListOf<String>()
    val messages = mutableStateListOf<ChatMessage>()
    var accepted by mutableStateOf(!invited)
}

class AgendaState {
    var loggedIn by mutableStateOf(false)
    var page by mutableStateOf(Page.AGENDA)
    var view by mutableStateOf(CalendarView.MONTH)
    var date by mutableStateOf(today())
    var name by mutableStateOf("Ana Martín")
    var password by mutableStateOf("Demo2026!")
    var dark by mutableStateOf(false)
    var accent by mutableStateOf(0)
    var alerts by mutableStateOf(true)
    var sound by mutableStateOf(true)
    var modalAlerts by mutableStateOf(true)
    var reminderMinutes by mutableStateOf(5)
    var query by mutableStateOf("")
    val categories = mutableStateListOf(*Category.entries.toTypedArray())
    val appointments = mutableStateListOf<Appointment>()
    val rooms = mutableStateListOf<Room>()
    var selectedRoom by mutableStateOf(1)
    var activeAlert by mutableStateOf<Appointment?>(null)
    var toast by mutableStateOf<String?>(null)
    private var nextId = 100
    val firedAlerts = mutableSetOf<String>()
    init {
        val monday = today().weekStart()
        appointments.addAll(listOf(
            Appointment(1, "Reunión de equipo", monday, 10 * 60, 10 * 60 + 45, Category.TEAM, "Sala Nexo", "Compartimos avances, prioridades y bloqueos de la semana."),
            Appointment(2, "Revisión de propuestas", monday, 12 * 60, 12 * 60 + 45, Category.PROJECT, "Sala Nexo", "Revisión de las nuevas pantallas con el equipo de diseño."),
            Appointment(3, "Planificar la semana", monday, 16 * 60, 16 * 60 + 45, Category.PERSONAL, "Sala Nexo", "Un bloque sin reuniones para trabajar con calma.", attendees = "Ana"),
            Appointment(4, "Sprint planning", monday.plusDays(1), 10 * 60, 11 * 60, Category.TEAM, "Sala Nexo"),
            Appointment(5, "Revisión de prototipos", monday.plusDays(2), 11 * 60, 12 * 60, Category.PROJECT, "Figma · Google Meet"),
            Appointment(6, "Café con Carla", monday.plusDays(3), 9 * 60, 9 * 60 + 30, Category.PERSONAL, "La cocina"),
            Appointment(7, "Demo de producto", monday.plusDays(4), 12 * 60, 13 * 60, Category.PROJECT, "Sala Nexo"),
            Appointment(8, "Retrospectiva", monday.plusDays(-3), 12 * 60, 13 * 60, Category.TEAM, "Google Meet"),
            Appointment(9, "Ideas para noviembre", monday.plusDays(-7), 10 * 60, 11 * 60, Category.PROJECT, "Sala Creativa"),
            Appointment(10, "Reunión con marketing", monday.plusDays(8), 11 * 60, 12 * 60, Category.TEAM, "Sala Nexo"),
            Appointment(11, "Entrega de propuesta", monday.plusDays(10), 10 * 60, 11 * 60, Category.PROJECT, "Google Meet")
        ))
        rooms.add(Room(1, "Equipo de diseño", "El punto de encuentro de Nexo", listOf("Ana Martín", "Bruno Ruiz", "Carla Soler")).apply {
            messages.addAll(listOf(
                ChatMessage("Bruno Ruiz", "¡Buenos días! ¿Revisamos las propuestas a las 12 en Sala Nexo?", "09:12"),
                ChatMessage("Carla Soler", "¡Sí! Ya he dejado los prototipos preparados para la revisión.", "09:14"),
                ChatMessage("Ana Martín", "Perfecto, acabo de añadir la cita a mi agenda. ¡Nos vemos allí!", "09:16", true),
                ChatMessage("Diego Vidal", "Perfecto, llevo las prioridades del sprint. ¡Hasta ahora!", "09:18")
            ))
        })
        rooms.add(Room(2, "Producto", "Un espacio para construir juntos", listOf("Ana Martín", "Bruno Ruiz", "Diego Vidal")))
        rooms.add(Room(3, "Lanzamiento de noviembre", "Carla te ha invitado a una nueva sala.", listOf("Carla Soler", "Elena Gil"), invited = true))
    }
    fun id() = nextId++
    fun visible(date: LocalDate) = appointments.filter { it.date == date && it.category in categories && (query.isBlank() || "${it.title} ${it.place} ${it.description}".contains(query, true)) }.sortedBy { it.start }
    fun save(appointment: Appointment) {
        val index = appointments.indexOfFirst { it.id == appointment.id }
        if (index < 0) appointments.add(appointment) else appointments[index] = appointment
        toast = if (index < 0) "Cita creada. Ya está en tu agenda." else "Cambios guardados en tu agenda."
    }
    fun move(direction: Int) {
        date = when(view) {
            CalendarView.MONTH -> date.monthStart().plus(direction, DateTimeUnit.MONTH)
            CalendarView.WEEK -> date.plusDays(direction * 7)
            CalendarView.DAY -> date.plusDays(direction)
        }
    }
    fun checkAlerts() {
        if (!loggedIn || !alerts) return
        val now = Clock.System.now()
        appointments.filter { it.reminder }.forEach { item ->
            val start = LocalDateTime(item.date, LocalTime(item.start / 60, item.start % 60)).toInstant(TimeZone.currentSystemDefault())
            val remaining = (start - now).inWholeSeconds
            val key = "${item.id}|${item.date}|${item.start}|$reminderMinutes"
            if (remaining in 1..(reminderMinutes * 60).toLong() && firedAlerts.add(key)) {
                if (sound) playAlertSound()
                if (modalAlerts) activeAlert = item else toast = "En $reminderMinutes min: ${item.title}"
            }
        }
    }
}
