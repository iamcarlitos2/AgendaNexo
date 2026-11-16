package studio.nexo

import kotlinx.datetime.LocalDate
import kotlin.test.*

class AgendaTest {
    @Test fun passwordRequiresLengthNumberAndSymbol() {
        assertTrue(validPassword("Demo2026!"))
        assertTrue(validPassword("1234567!"))
        listOf("Nexo26!", "Nexopass!", "Nexo2026", "Nexo 26!", "Nexo2026 ").forEach {assertFalse(validPassword(it),it)}
    }
    @Test fun timeParsingRejectsInvalidRanges() {
        assertEquals(570,parseTime("09:30"))
        assertEquals(0,parseTime("00:00"))
        assertEquals(1439,parseTime("23:59"))
        listOf("24:00","10:60","-1:00","hola","12:30:00").forEach{assertNull(parseTime(it))}
    }
    @Test fun calendarCrossesYearsAndLeapDays() {
        assertEquals(LocalDate(2027,1,1),LocalDate(2026,12,31).plusDays(1))
        assertEquals(LocalDate(2028,2,29),LocalDate(2028,2,28).plusDays(1))
        assertEquals(LocalDate(2026,10,26),LocalDate(2026,11,1).weekStart())
        assertEquals(LocalDate(2026,11,16),LocalDate(2026,11,16).weekStart())
    }
    @Test fun editingAnAppointmentKeepsItsIdentity() {
        val state=AgendaState()
        val original=state.appointments.first()
        val size=state.appointments.size
        state.save(original.copy(title="Nueva reunión",start=600,end=660))
        assertEquals(size,state.appointments.size)
        assertEquals("Nueva reunión",state.appointments.first{it.id==original.id}.title)
    }
    @Test fun categoryAndTextFiltersWorkTogether() {
        val state=AgendaState()
        state.appointments.clear()
        val date=LocalDate(2026,11,16)
        state.save(Appointment(42,"Plan de diseño",date,540,600,Category.PROJECT,"Sala Azul"))
        state.query="AZUL"
        assertEquals(1,state.visible(date).size)
        state.categories.remove(Category.PROJECT)
        assertTrue(state.visible(date).isEmpty())
        state.categories.add(Category.PROJECT)
        state.query="inexistente"
        assertTrue(state.visible(date).isEmpty())
    }
    @Test fun overlappingAppointmentsUseSeparateLanes() {
        val date=LocalDate(2026,11,16)
        val a=Appointment(1,"A",date,540,600)
        val b=Appointment(2,"B",date,570,660)
        val c=Appointment(3,"C",date,600,630)
        val d=Appointment(4,"D",date,660,720)
        val lanes=layoutEvents(listOf(a,b,c,d))
        assertEquals(listOf(0,1,0,0),lanes.map{it.lane})
        assertEquals(listOf(2,2,2,1),lanes.map{it.total})
    }
}
