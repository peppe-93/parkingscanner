package com.parking.scanner.db

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "parking_tickets")
data class ParkingTicketEntity(
    @PrimaryKey val id: String,
    val companyName: String,
    val color: String,
    val timestamp: String,
    val isValid: Boolean = true,
    val isDouble: Boolean = false // nuovo campo
)
