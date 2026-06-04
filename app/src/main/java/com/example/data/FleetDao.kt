package com.example.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface FleetDao {
    @Query("SELECT * FROM vehicles ORDER BY id DESC")
    fun getAllVehicles(): Flow<List<Vehicle>>

    @Query("SELECT * FROM vehicles WHERE id = :id LIMIT 1")
    suspend fun getVehicleById(id: Int): Vehicle?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertVehicle(vehicle: Vehicle)

    @Update
    suspend fun updateVehicle(vehicle: Vehicle)

    @Delete
    suspend fun deleteVehicle(vehicle: Vehicle)
    
    @Query("SELECT * FROM trip_logs ORDER BY timeOut DESC")
    fun getAllTripLogs(): Flow<List<TripLog>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTripLog(tripLog: TripLog)

    @Update
    suspend fun updateTripLog(tripLog: TripLog)

    @Delete
    suspend fun deleteTripLog(tripLog: TripLog)

    @Query("SELECT * FROM telemetry_history WHERE vehicleId = :vehicleId ORDER BY timestamp DESC LIMIT 10")
    fun getTelemetryHistory(vehicleId: Int): Flow<List<TelemetryHistory>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTelemetryHistory(history: TelemetryHistory)

    @Query("SELECT * FROM service_records WHERE vehicleId = :vehicleId ORDER BY date DESC")
    fun getServiceRecordsForVehicle(vehicleId: Int): Flow<List<ServiceRecord>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertServiceRecord(record: ServiceRecord)
}
