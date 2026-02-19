package covoyage.travel.cameroon.ui.chat

import cafe.adriel.voyager.core.model.ScreenModel
import cafe.adriel.voyager.core.model.screenModelScope
import covoyage.travel.cameroon.data.model.ChatInput
import covoyage.travel.cameroon.data.model.ChatMessage
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json

data class ChatUiState(
    val messages: List<ChatMessage> = emptyList(),
    val inputText: String = "",
    val isLoading: Boolean = false,
    val error: String = "",
)

class ChatScreenModel(
    private val bookingId: String,
    private val currentUserId: String,
    private val currentUserName: String,
    private val serverBaseUrl: String = "http://10.0.2.2:8080",
) : ScreenModel {

    private val _uiState = MutableStateFlow(ChatUiState())
    val uiState: StateFlow<ChatUiState> = _uiState.asStateFlow()

    private val httpClient = HttpClient {
        install(ContentNegotiation) {
            json(Json { ignoreUnknownKeys = true })
        }
    }

    fun loadHistory() {
        screenModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            try {
                val response = httpClient.get("$serverBaseUrl/api/chat/$bookingId")
                val messages = response.body<List<ChatMessage>>()
                _uiState.value = _uiState.value.copy(
                    messages = messages,
                    isLoading = false,
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = "Failed to load messages: ${e.message}",
                )
            }
        }
    }

    fun updateInput(text: String) {
        _uiState.value = _uiState.value.copy(inputText = text)
    }

    fun sendMessage() {
        val text = _uiState.value.inputText.trim()
        if (text.isBlank()) return

        screenModelScope.launch {
            try {
                // Optimistic local add
                val optimisticMsg = ChatMessage(
                    id = "local-${System.currentTimeMillis()}",
                    bookingId = bookingId,
                    senderId = currentUserId,
                    senderName = currentUserName,
                    text = text,
                    timestamp = kotlinx.datetime.Clock.System.now().toString(),
                )
                _uiState.value = _uiState.value.copy(
                    messages = _uiState.value.messages + optimisticMsg,
                    inputText = "",
                )

                // POST via REST (simpler than WebSocket for initial impl)
                val response = httpClient.post("$serverBaseUrl/api/chat/$bookingId/send") {
                    setBody(ChatInput(
                        senderId = currentUserId,
                        senderName = currentUserName,
                        text = text,
                    ))
                    contentType(io.ktor.http.ContentType.Application.Json)
                }

                // Refresh to get server-assigned IDs
                loadHistory()
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    error = "Failed to send: ${e.message}",
                )
            }
        }
    }
}
