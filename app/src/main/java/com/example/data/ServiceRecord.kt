package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
@Entity(tableName = "service_records")
data class ServiceRecord(
    @PrimaryKey val id: String = java.util.UUID.randomUUID().toString(),
    val vehicleId: String,
    val date: Long,
    val cost: Double,
    val technicianNotes: String,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis(),
    val dispatcherId: String = ""
)
