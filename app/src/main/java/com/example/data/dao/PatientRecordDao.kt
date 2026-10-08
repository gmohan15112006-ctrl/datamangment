package com.example.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.model.PatientRecordEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface PatientRecordDao {

    @Query("SELECT * FROM patient_records WHERE is_deleted = 0 ORDER BY serial_number ASC")
    fun getAllActiveRecords(): Flow<List<PatientRecordEntity>>

    @Query("SELECT * FROM patient_records WHERE is_deleted = 0 ORDER BY serial_number ASC")
    suspend fun getActiveRecordsSnapshot(): List<PatientRecordEntity>

    @Query("SELECT * FROM patient_records WHERE is_deleted = 0 AND created_by_nurse_id = :nurseId ORDER BY serial_number DESC")
    fun getRecordsByNurse(nurseId: String): Flow<List<PatientRecordEntity>>

    @Query("SELECT * FROM patient_records WHERE id = :id LIMIT 1")
    suspend fun getRecordById(id: Long): PatientRecordEntity?

    @Query("SELECT MAX(serial_number) FROM patient_records")
    suspend fun getMaxSerialNumber(): Int?

    @Query("SELECT COUNT(*) FROM patient_records WHERE is_deleted = 0")
    fun countActiveRecords(): Flow<Int>

    @Query("SELECT * FROM patient_records WHERE id IN (:ids) AND is_deleted = 0 ORDER BY serial_number ASC")
    suspend fun getRecordsByIds(ids: List<Long>): List<PatientRecordEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRecord(record: PatientRecordEntity): Long

    @Update
    suspend fun updateRecord(record: PatientRecordEntity)

    @Query("UPDATE patient_records SET is_deleted = 1, deleted_at = :deletedAt, last_modified_by = :modifiedBy, last_modified_at = :deletedAt WHERE id = :id")
    suspend fun softDeleteRecord(id: Long, deletedAt: Long, modifiedBy: String)

    @Query("SELECT * FROM patient_records WHERE is_deleted = 1 ORDER BY deleted_at DESC")
    fun getDeletedRecords(): Flow<List<PatientRecordEntity>>
}
