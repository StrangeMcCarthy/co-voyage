package covoyage.travel.cameroon.ui.journey

import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import covoyage.travel.cameroon.data.model.CameroonCities
import covoyage.travel.cameroon.data.model.UserProfile
import covoyage.travel.cameroon.i18n.LocalStrings
import covoyage.travel.cameroon.ui.components.CoVoyageButton
import covoyage.travel.cameroon.ui.components.CoVoyageTextField
import androidx.compose.foundation.background
import androidx.compose.ui.graphics.Color

class CreateJourneyScreen(
    private val journeyScreenModel: JourneyScreenModel,
    private val currentUser: UserProfile,
) : Screen {

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow
        val uiState by journeyScreenModel.uiState.collectAsState()
        val strings = LocalStrings.current

        LaunchedEffect(uiState.journeyCreated) {
            if (uiState.journeyCreated) {
                journeyScreenModel.resetJourneyCreated()
                navigator.pop()
            }
        }

        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text(strings.postARide) },
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
                    .padding(horizontal = 24.dp),
            ) {
                Spacer(modifier = Modifier.height(8.dp))

                // Error message
                AnimatedVisibility(visible = uiState.error.isNotBlank()) {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.errorContainer,
                        ),
                    ) {
                        Text(
                            text = uiState.error,
                            modifier = Modifier.padding(12.dp),
                            color = MaterialTheme.colorScheme.onErrorContainer,
                            style = MaterialTheme.typography.bodySmall,
                        )
                    }
                }

                if (uiState.error.isNotBlank()) {
                    Spacer(modifier = Modifier.height(8.dp))
                }

                // Section: Route
                SectionLabel(strings.route)

                CoVoyageTextField(
                    value = uiState.departureCity,
                    onValueChange = journeyScreenModel::updateDepartureCity,
                    label = strings.departureCity,
                    leadingIcon = Icons.Default.TripOrigin,
                )
                Spacer(modifier = Modifier.height(10.dp))

                CoVoyageTextField(
                    value = uiState.arrivalCity,
                    onValueChange = journeyScreenModel::updateArrivalCity,
                    label = strings.arrivalCity,
                    leadingIcon = Icons.Default.Place,
                )

                Spacer(modifier = Modifier.height(20.dp))

                // Section: Schedule
                SectionLabel(strings.schedule)

                CoVoyageTextField(
                    value = uiState.departureDate,
                    onValueChange = journeyScreenModel::updateDepartureDate,
                    label = strings.dateFormat,
                    leadingIcon = Icons.Default.CalendarMonth,
                )
                Spacer(modifier = Modifier.height(10.dp))

                CoVoyageTextField(
                    value = uiState.departureTime,
                    onValueChange = journeyScreenModel::updateDepartureTime,
                    label = strings.timeFormat,
                    leadingIcon = Icons.Default.Schedule,
                )

                Spacer(modifier = Modifier.height(20.dp))

                // Section: Details
                SectionLabel(strings.details)

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    CoVoyageTextField(
                        value = uiState.totalSeats,
                        onValueChange = journeyScreenModel::updateTotalSeats,
                        label = strings.seats,
                        leadingIcon = Icons.Default.AirlineSeatReclineNormal,
                        keyboardType = KeyboardType.Number,
                        modifier = Modifier.weight(1f),
                    )
                    CoVoyageTextField(
                        value = uiState.pricePerSeat,
                        onValueChange = journeyScreenModel::updatePricePerSeat,
                        label = strings.priceXaf,
                        leadingIcon = Icons.Default.Payments,
                        keyboardType = KeyboardType.Number,
                        modifier = Modifier.weight(1f),
                    )
                }
                // Saved vehicle dropdown (only if user has saved vehicles)
                if (uiState.savedVehicles.isNotEmpty()) {
                    var vehicleDropdownExpanded by remember { mutableStateOf(false) }
                    ExposedDropdownMenuBox(
                        expanded = vehicleDropdownExpanded,
                        onExpandedChange = { vehicleDropdownExpanded = it },
                    ) {
                        OutlinedTextField(
                            value = if (uiState.vehicleName.isNotBlank()) "${uiState.vehicleName} ${uiState.vehicleModel}" else "",
                            onValueChange = {},
                            readOnly = true,
                            label = { Text(strings.selectSavedVehicle) },
                            leadingIcon = { Icon(Icons.Default.DirectionsCar, null) },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = vehicleDropdownExpanded) },
                            modifier = Modifier.menuAnchor().fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                        )
                        ExposedDropdownMenu(
                            expanded = vehicleDropdownExpanded,
                            onDismissRequest = { vehicleDropdownExpanded = false },
                        ) {
                            uiState.savedVehicles.forEach { vehicle ->
                                DropdownMenuItem(
                                    text = { Text("${vehicle.name} ${vehicle.model} — ${vehicle.plateNumber}") },
                                    onClick = {
                                        journeyScreenModel.selectSavedVehicle(vehicle)
                                        vehicleDropdownExpanded = false
                                    },
                                )
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(10.dp))
                }

                CoVoyageTextField(
                    value = uiState.vehicleName,
                    onValueChange = journeyScreenModel::updateVehicleName,
                    label = strings.vehicleName,
                    leadingIcon = Icons.Default.DirectionsCar,
                )
                Spacer(modifier = Modifier.height(10.dp))

                CoVoyageTextField(
                    value = uiState.vehicleModel,
                    onValueChange = journeyScreenModel::updateVehicleModel,
                    label = strings.vehicleModel,
                    leadingIcon = Icons.Default.DirectionsCar,
                )
                Spacer(modifier = Modifier.height(10.dp))

                CoVoyageTextField(
                    value = uiState.vehiclePlateNumber,
                    onValueChange = journeyScreenModel::updateVehiclePlateNumber,
                    label = strings.plateNumber,
                    leadingIcon = Icons.Default.Pin,
                )
                Spacer(modifier = Modifier.height(10.dp))

                // Save vehicle checkbox
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Checkbox(
                        checked = uiState.saveVehicle,
                        onCheckedChange = { journeyScreenModel.toggleSaveVehicle() },
                    )
                    Text(
                        text = strings.saveVehicle,
                        style = MaterialTheme.typography.bodyMedium,
                    )
                }
                Spacer(modifier = Modifier.height(10.dp))

                CoVoyageTextField(
                    value = uiState.additionalNotes,
                    onValueChange = journeyScreenModel::updateAdditionalNotes,
                    label = strings.additionalNotes,
                    leadingIcon = Icons.Default.Notes,
                    singleLine = false,
                    maxLines = 3,
                )

                Spacer(modifier = Modifier.height(28.dp))

                CoVoyageButton(
                    text = strings.postRide,
                    onClick = {
                        journeyScreenModel.createJourney(
                            driverId = currentUser.id,
                            driverName = currentUser.name,
                            driverPhone = currentUser.phone,
                        )
                    },
                    isLoading = uiState.isLoading,
                )

                Spacer(modifier = Modifier.height(32.dp))
            }
        }
    }
}

@Composable
private fun SectionLabel(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.labelLarge,
        fontWeight = FontWeight.SemiBold,
        color = MaterialTheme.colorScheme.primary,
        modifier = Modifier.padding(bottom = 8.dp),
    )
}
