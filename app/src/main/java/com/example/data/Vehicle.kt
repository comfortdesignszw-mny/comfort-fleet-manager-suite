package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "vehicles")
data class Vehicle(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val brand: String,
    val model: String,
    val type: String, // SUV, Hilux Standard, Sedan, Truck, Hatchback, Other
    val registrationNumber: String,
    val condition: String, // Best, Good, Moderate, Poor, Not Working
    val status: String = "Parked", // Parked, Started, Moving, Immobilized
    val mockLatitude: Double = -17.824858,
    val mockLongitude: Double = 31.053028,
    val mockSpeed: Float = 0f,
    val currentMileage: Float = 10000f,
    val fuelLevel: Float = 100f, // starts at 100%
    val serviceThreshold: Float = 15000f
)
