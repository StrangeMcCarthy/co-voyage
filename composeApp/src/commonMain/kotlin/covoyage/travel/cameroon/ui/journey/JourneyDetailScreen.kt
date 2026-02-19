package covoyage.travel.cameroon.ui.journey

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import covoyage.travel.cameroon.data.model.Journey
import covoyage.travel.cameroon.data.model.UserProfile
import covoyage.travel.cameroon.i18n.LocalStrings
import covoyage.travel.cameroon.ui.booking.BookingScreen
import covoyage.travel.cameroon.ui.booking.BookingScreenModel

class JourneyDetailScreen(
    private val journey: Journey,
    private val currentUser: UserProfile,
    private val bookingScreenModel: BookingScreenModel,
) : Screen {

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow
        val strings = LocalStrings.current

        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text(strings.rideDetails) },
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
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                // Route header
                Card(
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f),
                    ),
                ) {
                    Column(
                        modifier = Modifier.fillMaxWidth().padding(20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceEvenly,
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    journey.departureCity,
                                    style = MaterialTheme.typography.titleLarge,
                                    fontWeight = FontWeight.Bold,
                                )
                                Text(
                                    strings.from,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                )
                            }

                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .clip(CircleShape)
                                    .background(MaterialTheme.colorScheme.primary),
                                contentAlignment = Alignment.Center,
                            ) {
                                Icon(
                                    Icons.Default.ArrowForward,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.onPrimary,
                                    modifier = Modifier.size(20.dp),
                                )
                            }

                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    journey.arrivalCity,
                                    style = MaterialTheme.typography.titleLarge,
                                    fontWeight = FontWeight.Bold,
                                )
                                Text(
                                    strings.to,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // Price badge
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(12.dp))
                                .background(MaterialTheme.colorScheme.primary)
                                .padding(horizontal = 20.dp, vertical = 8.dp),
                        ) {
                            Text(
                                "${journey.pricePerSeat} ${strings.xafSuffix} ${strings.perSeat}",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onPrimary,
                            )
                        }
                    }
                }

                // Details card
                Card(shape = RoundedCornerShape(16.dp)) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        DetailRow(Icons.Default.CalendarMonth, strings.date, journey.departureDate)
                        HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
                        DetailRow(Icons.Default.Schedule, strings.time, journey.departureTime)
                        HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
                        DetailRow(
                            Icons.Default.AirlineSeatReclineNormal,
                            strings.availableSeats,
                            "${journey.availableSeats} ${strings.ofSeats} ${journey.totalSeats}",
                        )
                        if (journey.vehicleName.isNotBlank()) {
                            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
                            DetailRow(
                                Icons.Default.DirectionsCar,
                                strings.vehicle,
                                "${journey.vehicleName} ${journey.vehicleModel}".trim(),
                            )
                        }
                        if (journey.vehiclePlateNumber.isNotBlank()) {
                            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
                            DetailRow(Icons.Default.Pin, strings.plate, journey.vehiclePlateNumber)
                        }
                    }
                }

                // Driver info
                Card(shape = RoundedCornerShape(16.dp)) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        DetailRow(Icons.Default.Person, strings.driverLabel, journey.driverName)
                        HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
                        DetailRow(Icons.Default.Phone, strings.phone, journey.driverPhone)
                        if (journey.additionalNotes.isNotBlank()) {
                            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
                            DetailRow(Icons.Default.Notes, strings.notes, journey.additionalNotes)
                        }
                    }
                }

                // Book button
                Button(
                    onClick = {
                        bookingScreenModel.resetBooking()
                        navigator.push(
                            BookingScreen(
                                journey = journey,
                                currentUser = currentUser,
                                bookingScreenModel = bookingScreenModel,
                            )
                        )
                    },
                    enabled = journey.availableSeats > 0,
                    modifier = Modifier.fillMaxWidth().height(56.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                    ),
                ) {
                    if (journey.availableSeats > 0) {
                        Text(
                            "${strings.bookSeat} — ${journey.pricePerSeat} ${strings.xafSuffix}",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                        )
                    } else {
                        Text(
                            strings.fullyBooked,
                            style = MaterialTheme.typography.titleSmall,
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }
}

@Composable
private fun DetailRow(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    value: String,
) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            modifier = Modifier.size(20.dp),
            tint = MaterialTheme.colorScheme.primary,
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
