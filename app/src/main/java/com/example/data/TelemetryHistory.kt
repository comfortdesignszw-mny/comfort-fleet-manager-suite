package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "telemetry_history")
data class TelemetryHistory(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val vehicleId: Int,
    val timestamp: Long,
    val speed: Float,
    val mileage: Float,
    val fuelLevel: Float
)
