package covoyage.travel.cameroon.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import kotlinx.datetime.*

/**
 * Reusable DatePickerField composable that restricts selecting past dates.
 * It displays an OutlinedTextField that reads the selected date but cannot be typed into directly.
 * Tapping it opens the Material 3 DatePickerDialog.
 *
 * @param value The selected date string (e.g. "YYYY-MM-DD")
 * @param onValueChange Callback when a new date is selected
 * @param label The label for the text field
 * @param modifier The modifier to apply to the OutlinedTextField
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DatePickerField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    modifier: Modifier = Modifier
) {
    var showDialog by remember { mutableStateOf(false) }

    // Today's UTC milliseconds
    val today = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault())
    val todayMidnight = LocalDateTime(today.year, today.month, today.dayOfMonth, 0, 0)
    val todayMillis = todayMidnight.toInstant(TimeZone.UTC).toEpochMilliseconds()

    // Initialize the state, restricting to dates >= todayMillis.
    // DatePicker expects UTC milliseconds.
    val datePickerState = rememberDatePickerState(
        selectableDates = object : SelectableDates {
            override fun isSelectableDate(utcTimeMillis: Long): Boolean {
                return utcTimeMillis >= todayMillis
            }
        }
    )

    // A read-only outlined text field that intercepts clicks to show the dialog
    Box(modifier = modifier) {
        OutlinedTextField(
            value = value,
            onValueChange = {}, // Ignored since readOnly = true
            readOnly = true,
            label = { Text(label) },
            trailingIcon = {
                Icon(Icons.Default.CalendarToday, contentDescription = "Select Date")
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
                    onClick = { showDialog = true }
                )
        )
    }

    if (showDialog) {
        DatePickerDialog(
            onDismissRequest = { showDialog = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        datePickerState.selectedDateMillis?.let { millis ->
                            // Convert selected milliseconds back into a YYYY-MM-DD string
                            val instant = Instant.fromEpochMilliseconds(millis)
                            val localDate = instant.toLocalDateTime(TimeZone.UTC).date
                            onValueChange(localDate.toString())
                        }
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
        ) {
            DatePicker(state = datePickerState)
        }
    }
}
