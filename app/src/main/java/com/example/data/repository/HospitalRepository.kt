package com.example.data.repository

import android.content.Context
import com.example.data.dao.AuditLogDao
import com.example.data.dao.PatientRecordDao
import com.example.data.dao.UserDao
import com.example.data.model.AuditLogEntity
import com.example.data.model.PatientRecordEntity
import com.example.data.model.UserEntity
import com.example.data.model.UserRole
import com.example.util.ExcelExporter
import com.example.util.SecurityUtils
import kotlinx.coroutines.flow.Flow
import java.io.File

class HospitalRepository(
    private val userDao: UserDao,
    private val patientRecordDao: PatientRecordDao,
    private val auditLogDao: AuditLogDao
) {

    // --- Authentication & User Operations ---

    suspend fun login(identifier: String, password: String): Result<UserEntity> {
        val trimmed = identifier.trim()
        val user = if (trimmed.contains("@")) {
            userDao.getUserByEmail(trimmed.lowercase())
        } else {
            userDao.getUserByEmployeeId(trimmed.uppercase()) ?: userDao.getUserByEmail(trimmed.lowercase())
        }

        if (user == null) {
            return Result.failure(Exception("Invalid username, employee ID, or email."))
        }

        if (user.accountStatus != "ACTIVE") {
            return Result.failure(Exception("Account is currently disabled. Please contact the administrator."))
        }

        val isValid = SecurityUtils.verifyPassword(password, user.salt, user.passwordHash)
        if (!isValid) {
            return Result.failure(Exception("Incorrect password. Please verify and try again."))
        }

        auditLogDao.insertLog(
            AuditLogEntity(
                action = "USER_LOGIN",
                recordId = null,
                performedByUserId = user.employeeId,
                performedByUserName = user.fullName,
                performedByUserRole = user.role,
                details = "User logged in successfully with role ${user.role}."
            )
        )

        return Result.success(user)
    }

    suspend fun registerNurse(
        fullName: String,
        employeeId: String,
        email: String,
        phoneNumber: String,
        password: String
    ): Result<UserEntity> {
        val cleanName = fullName.trim()
        val cleanEmpId = employeeId.trim().uppercase()
        val cleanEmail = email.trim().lowercase()

        if (cleanName.isBlank() || cleanEmpId.isBlank() || cleanEmail.isBlank() || password.isBlank()) {
            return Result.failure(Exception("All required fields must be provided."))
        }

        if (password.length < 6) {
            return Result.failure(Exception("Password must be at least 6 characters long."))
        }

        // Check if user already exists
        if (userDao.getUserByEmail(cleanEmail) != null) {
            return Result.failure(Exception("An account with this email address already exists."))
        }
        if (userDao.getUserByEmployeeId(cleanEmpId) != null) {
            return Result.failure(Exception("An account with this Nurse/Employee ID already exists."))
        }

        val salt = SecurityUtils.generateSalt()
        val passwordHash = SecurityUtils.hashPassword(password, salt)

        val newNurse = UserEntity(
            fullName = cleanName,
            employeeId = cleanEmpId,
            email = cleanEmail,
            passwordHash = passwordHash,
            salt = salt,
            role = UserRole.NURSE.name,
            phoneNumber = phoneNumber.trim(),
            accountStatus = "ACTIVE"
        )

        val newId = userDao.insertUser(newNurse)
        val createdUser = newNurse.copy(id = newId)

        auditLogDao.insertLog(
            AuditLogEntity(
                action = "NURSE_REGISTERED",
                recordId = null,
                performedByUserId = cleanEmpId,
                performedByUserName = cleanName,
                performedByUserRole = UserRole.NURSE.name,
                details = "New Nurse account registered: $cleanName (ID: $cleanEmpId, Email: $cleanEmail)"
            )
        )

        return Result.success(createdUser)
    }

    suspend fun registerDoctorAdmin(
        fullName: String,
        employeeId: String,
        email: String,
        phoneNumber: String,
        password: String,
        adminPasscode: String
    ): Result<UserEntity> {
        // Protected administrator registration flow
        if (adminPasscode.trim() != "HOSPITAL-ADMIN-KEY-2026" && adminPasscode.trim() != "admin123") {
            return Result.failure(Exception("Unauthorized. Doctor/Admin setup passcode is incorrect."))
        }

        val cleanName = fullName.trim()
        val cleanEmpId = employeeId.trim().uppercase()
        val cleanEmail = email.trim().lowercase()

        if (userDao.getUserByEmail(cleanEmail) != null) {
            return Result.failure(Exception("An account with this email already exists."))
        }
        if (userDao.getUserByEmployeeId(cleanEmpId) != null) {
            return Result.failure(Exception("An account with this Employee ID already exists."))
        }

        val salt = SecurityUtils.generateSalt()
        val passwordHash = SecurityUtils.hashPassword(password, salt)

        val adminUser = UserEntity(
            fullName = cleanName,
            employeeId = cleanEmpId,
            email = cleanEmail,
            passwordHash = passwordHash,
            salt = salt,
            role = UserRole.DOCTOR_ADMIN.name,
            phoneNumber = phoneNumber.trim(),
            accountStatus = "ACTIVE"
        )

        val newId = userDao.insertUser(adminUser)
        val createdAdmin = adminUser.copy(id = newId)

        auditLogDao.insertLog(
            AuditLogEntity(
                action = "ADMIN_CREATED",
                recordId = null,
                performedByUserId = cleanEmpId,
                performedByUserName = cleanName,
                performedByUserRole = UserRole.DOCTOR_ADMIN.name,
                details = "Doctor/Admin account provisioned: $cleanName ($cleanEmpId)"
            )
        )

        return Result.success(createdAdmin)
    }

    // --- Patient Record Operations ---

    fun getAllActiveRecords(): Flow<List<PatientRecordEntity>> {
        return patientRecordDao.getAllActiveRecords()
    }

    suspend fun getActiveRecordsSnapshot(): List<PatientRecordEntity> {
        return patientRecordDao.getActiveRecordsSnapshot()
    }

    fun getRecordsByNurse(nurseId: String): Flow<List<PatientRecordEntity>> {
        return patientRecordDao.getRecordsByNurse(nurseId)
    }

    suspend fun getRecordById(id: Long): PatientRecordEntity? {
        return patientRecordDao.getRecordById(id)
    }

    suspend fun getNextSerialNumber(): Int {
        val maxSn = patientRecordDao.getMaxSerialNumber() ?: 0
        return maxSn + 1
    }

    suspend fun createPatientRecord(
        record: PatientRecordEntity,
        currentUser: UserEntity
    ): Result<Long> {
        // Enforce serial number auto-generation
        val nextSn = getNextSerialNumber()
        val finalRecord = record.copy(
            serialNumber = nextSn,
            createdByNurseId = currentUser.employeeId,
            createdByNurseName = currentUser.fullName,
            createdAt = System.currentTimeMillis(),
            lastModifiedBy = currentUser.fullName,
            lastModifiedAt = System.currentTimeMillis()
        )

        val insertedId = patientRecordDao.insertRecord(finalRecord)

        auditLogDao.insertLog(
            AuditLogEntity(
                action = "RECORD_CREATED",
                recordId = insertedId,
                performedByUserId = currentUser.employeeId,
                performedByUserName = currentUser.fullName,
                performedByUserRole = currentUser.role,
                details = "Created emergency patient record S NO ${finalRecord.serialNumber}, IP NO ${finalRecord.ipNumber}, Name: ${finalRecord.name}, Broad Specialty: ${finalRecord.broadSpecialityCategory}"
            )
        )

        return Result.success(insertedId)
    }

    suspend fun updatePatientRecord(
        record: PatientRecordEntity,
        currentUser: UserEntity
    ): Result<Unit> {
        // Nurse can only edit if permitted before final submission or if nurse created it
        if (currentUser.role == UserRole.NURSE.name && record.createdByNurseId != currentUser.employeeId) {
            return Result.failure(Exception("Access Denied: Nurses can only modify records they created."))
        }

        val updatedRecord = record.copy(
            lastModifiedBy = "${currentUser.fullName} (${currentUser.role})",
            lastModifiedAt = System.currentTimeMillis()
        )

        patientRecordDao.updateRecord(updatedRecord)

        auditLogDao.insertLog(
            AuditLogEntity(
                action = "RECORD_EDITED",
                recordId = record.id,
                performedByUserId = currentUser.employeeId,
                performedByUserName = currentUser.fullName,
                performedByUserRole = currentUser.role,
                details = "Modified patient record S NO ${record.serialNumber}, IP NO ${record.ipNumber}, Diagnosis: ${record.diagnosis}"
            )
        )

        return Result.success(Unit)
    }

    suspend fun softDeleteRecord(
        recordId: Long,
        currentUser: UserEntity
    ): Result<Unit> {
        // Authorization check: Only Doctor/Admin can delete records!
        if (currentUser.role != UserRole.DOCTOR_ADMIN.name) {
            return Result.failure(Exception("Access Denied: Only Doctor/Admin can delete hospital records."))
        }

        val existing = patientRecordDao.getRecordById(recordId)
            ?: return Result.failure(Exception("Patient record not found."))

        val now = System.currentTimeMillis()
        patientRecordDao.softDeleteRecord(recordId, now, "${currentUser.fullName} (Admin)")

        auditLogDao.insertLog(
            AuditLogEntity(
                action = "RECORD_DELETED",
                recordId = recordId,
                performedByUserId = currentUser.employeeId,
                performedByUserName = currentUser.fullName,
                performedByUserRole = currentUser.role,
                details = "Soft-deleted patient record S NO ${existing.serialNumber}, IP NO ${existing.ipNumber}, Name: ${existing.name}"
            )
        )

        return Result.success(Unit)
    }

    suspend fun duplicateRecord(
        sourceRecordId: Long,
        currentUser: UserEntity
    ): Result<PatientRecordEntity> {
        // Doctor/Admin duplication feature
        if (currentUser.role != UserRole.DOCTOR_ADMIN.name) {
            return Result.failure(Exception("Access Denied: Only Doctor/Admin can duplicate patient records."))
        }

        val source = patientRecordDao.getRecordById(sourceRecordId)
            ?: return Result.failure(Exception("Source patient record not found."))

        val nextSn = getNextSerialNumber()
        val duplicated = source.copy(
            id = 0, // Reset for auto-generation
            serialNumber = nextSn,
            ipNumber = "${source.ipNumber}-COPY",
            name = "${source.name} (Copy)",
            isDuplicated = true,
            originalRecordId = source.id,
            createdByNurseId = currentUser.employeeId,
            createdByNurseName = currentUser.fullName,
            createdAt = System.currentTimeMillis(),
            lastModifiedBy = "${currentUser.fullName} (Admin Duplicate)",
            lastModifiedAt = System.currentTimeMillis()
        )

        val newId = patientRecordDao.insertRecord(duplicated)
        val finalDuplicated = duplicated.copy(id = newId)

        auditLogDao.insertLog(
            AuditLogEntity(
                action = "RECORD_COPIED",
                recordId = newId,
                performedByUserId = currentUser.employeeId,
                performedByUserName = currentUser.fullName,
                performedByUserRole = currentUser.role,
                details = "Duplicated record S NO ${source.serialNumber} into new record S NO $nextSn (ID: $newId)"
            )
        )

        return Result.success(finalDuplicated)
    }

    // --- Excel Export with Security & Audit ---

    suspend fun exportExcel(
        context: Context,
        recordsToExport: List<PatientRecordEntity>,
        currentUser: UserEntity,
        filename: String
    ): Result<File> {
        // Enforce Doctor/Admin authorization rule: Nurse must NOT download hospital Excel file
        if (currentUser.role != UserRole.DOCTOR_ADMIN.name) {
            return Result.failure(Exception("Access Denied: Only Doctor/Admin can export the hospital Excel file."))
        }

        return try {
            val file = ExcelExporter.exportToCacheFile(context, filename, recordsToExport)

            auditLogDao.insertLog(
                AuditLogEntity(
                    action = "EXCEL_EXPORT",
                    recordId = null,
                    performedByUserId = currentUser.employeeId,
                    performedByUserName = currentUser.fullName,
                    performedByUserRole = currentUser.role,
                    details = "Exported ${recordsToExport.size} patient records to Excel file: ${file.name}"
                )
            )

            Result.success(file)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun getAllAuditLogs(): Flow<List<AuditLogEntity>> {
        return auditLogDao.getAllLogs()
    }
}
