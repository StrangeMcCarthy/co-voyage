package covoyage.travel.cameroon.ui.booking

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import covoyage.travel.cameroon.data.model.Booking
import covoyage.travel.cameroon.data.model.Journey
import covoyage.travel.cameroon.data.model.Payment
import covoyage.travel.cameroon.i18n.LocalStrings
import covoyage.travel.cameroon.ui.theme.SuccessGreen

class PaymentConfirmationScreen(
    private val booking: Booking,
    private val payment: Payment?,
    private val journey: Journey,
) : Screen {

    @Composable
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow
        val strings = LocalStrings.current

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            // ── Success icon ──
            Surface(
                shape = CircleShape,
                color = SuccessGreen.copy(alpha = 0.15f),
                modifier = Modifier.size(96.dp),
            ) {
                Icon(
                    Icons.Default.CheckCircle,
                    contentDescription = strings.done,
                    tint = SuccessGreen,
                    modifier = Modifier.padding(16.dp),
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                strings.bookingConfirmed,
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                strings.paymentEscrowInfo,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
            )

            Spacer(modifier = Modifier.height(32.dp))

            // ── Booking summary ──
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                ),
            ) {
                Column(modifier = Modifier.padding(20.dp).fillMaxWidth()) {
                    SummaryRow(strings.summaryRoute, "${journey.departureCity} → ${journey.arrivalCity}")
                    SummaryRow(strings.summarySeats, "${booking.seatsBooked}")
                    SummaryRow(strings.summaryTotalPaid, "${booking.totalAmount} ${strings.xafSuffix}")

                    if (payment != null) {
                        HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp))
                        SummaryRow(strings.summaryPaymentMethod, when (payment.paymentMethod.name) {
                            "MTN_MOMO" -> strings.mtnMobileMoney
                            "ORANGE_MONEY" -> strings.orangeMoney
                            "CARD" -> strings.cardPayment
                            else -> payment.paymentMethod.name
                        })
                        SummaryRow(strings.summaryStatus, strings.heldInEscrow)
                        if (payment.txRef.isNotBlank()) {
                            SummaryRow(strings.reference, payment.txRef)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // ── Escrow info ──
            Card(
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f),
                ),
            ) {
                Text(
                    text = strings.escrowExplanation,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(16.dp),
                )
            }

            Spacer(modifier = Modifier.height(32.dp))

            // ── Done button ──
            Button(
                onClick = {
                    // Pop back to journey feed
                    navigator.popUntilRoot()
                },
                modifier = Modifier.fillMaxWidth().height(52.dp),
                shape = RoundedCornerShape(16.dp),
            ) {
                Text(strings.done, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
private fun SummaryRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Text(
            label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Text(
            value,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.SemiBold,
        )
    }
}
