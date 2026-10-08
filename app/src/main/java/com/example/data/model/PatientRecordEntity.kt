package com.example.data.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "patient_records",
    indices = [
        Index(value = ["serial_number"]),
        Index(value = ["ip_number"]),
        Index(value = ["created_by_nurse_id"]),
        Index(value = ["is_deleted"])
    ]
)
data class PatientRecordEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,

    @ColumnInfo(name = "serial_number")
    val serialNumber: Int,

    @ColumnInfo(name = "ip_number")
    val ipNumber: String,

    @ColumnInfo(name = "patient_name")
    val name: String,

    @ColumnInfo(name = "age")
    val age: Int,

    @ColumnInfo(name = "sex")
    val sex: String, // "M", "F", "Other"

    @ColumnInfo(name = "admission_datetime")
    val admissionDateTime: String,

    @ColumnInfo(name = "patient_received_time")
    val patientReceivedTime: String,

    @ColumnInfo(name = "broad_speciality_category")
    val broadSpecialityCategory: String, // "SURGERY CASES", "MEDICINE CASES"

    @ColumnInfo(name = "diagnosis")
    val diagnosis: String,

    @ColumnInfo(name = "age_interval")
    val ageInterval: String, // "12-60", ">60", "<12", "Other"

    @ColumnInfo(name = "pillar_status")
    val pillarStatus: String, // "PILLAR", "NON PILLAR"

    @ColumnInfo(name = "medicolegal_category")
    val medicolegalCategory: String, // "MLC", "NMLC"

    @ColumnInfo(name = "transferred_out")
    val transferredOut: String, // Destination or "NO" / "NONE"

    @ColumnInfo(name = "transferred_out_time")
    val transferredOutTime: String,

    @ColumnInfo(name = "emergency_response_time")
    val emergencyResponseTime: String,

    // Internal system audit & tracking fields
    @ColumnInfo(name = "created_by_nurse_id")
    val createdByNurseId: String,

    @ColumnInfo(name = "created_by_nurse_name")
    val createdByNurseName: String,

    @ColumnInfo(name = "created_at")
    val createdAt: Long = System.currentTimeMillis(),

    @ColumnInfo(name = "last_modified_by")
    val lastModifiedBy: String = "",

    @ColumnInfo(name = "last_modified_at")
    val lastModifiedAt: Long = System.currentTimeMillis(),

    @ColumnInfo(name = "is_deleted")
    val isDeleted: Boolean = false,

    @ColumnInfo(name = "deleted_at")
    val deletedAt: Long? = null,

    @ColumnInfo(name = "is_duplicated")
    val isDuplicated: Boolean = false,

    @ColumnInfo(name = "original_record_id")
    val originalRecordId: Long? = null,

    @ColumnInfo(name = "record_status")
    val recordStatus: String = "SUBMITTED"
)
