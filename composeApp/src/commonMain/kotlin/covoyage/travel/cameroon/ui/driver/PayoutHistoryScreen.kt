package covoyage.travel.cameroon.ui.driver

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import covoyage.travel.cameroon.data.remote.DriverPayoutResponse
import covoyage.travel.cameroon.data.remote.PayoutItem
import covoyage.travel.cameroon.i18n.LocalStrings
import covoyage.travel.cameroon.ui.theme.SuccessGreen

class PayoutHistoryScreen(
    private val payoutSummary: DriverPayoutResponse,
    private val onDismiss: () -> Unit,
) : Screen {

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow
        val strings = LocalStrings.current

        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text(strings.earningsAndPayouts) },
                    navigationIcon = {
                        IconButton(onClick = {
                            onDismiss()
                            navigator.pop()
                        }) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, strings.back)
                        }
                    },
                )
            },
        ) { padding ->
            LazyColumn(
                modifier = Modifier.fillMaxSize().padding(padding),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                // ── Summary cards ──
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                    ) {
                        EarningsCard(
                            modifier = Modifier.weight(1f),
                            label = strings.totalEarned,
                            amount = "${payoutSummary.totalEarned} ${strings.xafSuffix}",
                            icon = "💰",
                            color = SuccessGreen,
                        )
                        EarningsCard(
                            modifier = Modifier.weight(1f),
                            label = strings.pending,
                            amount = "${payoutSummary.pendingEarnings} ${strings.xafSuffix}",
                            icon = "⏳",
                            color = MaterialTheme.colorScheme.primary,
                        )
                    }
                }

                item {
                    Card(
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f),
                        ),
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(16.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Column {
                                Text(
                                    strings.completedTrips,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                )
                                Text(
                                    "${payoutSummary.totalTrips}",
                                    style = MaterialTheme.typography.headlineMedium,
                                    fontWeight = FontWeight.Bold,
                                )
                            }
                            Surface(
                                shape = CircleShape,
                                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                                modifier = Modifier.size(56.dp),
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        Icons.Default.AccountBalanceWallet,
                                        null,
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(28.dp),
                                    )
                                }
                            }
                        }
                    }
                }

                // ── Payout list ──
                item {
                    Text(
                        strings.payoutHistory,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(top = 8.dp),
                    )
                }

                if (payoutSummary.payouts.isEmpty()) {
                    item {
                        Card(shape = RoundedCornerShape(12.dp)) {
                            Box(
                                modifier = Modifier.fillMaxWidth().padding(32.dp),
                                contentAlignment = Alignment.Center,
                            ) {
                                Text(
                                    strings.noPayoutsYet,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                )
                            }
                        }
                    }
                } else {
                    items(payoutSummary.payouts) { payout ->
                        PayoutCard(payout)
                    }
                }
            }
        }
    }
}

@Composable
private fun EarningsCard(
    modifier: Modifier = Modifier,
    label: String,
    amount: String,
    icon: String,
    color: androidx.compose.ui.graphics.Color,
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = color.copy(alpha = 0.08f),
        ),
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(icon, style = MaterialTheme.typography.headlineSmall)
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                label,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Text(
                amount,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = color,
            )
        }
    }
}

@Composable
private fun PayoutCard(payout: PayoutItem) {
    val strings = LocalStrings.current

    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
        ),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    payout.passengerName.ifBlank { strings.passengerFallback },
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold,
                )
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        when (payout.paymentMethod) {
                            "MTN_MOMO" -> "📱 MTN"
                            "ORANGE_MONEY" -> "🍊 OM"
                            "CARD" -> "💳 Card"
                            else -> payout.paymentMethod
                        },
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    Text(
                        payout.releasedAt.take(10),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.outline,
                    )
                }
            }
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    "+${payout.driverPayout} ${strings.xafSuffix}",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = SuccessGreen,
                )
                Text(
                    "${strings.ofAmount} ${payout.totalAmount} ${strings.xafSuffix}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.outline,
                )
            }
        }
    }
}
