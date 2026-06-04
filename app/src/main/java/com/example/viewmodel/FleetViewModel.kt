package com.example.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.FleetDatabase
import com.example.data.FleetRepository
import com.example.data.TripLog
import com.example.data.Vehicle
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlin.random.Random

data class CompanyProfile(
    val contactName: String = "",
    val contactNumber: String = "",
    val contactEmail: String = "",
    val companyName: String = "",
    val companyNumber: String = "",
    val companyEmail: String = "",
    val companyAddress: String = ""
)

class FleetViewModel(application: Application) : AndroidViewModel(application) {
    private val repository: FleetRepository
    private val sharedPrefs = application.getSharedPreferences("CompanyProfileCache", android.content.Context.MODE_PRIVATE)

    private val _companyProfile = MutableStateFlow(loadProfile())
    val companyProfile = _companyProfile.asStateFlow()

    init {
        val fleetDao = FleetDatabase.getDatabase(application).fleetDao()
        repository = FleetRepository(fleetDao)
        startTelemetrySimulation()
    }

    val vehicles: StateFlow<List<Vehicle>> = repository.allVehicles
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val tripLogs: StateFlow<List<TripLog>> = repository.allTripLogs
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    // --- Vehicle Operations ---
    
    fun addVehicle(brand: String, model: String, type: String, regNo: String, condition: String, currentMileage: Float = 10000f, serviceThreshold: Float = 15000f) {
        viewModelScope.launch {
            repository.insertVehicle(
                Vehicle(
                    brand = brand,
                    model = model,
                    type = type,
                    registrationNumber = regNo,
                    condition = condition,
                    currentMileage = currentMileage,
                    serviceThreshold = serviceThreshold
                )
            )
        }
    }

    fun updateVehicleStatus(vehicle: Vehicle, newStatus: String) {
        viewModelScope.launch {
            val updated = vehicle.copy(status = newStatus, mockSpeed = if (newStatus == "Immobilized" || newStatus == "Parked") 0f else vehicle.mockSpeed)
            repository.updateVehicle(updated)
        }
    }
    
    fun deleteVehicle(vehicle: Vehicle) {
        viewModelScope.launch {
            repository.deleteVehicle(vehicle)
        }
    }

    fun updateVehicle(vehicle: Vehicle) {
        viewModelScope.launch {
            repository.updateVehicle(vehicle)
        }
    }

    // --- Trip Operations ---

    fun dispatchVehicle(vehicleId: Int, driverName: String, destination: String, reason: String, mileageOut: Int, fuelOut: String) {
        viewModelScope.launch {
            val trip = TripLog(
                vehicleId = vehicleId,
                driverName = driverName,
                destination = destination,
                tripReason = reason,
                mileageOut = mileageOut,
                fuelOut = fuelOut,
                timeOut = System.currentTimeMillis()
            )
            repository.insertTripLog(trip)
            
            // Update vehicle status
            val vehicle = repository.getVehicleById(vehicleId)
            vehicle?.let {
                repository.updateVehicle(it.copy(status = "Moving"))
            }
        }
    }

    fun returnVehicle(tripLog: TripLog, mileageIn: Int, fuelIn: String) {
        viewModelScope.launch {
            val updatedTrip = tripLog.copy(
                mileageIn = mileageIn,
                fuelIn = fuelIn,
                timeIn = System.currentTimeMillis(),
                totalMileage = mileageIn - tripLog.mileageOut,
                status = "Completed"
            )
            repository.updateTripLog(updatedTrip)
            
            // Update vehicle status back to Parked and update mileage
            val vehicle = repository.getVehicleById(tripLog.vehicleId)
            vehicle?.let {
                val newStatus = if(it.status != "Immobilized") "Parked" else it.status
                repository.updateVehicle(
                    it.copy(
                        status = newStatus, 
                        mockSpeed = 0f, 
                        currentMileage = maxOf(it.currentMileage, mileageIn.toFloat())
                    )
                )
            }
        }
    }
    
    fun deleteTripLog(tripLog: TripLog) {
        viewModelScope.launch {
            repository.deleteTripLog(tripLog)
        }
    }

    fun logCompletedTrip(vehicleId: Int, driverName: String, destination: String, reason: String, mileageOut: Int, mileageIn: Int, fuelOut: String, fuelIn: String) {
        viewModelScope.launch {
            val trip = TripLog(
                vehicleId = vehicleId,
                driverName = driverName,
                destination = destination,
                tripReason = reason,
                mileageOut = mileageOut,
                mileageIn = mileageIn,
                fuelOut = fuelOut,
                fuelIn = fuelIn,
                timeOut = System.currentTimeMillis() - 3600000,
                timeIn = System.currentTimeMillis(),
                totalMileage = mileageIn - mileageOut,
                status = "Completed"
            )
            repository.insertTripLog(trip)
            
            val vehicle = repository.getVehicleById(vehicleId)
            vehicle?.let {
                if (it.currentMileage < mileageIn) {
                    repository.updateVehicle(it.copy(currentMileage = mileageIn.toFloat()))
                }
            }
        }
    }

    fun getTelemetryHistory(vehicleId: Int): kotlinx.coroutines.flow.Flow<List<com.example.data.TelemetryHistory>> {
        return repository.getTelemetryHistory(vehicleId)
    }

    // --- Service Records ---
    fun getServiceRecords(vehicleId: Int): kotlinx.coroutines.flow.Flow<List<com.example.data.ServiceRecord>> {
        return repository.getServiceRecordsForVehicle(vehicleId)
    }

    fun addServiceRecord(vehicleId: Int, cost: Double, notes: String) {
        viewModelScope.launch {
            val record = com.example.data.ServiceRecord(
                vehicleId = vehicleId,
                date = System.currentTimeMillis(),
                cost = cost,
                technicianNotes = notes
            )
            repository.insertServiceRecord(record)
            
            // Optionally update vehicle's service status if it was due
        }
    }

    // --- Profile Operations ---
    private fun loadProfile(): CompanyProfile {
        return CompanyProfile(
            contactName = sharedPrefs.getString("contactName", "") ?: "",
            contactNumber = sharedPrefs.getString("contactNumber", "") ?: "",
            contactEmail = sharedPrefs.getString("contactEmail", "") ?: "",
            companyName = sharedPrefs.getString("companyName", "") ?: "",
            companyNumber = sharedPrefs.getString("companyNumber", "") ?: "",
            companyEmail = sharedPrefs.getString("companyEmail", "") ?: "",
            companyAddress = sharedPrefs.getString("companyAddress", "") ?: ""
        )
    }

    fun saveCompanyProfile(profile: CompanyProfile) {
        sharedPrefs.edit().apply {
            putString("contactName", profile.contactName)
            putString("contactNumber", profile.contactNumber)
            putString("contactEmail", profile.contactEmail)
            putString("companyName", profile.companyName)
            putString("companyNumber", profile.companyNumber)
            putString("companyEmail", profile.companyEmail)
            putString("companyAddress", profile.companyAddress)
        }.apply()
        _companyProfile.value = profile
    }

    // --- Simulation ---
    private fun startTelemetrySimulation() {
        viewModelScope.launch {
            while (true) {
                delay(3000)
                val currentVehicles = vehicles.value
                currentVehicles.forEach { vehicle ->
                    val isMoving = vehicle.status == "Moving"
                    val isStarted = vehicle.status == "Started"
                    
                    val latShift = if (isMoving) Random.nextDouble(-0.0005, 0.0005) else 0.0
                    val lonShift = if (isMoving) Random.nextDouble(-0.0005, 0.0005) else 0.0
                    val speedShift = if (isMoving) Random.nextFloat() * 10f - 5f else 0f
                    
                    var newSpeed = vehicle.mockSpeed + speedShift
                    if (newSpeed < 0) newSpeed = 0f
                    if (newSpeed > 100) newSpeed = 100f
                    if (!isMoving) newSpeed = 0f
                    
                    val mileUsage = if (isMoving) (newSpeed * (3f / 3600f)) else 0f
                    val fuelUsage = if (isMoving) (newSpeed * 0.005f) else if (isStarted) 0.01f else 0f
                    
                    val updatedMileage = vehicle.currentMileage + mileUsage
                    val updatedFuel = (vehicle.fuelLevel - fuelUsage).coerceIn(0f, 100f)
                    
                    val updatedVehicle = vehicle.copy(
                        mockLatitude = vehicle.mockLatitude + latShift,
                        mockLongitude = vehicle.mockLongitude + lonShift,
                        mockSpeed = newSpeed,
                        currentMileage = updatedMileage,
                        fuelLevel = updatedFuel
                    )
                    
                    repository.updateVehicle(updatedVehicle)
                    
                    repository.insertTelemetryHistory(
                        com.example.data.TelemetryHistory(
                            vehicleId = vehicle.id,
                            timestamp = System.currentTimeMillis(),
                            speed = newSpeed,
                            mileage = updatedMileage,
                            fuelLevel = updatedFuel
                        )
                    )
                }
            }
        }
    }
}
