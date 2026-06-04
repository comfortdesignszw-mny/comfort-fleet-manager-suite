package com.example.data

import kotlinx.coroutines.flow.Flow

class FleetRepository(private val fleetDao: FleetDao) {
    val allVehicles: Flow<List<Vehicle>> = fleetDao.getAllVehicles()
    val allTripLogs: Flow<List<TripLog>> = fleetDao.getAllTripLogs()

    suspend fun insertVehicle(vehicle: Vehicle) {
        fleetDao.insertVehicle(vehicle)
    }

    suspend fun updateVehicle(vehicle: Vehicle) {
        fleetDao.updateVehicle(vehicle)
    }

    suspend fun deleteVehicle(vehicle: Vehicle) {
        fleetDao.deleteVehicle(vehicle)
    }

    suspend fun insertTripLog(tripLog: TripLog) {
        fleetDao.insertTripLog(tripLog)
    }

    suspend fun updateTripLog(tripLog: TripLog) {
        fleetDao.updateTripLog(tripLog)
    }

    suspend fun deleteTripLog(tripLog: TripLog) {
        fleetDao.deleteTripLog(tripLog)
    }
    
    suspend fun getVehicleById(id: Int): Vehicle? {
        return fleetDao.getVehicleById(id)
    }

    fun getTelemetryHistory(vehicleId: Int): Flow<List<TelemetryHistory>> {
        return fleetDao.getTelemetryHistory(vehicleId)
    }

    suspend fun insertTelemetryHistory(history: TelemetryHistory) {
        fleetDao.insertTelemetryHistory(history)
    }

    fun getServiceRecordsForVehicle(vehicleId: Int): Flow<List<ServiceRecord>> {
        return fleetDao.getServiceRecordsForVehicle(vehicleId)
    }

    suspend fun insertServiceRecord(record: ServiceRecord) {
        fleetDao.insertServiceRecord(record)
    }
}
