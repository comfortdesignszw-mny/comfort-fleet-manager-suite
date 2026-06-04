package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "trip_logs")
data class TripLog(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val vehicleId: Int,
    val driverName: String,
    val destination: String,
    val tripReason: String,
    val mileageOut: Int,
    val fuelOut: String, // Full, 3/4, 1/2, 1/4, Empty
    val timeOut: Long,
    
    // Nullable fields for Check-In
    val mileageIn: Int? = null,
    val fuelIn: String? = null,
    val timeIn: Long? = null,
    val totalMileage: Int? = null,
    val status: String = "Active" // Active, Completed
)
