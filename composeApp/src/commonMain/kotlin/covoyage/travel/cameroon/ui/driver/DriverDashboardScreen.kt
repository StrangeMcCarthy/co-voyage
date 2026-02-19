package covoyage.travel.cameroon.ui.driver

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
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
import covoyage.travel.cameroon.data.model.Journey
import covoyage.travel.cameroon.data.model.JourneyStatus
import covoyage.travel.cameroon.data.model.UserProfile
import covoyage.travel.cameroon.i18n.LocalStrings
import covoyage.travel.cameroon.i18n.Strings
import covoyage.travel.cameroon.ui.journey.CreateJourneyScreen
import covoyage.travel.cameroon.ui.journey.JourneyScreenModel
import covoyage.travel.cameroon.ui.theme.SuccessGreen

class DriverDashboardScreen(
    private val currentUser: UserProfile,
    private val driverScreenModel: DriverScreenModel,
    private val journeyScreenModel: JourneyScreenModel,
) : Screen {

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow
        val uiState by driverScreenModel.uiState.collectAsState()
        val strings = LocalStrings.current

        LaunchedEffect(Unit) {
            driverScreenModel.loadMyJourneys(currentUser.id)
        }

        // Snackbar for messages
        val snackbarHostState = remember { SnackbarHostState() }
        LaunchedEffect(uiState.successMessage, uiState.error) {
            if (uiState.successMessage.isNotBlank()) {
                snackbarHostState.showSnackbar(uiState.successMessage)
                driverScreenModel.clearMessages()
            } else if (uiState.error.isNotBlank()) {
                snackbarHostState.showSnackbar(uiState.error)
                driverScreenModel.clearMessages()
            }
        }

        Scaffold(
            snackbarHost = { SnackbarHost(snackbarHostState) },
            topBar = {
                TopAppBar(
                    title = { Text(strings.myTrips) },
                    actions = {
                        IconButton(onClick = { driverScreenModel.loadPayouts(currentUser.id) }) {
                            Icon(Icons.Default.AccountBalanceWallet, strings.payouts)
                        }
                        IconButton(onClick = { driverScreenModel.loadMyJourneys(currentUser.id) }) {
                            Icon(Icons.Default.Refresh, strings.refresh)
                        }
                    },
                )
            },
            floatingActionButton = {
                ExtendedFloatingActionButton(
                    onClick = {
                        navigator.push(
                            CreateJourneyScreen(
                                journeyScreenModel = journeyScreenModel,
                                currentUser = currentUser,
                            )
                        )
                    },
                    containerColor = MaterialTheme.colorScheme.primary,
                    shape = RoundedCornerShape(16.dp),
                ) {
                    Icon(Icons.Default.Add, strings.newTrip)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(strings.newTrip, fontWeight = FontWeight.SemiBold)
                }
            },
        ) { padding ->
            if (uiState.isLoading && uiState.myJourneys.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize().padding(padding),
                    contentAlignment = Alignment.Center,
                ) {
                    CircularProgressIndicator()
                }
            } else if (uiState.myJourneys.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize().padding(padding),
                    contentAlignment = Alignment.Center,
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("🚐", style = MaterialTheme.typography.displayLarge)
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            strings.noTripsYet,
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                        Text(
                            strings.createFirstTrip,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.outline,
                        )
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize().padding(padding),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    items(uiState.myJourneys) { journey ->
                        DriverJourneyCard(
                            journey = journey,
                            strings = strings,
                            onStart = { driverScreenModel.startTrip(journey.id, currentUser.id) },
                            onComplete = { driverScreenModel.completeTrip(journey.id, currentUser.id) },
                            onCancel = { driverScreenModel.cancelJourney(journey.id, currentUser.id) },
                        )
                    }
                }
            }

            // Payout bottom sheet
            if (uiState.showPayouts && uiState.payoutSummary != null) {
                navigator.push(
                    PayoutHistoryScreen(
                        payoutSummary = uiState.payoutSummary!!,
                        onDismiss = { driverScreenModel.hidePayouts() },
                    )
                )
            }
        }
    }
}

@Composable
private fun DriverJourneyCard(
    journey: Journey,
    strings: Strings,
    onStart: () -> Unit,
    onComplete: () -> Unit,
    onCancel: () -> Unit,
) {
    var showConfirmDialog by remember { mutableStateOf(false) }
    var confirmAction by remember { mutableStateOf("") }

    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
        ),
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Route and status
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    "${journey.departureCity} → ${journey.arrivalCity}",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                )
                JourneyStatusChip(journey.status, strings)
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Details row
            Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                InfoChip("📅 ${journey.departureDate}")
                InfoChip("🕐 ${journey.departureTime}")
                InfoChip("💺 ${journey.availableSeats}/${journey.totalSeats}")
            }

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                "${journey.pricePerSeat} ${strings.xafSuffix}/${strings.seats.lowercase()}",
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.primary,
            )

            // Action buttons based on status
            Spacer(modifier = Modifier.height(12.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                when (journey.status) {
                    JourneyStatus.SCHEDULED -> {
                        OutlinedButton(
                            onClick = {
                                confirmAction = "cancel"
                                showConfirmDialog = true
                            },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.outlinedButtonColors(
                                contentColor = MaterialTheme.colorScheme.error,
                            ),
                        ) {
                            Text(strings.cancel)
                        }
                        Button(
                            onClick = {
                                confirmAction = "start"
                                showConfirmDialog = true
                            },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(12.dp),
                        ) {
                            Icon(Icons.Default.PlayArrow, null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(strings.startTrip)
                        }
                    }
                    JourneyStatus.IN_PROGRESS -> {
                        Button(
                            onClick = {
                                confirmAction = "complete"
                                showConfirmDialog = true
                            },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = SuccessGreen,
                            ),
                        ) {
                            Icon(Icons.Default.CheckCircle, null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(strings.completeAndRelease)
                        }
                    }
                    else -> { /* No actions for COMPLETED/CANCELLED */ }
                }
            }
        }
    }

    // Confirmation dialog
    if (showConfirmDialog) {
        val (title, text) = when (confirmAction) {
            "start" -> strings.startTripQuestion to strings.startTripConfirmText
            "complete" -> strings.completeTripQuestion to strings.completeTripConfirmText
            "cancel" -> strings.cancelTripQuestion to strings.cancelTripConfirmText
            else -> "" to ""
        }
        AlertDialog(
            onDismissRequest = { showConfirmDialog = false },
            title = { Text(title) },
            text = { Text(text) },
            confirmButton = {
                TextButton(onClick = {
                    showConfirmDialog = false
                    when (confirmAction) {
                        "start" -> onStart()
                        "complete" -> onComplete()
                        "cancel" -> onCancel()
                    }
                }) {
                    Text(strings.confirm)
                }
            },
            dismissButton = {
                TextButton(onClick = { showConfirmDialog = false }) {
                    Text(strings.cancel)
                }
            },
        )
    }
}

@Composable
private fun JourneyStatusChip(status: JourneyStatus, strings: Strings) {
    val (color, label) = when (status) {
        JourneyStatus.SCHEDULED -> MaterialTheme.colorScheme.primary to strings.statusScheduled
        JourneyStatus.IN_PROGRESS -> SuccessGreen to strings.statusInProgress
        JourneyStatus.COMPLETED -> MaterialTheme.colorScheme.outline to strings.statusCompleted
        JourneyStatus.CANCELLED -> MaterialTheme.colorScheme.error to strings.statusCancelled
    }
    Surface(
        shape = RoundedCornerShape(8.dp),
        color = color.copy(alpha = 0.12f),
    ) {
        Text(
            label,
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.SemiBold,
            color = color,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
        )
    }
}

@Composable
private fun InfoChip(text: String) {
    Text(
        text,
        style = MaterialTheme.typography.bodySmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
    )
}
