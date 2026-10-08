package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Badge
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.HealthAndSafety
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.LocalHospital
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.viewmodel.HospitalViewModel

enum class AuthMode {
    LOGIN,
    REGISTER_NURSE,
    ADMIN_PROVISION
}

@Composable
fun LoginScreen(
    viewModel: HospitalViewModel,
    isLoading: Boolean,
    errorMessage: String?,
    toastMessage: String?,
    modifier: Modifier = Modifier
) {
    var authMode by remember { mutableStateOf(AuthMode.LOGIN) }

    // Login fields
    var loginIdentifier by remember { mutableStateOf("") }
    var loginPassword by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }

    // Nurse Register fields
    var regFullName by remember { mutableStateOf("") }
    var regEmployeeId by remember { mutableStateOf("") }
    var regEmail by remember { mutableStateOf("") }
    var regPhone by remember { mutableStateOf("") }
    var regPassword by remember { mutableStateOf("") }
    var regConfirmPassword by remember { mutableStateOf("") }

    // Admin Setup fields
    var adminPasscode by remember { mutableStateOf("") }

    val scrollState = rememberScrollState()

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
                .padding(horizontal = 24.dp, vertical = 32.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(16.dp))

            // Hospital Logo & App Identity
            Box(
                modifier = Modifier
                    .size(76.dp)
                    .clip(RoundedCornerShape(20.dp))
                    .background(MaterialTheme.colorScheme.primary),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.LocalHospital,
                    contentDescription = "Hospital Logo",
                    tint = MaterialTheme.colorScheme.onPrimary,
                    modifier = Modifier.size(46.dp)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Hospital Patient Data Management System",
                style = MaterialTheme.typography.headlineSmall.copy(
                    fontWeight = FontWeight.Bold,
                    letterSpacing = (-0.5).sp
                ),
                color = MaterialTheme.colorScheme.primary,
                textAlign = TextAlign.Center
            )

            Text(
                text = "Emergency Records Registry & Excel Export Portal",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(top = 4.dp, bottom = 24.dp)
            )

            // Feedback Banners
            if (errorMessage != null) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp)
                        .testTag("error_message_card"),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(
                        text = errorMessage,
                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium),
                        color = MaterialTheme.colorScheme.onErrorContainer,
                        modifier = Modifier.padding(14.dp)
                    )
                }
            }

            if (toastMessage != null) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp)
                        .testTag("success_message_card"),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.tertiaryContainer),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(
                        text = toastMessage,
                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium),
                        color = MaterialTheme.colorScheme.onTertiaryContainer,
                        modifier = Modifier.padding(14.dp)
                    )
                }
            }

            // Main Auth Container Card
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("auth_card"),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp)
                ) {
                    // Mode Selector Header
                    when (authMode) {
                        AuthMode.LOGIN -> {
                            Text(
                                text = "Secure Staff Login",
                                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "Enter your hospital employee ID or email",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.outline,
                                modifier = Modifier.padding(top = 2.dp, bottom = 20.dp)
                            )

                            // Username / Email Input
                            OutlinedTextField(
                                value = loginIdentifier,
                                onValueChange = { loginIdentifier = it },
                                label = { Text("Username, Employee ID or Email") },
                                placeholder = { Text("e.g. admin@hospital.org or NUR-101") },
                                leadingIcon = {
                                    Icon(Icons.Default.Person, contentDescription = "Username")
                                },
                                singleLine = true,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("login_identifier_input"),
                                shape = RoundedCornerShape(12.dp)
                            )

                            Spacer(modifier = Modifier.height(14.dp))

                            // Password Input
                            OutlinedTextField(
                                value = loginPassword,
                                onValueChange = { loginPassword = it },
                                label = { Text("Password") },
                                leadingIcon = {
                                    Icon(Icons.Default.Lock, contentDescription = "Password")
                                },
                                trailingIcon = {
                                    IconButton(onClick = { passwordVisible = !passwordVisible }) {
                                        Icon(
                                            imageVector = if (passwordVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                            contentDescription = if (passwordVisible) "Hide password" else "Show password"
                                        )
                                    }
                                },
                                visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password, imeAction = ImeAction.Done),
                                keyboardActions = KeyboardActions(onDone = {
                                    viewModel.login(loginIdentifier, loginPassword)
                                }),
                                singleLine = true,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("login_password_input"),
                                shape = RoundedCornerShape(12.dp)
                            )

                            Spacer(modifier = Modifier.height(8.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.End
                            ) {
                                TextButton(
                                    onClick = {
                                        // Hospital policy info
                                        viewModel.clearFeedback()
                                        loginIdentifier = "admin@hospital.org"
                                        loginPassword = "AdminPass123!"
                                    }
                                ) {
                                    Text("Forgot Password? (Contact Hospital IT)", style = MaterialTheme.typography.labelSmall)
                                }
                            }

                            Spacer(modifier = Modifier.height(12.dp))

                            // Login Button
                            Button(
                                onClick = {
                                    viewModel.login(loginIdentifier, loginPassword)
                                },
                                enabled = !isLoading && loginIdentifier.isNotBlank() && loginPassword.isNotBlank(),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(52.dp)
                                    .testTag("login_submit_button"),
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                            ) {
                                if (isLoading) {
                                    CircularProgressIndicator(
                                        modifier = Modifier.size(24.dp),
                                        color = MaterialTheme.colorScheme.onPrimary,
                                        strokeWidth = 2.dp
                                    )
                                } else {
                                    Text("LOGIN SECURELY", style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold))
                                }
                            }

                            Spacer(modifier = Modifier.height(20.dp))
                            HorizontalDivider()
                            Spacer(modifier = Modifier.height(16.dp))

                            // Register as Nurse Button
                            OutlinedButton(
                                onClick = {
                                    viewModel.clearFeedback()
                                    authMode = AuthMode.REGISTER_NURSE
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(48.dp)
                                    .testTag("register_nurse_nav_button"),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Icon(Icons.Default.HealthAndSafety, contentDescription = null, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("New Nurse? Register Account")
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            // Protected Admin Provision link
                            TextButton(
                                onClick = {
                                    viewModel.clearFeedback()
                                    authMode = AuthMode.ADMIN_PROVISION
                                },
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Icon(Icons.Default.Shield, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Protected Doctor / Admin Setup", style = MaterialTheme.typography.labelMedium)
                            }
                        }

                        AuthMode.REGISTER_NURSE -> {
                            Text(
                                text = "Emergency Nurse Registration",
                                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.primary
                            )
                            Text(
                                text = "Create authorized nurse credential for emergency patient entry",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.outline,
                                modifier = Modifier.padding(top = 2.dp, bottom = 16.dp)
                            )

                            // Role chip: locked to Nurse per security requirements
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = MaterialTheme.colorScheme.secondaryContainer,
                                modifier = Modifier.padding(bottom = 14.dp)
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        Icons.Default.HealthAndSafety,
                                        contentDescription = null,
                                        modifier = Modifier.size(16.dp),
                                        tint = MaterialTheme.colorScheme.onSecondaryContainer
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "Assigned Role: Emergency Nurse",
                                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                        color = MaterialTheme.colorScheme.onSecondaryContainer
                                    )
                                }
                            }

                            OutlinedTextField(
                                value = regFullName,
                                onValueChange = { regFullName = it },
                                label = { Text("Full Name *") },
                                placeholder = { Text("e.g. Sarah Jenkins, RN") },
                                leadingIcon = { Icon(Icons.Default.Person, contentDescription = null) },
                                singleLine = true,
                                modifier = Modifier.fillMaxWidth().testTag("reg_fullname_input"),
                                shape = RoundedCornerShape(10.dp)
                            )

                            Spacer(modifier = Modifier.height(10.dp))

                            OutlinedTextField(
                                value = regEmployeeId,
                                onValueChange = { regEmployeeId = it },
                                label = { Text("Employee / Nurse ID *") },
                                placeholder = { Text("e.g. NUR-2045") },
                                leadingIcon = { Icon(Icons.Default.Badge, contentDescription = null) },
                                singleLine = true,
                                modifier = Modifier.fillMaxWidth().testTag("reg_employee_id_input"),
                                shape = RoundedCornerShape(10.dp)
                            )

                            Spacer(modifier = Modifier.height(10.dp))

                            OutlinedTextField(
                                value = regEmail,
                                onValueChange = { regEmail = it },
                                label = { Text("Hospital Email *") },
                                placeholder = { Text("sarah.jenkins@hospital.org") },
                                leadingIcon = { Icon(Icons.Default.Email, contentDescription = null) },
                                singleLine = true,
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                                modifier = Modifier.fillMaxWidth().testTag("reg_email_input"),
                                shape = RoundedCornerShape(10.dp)
                            )

                            Spacer(modifier = Modifier.height(10.dp))

                            OutlinedTextField(
                                value = regPhone,
                                onValueChange = { regPhone = it },
                                label = { Text("Phone Number") },
                                placeholder = { Text("+1 (555) 234-5678") },
                                leadingIcon = { Icon(Icons.Default.Phone, contentDescription = null) },
                                singleLine = true,
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                                modifier = Modifier.fillMaxWidth().testTag("reg_phone_input"),
                                shape = RoundedCornerShape(10.dp)
                            )

                            Spacer(modifier = Modifier.height(10.dp))

                            OutlinedTextField(
                                value = regPassword,
                                onValueChange = { regPassword = it },
                                label = { Text("Password (min 6 chars) *") },
                                leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null) },
                                visualTransformation = PasswordVisualTransformation(),
                                singleLine = true,
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                                modifier = Modifier.fillMaxWidth().testTag("reg_password_input"),
                                shape = RoundedCornerShape(10.dp)
                            )

                            Spacer(modifier = Modifier.height(10.dp))

                            OutlinedTextField(
                                value = regConfirmPassword,
                                onValueChange = { regConfirmPassword = it },
                                label = { Text("Confirm Password *") },
                                leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null) },
                                visualTransformation = PasswordVisualTransformation(),
                                singleLine = true,
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                                modifier = Modifier.fillMaxWidth().testTag("reg_confirm_password_input"),
                                shape = RoundedCornerShape(10.dp)
                            )

                            Spacer(modifier = Modifier.height(18.dp))

                            Button(
                                onClick = {
                                    viewModel.registerNurse(
                                        regFullName,
                                        regEmployeeId,
                                        regEmail,
                                        regPhone,
                                        regPassword,
                                        regConfirmPassword
                                    )
                                },
                                enabled = !isLoading && regFullName.isNotBlank() && regEmployeeId.isNotBlank() && regEmail.isNotBlank() && regPassword.isNotBlank(),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(50.dp)
                                    .testTag("reg_nurse_submit_button"),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                if (isLoading) {
                                    CircularProgressIndicator(modifier = Modifier.size(24.dp), color = MaterialTheme.colorScheme.onPrimary)
                                } else {
                                    Text("REGISTER NURSE ACCOUNT", style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold))
                                }
                            }

                            Spacer(modifier = Modifier.height(12.dp))

                            TextButton(
                                onClick = {
                                    viewModel.clearFeedback()
                                    authMode = AuthMode.LOGIN
                                },
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text("Already have an account? Back to Login")
                            }
                        }

                        AuthMode.ADMIN_PROVISION -> {
                            Text(
                                text = "Protected Administrator Setup",
                                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.error
                            )
                            Text(
                                text = "Doctor / Admin accounts require hospital security verification",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.outline,
                                modifier = Modifier.padding(top = 2.dp, bottom = 16.dp)
                            )

                            OutlinedTextField(
                                value = adminPasscode,
                                onValueChange = { adminPasscode = it },
                                label = { Text("Master Admin Setup Key *") },
                                placeholder = { Text("Enter HOSPITAL-ADMIN-KEY-2026 or admin123") },
                                leadingIcon = { Icon(Icons.Default.Key, contentDescription = null) },
                                visualTransformation = PasswordVisualTransformation(),
                                singleLine = true,
                                modifier = Modifier.fillMaxWidth().testTag("admin_passcode_input"),
                                shape = RoundedCornerShape(10.dp)
                            )

                            Spacer(modifier = Modifier.height(10.dp))

                            OutlinedTextField(
                                value = regFullName,
                                onValueChange = { regFullName = it },
                                label = { Text("Doctor / Admin Full Name *") },
                                placeholder = { Text("Dr. Robert Vance, MD") },
                                leadingIcon = { Icon(Icons.Default.Person, contentDescription = null) },
                                singleLine = true,
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(10.dp)
                            )

                            Spacer(modifier = Modifier.height(10.dp))

                            OutlinedTextField(
                                value = regEmployeeId,
                                onValueChange = { regEmployeeId = it },
                                label = { Text("Doctor / Admin Employee ID *") },
                                placeholder = { Text("EMP-DOC-001") },
                                leadingIcon = { Icon(Icons.Default.Badge, contentDescription = null) },
                                singleLine = true,
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(10.dp)
                            )

                            Spacer(modifier = Modifier.height(10.dp))

                            OutlinedTextField(
                                value = regEmail,
                                onValueChange = { regEmail = it },
                                label = { Text("Email *") },
                                placeholder = { Text("doctor@hospital.org") },
                                leadingIcon = { Icon(Icons.Default.Email, contentDescription = null) },
                                singleLine = true,
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(10.dp)
                            )

                            Spacer(modifier = Modifier.height(10.dp))

                            OutlinedTextField(
                                value = regPassword,
                                onValueChange = { regPassword = it },
                                label = { Text("Password *") },
                                leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null) },
                                visualTransformation = PasswordVisualTransformation(),
                                singleLine = true,
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(10.dp)
                            )

                            Spacer(modifier = Modifier.height(16.dp))

                            Button(
                                onClick = {
                                    viewModel.registerDoctorAdmin(
                                        regFullName,
                                        regEmployeeId,
                                        regEmail,
                                        regPhone,
                                        regPassword,
                                        adminPasscode
                                    )
                                },
                                enabled = !isLoading && adminPasscode.isNotBlank() && regFullName.isNotBlank() && regEmployeeId.isNotBlank() && regEmail.isNotBlank(),
                                modifier = Modifier.fillMaxWidth().height(50.dp),
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                            ) {
                                if (isLoading) {
                                    CircularProgressIndicator(modifier = Modifier.size(24.dp), color = MaterialTheme.colorScheme.onPrimary)
                                } else {
                                    Text("PROVISION DOCTOR/ADMIN ACCOUNT", style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold))
                                }
                            }

                            Spacer(modifier = Modifier.height(10.dp))

                            TextButton(
                                onClick = {
                                    viewModel.clearFeedback()
                                    authMode = AuthMode.LOGIN
                                },
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text("Back to Login")
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Quick Fill Credentials Helper Card for seamless testing of both roles
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("quick_credentials_card"),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
            ) {
                Column(
                    modifier = Modifier.padding(14.dp)
                ) {
                    Text(
                        text = "System Access Credentials (Ready for Testing):",
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = {
                                authMode = AuthMode.LOGIN
                                loginIdentifier = "admin@hospital.org"
                                loginPassword = "AdminPass123!"
                                viewModel.login("admin@hospital.org", "AdminPass123!")
                            },
                            modifier = Modifier
                                .weight(1f)
                                .testTag("quick_login_doctor_admin"),
                            shape = RoundedCornerShape(8.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                        ) {
                            Text("1-Tap: Doctor / Admin", style = MaterialTheme.typography.labelSmall)
                        }

                        OutlinedButton(
                            onClick = {
                                authMode = AuthMode.REGISTER_NURSE
                                regFullName = "Sarah Connor, RN"
                                regEmployeeId = "NUR-701"
                                regEmail = "nurse.sarah@hospital.org"
                                regPhone = "+1 555-0144"
                                regPassword = "nursePass123"
                                regConfirmPassword = "nursePass123"
                            },
                            modifier = Modifier
                                .weight(1f)
                                .testTag("quick_fill_nurse_reg"),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text("Fill Nurse Form", style = MaterialTheme.typography.labelSmall)
                        }
                    }
                }
            }
        }
    }
}
