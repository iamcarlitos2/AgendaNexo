@file:OptIn(kotlin.js.ExperimentalWasmJsInterop::class)

package studio.nexo

import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.window.ComposeViewport

@OptIn(ExperimentalComposeUiApi::class)
fun main() {
    removeLoader()
    ComposeViewport { App() }
}

@JsFun("() => { document.getElementById('loading')?.remove(); }")
private external fun removeLoader()

@JsFun("""() => {
  try {
    const ctx = new (window.AudioContext || window.webkitAudioContext)();
    ctx.resume().then(() => {
      const osc = ctx.createOscillator(), gain = ctx.createGain();
      osc.connect(gain); gain.connect(ctx.destination); osc.frequency.value = 740;
      gain.gain.setValueAtTime(0.12, ctx.currentTime);
      gain.gain.exponentialRampToValueAtTime(0.001, ctx.currentTime + 0.45);
      osc.start(); osc.stop(ctx.currentTime + 0.45);
      osc.onended = () => ctx.close();
    }).catch(() => ctx.close());
  } catch (_) {}
}""")
private external fun browserBeep()

actual fun playAlertSound() = browserBeep()
