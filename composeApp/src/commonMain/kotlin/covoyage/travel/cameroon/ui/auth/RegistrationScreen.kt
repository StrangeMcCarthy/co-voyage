package covoyage.travel.cameroon.ui.auth

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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import covoyage.travel.cameroon.data.model.UserType
import covoyage.travel.cameroon.i18n.LocalStrings
import covoyage.travel.cameroon.ui.components.CoVoyageButton
import covoyage.travel.cameroon.ui.components.CoVoyageTextField

class RegistrationScreen(
    private val authScreenModel: AuthScreenModel,
) : Screen {

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow
        val uiState by authScreenModel.uiState.collectAsState()
        var passwordVisible by remember { mutableStateOf(false) }
        val strings = LocalStrings.current

        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text(strings.createAccount) },
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

                // Error
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

                if (uiState.error.isNotBlank()) Spacer(modifier = Modifier.height(8.dp))

                // User type selector
                Text(
                    text = strings.iAmA,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                )
                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    UserTypeCard(
                        emoji = "🧳",
                        title = strings.passenger,
                        subtitle = strings.findRides,
                        selected = uiState.regUserType == UserType.PASSENGER,
                        onClick = { authScreenModel.updateRegUserType(UserType.PASSENGER) },
                        modifier = Modifier.weight(1f),
                    )
                    UserTypeCard(
                        emoji = "🚗",
                        title = strings.driver,
                        subtitle = strings.offerRides,
                        selected = uiState.regUserType == UserType.DRIVER,
                        onClick = { authScreenModel.updateRegUserType(UserType.DRIVER) },
                        modifier = Modifier.weight(1f),
                    )
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Fields
                CoVoyageTextField(
                    value = uiState.regName,
                    onValueChange = authScreenModel::updateRegName,
                    label = strings.fullName,
                    leadingIcon = Icons.Default.Person,
                )
                Spacer(modifier = Modifier.height(10.dp))

                CoVoyageTextField(
                    value = uiState.regEmail,
                    onValueChange = authScreenModel::updateRegEmail,
                    label = strings.email,
                    leadingIcon = Icons.Default.Email,
                )
                Spacer(modifier = Modifier.height(10.dp))

                CoVoyageTextField(
                    value = uiState.regPhone,
                    onValueChange = authScreenModel::updateRegPhone,
                    label = strings.phoneNumber,
                    leadingIcon = Icons.Default.Phone,
                )
                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = uiState.regPassword,
                    onValueChange = authScreenModel::updateRegPassword,
                    label = { Text(strings.password) },
                    leadingIcon = { Icon(Icons.Default.Lock, null) },
                    trailingIcon = {
                        IconButton(onClick = { passwordVisible = !passwordVisible }) {
                            Icon(
                                if (passwordVisible) Icons.Default.VisibilityOff
                                else Icons.Default.Visibility,
                                contentDescription = null,
                            )
                        }
                    },
                    visualTransformation = if (passwordVisible) VisualTransformation.None
                    else PasswordVisualTransformation(),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                )
                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = uiState.regConfirmPassword,
                    onValueChange = authScreenModel::updateRegConfirmPassword,
                    label = { Text(strings.confirmPassword) },
                    leadingIcon = { Icon(Icons.Default.Lock, null) },
                    visualTransformation = PasswordVisualTransformation(),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                )

                // Driver fields
                AnimatedVisibility(visible = uiState.regUserType == UserType.DRIVER) {
                    Column {
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = strings.driverDocuments,
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.primary,
                        )
                        Spacer(modifier = Modifier.height(8.dp))

                        CoVoyageTextField(
                            value = uiState.regDrivingPermit,
                            onValueChange = authScreenModel::updateRegDrivingPermit,
                            label = strings.drivingPermitNumber,
                            leadingIcon = Icons.Default.Badge,
                        )
                        Spacer(modifier = Modifier.height(10.dp))

                        CoVoyageTextField(
                            value = uiState.regGreyCard,
                            onValueChange = authScreenModel::updateRegGreyCard,
                            label = strings.greyCardNumber,
                            leadingIcon = Icons.Default.CreditCard,
                        )
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                CoVoyageButton(
                    text = if (uiState.regUserType == UserType.DRIVER) strings.registerAsDriver else strings.registerAsPassenger,
                    onClick = { authScreenModel.register() },
                    isLoading = uiState.isLoading,
                )

                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = strings.alreadyHaveAccount,
                        style = MaterialTheme.typography.bodyMedium,
                    )
                    TextButton(onClick = { navigator.pop() }) {
                        Text(strings.signIn, fontWeight = FontWeight.Bold)
                    }
                }

                Spacer(modifier = Modifier.height(32.dp))
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun UserTypeCard(
    emoji: String,
    title: String,
    subtitle: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Card(
        onClick = onClick,
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (selected)
                MaterialTheme.colorScheme.primaryContainer
            else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
        ),
        border = if (selected) androidx.compose.foundation.BorderStroke(
            2.dp,
            MaterialTheme.colorScheme.primary,
        ) else null,
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(emoji, style = MaterialTheme.typography.headlineLarge)
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                title,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
            )
            Text(
                subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}
