package com.example.ui.state

import com.example.data.model.AuditLogEntity
import com.example.data.model.PatientRecordEntity
import com.example.data.model.UserEntity

enum class Screen {
    LOGIN,
    REGISTER_NURSE,
    ADMIN_REGISTRATION_GATE,
    NURSE_DASHBOARD,
    NURSE_ENTRY_FORM,
    NURSE_RECORDS,
    ADMIN_DASHBOARD,
    ADMIN_ALL_RECORDS,
    ADMIN_AUDIT_LOGS
}

enum class SortField(val label: String) {
    SERIAL_NUMBER("S NO"),
    ADMISSION_DATE("Admission Date"),
    PATIENT_NAME("Patient Name"),
    AGE("Age")
}

data class FilterCriteria(
    val query: String = "",
    val broadSpecialty: String? = null, // "SURGERY CASES", "MEDICINE CASES"
    val medicolegal: String? = null, // "MLC", "NMLC"
    val pillarStatus: String? = null, // "PILLAR", "NON PILLAR"
    val transferredOut: String? = null, // "YES", "NO"
    val dateFilter: String? = null, // "TODAY", "ALL"
    val nurseId: String? = null
)

data class HospitalDashboardStats(
    val totalRecords: Int = 0,
    val todayRecords: Int = 0,
    val surgeryCount: Int = 0,
    val medicineCount: Int = 0,
    val mlcCount: Int = 0,
    val nmlcCount: Int = 0,
    val transferredCount: Int = 0
)
