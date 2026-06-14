package com.example.data

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class FleetExport(
    val vehicles: List<Vehicle>,
    val tripLogs: List<TripLog>,
    val telemetryHistory: List<TelemetryHistory>,
    val serviceRecords: List<ServiceRecord>
)
