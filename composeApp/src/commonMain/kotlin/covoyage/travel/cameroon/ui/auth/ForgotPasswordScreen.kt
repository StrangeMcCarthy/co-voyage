package covoyage.travel.cameroon.ui.auth

import androidx.compose.animation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import covoyage.travel.cameroon.i18n.Language
import covoyage.travel.cameroon.i18n.LocalLanguage
import covoyage.travel.cameroon.i18n.LocalStrings
import covoyage.travel.cameroon.ui.components.CoVoyageButton
import covoyage.travel.cameroon.ui.components.CoVoyageTextField

class ForgotPasswordScreen(
    private val authScreenModel: AuthScreenModel,
    private val onLanguageChange: (Language) -> Unit,
) : Screen {

    @Composable
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow
        val uiState by authScreenModel.uiState.collectAsState()
        val strings = LocalStrings.current
        val currentLanguage = LocalLanguage.current
        var passwordVisible by remember { mutableStateOf(false) }

        Scaffold(
            topBar = {
                CenterAlignedTopAppBar(
                    title = { Text(strings.resetPassword) },
                    navigationIcon = {
                        IconButton(onClick = { 
                            if (uiState.resetSuccess) {
                                navigator.pop()
                            } else {
                                authScreenModel.resetForgotPasswordFlow()
                                navigator.pop()
                            }
                        }) {
                            Icon(Icons.Default.ArrowBack, contentDescription = strings.back)
                        }
                    }
                )
            }
        ) { padding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(horizontal = 24.dp)
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Spacer(modifier = Modifier.height(32.dp))

                Icon(
                    imageVector = when {
                        uiState.resetSuccess -> Icons.Default.CheckCircle
                        uiState.forgotPasswordStep == ForgotPasswordStep.NEW_PASSWORD -> Icons.Default.LockReset
                        uiState.forgotPasswordStep == ForgotPasswordStep.OTP -> Icons.Default.VerifiedUser
                        else -> Icons.Default.Email
                    },
                    contentDescription = null,
                    modifier = Modifier.size(80.dp),
                    color = if (uiState.resetSuccess) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.secondary
                )

                Spacer(modifier = Modifier.height(24.dp))

                if (uiState.resetSuccess) {
                    Text(
                        text = strings.passwordResetSuccess,
                        style = MaterialTheme.typography.bodyLarge,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(48.dp))
                    CoVoyageButton(
                        text = strings.backToLogin,
                        onClick = {
                            authScreenModel.resetForgotPasswordFlow()
                            navigator.pop()
                        }
                    )
                } else {
                    AnimatedContent(targetState = uiState.forgotPasswordStep) { step ->
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            when (step) {
                                ForgotPasswordStep.EMAIL -> {
                                    Text(
                                        text = strings.enterEmailToReset,
                                        style = MaterialTheme.typography.bodyMedium,
                                        textAlign = TextAlign.Center,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    Spacer(modifier = Modifier.height(32.dp))
                                    CoVoyageTextField(
                                        value = uiState.forgotEmail,
                                        onValueChange = authScreenModel::updateForgotEmail,
                                        label = strings.email,
                                        leadingIcon = Icons.Default.Email,
                                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email)
                                    )
                                    Spacer(modifier = Modifier.height(32.dp))
                                    CoVoyageButton(
                                        text = strings.sendResetCode,
                                        onClick = { authScreenModel.requestOtp() },
                                        isLoading = uiState.isLoading
                                    )
                                }
                                ForgotPasswordStep.OTP -> {
                                    Text(
                                        text = strings.enterResetCode,
                                        style = MaterialTheme.typography.bodyMedium,
                                        textAlign = TextAlign.Center,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    Spacer(modifier = Modifier.height(32.dp))
                                    CoVoyageTextField(
                                        value = uiState.resetOtp,
                                        onValueChange = { if (it.length <= 6) authScreenModel.updateResetOtp(it) },
                                        label = strings.verificationCode,
                                        leadingIcon = Icons.Default.VpnKey,
                                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                                    )
                                    Spacer(modifier = Modifier.height(32.dp))
                                    CoVoyageButton(
                                        text = strings.verifyCode,
                                        onClick = { authScreenModel.verifyOtp() },
                                        isLoading = uiState.isLoading
                                    )
                                }
                                ForgotPasswordStep.NEW_PASSWORD -> {
                                    Text(
                                        text = strings.createNewPassword,
                                        style = MaterialTheme.typography.bodyMedium,
                                        textAlign = TextAlign.Center,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    Spacer(modifier = Modifier.height(32.dp))
                                    OutlinedTextField(
                                        value = uiState.resetNewPassword,
                                        onValueChange = authScreenModel::updateResetNewPassword,
                                        label = { Text(strings.newPassword) },
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
                                    Spacer(modifier = Modifier.height(12.dp))
                                    OutlinedTextField(
                                        value = uiState.resetConfirmPassword,
                                        onValueChange = authScreenModel::updateResetConfirmPassword,
                                        label = { Text(strings.confirmPassword) },
                                        leadingIcon = { Icon(Icons.Default.Lock, null) },
                                        visualTransformation = PasswordVisualTransformation(),
                                        singleLine = true,
                                        modifier = Modifier.fillMaxWidth(),
                                        shape = RoundedCornerShape(12.dp),
                                    )
                                    Spacer(modifier = Modifier.height(32.dp))
                                    CoVoyageButton(
                                        text = strings.resetPassword,
                                        onClick = { authScreenModel.resetPassword() },
                                        isLoading = uiState.isLoading
                                    )
                                }
                            }
                        }
                    }
                }

                if (uiState.error.isNotBlank()) {
                    Spacer(modifier = Modifier.height(24.dp))
                    Text(
                        text = uiState.error,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall,
                        textAlign = TextAlign.Center
                    )
                }

                Spacer(modifier = Modifier.height(32.dp))

                // Language toggle
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

                Spacer(modifier = Modifier.height(32.dp))
            }
        }
    }
}
