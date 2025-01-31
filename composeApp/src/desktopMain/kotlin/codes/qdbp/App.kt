package codes.qdbp

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Button
import androidx.compose.material.ButtonColors
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.MaterialTheme
import androidx.compose.material.OutlinedTextField
import androidx.compose.material.Text
import androidx.compose.material.TextFieldColors
import androidx.compose.material.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.github.kwhat.jnativehook.GlobalScreen
import org.jetbrains.compose.ui.tooling.preview.Preview
import kotlin.concurrent.thread

val imageX = mutableStateOf(0)
val imageY = mutableStateOf(0)
val colorStart = mutableStateOf(Point(215, 1370))
val colorEnd = mutableStateOf(Point(0, 0))
val visibleColors = mutableStateOf(0) // TODO necessary
val nonSquare = mutableStateOf(false)
val bg = mutableStateOf(Point(0, 0))
val left = mutableStateOf(Point(0, 0))
val top = mutableStateOf(Point(0, 0))
val right = mutableStateOf(Point(0, 0))
val bottom = mutableStateOf(Point(0, 0))

@Composable
@Preview
fun App() {

    thread { // FIXME no thread usage
        GlobalScreen.registerNativeHook()

        GlobalScreen.addNativeMouseListener(Listener)
        GlobalScreen.addNativeMouseMotionListener(Listener)
        GlobalScreen.addNativeKeyListener(Listener)
    }

    MaterialTheme {
        Column(
            Modifier.fillMaxHeight().fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(20.dp, Alignment.Top)
        ) {
            Row { Text("Coloring Pixels Painter", fontSize = 36.sp) }

            coordinateButtonsRow()
            colorButtonsRow()
            canvasDimensionRow()
            delayRow()
            miscOptionsRow()

            Row { Text(SharedState.message, fontSize = 24.sp) }

            Row {
                Button(
                    onClick = {
                        Clicker.start(
                            imageX.value,
                            imageY.value,
                            listOf(colorStart.value, colorEnd.value),
                            visibleColors.value,
                            nonSquare.value,
                            bg.value,
                            arrayOf(left.value, top.value, right.value, bottom.value)
                        )
                    }
                ) {
                    Text("Start Clicker")
                }
            }
        }
    }
}

@Composable
fun coordinateButtonsRow() {
    Row(
        Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(20.dp, Alignment.CenterHorizontally)
    ) {
        Button(
            onClick = { Listener.startListening { left.value = Point(it.x, it.y) } },
            colors = btnColorsBy { left.isZero() }
        ) {
            Text(if (left.isZero()) "Set Approx. Left" else "Left: ${left.value.x} | ${left.value.y}")
        }

        Button(
            onClick = { Listener.startListening { top.value = Point(it.x, it.y) } },
            colors = btnColorsBy { top.isZero() }
        ) {
            Text(if (top.isZero()) "Set Approx. Top" else "Top: ${top.value.x} | ${top.value.y}")
        }

        Button(
            onClick = { Listener.startListening { right.value = Point(it.x, it.y) } },
            colors = btnColorsBy { right.isZero() }
        ) {
            Text(if (right.isZero()) "Set Approx. Right" else "Right: ${right.value.x} | ${right.value.y}")
        }

        Button(
            onClick = { Listener.startListening { bottom.value = Point(it.x, it.y) } },
            colors = btnColorsBy { bottom.isZero() }
        ) {
            Text(if (bottom.isZero()) "Set Approx. Bottom" else "Bottom: ${bottom.value.x} | ${bottom.value.y}")
        }

        Button(
            onClick = { Listener.startListening { bg.value = Point(it.x, it.y) } },
            colors = btnColorsBy { bg.isZero() }
        ) {
            Text(if (bg.isZero()) "Set Background" else "Background: ${bg.value.x} | ${bg.value.y}")
        }
    }
}

@Composable
fun colorButtonsRow() {
    Row(
        Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(20.dp, Alignment.CenterHorizontally)
    ) {
        Button(
            onClick = { Listener.startListening { colorStart.value = Point(it.x, it.y) } },
            colors = btnColorsBy { colorStart.isZero() }
        ) {
            Text(if (colorStart.isZero()) "Set first Color Position" else "First Color: ${colorStart.value.x} | ${colorStart.value.y}")
        }

        Button(
            onClick = { Listener.startListening { colorEnd.value = Point(it.x, it.y) } },
            colors = btnColorsBy { colorEnd.isZero() }
        ) {
            Text(if (colorEnd.isZero()) "Set last visible Color Position" else "First Color: ${colorEnd.value.x} | ${colorEnd.value.y}")
        }

        OutlinedTextField(
            value = "${visibleColors.value}",
            onValueChange = { visibleColors.value = it.toIntOrNull() ?: 0 },
            label = { Text("Max Colors displayed on Screen") },
            colors = textColorsBy { visibleColors.value <= 0 }
        )
    }
}

@Composable
fun canvasDimensionRow() {
    Row(
        Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(20.dp, Alignment.CenterHorizontally)
    ) {
        OutlinedTextField(
            value = "${imageX.value}",
            onValueChange = { imageX.value = it.toIntOrNull() ?: 0 },
            label = { Text("X of the Image") },
            colors = textColorsBy { imageX.value <= 0 }
        )

        OutlinedTextField(
            value = "${imageY.value}",
            onValueChange = { imageY.value = it.toIntOrNull() ?: 0 },
            label = { Text("Y of the Image") },
            colors = textColorsBy { imageY.value <= 0 }
        )
    }
}

@Composable
fun delayRow() {
    Row(
        Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(20.dp, Alignment.CenterHorizontally)
    ) {
        OutlinedTextField(
            value = "${SharedState.longDelay}",
            onValueChange = { SharedState.longDelay = it.toIntOrNull() ?: 0 },
            label = { Text("Long Delay") },
            colors = textColorsBy { SharedState.longDelay <= 0 }
        )

        OutlinedTextField(
            value = "${SharedState.shortDelay}",
            onValueChange = { SharedState.shortDelay = it.toIntOrNull() ?: 0 },
            label = { Text("Short Delay") },
            colors = textColorsBy { SharedState.shortDelay <= 0 }
        )
    }
}

@Composable
fun miscOptionsRow() {
    Row(
        Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(20.dp, Alignment.CenterHorizontally)
    ) {
        Button(
            onClick = { Listener.startKeyListening { SharedState.pauseKey = it } },
            colors = btnColorsBy { SharedState.pauseKey == 0 } // idk what 0 is, I hope it's no normal key
        ) {
            Text(
                """
                    Click to set Pause Key
                    Current: ${SharedState.pauseKey.toChar()}
                """.trimIndent()
            )
        }

        Button(onClick = { nonSquare.value = !nonSquare.value }) {
            Text(if (nonSquare.value) "NonSquare" else "Square")
        }
    }
}

private val TODO = Color(0xffa00e15)
private val DONE = Color(0xff66ee46)
@Composable
fun btnColorsBy(func: () -> Boolean): ButtonColors {
    return ButtonDefaults.buttonColors(
        contentColor = if (func.invoke()) Color.White else Color.Black,
        backgroundColor = if (func.invoke()) TODO else DONE
    )
}

@Composable
fun textColorsBy(func: () -> Boolean): TextFieldColors {
    return TextFieldDefaults.outlinedTextFieldColors(
        unfocusedLabelColor = if (func.invoke()) TODO else DONE,
        focusedLabelColor = if (func.invoke()) TODO else DONE,
        unfocusedBorderColor = if (func.invoke()) TODO else DONE,
        focusedBorderColor = if (func.invoke()) TODO else DONE
    )
}

fun MutableState<Point>.isZero() = this.value.x == 0 && this.value.y == 0