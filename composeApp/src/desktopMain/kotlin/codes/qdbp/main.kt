package codes.qdbp

import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application

fun main() = application {
    Window(
        onCloseRequest = ::exitApplication,
        title = "Coloring Pixels Painter",
    ) {
        App()
    }
}