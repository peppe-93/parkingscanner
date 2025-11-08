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

        val tickets = mutableListOf<ParkingTicketEntity>()

        for (record in csvParser) {
            try {
                val ticket = ParkingTicketEntity(
                    id = record["ID"],
                    companyName = record["COMPANY_NAME"],
                    color = record["COLOR"],
                    timestamp = record["TIMESTAMP"],
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
