package com.covoyage.android.ui.screens.auth

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import com.covoyage.domain.model.UserRole
import com.covoyage.presentation.auth.AuthViewModel
import org.koin.androidx.compose.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RegisterScreen(
    onNavigateToLogin: () -> Unit,
    onRegisterSuccess: () -> Unit,
    viewModel: AuthViewModel = koinViewModel()
) {
    var name by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var phoneNumber by remember { mutableStateOf("") }
    var town by remember { mutableStateOf("") }
    var selectedRole by remember { mutableStateOf(UserRole.PASSENGER) }
    var driverLicenseNumber by remember { mutableStateOf("") }
    var idNumber by remember { mutableStateOf("") }
    var localError by remember { mutableStateOf<String?>(null) }

    val authState by viewModel.uiState.collectAsState()

    LaunchedEffect(authState.isSuccess) {
        if (authState.isSuccess) {
            onRegisterSuccess()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(32.dp))

        Text(
            text = "Create Account",
            style = MaterialTheme.typography.displaySmall,
            color = MaterialTheme.colorScheme.primary
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "Join the Co-Voyage community",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f)
        )

        Spacer(modifier = Modifier.height(32.dp))

        // Name
        OutlinedTextField(
            value = name,
            onValueChange = { name = it },
            label = { Text("Full Name") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )

        Spacer(modifier = Modifier.height(12.dp))

        // Email
        OutlinedTextField(
            value = email,
            onValueChange = { email = it },
            label = { Text("Email") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )

        Spacer(modifier = Modifier.height(12.dp))

        // Phone
        OutlinedTextField(
            value = phoneNumber,
            onValueChange = { phoneNumber = it },
            label = { Text("Phone Number") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            placeholder = { Text("+237 ...") }
        )

        Spacer(modifier = Modifier.height(12.dp))

        // Town
        OutlinedTextField(
            value = town,
            onValueChange = { town = it },
            label = { Text("Town") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            placeholder = { Text("e.g. Douala, Yaoundé, Bamenda") }
        )

        Spacer(modifier = Modifier.height(12.dp))

        // Password
        OutlinedTextField(
            value = password,
            onValueChange = { password = it },
            label = { Text("Password") },
            modifier = Modifier.fillMaxWidth(),
            visualTransformation = PasswordVisualTransformation(),
            singleLine = true
        )

        Spacer(modifier = Modifier.height(12.dp))

        // Confirm password
        OutlinedTextField(
            value = confirmPassword,
            onValueChange = { confirmPassword = it },
            label = { Text("Confirm Password") },
            modifier = Modifier.fillMaxWidth(),
            visualTransformation = PasswordVisualTransformation(),
            singleLine = true
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Role selector
        Text(
            text = "I want to",
            style = MaterialTheme.typography.titleSmall,
            color = MaterialTheme.colorScheme.onBackground
        )

        Spacer(modifier = Modifier.height(8.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            FilterChip(
                selected = selectedRole == UserRole.PASSENGER,
                onClick = { selectedRole = UserRole.PASSENGER },
                label = { Text("🧳 Travel") },
                modifier = Modifier.weight(1f)
            )
            FilterChip(
                selected = selectedRole == UserRole.DRIVER,
                onClick = { selectedRole = UserRole.DRIVER },
                label = { Text("🚗 Drive") },
                modifier = Modifier.weight(1f)
            )
        }

        // Driver-specific fields
        if (selectedRole == UserRole.DRIVER) {
            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Driver Information",
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.primary
            )

            Spacer(modifier = Modifier.height(8.dp))

            OutlinedTextField(
                value = driverLicenseNumber,
                onValueChange = { driverLicenseNumber = it },
                label = { Text("Driver License Number") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            Spacer(modifier = Modifier.height(12.dp))

            OutlinedTextField(
                value = idNumber,
                onValueChange = { idNumber = it },
                label = { Text("National ID Number") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Register button
        Button(
            onClick = {
                localError = null
                when {
                    name.length < 2 -> localError = "Name must be at least 2 characters"
                    !email.contains("@") -> localError = "Please enter a valid email"
                    password.length < 6 -> localError = "Password must be at least 6 characters"
                    password != confirmPassword -> localError = "Passwords do not match"
                    phoneNumber.isBlank() -> localError = "Phone number is required"
                    town.isBlank() -> localError = "Town is required"
                    selectedRole == UserRole.DRIVER && driverLicenseNumber.isBlank() ->
                        localError = "Driver license number is required"
                    else -> viewModel.register(
                        name = name,
                        email = email,
                        password = password,
                        phoneNumber = phoneNumber,
                        town = town,
                        role = selectedRole,
                        driverLicenseNumber = driverLicenseNumber.ifBlank { null },
                        idNumber = idNumber.ifBlank { null }
                    )
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            enabled = !authState.isLoading
        ) {
            if (authState.isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(24.dp),
                    color = MaterialTheme.colorScheme.onPrimary
                )
            } else {
                Text("Create Account")
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        TextButton(onClick = onNavigateToLogin) {
            Text("Already have an account? Sign In")
        }

        // Error display
        val error = localError ?: authState.error
        if (error != null) {
            Spacer(modifier = Modifier.height(16.dp))
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.errorContainer
                ),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = error,
                    color = MaterialTheme.colorScheme.onErrorContainer,
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.padding(12.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))
    }
}
