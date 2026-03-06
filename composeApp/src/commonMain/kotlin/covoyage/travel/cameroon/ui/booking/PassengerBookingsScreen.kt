package covoyage.travel.cameroon.ui.booking

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.Image
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import covoyage.travel.cameroon.data.model.Booking
import covoyage.travel.cameroon.data.model.BookingStatus
import covoyage.travel.cameroon.data.model.UserProfile
import covoyage.travel.cameroon.i18n.LocalStrings
import covoyage.travel.cameroon.ui.theme.SuccessGreen
import kotlinx.coroutines.delay
import kotlinx.datetime.Clock

class PassengerBookingsScreen(
    private val currentUser: UserProfile,
    private val bookingScreenModel: BookingScreenModel,
) : Screen {

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    override fun Content() {
        val uiState by bookingScreenModel.uiState.collectAsState()
        val strings = LocalStrings.current

        LaunchedEffect(Unit) {
            bookingScreenModel.loadMyBookings(currentUser.id)
        }

        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text(strings.tabMyTrips) },
                    actions = {
                        IconButton(onClick = { bookingScreenModel.loadMyBookings(currentUser.id) }) {
                            Text("↻")
                        }
                    }
                )
            }
        ) { padding ->
            if (uiState.isLoading && uiState.myBookings.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            } else if (uiState.myBookings.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("🎫", style = MaterialTheme.typography.displayLarge)
                        Spacer(modifier = Modifier.height(16.dp))
                        Text("No bookings yet", style = MaterialTheme.typography.titleMedium)
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize().padding(padding),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    items(uiState.myBookings) { booking ->
                        PassengerBookingCard(
                            booking = booking,
                            onConfirm = { bookingScreenModel.confirmRide(booking.id) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun PassengerBookingCard(booking: Booking, onConfirm: () -> Unit) {
    val strings = LocalStrings.current
    
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "Booking ID: ${booking.id.takeLast(6)}",
                    fontWeight = FontWeight.Bold
                )
                BookingStatusChip(booking.status)
            }

            Spacer(modifier = Modifier.height(8.dp))
            
            Text("Seats: ${booking.seatsBooked}")
            Text("Total: ${booking.totalAmount} XAF")



            // 1-HOUR CONFIRMATION PROMPT
            if (booking.status == BookingStatus.COMPLETED_BY_DRIVER && booking.completedAt != null) {
                Spacer(modifier = Modifier.height(16.dp))
                RemainingTimeCard(completedAt = booking.completedAt, onConfirm = onConfirm)
            }
        }
    }
}

@Composable
fun RemainingTimeCard(completedAt: Long, onConfirm: () -> Unit) {
    var remainingMinutes by remember { mutableStateOf(60) }

    LaunchedEffect(completedAt) {
        while (true) {
            val now = Clock.System.now().toEpochMilliseconds()
            val passedMillis = now - completedAt
            val passedMinutes = (passedMillis / (1000 * 60)).toInt()
            remainingMinutes = maxOf(0, 60 - passedMinutes)
            if (remainingMinutes == 0) break
            delay(60000) // update every minute
        }
    }

    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp).fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                "Did you arrive safely at your destination?",
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                "Auto-releasing payment in $remainingMinutes minutes.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.error
            )
            Spacer(modifier = Modifier.height(12.dp))
            Button(
                onClick = onConfirm,
                colors = ButtonDefaults.buttonColors(containerColor = SuccessGreen),
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(Icons.Default.CheckCircle, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("Confirm Ride")
            }
        }
    }
}

@Composable
fun BookingStatusChip(status: BookingStatus) {
    val (color, label) = when (status) {
        BookingStatus.PENDING -> MaterialTheme.colorScheme.primary to "Pending"
        BookingStatus.ACCEPTED -> MaterialTheme.colorScheme.primary to "Accepted"
        BookingStatus.BOARDED -> SuccessGreen to "Boarded"
        BookingStatus.COMPLETED_BY_DRIVER -> MaterialTheme.colorScheme.error to "Driver Finished"
        BookingStatus.CONFIRMED -> SuccessGreen to "Confirmed"
        BookingStatus.CANCELLED -> MaterialTheme.colorScheme.error to "Cancelled"
        BookingStatus.REFUNDED -> MaterialTheme.colorScheme.outline to "Refunded"
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
