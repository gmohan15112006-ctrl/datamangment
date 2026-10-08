package com.example.ui.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.model.AuditLogEntity
import com.example.data.model.PatientRecordEntity
import com.example.data.model.UserEntity
import com.example.data.model.UserRole
import com.example.data.repository.HospitalRepository
import com.example.ui.state.FilterCriteria
import com.example.ui.state.HospitalDashboardStats
import com.example.ui.state.Screen
import com.example.ui.state.SortField
import com.example.util.ExcelExporter
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class HospitalViewModel(
    private val repository: HospitalRepository
) : ViewModel() {

    // Current Session
    private val _currentUser = MutableStateFlow<UserEntity?>(null)
    val currentUser: StateFlow<UserEntity?> = _currentUser.asStateFlow()

    // Screen State
    private val _currentScreen = MutableStateFlow(Screen.LOGIN)
    val currentScreen: StateFlow<Screen> = _currentScreen.asStateFlow()

    // Navigation Back Stack for clean back button handling
    private val _screenStack = MutableStateFlow<List<Screen>>(listOf(Screen.LOGIN))

    // UI Feedback
    private val _toastMessage = MutableStateFlow<String?>(null)
    val toastMessage: StateFlow<String?> = _toastMessage.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    // Raw Records from Database
    val rawRecords: StateFlow<List<PatientRecordEntity>> = repository.getAllActiveRecords()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val auditLogs: StateFlow<List<AuditLogEntity>> = repository.getAllAuditLogs()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Filtering & Sorting
    private val _filters = MutableStateFlow(FilterCriteria())
    val filters: StateFlow<FilterCriteria> = _filters.asStateFlow()

    private val _sortField = MutableStateFlow(SortField.SERIAL_NUMBER)
    val sortField: StateFlow<SortField> = _sortField.asStateFlow()

    private val _sortAscending = MutableStateFlow(true)
    val sortAscending: StateFlow<Boolean> = _sortAscending.asStateFlow()

    // Selected Record IDs for Admin actions / selective Excel export
    private val _selectedRecordIds = MutableStateFlow<Set<Long>>(emptySet())
    val selectedRecordIds: StateFlow<Set<Long>> = _selectedRecordIds.asStateFlow()

    // Modal / Dialog States
    private val _detailRecord = MutableStateFlow<PatientRecordEntity?>(null)
    val detailRecord: StateFlow<PatientRecordEntity?> = _detailRecord.asStateFlow()

    private val _editingRecord = MutableStateFlow<PatientRecordEntity?>(null)
    val editingRecord: StateFlow<PatientRecordEntity?> = _editingRecord.asStateFlow()

    private val _recordToDelete = MutableStateFlow<PatientRecordEntity?>(null)
    val recordToDelete: StateFlow<PatientRecordEntity?> = _recordToDelete.asStateFlow()

    private val _showExportDialog = MutableStateFlow(false)
    val showExportDialog: StateFlow<Boolean> = _showExportDialog.asStateFlow()

    private val _exportedFile = MutableStateFlow<File?>(null)
    val exportedFile: StateFlow<File?> = _exportedFile.asStateFlow()

    // Filtered & Sorted Records for Admin
    val filteredAdminRecords: StateFlow<List<PatientRecordEntity>> = combine(
        rawRecords,
        _filters,
        _sortField,
        _sortAscending
    ) { records, criteria, sort, asc ->
        var list = records

        if (criteria.query.isNotBlank()) {
            val q = criteria.query.trim().lowercase()
            list = list.filter {
                it.serialNumber.toString().contains(q) ||
                    it.ipNumber.lowercase().contains(q) ||
                    it.name.lowercase().contains(q) ||
                    it.diagnosis.lowercase().contains(q) ||
                    it.broadSpecialityCategory.lowercase().contains(q) ||
                    it.medicolegalCategory.lowercase().contains(q) ||
                    it.transferredOut.lowercase().contains(q) ||
                    it.createdByNurseName.lowercase().contains(q)
            }
        }

        if (criteria.broadSpecialty != null) {
            list = list.filter { it.broadSpecialityCategory.equals(criteria.broadSpecialty, ignoreCase = true) }
        }

        if (criteria.medicolegal != null) {
            list = list.filter { it.medicolegalCategory.equals(criteria.medicolegal, ignoreCase = true) }
        }

        if (criteria.pillarStatus != null) {
            list = list.filter { it.pillarStatus.equals(criteria.pillarStatus, ignoreCase = true) }
        }

        if (criteria.transferredOut != null) {
            if (criteria.transferredOut == "YES") {
                list = list.filter { !it.transferredOut.equals("NO", ignoreCase = true) && !it.transferredOut.equals("NONE", ignoreCase = true) && it.transferredOut.isNotBlank() }
            } else {
                list = list.filter { it.transferredOut.equals("NO", ignoreCase = true) || it.transferredOut.equals("NONE", ignoreCase = true) || it.transferredOut.isBlank() }
            }
        }

        if (criteria.dateFilter == "TODAY") {
            val todayDate = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
            list = list.filter { it.admissionDateTime.startsWith(todayDate) }
        }

        if (criteria.nurseId != null) {
            list = list.filter { it.createdByNurseId == criteria.nurseId }
        }

        // Apply Sorting
        val sorted = when (sort) {
            SortField.SERIAL_NUMBER -> if (asc) list.sortedBy { it.serialNumber } else list.sortedByDescending { it.serialNumber }
            SortField.ADMISSION_DATE -> if (asc) list.sortedBy { it.admissionDateTime } else list.sortedByDescending { it.admissionDateTime }
            SortField.PATIENT_NAME -> if (asc) list.sortedBy { it.name.lowercase() } else list.sortedByDescending { it.name.lowercase() }
            SortField.AGE -> if (asc) list.sortedBy { it.age } else list.sortedByDescending { it.age }
        }
        sorted
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Nurse Specific Records (Strictly isolated to nurse's own entries)
    val nurseRecords: StateFlow<List<PatientRecordEntity>> = combine(
        rawRecords,
        _currentUser,
        _filters
    ) { records, user, criteria ->
        if (user == null || user.role != UserRole.NURSE.name) return@combine emptyList<PatientRecordEntity>()
        var list = records.filter { it.createdByNurseId == user.employeeId }
        if (criteria.query.isNotBlank()) {
            val q = criteria.query.trim().lowercase()
            list = list.filter {
                it.serialNumber.toString().contains(q) ||
                    it.ipNumber.lowercase().contains(q) ||
                    it.name.lowercase().contains(q) ||
                    it.diagnosis.lowercase().contains(q)
            }
        }
        list.sortedByDescending { it.serialNumber }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Dashboard Statistics
    val dashboardStats: StateFlow<HospitalDashboardStats> = rawRecords.combine(_currentUser) { records, _ ->
        val todayStr = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
        val todayCount = records.count { it.admissionDateTime.startsWith(todayStr) }
        val surgery = records.count { it.broadSpecialityCategory.contains("SURGERY", ignoreCase = true) }
        val medicine = records.count { it.broadSpecialityCategory.contains("MEDICINE", ignoreCase = true) }
        val mlc = records.count { it.medicolegalCategory.equals("MLC", ignoreCase = true) }
        val nmlc = records.count { it.medicolegalCategory.equals("NMLC", ignoreCase = true) }
        val transferred = records.count {
            !it.transferredOut.equals("NO", ignoreCase = true) &&
                !it.transferredOut.equals("NONE", ignoreCase = true) &&
                it.transferredOut.isNotBlank()
        }

        HospitalDashboardStats(
            totalRecords = records.size,
            todayRecords = todayCount,
            surgeryCount = surgery,
            medicineCount = medicine,
            mlcCount = mlc,
            nmlcCount = nmlc,
            transferredCount = transferred
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), HospitalDashboardStats())

    // --- Navigation ---

    fun navigateTo(screen: Screen) {
        // Enforce role-based access control
        val user = _currentUser.value
        if (user == null && screen != Screen.LOGIN && screen != Screen.REGISTER_NURSE && screen != Screen.ADMIN_REGISTRATION_GATE) {
            _currentScreen.value = Screen.LOGIN
            return
        }

        // Nurse cannot access Admin screens
        if (user?.role == UserRole.NURSE.name && (screen == Screen.ADMIN_DASHBOARD || screen == Screen.ADMIN_ALL_RECORDS || screen == Screen.ADMIN_AUDIT_LOGS)) {
            _errorMessage.value = "Access Denied: Nurses do not have administrator permissions."
            return
        }

        val stack = _screenStack.value.toMutableList()
        stack.add(screen)
        _screenStack.value = stack
        _currentScreen.value = screen
    }

    fun handleBack(): Boolean {
        val stack = _screenStack.value.toMutableList()
        if (stack.size > 1) {
            stack.removeAt(stack.size - 1)
            val prev = stack.last()
            _screenStack.value = stack
            _currentScreen.value = prev
            return true
        }
        return false
    }

    // --- Authentication ---

    fun login(identifier: String, pass: String) {
        if (identifier.isBlank() || pass.isBlank()) {
            _errorMessage.value = "Please enter both your Username/Email and Password."
            return
        }

        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null
            val result = repository.login(identifier, pass)
            _isLoading.value = false
            result.onSuccess { user ->
                _currentUser.value = user
                _toastMessage.value = "Welcome back, ${user.fullName}"
                if (user.role == UserRole.DOCTOR_ADMIN.name) {
                    _screenStack.value = listOf(Screen.ADMIN_DASHBOARD)
                    _currentScreen.value = Screen.ADMIN_DASHBOARD
                } else {
                    _screenStack.value = listOf(Screen.NURSE_DASHBOARD)
                    _currentScreen.value = Screen.NURSE_DASHBOARD
                }
            }.onFailure { err ->
                _errorMessage.value = err.message ?: "Authentication failed."
            }
        }
    }

    fun registerNurse(
        fullName: String,
        employeeId: String,
        email: String,
        phoneNumber: String,
        password: String,
        confirmPassword: String
    ) {
        if (password != confirmPassword) {
            _errorMessage.value = "Passwords do not match."
            return
        }

        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null
            val result = repository.registerNurse(fullName, employeeId, email, phoneNumber, password)
            _isLoading.value = false
            result.onSuccess {
                _toastMessage.value = "Nurse registration successful! Please log in."
                _currentScreen.value = Screen.LOGIN
            }.onFailure { err ->
                _errorMessage.value = err.message ?: "Registration failed."
            }
        }
    }

    fun registerDoctorAdmin(
        fullName: String,
        employeeId: String,
        email: String,
        phoneNumber: String,
        password: String,
        passcode: String
    ) {
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null
            val result = repository.registerDoctorAdmin(fullName, employeeId, email, phoneNumber, password, passcode)
            _isLoading.value = false
            result.onSuccess {
                _toastMessage.value = "Doctor/Admin account provisioned successfully! Please log in."
                _currentScreen.value = Screen.LOGIN
            }.onFailure { err ->
                _errorMessage.value = err.message ?: "Admin creation failed."
            }
        }
    }

    fun logout() {
        _currentUser.value = null
        _selectedRecordIds.value = emptySet()
        _filters.value = FilterCriteria()
        _screenStack.value = listOf(Screen.LOGIN)
        _currentScreen.value = Screen.LOGIN
        _toastMessage.value = "Logged out successfully."
    }

    // --- Record Operations ---

    fun saveNewRecord(record: PatientRecordEntity, onSuccess: () -> Unit) {
        val user = _currentUser.value ?: run {
            _errorMessage.value = "Session expired. Please log in again."
            return
        }

        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null
            val result = repository.createPatientRecord(record, user)
            _isLoading.value = false
            result.onSuccess {
                _toastMessage.value = "Patient record saved successfully."
                onSuccess()
            }.onFailure { err ->
                _errorMessage.value = "Unable to save patient record: ${err.message}"
            }
        }
    }

    fun updateRecord(record: PatientRecordEntity, onSuccess: () -> Unit) {
        val user = _currentUser.value ?: return

        viewModelScope.launch {
            _isLoading.value = true
            val result = repository.updatePatientRecord(record, user)
            _isLoading.value = false
            result.onSuccess {
                _editingRecord.value = null
                _toastMessage.value = "Record updated successfully."
                onSuccess()
            }.onFailure { err ->
                _errorMessage.value = err.message ?: "Failed to update record."
            }
        }
    }

    fun confirmDelete(record: PatientRecordEntity) {
        val user = _currentUser.value
        if (user?.role != UserRole.DOCTOR_ADMIN.name) {
            _errorMessage.value = "Access Denied: Only Doctor/Admin can delete records."
            return
        }
        _recordToDelete.value = record
    }

    fun dismissDelete() {
        _recordToDelete.value = null
    }

    fun executeDelete() {
        val record = _recordToDelete.value ?: return
        val user = _currentUser.value ?: return

        viewModelScope.launch {
            _isLoading.value = true
            val result = repository.softDeleteRecord(record.id, user)
            _isLoading.value = false
            _recordToDelete.value = null
            result.onSuccess {
                _toastMessage.value = "Record S NO ${record.serialNumber} deleted successfully."
            }.onFailure { err ->
                _errorMessage.value = err.message ?: "Failed to delete record."
            }
        }
    }

    fun duplicateRecord(sourceRecordId: Long) {
        val user = _currentUser.value
        if (user?.role != UserRole.DOCTOR_ADMIN.name) {
            _errorMessage.value = "Access Denied: Only Doctor/Admin can duplicate records."
            return
        }

        viewModelScope.launch {
            _isLoading.value = true
            val result = repository.duplicateRecord(sourceRecordId, user)
            _isLoading.value = false
            result.onSuccess { newRec ->
                _toastMessage.value = "Record duplicated as S NO ${newRec.serialNumber}."
                _editingRecord.value = newRec
            }.onFailure { err ->
                _errorMessage.value = err.message ?: "Failed to duplicate record."
            }
        }
    }

    // --- Selection & Filtering ---

    fun updateSearchQuery(query: String) {
        _filters.value = _filters.value.copy(query = query)
    }

    fun setFilterSpecialty(specialty: String?) {
        _filters.value = _filters.value.copy(broadSpecialty = specialty)
    }

    fun setFilterMlc(mlc: String?) {
        _filters.value = _filters.value.copy(medicolegal = mlc)
    }

    fun setFilterPillar(pillar: String?) {
        _filters.value = _filters.value.copy(pillarStatus = pillar)
    }

    fun setFilterTransferred(transferred: String?) {
        _filters.value = _filters.value.copy(transferredOut = transferred)
    }

    fun setFilterDate(dateFilter: String?) {
        _filters.value = _filters.value.copy(dateFilter = dateFilter)
    }

    fun clearFilters() {
        _filters.value = FilterCriteria()
    }

    fun setSort(field: SortField) {
        if (_sortField.value == field) {
            _sortAscending.value = !_sortAscending.value
        } else {
            _sortField.value = field
            _sortAscending.value = true
        }
    }

    fun toggleRecordSelection(id: Long) {
        val current = _selectedRecordIds.value.toMutableSet()
        if (current.contains(id)) {
            current.remove(id)
        } else {
            current.add(id)
        }
        _selectedRecordIds.value = current
    }

    fun selectAllVisibleRecords() {
        val visibleIds = filteredAdminRecords.value.map { it.id }.toSet()
        if (_selectedRecordIds.value.containsAll(visibleIds)) {
            _selectedRecordIds.value = emptySet()
        } else {
            _selectedRecordIds.value = visibleIds
        }
    }

    fun clearSelection() {
        _selectedRecordIds.value = emptySet()
    }

    // --- Excel Export ---

    fun openExportDialog() {
        val user = _currentUser.value
        if (user?.role != UserRole.DOCTOR_ADMIN.name) {
            _errorMessage.value = "Access Denied: Only Doctor/Admin can export patient records to Excel."
            return
        }
        _showExportDialog.value = true
    }

    fun dismissExportDialog() {
        _showExportDialog.value = false
    }

    fun exportToExcel(
        context: Context,
        filename: String,
        exportOnlySelected: Boolean
    ) {
        val user = _currentUser.value ?: return
        if (user.role != UserRole.DOCTOR_ADMIN.name) {
            _errorMessage.value = "Access Denied: Excel export is restricted to Doctor/Admin."
            return
        }

        val recordsToExport = if (exportOnlySelected && _selectedRecordIds.value.isNotEmpty()) {
            rawRecords.value.filter { _selectedRecordIds.value.contains(it.id) }
        } else if (_filters.value != FilterCriteria()) {
            filteredAdminRecords.value
        } else {
            rawRecords.value
        }

        if (recordsToExport.isEmpty()) {
            _errorMessage.value = "No patient records available to export."
            _showExportDialog.value = false
            return
        }

        viewModelScope.launch {
            _isLoading.value = true
            val result = repository.exportExcel(context, recordsToExport, user, filename)
            _isLoading.value = false
            _showExportDialog.value = false
            result.onSuccess { file ->
                _exportedFile.value = file
                _toastMessage.value = "Excel export generated: ${file.name} (${recordsToExport.size} records)"
            }.onFailure { err ->
                _errorMessage.value = "Export failed: ${err.message}"
            }
        }
    }

    fun clearExportedFile() {
        _exportedFile.value = null
    }

    // Modal details
    fun setDetailRecord(record: PatientRecordEntity?) {
        _detailRecord.value = record
    }

    fun setEditingRecord(record: PatientRecordEntity?) {
        _editingRecord.value = record
    }

    fun clearFeedback() {
        _toastMessage.value = null
        _errorMessage.value = null
    }
}

class HospitalViewModelFactory(
    private val repository: HospitalRepository
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(HospitalViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return HospitalViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
