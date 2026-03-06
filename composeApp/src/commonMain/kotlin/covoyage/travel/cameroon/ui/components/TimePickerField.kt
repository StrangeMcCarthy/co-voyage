package covoyage.travel.cameroon.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import kotlinx.datetime.*

/**
 * Reusable TimePickerField composable that restricts selecting an hour that has already passed,
 * provided the day previously selected is the current date.
 *
 * @param value The selected time string (e.g. "HH:MM")
 * @param onValueChange Callback when a new time is selected
 * @param selectedDate The currently selected date string (e.g. "YYYY-MM-DD"). Used for validation.
 * @param label The label for the text field
 * @param modifier The modifier to apply to the OutlinedTextField
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TimePickerField(
    value: String,
    onValueChange: (String) -> Unit,
    selectedDate: String,
    label: String,
    modifier: Modifier = Modifier
) {
    var showDialog by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf("") }

    // Parse the system time (which acts as the baseline for "Today" operations)
    val now = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault())
    val today = now.date

    // Extract current hour and minute if we already have a previous selection to populate the state initially.
    val initialHour = value.substringBefore(":").toIntOrNull() ?: now.hour
    val initialMinute = value.substringAfter(":").toIntOrNull() ?: now.minute

    val timePickerState = rememberTimePickerState(
        initialHour = initialHour,
        initialMinute = initialMinute,
        is24Hour = true,
    )

    Box(modifier = modifier) {
        OutlinedTextField(
            value = value,
            onValueChange = {}, // Ignored since readOnly = true
            readOnly = true,
            label = { Text(label) },
            trailingIcon = {
                Icon(Icons.Default.Schedule, contentDescription = "Select Time")
            },
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier.fillMaxWidth(),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = MaterialTheme.colorScheme.primary,
                unfocusedBorderColor = MaterialTheme.colorScheme.outline
            ),
        )

        // Overlay Box to catch clicks across the entire TextField surface
        Box(
            modifier = Modifier
                .matchParentSize()
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                    onClick = {
                        errorMessage = "" // Clear any previous errors on open
                        showDialog = true
                    }
                )
        )
    }

    if (showDialog) {
        AlertDialog(
            onDismissRequest = { showDialog = false },
            title = { Text("Select Time") },
            text = {
                Column(horizontalAlignment = androidx.compose.ui.Alignment.CenterHorizontally) {
                    TimePicker(state = timePickerState)

                    if (errorMessage.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = errorMessage,
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        var parsedSelectedDate: LocalDate? = null
                        try {
                            if (selectedDate.isNotBlank()) parsedSelectedDate = LocalDate.parse(selectedDate)
                        } catch (e: Exception) {
                            // Ignored if invalid date format
                        }

                        // Validation: If the chosen date is Today, ensure the time hasn't passed!
                        if (parsedSelectedDate != null && parsedSelectedDate == today) {
                            val currentHour = now.hour
                            val currentMinute = now.minute

                            val chosenHour = timePickerState.hour
                            val chosenMinute = timePickerState.minute

                            if (chosenHour < currentHour || (chosenHour == currentHour && chosenMinute < currentMinute)) {
                                errorMessage = "Please select a future time"
                                return@TextButton // Prevent closing the dialog!
                            }
                        }

                        // Success scenario: Formats numbers as two digits, e.g., "08:05" instead of "8:5"
                        val formattedHour = timePickerState.hour.toString().padStart(2, '0')
                        val formattedMinute = timePickerState.minute.toString().padStart(2, '0')

                        onValueChange("$formattedHour:$formattedMinute")
                        showDialog = false
                    }
                ) {
                    Text("OK")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}
