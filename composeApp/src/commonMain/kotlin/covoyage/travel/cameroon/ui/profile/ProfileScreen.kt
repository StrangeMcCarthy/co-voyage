package covoyage.travel.cameroon.ui.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import cafe.adriel.voyager.core.screen.Screen
import covoyage.travel.cameroon.data.model.UserProfile
import covoyage.travel.cameroon.data.model.UserType
import covoyage.travel.cameroon.i18n.Language
import covoyage.travel.cameroon.i18n.LocalLanguage
import covoyage.travel.cameroon.i18n.LocalStrings
import covoyage.travel.cameroon.ui.components.CoVoyageOutlinedButton
import covoyage.travel.cameroon.ui.components.CoVoyageTextField
import cafe.adriel.voyager.koin.koinScreenModel
import androidx.compose.ui.text.input.KeyboardType

class ProfileScreen(
    private val currentUser: UserProfile,
    private val onLogout: () -> Unit,
    private val onLanguageChange: (Language) -> Unit,
) : Screen {

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    override fun Content() {
        val strings = LocalStrings.current
        val currentLanguage = LocalLanguage.current
        val profileScreenModel = koinScreenModel<ProfileScreenModel>()
        val uiState by profileScreenModel.uiState.collectAsState()
        
        var savedCheckoutNumber by remember { mutableStateOf(currentUser.payoutPhoneNumber) }
        var checkoutNumber by remember { mutableStateOf(savedCheckoutNumber) }

        LaunchedEffect(uiState.isSuccess) {
            if (uiState.isSuccess) {
                savedCheckoutNumber = checkoutNumber
            }
        }

        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text(strings.profile) },
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
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Spacer(modifier = Modifier.height(24.dp))

                // Avatar
                Box(
                    modifier = Modifier
                        .size(80.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primaryContainer),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = currentUser.name.take(1).uppercase(),
                        style = MaterialTheme.typography.headlineLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = currentUser.name,
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                )

                // User type badge
                Box(
                    modifier = Modifier
                        .padding(top = 4.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(
                            if (currentUser.userType == UserType.DRIVER)
                                MaterialTheme.colorScheme.primaryContainer
                            else
                                MaterialTheme.colorScheme.secondaryContainer
                        )
                        .padding(horizontal = 12.dp, vertical = 4.dp),
                ) {
                    Text(
                        text = if (currentUser.userType == UserType.DRIVER) strings.driverBadge else strings.passengerBadge,
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Medium,
                    )
                }

                Spacer(modifier = Modifier.height(28.dp))

                // Info card
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surface,
                    ),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                ) {
                    Column(modifier = Modifier.padding(20.dp)) {
                        InfoRow(Icons.Default.Email, strings.email, currentUser.email)
                        HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp))
                        InfoRow(Icons.Default.Phone, strings.phone, currentUser.phone)

                        if (currentUser.userType == UserType.DRIVER) {
                            HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp))
                            InfoRow(Icons.Default.Badge, strings.drivingPermit, currentUser.drivingPermitNumber)
                            HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp))
                            InfoRow(Icons.Default.CreditCard, strings.greyCard, currentUser.greyCardNumber)
                            HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp))
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                CoVoyageTextField(
                                    value = checkoutNumber,
                                    onValueChange = { 
                                        checkoutNumber = it
                                        profileScreenModel.clearError() 
                                    },
                                    label = "CheckOut Number",
                                    leadingIcon = Icons.Default.PhoneAndroid,
                                    keyboardType = KeyboardType.Phone,
                                    modifier = Modifier.weight(1f)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                if (checkoutNumber != savedCheckoutNumber) {
                                    IconButton(
                                        onClick = { profileScreenModel.updatePayoutPhoneNumber(checkoutNumber) },
                                        enabled = !uiState.isLoading
                                    ) {
                                        if (uiState.isLoading) {
                                            CircularProgressIndicator(modifier = Modifier.size(24.dp))
                                        } else {
                                            Icon(Icons.Default.Save, contentDescription = "Save")
                                        }
                                    }
                                }
                            }
                            if (uiState.error.isNotBlank()) {
                                Text(
                                    text = uiState.error,
                                    color = MaterialTheme.colorScheme.error,
                                    style = MaterialTheme.typography.bodySmall,
                                    modifier = Modifier.padding(top = 4.dp, start = 44.dp)
                                )
                            }
                            if (uiState.isSuccess) {
                                Text(
                                    text = "Updated successfully",
                                    color = MaterialTheme.colorScheme.primary,
                                    style = MaterialTheme.typography.bodySmall,
                                    modifier = Modifier.padding(top = 4.dp, start = 44.dp)
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Language toggle
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surface,
                    ),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                ) {
                    Column(modifier = Modifier.padding(20.dp)) {
                        Text(
                            text = strings.language,
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.SemiBold,
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                        ) {
                            Language.entries.forEach { lang ->
                                val isSelected = lang == currentLanguage
                                FilterChip(
                                    selected = isSelected,
                                    onClick = { onLanguageChange(lang) },
                                    label = {
                                        Text(
                                            "${lang.flag} ${lang.displayName}",
                                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                        )
                                    },
                                    modifier = Modifier.weight(1f),
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                                        selectedLabelColor = MaterialTheme.colorScheme.primary,
                                    ),
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(32.dp))

                // Logout
                Box(modifier = Modifier.padding(horizontal = 16.dp)) {
                    CoVoyageOutlinedButton(
                        text = strings.signOut,
                        onClick = onLogout,
                        contentColor = MaterialTheme.colorScheme.error,
                    )
                }

                Spacer(modifier = Modifier.height(32.dp))
            }
        }
    }
}

@Composable
private fun InfoRow(
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
