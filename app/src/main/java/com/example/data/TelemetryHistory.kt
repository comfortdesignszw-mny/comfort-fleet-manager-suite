package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
@Entity(tableName = "telemetry_history")
data class TelemetryHistory(
    @PrimaryKey val id: String = java.util.UUID.randomUUID().toString(),
    val vehicleId: String,
    val timestamp: Long,
    val speed: Float,
    val mileage: Float,
    val fuelLevel: Float,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis(),
    val dispatcherId: String = ""
)
