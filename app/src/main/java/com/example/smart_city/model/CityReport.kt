package com.example.smart_city.model

data class CityReport(
    val id: String,
    val title: String,
    val type: String,
    val lat: Double,
    val lng: Double,
    val impactPoints: Int,
    val severity: Int,        // 1–10
    val imageUrl: String,
    val address: String,
    val lastUpdated: String
)