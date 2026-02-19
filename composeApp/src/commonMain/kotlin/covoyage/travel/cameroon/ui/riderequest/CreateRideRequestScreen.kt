package covoyage.travel.cameroon.ui.riderequest

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import covoyage.travel.cameroon.data.model.UserProfile
import covoyage.travel.cameroon.i18n.LocalStrings

class CreateRideRequestScreen(
    private val rideRequestScreenModel: RideRequestScreenModel,
    private val currentUser: UserProfile,
) : Screen {

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow
        val uiState by rideRequestScreenModel.uiState.collectAsState()
        val strings = LocalStrings.current

        // Navigate back after successful creation
        LaunchedEffect(uiState.requestCreated) {
            if (uiState.requestCreated) {
                rideRequestScreenModel.resetRequestCreated()
                navigator.pop()
            }
        }

        Scaffold(
            topBar = {
                TopAppBar(
                    title = {
                        Text(
                            strings.createRideRequest,
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
                    .padding(horizontal = 16.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                Spacer(modifier = Modifier.height(8.dp))

                // ── Route Section ──
                Text(
                    text = strings.route,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                )

                OutlinedTextField(
                    value = uiState.departureCity,
                    onValueChange = rideRequestScreenModel::updateDepartureCity,
                    label = { Text(strings.departureCity) },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true,
                )

                OutlinedTextField(
                    value = uiState.destinationCity,
                    onValueChange = rideRequestScreenModel::updateDestinationCity,
                    label = { Text(strings.destination) },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true,
                )

                Spacer(modifier = Modifier.height(4.dp))

                // ── Schedule Section ──
                Text(
                    text = strings.schedule,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                )

                OutlinedTextField(
                    value = uiState.travelDate,
                    onValueChange = rideRequestScreenModel::updateTravelDate,
                    label = { Text(strings.travelDate) },
                    placeholder = { Text(strings.dateFormat) },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true,
                )

                Spacer(modifier = Modifier.height(4.dp))

                // ── Details Section ──
                Text(
                    text = strings.details,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                )

                OutlinedTextField(
                    value = uiState.seatsNeeded,
                    onValueChange = rideRequestScreenModel::updateSeatsNeeded,
                    label = { Text(strings.seatsNeeded) },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                )

                OutlinedTextField(
                    value = uiState.message,
                    onValueChange = rideRequestScreenModel::updateMessage,
                    label = { Text(strings.yourMessage) },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    minLines = 3,
                    maxLines = 5,
                )

                // Error
                if (uiState.error.isNotBlank()) {
                    Text(
                        text = uiState.error,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall,
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Submit button
                Button(
                    onClick = {
                        rideRequestScreenModel.createRequest(
                            passengerId = currentUser.id,
                            passengerName = currentUser.name,
                            passengerPhone = currentUser.phone,
                        )
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.tertiary,
                    ),
                    enabled = !uiState.isLoading,
                ) {
                    if (uiState.isLoading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(24.dp),
                            color = MaterialTheme.colorScheme.onTertiary,
                        )
                    } else {
                        Text(
                            text = strings.submitRequest,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onTertiary,
                        )
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }
}
