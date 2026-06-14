package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
@Entity(tableName = "trip_logs")
data class TripLog(
    @PrimaryKey val id: String = java.util.UUID.randomUUID().toString(),
    val vehicleId: String,
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
    val status: String = "Active", // Active, Completed
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis(),
    val dispatcherId: String = ""
)
