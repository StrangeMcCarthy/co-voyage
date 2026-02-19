package covoyage.travel.cameroon.ui.riderequest

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import covoyage.travel.cameroon.data.model.UserProfile
import covoyage.travel.cameroon.data.model.UserType
import covoyage.travel.cameroon.i18n.LocalStrings
import covoyage.travel.cameroon.ui.components.RideRequestCard

class RideRequestFeedScreen(
    private val rideRequestScreenModel: RideRequestScreenModel,
    private val currentUser: UserProfile,
) : Screen {

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow
        val uiState by rideRequestScreenModel.uiState.collectAsState()
        val strings = LocalStrings.current

        LaunchedEffect(Unit) {
            rideRequestScreenModel.loadRequests()
        }

        Scaffold(
            topBar = {
                TopAppBar(
                    title = {
                        Text(
                            strings.rideRequests,
                            fontWeight = FontWeight.Bold,
                        )
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.surface,
                    ),
                )
            },
            floatingActionButton = {
                if (currentUser.userType == UserType.PASSENGER) {
                    ExtendedFloatingActionButton(
                        onClick = {
                            navigator.push(
                                CreateRideRequestScreen(
                                    rideRequestScreenModel = rideRequestScreenModel,
                                    currentUser = currentUser,
                                )
                            )
                        },
                        containerColor = MaterialTheme.colorScheme.tertiary,
                        shape = RoundedCornerShape(16.dp),
                    ) {
                        Icon(
                            Icons.Default.Add,
                            strings.postARequest,
                            tint = MaterialTheme.colorScheme.onTertiary,
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            strings.postARequest,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onTertiary,
                        )
                    }
                }
            },
        ) { padding ->
            Column(
                modifier = Modifier.fillMaxSize().padding(padding),
            ) {
                // Search bar
                OutlinedTextField(
                    value = uiState.searchQuery,
                    onValueChange = rideRequestScreenModel::updateSearchQuery,
                    placeholder = { Text(strings.search) },
                    leadingIcon = { Icon(Icons.Default.Search, null) },
                    trailingIcon = {
                        if (uiState.searchQuery.isNotBlank()) {
                            IconButton(onClick = { rideRequestScreenModel.updateSearchQuery("") }) {
                                Icon(Icons.Default.Clear, strings.clear)
                            }
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    shape = RoundedCornerShape(16.dp),
                    singleLine = true,
                )

                // Content
                if (uiState.isLoading) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center,
                    ) {
                        CircularProgressIndicator()
                    }
                } else if (uiState.filteredRequests.isEmpty()) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center,
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("🙋", style = MaterialTheme.typography.displayLarge)
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                strings.noRequestsYet,
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                            Text(
                                strings.postRequestHint,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.outline,
                            )
                        }
                    }
                } else {
                    LazyColumn(
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                    ) {
                        items(uiState.filteredRequests) { request ->
                            RideRequestCard(
                                request = request,
                                onClick = {
                                    navigator.push(
                                        RideRequestDetailScreen(
                                            request = request,
                                            currentUser = currentUser,
                                            rideRequestScreenModel = rideRequestScreenModel,
                                        )
                                    )
                                },
                            )
                        }
                    }
                }
            }
        }
    }
}
