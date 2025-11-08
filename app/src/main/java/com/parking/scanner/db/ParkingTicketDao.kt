package com.parking.scanner.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface ParkingTicketDao {
    @Query("SELECT * FROM parking_tickets WHERE id = :ticketId")
    suspend fun findById(ticketId: String): ParkingTicketEntity?

    @Query("SELECT * FROM parking_tickets ORDER BY companyName")
    suspend fun getAllTickets(): List<ParkingTicketEntity>

    @Query("SELECT COUNT(*) FROM parking_tickets")
    suspend fun getTotalTickets(): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(tickets: List<ParkingTicketEntity>)

    @Query("DELETE FROM parking_tickets")
    suspend fun clearAll()

    @Query("SELECT DISTINCT companyName FROM parking_tickets ORDER BY companyName")
    suspend fun getCompanies(): List<String>
}
