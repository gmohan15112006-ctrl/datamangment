package com.example.data.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.data.dao.AuditLogDao
import com.example.data.dao.PatientRecordDao
import com.example.data.dao.UserDao
import com.example.data.model.AuditLogEntity
import com.example.data.model.PatientRecordEntity
import com.example.data.model.UserEntity
import com.example.data.model.UserRole
import com.example.util.SecurityUtils
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(
    entities = [
        UserEntity::class,
        PatientRecordEntity::class,
        AuditLogEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class HospitalDatabase : RoomDatabase() {

    abstract fun userDao(): UserDao
    abstract fun patientRecordDao(): PatientRecordDao
    abstract fun auditLogDao(): AuditLogDao

    companion object {
        @Volatile
        private var INSTANCE: HospitalDatabase? = null

        fun getDatabase(context: Context, scope: CoroutineScope): HospitalDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    HospitalDatabase::class.java,
                    "hospital_patient_data.db"
                )
                    .addCallback(DatabaseCallback(scope))
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }

        private class DatabaseCallback(
            private val scope: CoroutineScope
        ) : RoomDatabase.Callback() {
            override fun onCreate(db: SupportSQLiteDatabase) {
                super.onCreate(db)
                INSTANCE?.let { database ->
                    scope.launch(Dispatchers.IO) {
                        populateInitialAdmin(database)
                    }
                }
            }

            private suspend fun populateInitialAdmin(database: HospitalDatabase) {
                val userDao = database.userDao()
                val auditDao = database.auditLogDao()

                // Ensure initial system Doctor/Admin account exists securely
                // Patient records are strictly left empty per requirements!
                val adminSalt = SecurityUtils.generateSalt()
                val adminHash = SecurityUtils.hashPassword("AdminPass123!", adminSalt)
                val adminUser = UserEntity(
                    fullName = "Dr. Robert Vance, MD",
                    employeeId = "EMP-DOC-001",
                    email = "admin@hospital.org",
                    passwordHash = adminHash,
                    salt = adminSalt,
                    role = UserRole.DOCTOR_ADMIN.name,
                    phoneNumber = "+1-555-0199",
                    accountStatus = "ACTIVE"
                )
                userDao.insertUser(adminUser)

                auditDao.insertLog(
                    AuditLogEntity(
                        action = "SYSTEM_INITIALIZED",
                        recordId = null,
                        performedByUserId = "SYSTEM",
                        performedByUserName = "System Core",
                        performedByUserRole = "SYSTEM",
                        details = "Hospital Patient Data Management System initialized with secure empty patient records database."
                    )
                )
            }
        }
    }
}
