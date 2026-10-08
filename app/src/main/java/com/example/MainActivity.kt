package com.example

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.data.database.HospitalDatabase
import com.example.data.repository.HospitalRepository
import com.example.ui.screens.AdminDashboardScreen
import com.example.ui.screens.AuditLogScreen
import com.example.ui.screens.LoginScreen
import com.example.ui.screens.NurseDashboardScreen
import com.example.ui.screens.PatientEntryFormScreen
import com.example.ui.state.Screen
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.viewmodel.HospitalViewModel
import com.example.ui.viewmodel.HospitalViewModelFactory

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val database = HospitalDatabase.getDatabase(applicationContext, lifecycleScope)
        val repository = HospitalRepository(
            userDao = database.userDao(),
            patientRecordDao = database.patientRecordDao(),
            auditLogDao = database.auditLogDao()
        )
        val viewModelFactory = HospitalViewModelFactory(repository)

        setContent {
            MyApplicationTheme {
                val viewModel: HospitalViewModel = viewModel(factory = viewModelFactory)
                HospitalApp(viewModel = viewModel)
            }
        }
    }
}

@Composable
fun HospitalApp(viewModel: HospitalViewModel) {
    val context = LocalContext.current
    val currentScreen by viewModel.currentScreen.collectAsState()
    val currentUser by viewModel.currentUser.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val errorMessage by viewModel.errorMessage.collectAsState()
    val toastMessage by viewModel.toastMessage.collectAsState()

    val snackbarHostState = remember { SnackbarHostState() }

    // Toast & Snackbar feedback
    LaunchedEffect(toastMessage) {
        toastMessage?.let { msg ->
            Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
            viewModel.clearFeedback()
        }
    }

    LaunchedEffect(errorMessage) {
        errorMessage?.let { err ->
            snackbarHostState.showSnackbar(err)
            viewModel.clearFeedback()
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        modifier = Modifier.fillMaxSize()
    ) { innerPadding ->
        Box(modifier = Modifier.fillMaxSize()) {
            when (currentScreen) {
                Screen.LOGIN,
                Screen.REGISTER_NURSE,
                Screen.ADMIN_REGISTRATION_GATE -> {
                    LoginScreen(
                        viewModel = viewModel,
                        isLoading = isLoading,
                        errorMessage = errorMessage,
                        toastMessage = toastMessage
                    )
                }

                Screen.NURSE_DASHBOARD,
                Screen.NURSE_RECORDS -> {
                    NurseDashboardScreen(
                        viewModel = viewModel,
                        currentUser = currentUser
                    )
                }

                Screen.NURSE_ENTRY_FORM -> {
                    PatientEntryFormScreen(
                        viewModel = viewModel,
                        currentUser = currentUser,
                        onNavigateBack = {
                            viewModel.handleBack()
                        }
                    )
                }

                Screen.ADMIN_DASHBOARD,
                Screen.ADMIN_ALL_RECORDS -> {
                    AdminDashboardScreen(
                        viewModel = viewModel,
                        currentUser = currentUser
                    )
                }

                Screen.ADMIN_AUDIT_LOGS -> {
                    AuditLogScreen(
                        viewModel = viewModel,
                        onNavigateBack = {
                            viewModel.handleBack()
                        }
                    )
                }
            }
        }
    }
}
