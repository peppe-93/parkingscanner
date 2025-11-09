package com.parking.scanner.utils

import android.content.Context
import com.parking.scanner.db.ParkingDatabase
import com.parking.scanner.db.ParkingTicketEntity
import org.apache.commons.csv.CSVFormat
import java.io.File
import java.io.InputStreamReader

class CsvManager(private val context: Context) {
    private val database = ParkingDatabase.getDatabase(context)
    private val dao = database.parkingTicketDao()

    suspend fun loadCsvFromFile(file: File): Result<Int> = try {
        val csvParser = CSVFormat.DEFAULT.withFirstRecordAsHeader()
            .parse(InputStreamReader(file.inputStream()))

        // Mapping header: consente sia maiuscolo che minuscolo
        val headerToField = mutableMapOf<String, String>()
        csvParser.headerMap.keys.forEach { key ->
            val normalized = key.trim().lowercase()
            when (normalized) {
                "id" -> headerToField["id"] = key
                "company_name" -> headerToField["company_name"] = key
                "color" -> headerToField["color"] = key
                "timestamp" -> headerToField["timestamp"] = key
            }
        }
        val tickets = mutableListOf<ParkingTicketEntity>()
        for (record in csvParser) {
            try {
                val ticket = ParkingTicketEntity(
                    id = record.get(headerToField["id"]),
                    companyName = record.get(headerToField["company_name"]),
                    color = record.get(headerToField["color"]),
                    timestamp = record.get(headerToField["timestamp"]),
                    isValid = true
                )
                tickets.add(ticket)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        dao.clearAll()
        dao.insertAll(tickets)
        csvParser.close()

        Result.success(tickets.size)
    } catch (e: Exception) {
        Result.failure(e)
    }

    suspend fun getTickets() = dao.getAllTickets()
    suspend fun getCompanies() = dao.getCompanies()
}
