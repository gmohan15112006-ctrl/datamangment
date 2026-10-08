package com.example.data.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "audit_logs",
    indices = [
        Index(value = ["timestamp"]),
        Index(value = ["performed_by_user_id"]),
        Index(value = ["action"])
    ]
)
data class AuditLogEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,

    @ColumnInfo(name = "action")
    val action: String,

    @ColumnInfo(name = "record_id")
    val recordId: Long? = null,

    @ColumnInfo(name = "performed_by_user_id")
    val performedByUserId: String,

    @ColumnInfo(name = "performed_by_user_name")
    val performedByUserName: String,

    @ColumnInfo(name = "performed_by_user_role")
    val performedByUserRole: String,

    @ColumnInfo(name = "details")
    val details: String,

    @ColumnInfo(name = "timestamp")
    val timestamp: Long = System.currentTimeMillis()
)
