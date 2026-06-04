package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "service_records")
data class ServiceRecord(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val vehicleId: Int,
    val date: Long,
    val cost: Double,
    val technicianNotes: String
)
