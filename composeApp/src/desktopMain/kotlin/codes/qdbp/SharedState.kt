package codes.qdbp

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue

object SharedState {
    var message by mutableStateOf("")
    var longDelay by mutableStateOf(25)
    var shortDelay by mutableStateOf(2)
    var pauseKey by mutableStateOf('X'.code)
    var tgChatId by mutableLongStateOf(1370228954)
}

fun updateMessage(newMessage: String) {
    SharedState.message = newMessage
}