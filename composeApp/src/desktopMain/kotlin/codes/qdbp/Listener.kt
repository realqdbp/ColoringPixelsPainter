package codes.qdbp

import com.github.kwhat.jnativehook.keyboard.NativeKeyEvent
import com.github.kwhat.jnativehook.keyboard.NativeKeyListener
import com.github.kwhat.jnativehook.mouse.NativeMouseEvent
import com.github.kwhat.jnativehook.mouse.NativeMouseInputListener
import java.awt.Point

object Listener : NativeMouseInputListener, NativeKeyListener {
    private var onPointCaptured: ((Point) -> Unit)? = null
    private var onKeyCaptured: ((Int) -> Unit)? = null

    @Volatile
    private var listening = false

    @Volatile
    private var keyListening = false

    override fun nativeMouseClicked(nativeEvent: NativeMouseEvent?) {
        if (!listening) return

        val point = nativeEvent?.point ?: Point(0, 0)
        onPointCaptured?.invoke(point)
        stopListening()
    }

    fun startListening(onPointCaptured: (Point) -> Unit) {
        this.onPointCaptured = onPointCaptured
        listening = true
    }

    private fun stopListening() {
        listening = false
        onPointCaptured = null
    }

    override fun nativeKeyPressed(nativeEvent: NativeKeyEvent?) {
        if ((nativeEvent?.rawCode ?: 'X'.code) == SharedState.pauseKey) {
            synchronized(Clicker.lock) {
                Clicker.paused = !Clicker.paused
                if (!Clicker.paused) {
                    Clicker.lock.notify()
                }
            }
            return
        }

        if (!keyListening) return

        val keyCode = nativeEvent?.rawCode ?: 'X'.code

        onKeyCaptured?.invoke(keyCode)
        stopKeyListening()
    }

    fun startKeyListening(onKeyCaptured: (Int) -> Unit) {
        this.onKeyCaptured = onKeyCaptured
        keyListening = true
    }

    private fun stopKeyListening() {
        keyListening = false
        onKeyCaptured = null
    }

}