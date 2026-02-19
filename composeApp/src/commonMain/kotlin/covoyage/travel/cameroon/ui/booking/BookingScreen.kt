package covoyage.travel.cameroon.ui.booking

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import covoyage.travel.cameroon.data.model.Journey
import covoyage.travel.cameroon.data.model.PaymentMethod
import covoyage.travel.cameroon.data.model.UserProfile
import covoyage.travel.cameroon.i18n.LocalStrings

class BookingScreen(
    private val journey: Journey,
    private val currentUser: UserProfile,
    private val bookingScreenModel: BookingScreenModel,
) : Screen {

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow
        val uiState by bookingScreenModel.uiState.collectAsState()
        val totalPrice = journey.pricePerSeat * uiState.seatsToBook
        val strings = LocalStrings.current

        // Navigate to confirmation when booking is complete
        LaunchedEffect(uiState.bookingComplete) {
            if (uiState.bookingComplete && uiState.currentBooking != null) {
                navigator.push(
                    PaymentConfirmationScreen(
                        booking = uiState.currentBooking!!,
                        payment = uiState.currentPayment,
                        journey = journey,
                    )
                )
            }
        }

        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text(strings.bookYourRide) },
                    navigationIcon = {
                        IconButton(onClick = {
                            bookingScreenModel.resetBooking()
                            navigator.pop()
                        }) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, strings.back)
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.surface,
                    ),
                )
            },
        ) { padding ->
            Box(modifier = Modifier.fillMaxSize().padding(padding)) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                ) {
                    // ── Trip summary ──
                    Card(
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f),
                        ),
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(
                                "${journey.departureCity} → ${journey.arrivalCity}",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                "${journey.pricePerSeat} ${strings.xafSuffix} ${strings.perSeat} • ${journey.availableSeats} ${strings.seatsLeft}",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                    }

                    // ── Seat selector ──
                    Card(shape = RoundedCornerShape(16.dp)) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(
                                strings.numberOfSeats,
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.SemiBold,
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Center,
                                modifier = Modifier.fillMaxWidth(),
                            ) {
                                FilledIconButton(
                                    onClick = { bookingScreenModel.updateSeatsToBook(uiState.seatsToBook - 1) },
                                    enabled = uiState.seatsToBook > 1,
                                    colors = IconButtonDefaults.filledIconButtonColors(
                                        containerColor = MaterialTheme.colorScheme.surfaceVariant,
                                    ),
                                ) {
                                    Icon(Icons.Default.Remove, strings.removeSeat)
                                }
                                Text(
                                    text = "${uiState.seatsToBook}",
                                    style = MaterialTheme.typography.headlineMedium,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 24.dp),
                                )
                                FilledIconButton(
                                    onClick = { bookingScreenModel.updateSeatsToBook(uiState.seatsToBook + 1) },
                                    enabled = uiState.seatsToBook < journey.availableSeats,
                                    colors = IconButtonDefaults.filledIconButtonColors(
                                        containerColor = MaterialTheme.colorScheme.primaryContainer,
                                    ),
                                ) {
                                    Icon(Icons.Default.Add, strings.addSeat)
                                }
                            }
                        }
                    }

                    // ── Payment method ──
                    Card(shape = RoundedCornerShape(16.dp)) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(
                                strings.paymentMethod,
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.SemiBold,
                            )
                            Spacer(modifier = Modifier.height(12.dp))

                            PaymentMethodOption(
                                label = strings.mtnMobileMoney,
                                emoji = "📱",
                                selected = uiState.selectedPaymentMethod == PaymentMethod.MTN_MOMO,
                                onClick = { bookingScreenModel.updatePaymentMethod(PaymentMethod.MTN_MOMO) },
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            PaymentMethodOption(
                                label = strings.orangeMoney,
                                emoji = "🍊",
                                selected = uiState.selectedPaymentMethod == PaymentMethod.ORANGE_MONEY,
                                onClick = { bookingScreenModel.updatePaymentMethod(PaymentMethod.ORANGE_MONEY) },
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            PaymentMethodOption(
                                label = strings.cardPayment,
                                emoji = "💳",
                                selected = uiState.selectedPaymentMethod == PaymentMethod.CARD,
                                onClick = { bookingScreenModel.updatePaymentMethod(PaymentMethod.CARD) },
                            )
                        }
                    }

                    // ── Phone number (for MoMo/OM) ──
                    AnimatedVisibility(
                        visible = uiState.selectedPaymentMethod != PaymentMethod.CARD,
                    ) {
                        Card(shape = RoundedCornerShape(16.dp)) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Text(
                                    strings.mobileMoneyNumber,
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.SemiBold,
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                OutlinedTextField(
                                    value = uiState.phoneNumber,
                                    onValueChange = bookingScreenModel::updatePhoneNumber,
                                    label = { Text(strings.phoneHint) },
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                                    singleLine = true,
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(12.dp),
                                )
                            }
                        }
                    }

                    // ── Card details (for Card payment) ──
                    AnimatedVisibility(
                        visible = uiState.selectedPaymentMethod == PaymentMethod.CARD,
                    ) {
                        Card(shape = RoundedCornerShape(16.dp)) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Text(
                                    strings.cardDetails,
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.SemiBold,
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                OutlinedTextField(
                                    value = uiState.cardNumber,
                                    onValueChange = bookingScreenModel::updateCardNumber,
                                    label = { Text(strings.cardNumber) },
                                    leadingIcon = { Icon(Icons.Default.CreditCard, null) },
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                    singleLine = true,
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(12.dp),
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    OutlinedTextField(
                                        value = uiState.cardExpiryMonth,
                                        onValueChange = bookingScreenModel::updateCardExpiryMonth,
                                        label = { Text("MM") },
                                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                        singleLine = true,
                                        modifier = Modifier.weight(1f),
                                        shape = RoundedCornerShape(12.dp),
                                    )
                                    OutlinedTextField(
                                        value = uiState.cardExpiryYear,
                                        onValueChange = bookingScreenModel::updateCardExpiryYear,
                                        label = { Text("YY") },
                                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                        singleLine = true,
                                        modifier = Modifier.weight(1f),
                                        shape = RoundedCornerShape(12.dp),
                                    )
                                    OutlinedTextField(
                                        value = uiState.cardCvv,
                                        onValueChange = bookingScreenModel::updateCardCvv,
                                        label = { Text("CVV") },
                                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                        singleLine = true,
                                        modifier = Modifier.weight(1f),
                                        shape = RoundedCornerShape(12.dp),
                                    )
                                }
                            }
                        }
                    }

                    // ── Error ──
                    if (uiState.error.isNotBlank()) {
                        Card(
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.errorContainer,
                            ),
                        ) {
                            Text(
                                text = uiState.error,
                                color = MaterialTheme.colorScheme.onErrorContainer,
                                modifier = Modifier.padding(16.dp),
                                style = MaterialTheme.typography.bodySmall,
                            )
                        }
                    }

                    // ── Price summary ──
                    Card(
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                        ),
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                            ) {
                                Text("${uiState.seatsToBook} ${strings.seatsTimesPrice} ${journey.pricePerSeat} ${strings.xafSuffix}")
                                Text("$totalPrice ${strings.xafSuffix}", fontWeight = FontWeight.Bold)
                            }
                            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                            ) {
                                Text(strings.total, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                                Text(
                                    "$totalPrice ${strings.xafSuffix}",
                                    fontWeight = FontWeight.Bold,
                                    style = MaterialTheme.typography.titleMedium,
                                    color = MaterialTheme.colorScheme.primary,
                                )
                            }
                        }
                    }

                    // ── Pay button ──
                    Button(
                        onClick = { bookingScreenModel.bookSeats(journey, currentUser) },
                        enabled = !uiState.isLoading && !uiState.waitingForConfirmation,
                        modifier = Modifier.fillMaxWidth().height(56.dp),
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary,
                        ),
                    ) {
                        if (uiState.isLoading) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(24.dp),
                                color = MaterialTheme.colorScheme.onPrimary,
                                strokeWidth = 2.dp,
                            )
                        } else {
                            Text(
                                "${strings.payAmount} $totalPrice ${strings.xafSuffix}",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))
                }

                // ── Waiting overlay (MoMo/OM confirmation) ──
                AnimatedVisibility(
                    visible = uiState.waitingForConfirmation,
                    enter = fadeIn(),
                    exit = fadeOut(),
                ) {
                    Surface(
                        modifier = Modifier.fillMaxSize(),
                        color = MaterialTheme.colorScheme.scrim.copy(alpha = 0.6f),
                    ) {
                        Column(
                            modifier = Modifier.fillMaxSize(),
                            verticalArrangement = Arrangement.Center,
                            horizontalAlignment = Alignment.CenterHorizontally,
                        ) {
                            Card(
                                shape = RoundedCornerShape(24.dp),
                                modifier = Modifier.padding(32.dp),
                            ) {
                                Column(
                                    modifier = Modifier.padding(32.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                ) {
                                    CircularProgressIndicator(
                                        color = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(48.dp),
                                    )
                                    Spacer(modifier = Modifier.height(24.dp))
                                    Text(
                                        strings.waitingForPayment,
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold,
                                    )
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text(
                                        when (uiState.selectedPaymentMethod) {
                                            PaymentMethod.MTN_MOMO -> strings.approveMtnMomo
                                            PaymentMethod.ORANGE_MONEY -> strings.approveOrangeMoney
                                            else -> strings.processingCard
                                        },
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        textAlign = TextAlign.Center,
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        strings.checkPhoneNotification,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.outline,
                                        textAlign = TextAlign.Center,
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun PaymentMethodOption(
    label: String,
    emoji: String,
    selected: Boolean,
    onClick: () -> Unit,
) {
    OutlinedCard(
        onClick = onClick,
        shape = RoundedCornerShape(12.dp),
        border = BorderStroke(
            width = if (selected) 2.dp else 1.dp,
            color = if (selected) MaterialTheme.colorScheme.primary
            else MaterialTheme.colorScheme.outline.copy(alpha = 0.3f),
        ),
        colors = CardDefaults.outlinedCardColors(
            containerColor = if (selected)
                MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.2f)
            else MaterialTheme.colorScheme.surface,
        ),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(emoji, style = MaterialTheme.typography.titleLarge)
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                label,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal,
            )
            Spacer(modifier = Modifier.weight(1f))
            RadioButton(
                selected = selected,
                onClick = onClick,
                colors = RadioButtonDefaults.colors(
                    selectedColor = MaterialTheme.colorScheme.primary,
                ),
            )
        }
    }
}
