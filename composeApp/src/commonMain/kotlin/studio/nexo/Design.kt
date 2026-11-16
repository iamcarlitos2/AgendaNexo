package studio.nexo

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

data class Palette(val bg: Color, val surface: Color, val soft: Color, val ink: Color, val muted: Color, val line: Color, val accent: Color, val accentSoft: Color, val dark: Boolean)
val LocalPalette = staticCompositionLocalOf { lightPalette(0) }
val C: Palette @Composable get() = LocalPalette.current
val accents = listOf(Color(0xFF5157DB), Color(0xFF188270), Color(0xFFC15543))
fun lightPalette(accent: Int) = Palette(Color(0xFFF6F7FB), Color.White, Color(0xFFEDEDFE), Color(0xFF19233C), Color(0xFF626D84), Color(0xFFE2E5EF), accents[accent], listOf(Color(0xFFEDEDFE), Color(0xFFE6F3EF), Color(0xFFFBECE8))[accent], false)
fun darkPalette(accent: Int) = Palette(Color(0xFF101522), Color(0xFF1C2435), Color(0xFF252E46), Color(0xFFF0F3FF), Color(0xFFB2BDD3), Color(0xFF34405A), listOf(Color(0xFFA3A7FF), Color(0xFF70CDB8), Color(0xFFEF9D8D))[accent], Color(0xFF2B3155), true)

@Composable fun NexoTheme(dark: Boolean, accent: Int, content: @Composable () -> Unit) {
    val p = if (dark) darkPalette(accent) else lightPalette(accent)
    CompositionLocalProvider(LocalPalette provides p) {
        MaterialTheme(colorScheme = if (dark) darkColorScheme(primary = p.accent, secondary = p.accent, primaryContainer = p.accentSoft, secondaryContainer = p.accentSoft, onSecondaryContainer = p.accent, background = p.bg, surface = p.surface, onSurface = p.ink, outline = p.line) else lightColorScheme(primary = p.accent, secondary = p.accent, primaryContainer = p.accentSoft, secondaryContainer = p.accentSoft, onSecondaryContainer = p.accent, background = p.bg, surface = p.surface, onSurface = p.ink, outline = p.line), typography = Typography(bodyLarge = androidx.compose.ui.text.TextStyle(fontFamily = FontFamily.SansSerif, fontSize = 14.sp)), content = content)
    }
}

@Composable fun Txt(text: String, size: Int = 14, color: Color = C.ink, weight: FontWeight = FontWeight.Normal, modifier: Modifier = Modifier, lines: Int = Int.MAX_VALUE) {
    Text(text, modifier, color, fontSize = size.sp, fontWeight = weight, maxLines = lines, overflow = TextOverflow.Ellipsis, lineHeight = (size * 1.4).sp)
}
@Composable fun Caption(text: String, modifier: Modifier = Modifier) = Txt(text, 12, C.muted, modifier = modifier)
@Composable fun Rule(modifier: Modifier = Modifier) = HorizontalDivider(modifier, color = C.line)
@Composable fun Dot(color: Color, size: Dp = 7.dp) { Box(Modifier.size(size).background(color, CircleShape)) }
@Composable fun Avatar(initials: String, index: Int = 0, size: Dp = 34.dp) {
    val colors = listOf(Color(0xFFE8EEDB), Color(0xFFE9E4F1), Color(0xFFF6E5D7), Color(0xFFDAEAE5), Color(0xFFDDE7F4))
    Box(Modifier.size(size).background(colors[index % colors.size], CircleShape), contentAlignment = Alignment.Center) {
        Txt(initials, if (size < 32.dp) 10 else 12, Color(0xFF425445), FontWeight.SemiBold)
    }
}

enum class Symbol { CALENDAR, CHAT, SETTINGS, PLUS, LEFT, RIGHT, SEARCH, BELL, SUN, MOON, CHECK, CLOSE, ARROW, CLOCK, PIN, USERS, LOGOUT, LOCK, MAIL, EYE, SEND, INFO, EDIT, GRID }
@Composable fun Glyph(symbol: Symbol, modifier: Modifier = Modifier, color: Color = C.muted, size: Dp = 20.dp) {
    Canvas(modifier.size(size)) {
        val s = this.size.width / 24f
        fun ln(x: Float, y: Float, x2: Float, y2: Float) = drawLine(color, Offset(x * s, y * s), Offset(x2 * s, y2 * s), 1.6f * s, StrokeCap.Round)
        fun path(vararg points: Pair<Float, Float>, closed: Boolean = false) {
            val p = Path().apply { moveTo(points[0].first * s, points[0].second * s); points.drop(1).forEach { lineTo(it.first * s, it.second * s) }; if(closed) close() }
            drawPath(p, color, style = Stroke(1.6f * s, cap = StrokeCap.Round))
        }
        fun circle(x: Float, y: Float, r: Float) = drawCircle(color, r * s, Offset(x * s, y * s), style = Stroke(1.6f * s))
        when(symbol) {
            Symbol.PLUS -> { ln(12f,5f,12f,19f);ln(5f,12f,19f,12f) }
            Symbol.LEFT -> path(14f to 6f,8f to 12f,14f to 18f)
            Symbol.RIGHT -> path(10f to 6f,16f to 12f,10f to 18f)
            Symbol.CLOSE -> {ln(6f,6f,18f,18f);ln(18f,6f,6f,18f)}
            Symbol.CHECK -> path(5f to 12f,10f to 17f,19f to 7f)
            Symbol.ARROW -> {ln(4f,12f,20f,12f);path(14f to 6f,20f to 12f,14f to 18f)}
            Symbol.CALENDAR -> {path(4f to 5f,20f to 5f,20f to 21f,4f to 21f,closed=true);ln(4f,10f,20f,10f);ln(8f,3f,8f,7f);ln(16f,3f,16f,7f);circle(9f,15f,.6f);circle(15f,15f,.6f)}
            Symbol.CHAT -> path(4f to 4f,20f to 4f,20f to 16f,10f to 16f,4f to 21f,closed=true)
            Symbol.SEARCH -> {circle(10f,10f,6f);ln(15f,15f,21f,21f)}
            Symbol.CLOCK -> {circle(12f,12f,9f);path(12f to 6f,12f to 12f,16f to 14f)}
            Symbol.PIN -> {circle(12f,9f,3f);path(12f to 22f,5f to 11f,5f to 6f,8f to 2f,16f to 2f,19f to 6f,19f to 11f,12f to 22f)}
            Symbol.BELL -> {path(5f to 17f,7f to 14f,7f to 8f,9f to 4f,15f to 4f,17f to 8f,17f to 14f,19f to 17f,closed=true);path(10f to 20f,12f to 21f,14f to 20f)}
            Symbol.SUN -> {circle(12f,12f,4f);ln(12f,1f,12f,4f);ln(12f,20f,12f,23f);ln(1f,12f,4f,12f);ln(20f,12f,23f,12f);ln(4f,4f,6f,6f);ln(18f,18f,20f,20f);ln(4f,20f,6f,18f);ln(18f,6f,20f,4f)}
            Symbol.MOON -> path(16f to 3f,10f to 4f,5f to 9f,4f to 14f,7f to 19f,12f to 21f,17f to 20f,21f to 16f,15f to 16f,11f to 12f,11f to 7f,16f to 3f)
            Symbol.USERS -> {circle(9f,7f,3f);path(3f to 21f,3f to 17f,5f to 13f,13f to 13f,15f to 17f,15f to 21f);path(16f to 4f,19f to 5f,19f to 9f,17f to 10f);path(18f to 13f,21f to 16f,21f to 21f)}
            Symbol.MAIL -> {path(3f to 5f,21f to 5f,21f to 19f,3f to 19f,closed=true);path(3f to 6f,12f to 13f,21f to 6f)}
            Symbol.LOCK -> {path(5f to 10f,19f to 10f,19f to 21f,5f to 21f,closed=true);path(8f to 10f,8f to 6f,10f to 3f,14f to 3f,16f to 6f,16f to 10f);ln(12f,14f,12f,17f)}
            Symbol.EYE -> {path(2f to 12f,7f to 6f,17f to 6f,22f to 12f,17f to 18f,7f to 18f,closed=true);circle(12f,12f,3f)}
            Symbol.LOGOUT -> {path(10f to 3f,4f to 3f,4f to 21f,10f to 21f);ln(9f,12f,21f,12f);path(16f to 7f,21f to 12f,16f to 17f)}
            Symbol.SEND -> {path(3f to 3f,22f to 12f,3f to 21f,7f to 12f,closed=true);ln(7f,12f,22f,12f)}
            Symbol.INFO -> {circle(12f,12f,9f);ln(12f,11f,12f,17f);circle(12f,7f,.5f)}
            Symbol.EDIT -> {path(4f to 20f,5f to 14f,16f to 3f,21f to 8f,10f to 19f,4f to 20f);ln(13f,6f,18f,11f)}
            Symbol.GRID -> {path(3f to 3f,10f to 3f,10f to 10f,3f to 10f,closed=true);path(14f to 3f,21f to 3f,21f to 10f,14f to 10f,closed=true);path(3f to 14f,10f to 14f,10f to 21f,3f to 21f,closed=true);path(14f to 14f,21f to 14f,21f to 21f,14f to 21f,closed=true)}
            Symbol.SETTINGS -> {circle(12f,12f,4f);circle(12f,12f,9f);ln(12f,1f,12f,4f);ln(12f,20f,12f,23f);ln(1f,12f,4f,12f);ln(20f,12f,23f,12f)}
        }
    }
}
@Composable fun IconAction(symbol: Symbol, label: String, onClick: () -> Unit, modifier: Modifier = Modifier) {
    IconButton(onClick, modifier.size(36.dp).semantics { contentDescription = label }) { Glyph(symbol) }
}
@Composable fun Action(text: String, onClick: () -> Unit, modifier: Modifier = Modifier, symbol: Symbol? = null, primary: Boolean = true, enabled: Boolean = true) {
    val p = C
    Button(onClick, modifier.height(43.dp), enabled = enabled, shape = RoundedCornerShape(10.dp),
        colors = ButtonDefaults.buttonColors(containerColor = if(primary) p.accent else p.surface, contentColor = if(primary) (if(p.dark) Color(0xFF1D2A24) else Color.White) else p.ink),
        border = if(primary) null else androidx.compose.foundation.BorderStroke(1.dp,p.line), contentPadding = PaddingValues(horizontal = 17.dp)) {
        if(symbol != null) { Glyph(symbol, color = LocalContentColor.current, size = 17.dp);Spacer(Modifier.width(8.dp)) }
        Text(text, fontSize = 13.sp, fontWeight = FontWeight.Medium)
    }
}
@Composable fun Field(label: String, value: String, onValue: (String) -> Unit, modifier: Modifier = Modifier, placeholder: String = "", singleLine: Boolean = true, visualTransformation: VisualTransformation = VisualTransformation.None, trailing: @Composable (() -> Unit)? = null, error: Boolean = false) {
    Column(modifier, verticalArrangement = Arrangement.spacedBy(7.dp)) {
        if(label.isNotEmpty()) Txt(label, 12, weight = FontWeight.Medium)
        OutlinedTextField(value, onValue, Modifier.fillMaxWidth().semantics { contentDescription = label.ifBlank { placeholder } }, placeholder = { Txt(placeholder, 13, C.muted) }, singleLine = singleLine, textStyle = androidx.compose.ui.text.TextStyle(fontSize = 14.sp, color = C.ink), shape = RoundedCornerShape(10.dp), visualTransformation = visualTransformation, trailingIcon = trailing, isError = error,
            colors = OutlinedTextFieldDefaults.colors(unfocusedBorderColor = C.line, focusedBorderColor = C.accent, unfocusedContainerColor = C.surface, focusedContainerColor = C.surface, cursorColor = C.accent))
    }
}
@Composable fun Card(modifier: Modifier = Modifier, content: @Composable ColumnScope.() -> Unit) {
    Column(modifier.background(C.surface, RoundedCornerShape(16.dp)).border(1.dp, C.line, RoundedCornerShape(16.dp)), content = content)
}
@Composable fun Brand(compact: Boolean = false) {
    val logoInk = if(C.dark) C.bg else Color.White
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
        Box(Modifier.size(34.dp).background(C.accent, RoundedCornerShape(10.dp)), contentAlignment = Alignment.Center) {
            Canvas(Modifier.size(20.dp)) {
                val w = size.width; val h = size.height
                val p = Path().apply {moveTo(w*.15f,h*.82f);lineTo(w*.15f,h*.18f);lineTo(w*.38f,h*.18f);lineTo(w*.77f,h*.71f);lineTo(w*.77f,h*.18f)}
                drawPath(p, logoInk, style = Stroke(w*.17f, cap = StrokeCap.Round))
            }
        }
        if(!compact) Txt("nexo studio", 20, C.ink, FontWeight.Bold)
    }
}
fun Category.lightColor() = when(this) { Category.TEAM -> Color(0xFFEDEDFE); Category.PROJECT -> Color(0xFFE6F3EF); Category.PERSONAL -> Color(0xFFFBECE8) }
fun Category.strongColor() = when(this) { Category.TEAM -> Color(0xFF5157DB); Category.PROJECT -> Color(0xFF188270); Category.PERSONAL -> Color(0xFFC15543) }
@Composable fun CategoryBadge(category: Category) {
    Row(Modifier.background(if(C.dark) category.strongColor().copy(alpha=.2f) else category.lightColor(), RoundedCornerShape(6.dp)).padding(horizontal = 9.dp, vertical = 4.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
        Dot(category.strongColor(),5.dp);Txt(category.label,11,if(C.dark) C.ink else category.strongColor(), FontWeight.Medium)
    }
}
