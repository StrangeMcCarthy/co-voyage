package covoyage.travel.cameroon.ui.riderequest

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import covoyage.travel.cameroon.data.model.RideRequest
import covoyage.travel.cameroon.data.model.UserProfile
import covoyage.travel.cameroon.data.model.UserType
import covoyage.travel.cameroon.i18n.LocalStrings

/**
 * Detail screen for a ride request.
 * Drivers see "Contact Passenger", owners see "Close Request".
 */
class RideRequestDetailScreen(
    private val request: RideRequest,
    private val currentUser: UserProfile,
    private val rideRequestScreenModel: RideRequestScreenModel,
) : Screen {

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow
        val strings = LocalStrings.current
        var showCloseDialog by remember { mutableStateOf(false) }

        Scaffold(
            topBar = {
                TopAppBar(
                    title = {
                        Text(
                            strings.rideRequests,
                            fontWeight = FontWeight.Bold,
                        )
                    },
                    navigationIcon = {
                        IconButton(onClick = { navigator.pop() }) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, strings.back)
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.surface,
                    ),
                )
            },
        ) { padding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(16.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                // Route card
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.tertiaryContainer,
                    ),
                ) {
                    Column(modifier = Modifier.padding(20.dp)) {
                        Text(
                            text = strings.route,
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onTertiaryContainer,
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Text(
                                text = request.departureCity,
                                style = MaterialTheme.typography.headlineSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onTertiaryContainer,
                            )
                            Icon(
                                imageVector = Icons.Default.ArrowForward,
                                contentDescription = null,
                                modifier = Modifier.padding(horizontal = 12.dp),
                                tint = MaterialTheme.colorScheme.onTertiaryContainer,
                            )
                            Text(
                                text = request.destinationCity,
                                style = MaterialTheme.typography.headlineSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onTertiaryContainer,
                            )
                        }
                    }
                }

                // Info rows
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                ) {
                    Column(modifier = Modifier.padding(20.dp)) {
                        InfoRow(
                            icon = Icons.Default.CalendarMonth,
                            label = strings.travelDate,
                            value = request.travelDate,
                        )
                        HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp))
                        InfoRow(
                            icon = Icons.Default.EventSeat,
                            label = strings.seatsNeeded,
                            value = "${request.seatsNeeded}",
                        )
                        HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp))
                        InfoRow(
                            icon = Icons.Default.Person,
                            label = strings.passengerFallback,
                            value = request.passengerName,
                        )
                        HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp))
                        InfoRow(
                            icon = Icons.Default.Phone,
                            label = strings.phone,
                            value = request.passengerPhone,
                        )
                        if (request.message.isNotBlank()) {
                            HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp))
                            InfoRow(
                                icon = Icons.Default.Chat,
                                label = strings.notes,
                                value = request.message,
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Action buttons
                if (currentUser.userType == UserType.DRIVER) {
                    // Driver sees "Contact Passenger" 
                    Button(
                        onClick = { /* Could open chat or phone dialer */ },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp),
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary,
                        ),
                    ) {
                        Icon(Icons.Default.Phone, null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = strings.contactPassenger,
                            fontWeight = FontWeight.Bold,
                        )
                    }
                } else if (currentUser.id == request.passengerId) {
                    // Owner passenger sees "Close Request"
                    OutlinedButton(
                        onClick = { showCloseDialog = true },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp),
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = MaterialTheme.colorScheme.error,
                        ),
                    ) {
                        Icon(Icons.Default.Close, null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = strings.closeRequest,
                            fontWeight = FontWeight.Bold,
                        )
                    }
                }
            }
        }

        // Close confirmation dialog
        if (showCloseDialog) {
            AlertDialog(
                onDismissRequest = { showCloseDialog = false },
                title = { Text(strings.closeRequest) },
                text = { Text(strings.requestClosed) },
                confirmButton = {
                    TextButton(onClick = {
                        showCloseDialog = false
                        rideRequestScreenModel.closeRequest(request.id, currentUser.id)
                        navigator.pop()
                    }) {
                        Text(strings.confirm)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showCloseDialog = false }) {
                        Text(strings.cancel)
                    }
                },
            )
        }
    }
}

@Composable
private fun InfoRow(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    value: String,
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            modifier = Modifier.size(20.dp),
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Spacer(modifier = Modifier.width(12.dp))
        Column {
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Text(
                text = value,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Medium,
            )
        }
    }
}
