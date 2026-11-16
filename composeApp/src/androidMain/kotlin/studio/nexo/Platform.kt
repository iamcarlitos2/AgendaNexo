package studio.nexo

import android.media.AudioManager
import android.media.ToneGenerator
import android.os.Handler
import android.os.Looper

actual fun playAlertSound() {
    runCatching {
        val tone = ToneGenerator(AudioManager.STREAM_NOTIFICATION, 70)
        tone.startTone(ToneGenerator.TONE_PROP_BEEP, 250)
        Handler(Looper.getMainLooper()).postDelayed({ tone.release() }, 400)
    }
}
