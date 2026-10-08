package com.example

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.example.data.database.HospitalDatabase
import com.example.data.model.PatientRecordEntity
import com.example.data.model.UserEntity
import com.example.data.model.UserRole
import com.example.data.repository.HospitalRepository
import com.example.util.ExcelExporter
import com.example.util.SecurityUtils
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import java.io.ByteArrayInputStream
import java.util.zip.ZipInputStream

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class HospitalSystemTest {

    private lateinit var db: HospitalDatabase
    private lateinit var repository: HospitalRepository
    private lateinit var context: Context

    @Before
    fun setUp() {
        context = ApplicationProvider.getApplicationContext()
        db = Room.inMemoryDatabaseBuilder(context, HospitalDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        repository = HospitalRepository(
            userDao = db.userDao(),
            patientRecordDao = db.patientRecordDao(),
            auditLogDao = db.auditLogDao()
        )
    }

    @After
    fun tearDown() {
        db.close()
    }

    @Test
    fun testSecurityPasswordHashing() {
        val salt = SecurityUtils.generateSalt()
        val hash = SecurityUtils.hashPassword("HospitalSecret2026", salt)

        assertTrue(SecurityUtils.verifyPassword("HospitalSecret2026", salt, hash))
        assertFalse(SecurityUtils.verifyPassword("WrongPassword", salt, hash))
    }

    @Test
    fun testEmptyInitialPatientDatabase() = runBlocking {
        // Requirement: application should start with an empty patient-record database
        val records = repository.getAllActiveRecords().first()
        assertEquals(0, records.size)
    }

    @Test
    fun testNurseRegistrationAndLogin() = runBlocking {
        val regResult = repository.registerNurse(
            fullName = "Nurse Brenda Taylor",
            employeeId = "NUR-501",
            email = "brenda.taylor@hospital.org",
            phoneNumber = "+1-555-4011",
            password = "nursePassword1"
        )
        assertTrue(regResult.isSuccess)
        val nurse = regResult.getOrThrow()
        assertEquals(UserRole.NURSE.name, nurse.role)

        // Login with email
        val loginEmail = repository.login("brenda.taylor@hospital.org", "nursePassword1")
        assertTrue(loginEmail.isSuccess)
        assertEquals("NUR-501", loginEmail.getOrThrow().employeeId)

        // Login with employee ID
        val loginEmpId = repository.login("NUR-501", "nursePassword1")
        assertTrue(loginEmpId.isSuccess)

        // Failed login
        val loginBad = repository.login("NUR-501", "wrongPass")
        assertTrue(loginBad.isFailure)
    }

    @Test
    fun testDoctorAdminPasscodeProtection() = runBlocking {
        // Unauthorized creation should fail
        val badResult = repository.registerDoctorAdmin(
            fullName = "Fake Doctor",
            employeeId = "DOC-999",
            email = "fake@hospital.org",
            phoneNumber = "123",
            password = "pass",
            adminPasscode = "WRONG-KEY"
        )
        assertTrue(badResult.isFailure)

        // Authorized creation with admin passcode
        val goodResult = repository.registerDoctorAdmin(
            fullName = "Dr. Michael Chen, MD",
            employeeId = "DOC-102",
            email = "mchen@hospital.org",
            phoneNumber = "123",
            password = "doctorPassword1",
            adminPasscode = "HOSPITAL-ADMIN-KEY-2026"
        )
        assertTrue(goodResult.isSuccess)
        assertEquals(UserRole.DOCTOR_ADMIN.name, goodResult.getOrThrow().role)
    }

    @Test
    fun testPatientRecordCreationAndSerialAutoGeneration() = runBlocking {
        val nurse = UserEntity(
            id = 1,
            fullName = "Nurse Sarah",
            employeeId = "NUR-101",
            email = "sarah@hospital.org",
            passwordHash = "hash",
            salt = "salt",
            role = UserRole.NURSE.name
        )

        val patient1 = PatientRecordEntity(
            serialNumber = 0,
            ipNumber = "9985",
            name = "James Wilson",
            age = 42,
            sex = "M",
            admissionDateTime = "2026-10-08 08:30",
            patientReceivedTime = "08:35",
            broadSpecialityCategory = "SURGERY CASES",
            diagnosis = "RTA",
            ageInterval = "12-60",
            pillarStatus = "PILLAR",
            medicolegalCategory = "MLC",
            transferredOut = "ICU",
            transferredOutTime = "10:15",
            emergencyResponseTime = "5 mins",
            createdByNurseId = nurse.employeeId,
            createdByNurseName = nurse.fullName
        )

        val id1 = repository.createPatientRecord(patient1, nurse).getOrThrow()
        val saved1 = repository.getRecordById(id1)
        assertNotNull(saved1)
        assertEquals(1, saved1?.serialNumber) // First serial number must be 1!

        val patient2 = patient1.copy(
            ipNumber = "9986",
            name = "Mary Davis",
            diagnosis = "CHEST PAIN"
        )
        val id2 = repository.createPatientRecord(patient2, nurse).getOrThrow()
        val saved2 = repository.getRecordById(id2)
        assertEquals(2, saved2?.serialNumber) // Second serial number must be 2!
    }

    @Test
    fun testDoctorRecordDuplicationAndSoftDelete() = runBlocking {
        val admin = UserEntity(
            id = 99,
            fullName = "Dr. Robert Vance",
            employeeId = "EMP-DOC-001",
            email = "admin@hospital.org",
            passwordHash = "hash",
            salt = "salt",
            role = UserRole.DOCTOR_ADMIN.name
        )
        val nurse = UserEntity(
            id = 1,
            fullName = "Nurse Sarah",
            employeeId = "NUR-101",
            email = "sarah@hospital.org",
            passwordHash = "hash",
            salt = "salt",
            role = UserRole.NURSE.name
        )

        val orig = PatientRecordEntity(
            serialNumber = 0,
            ipNumber = "8801",
            name = "David Brown",
            age = 58,
            sex = "M",
            admissionDateTime = "2026-10-08 09:00",
            patientReceivedTime = "09:05",
            broadSpecialityCategory = "MEDICINE CASES",
            diagnosis = "HYPOGLYCEMIA",
            ageInterval = "12-60",
            pillarStatus = "NON PILLAR",
            medicolegalCategory = "NMLC",
            transferredOut = "NO",
            transferredOutTime = "N/A",
            emergencyResponseTime = "Immediate",
            createdByNurseId = nurse.employeeId,
            createdByNurseName = nurse.fullName
        )
        val origId = repository.createPatientRecord(orig, nurse).getOrThrow()

        // Doctor duplicates record
        val dupResult = repository.duplicateRecord(origId, admin)
        assertTrue(dupResult.isSuccess)
        val dupRecord = dupResult.getOrThrow()
        assertEquals(2, dupRecord.serialNumber)
        assertTrue(dupRecord.isDuplicated)
        assertEquals(origId, dupRecord.originalRecordId)

        // Nurse cannot delete
        val nurseDelResult = repository.softDeleteRecord(origId, nurse)
        assertTrue(nurseDelResult.isFailure)

        // Doctor can delete
        val adminDelResult = repository.softDeleteRecord(origId, admin)
        assertTrue(adminDelResult.isSuccess)

        // Active records now only contain the duplicated record
        val activeRecords = repository.getAllActiveRecords().first()
        assertEquals(1, activeRecords.size)
        assertEquals(dupRecord.id, activeRecords[0].id)
    }

    @Test
    fun testExcelXlsxGenerationPreservesAll15Columns() {
        val testRecords = listOf(
            PatientRecordEntity(
                id = 1,
                serialNumber = 1,
                ipNumber = "9985",
                name = "Emergency Patient 1",
                age = 34,
                sex = "M",
                admissionDateTime = "2026-10-08 10:00",
                patientReceivedTime = "10:05",
                broadSpecialityCategory = "SURGERY CASES",
                diagnosis = "RTA",
                ageInterval = "12-60",
                pillarStatus = "PILLAR",
                medicolegalCategory = "MLC",
                transferredOut = "ICU",
                transferredOutTime = "11:30",
                emergencyResponseTime = "5 mins",
                createdByNurseId = "NUR-101",
                createdByNurseName = "Nurse Sarah"
            )
        )

        val xlsxBytes = ExcelExporter.generateXlsxBytes(testRecords)
        assertTrue(xlsxBytes.isNotEmpty())

        // Read zip stream to ensure it is a valid OpenXML spreadsheet
        val zipIn = ZipInputStream(ByteArrayInputStream(xlsxBytes))
        val entryNames = mutableListOf<String>()
        var entry = zipIn.nextEntry
        while (entry != null) {
            entryNames.add(entry.name)
            if (entry.name == "xl/worksheets/sheet1.xml") {
                val sheetContent = zipIn.bufferedReader().readText()
                // Verify all 15 hospital columns are present
                assertTrue(sheetContent.contains("S NO"))
                assertTrue(sheetContent.contains("IP NO"))
                assertTrue(sheetContent.contains("NAME"))
                assertTrue(sheetContent.contains("AGE"))
                assertTrue(sheetContent.contains("SEX"))
                assertTrue(sheetContent.contains("ADMISSION DATE &amp; TIME"))
                assertTrue(sheetContent.contains("PATIENT RECEIVED TIME"))
                assertTrue(sheetContent.contains("BROAD SPECIALITY CATEGORY"))
                assertTrue(sheetContent.contains("DIAGNOSIS"))
                assertTrue(sheetContent.contains("AGE INTERVAL"))
                assertTrue(sheetContent.contains("TAEI/PILLAR/TAEI NON PILLAR"))
                assertTrue(sheetContent.contains("MEDICOLEGAL CATEGORY"))
                assertTrue(sheetContent.contains("TRANSFERRED OUT"))
                assertTrue(sheetContent.contains("TRANSFERRED OUT TIME"))
                assertTrue(sheetContent.contains("EMERGENCY RESPONSE TIME"))
                // Verify patient data in sheet
                assertTrue(sheetContent.contains("9985"))
                assertTrue(sheetContent.contains("Emergency Patient 1"))
            }
            entry = zipIn.nextEntry
        }

        assertTrue(entryNames.contains("[Content_Types].xml"))
        assertTrue(entryNames.contains("_rels/.rels"))
        assertTrue(entryNames.contains("xl/workbook.xml"))
        assertTrue(entryNames.contains("xl/styles.xml"))
        assertTrue(entryNames.contains("xl/worksheets/sheet1.xml"))
    }
}
