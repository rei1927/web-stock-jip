package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "users")
data class User(
    @PrimaryKey val username: String, // lowercase, unique identifier
    val name: String,
    val role: String, // "Sales", "Sales Manager", "Admin", "Super Admin"
    val pin: String = "1234",
    val managerName: String? = null,
    val authToken: String? = null // Sanctum token from server
)

@Entity(tableName = "housing_units")
data class HousingUnit(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val clusterName: String,
    val block: String,
    val typeName: String, // e.g. "Tipe 70/120"
    val price: Double,
    val isSold: Boolean,
    val buildingArea: Int, // m2
    val landArea: Int, // m2
    val bedrooms: Int,
    val bathrooms: Int,
    val notes: String = "",
    val status: String = "Tersedia", // "Tersedia", "Hold", "Pending Sold", "Terjual"
    val actionByUser: String? = null, // Username of salesperson who put on Hold or requested Sold
    val actionUserLabel: String? = null, // Nice readable name of person who did the action
    val holdTimestamp: Long? = null
)

@Entity(tableName = "notifications")
data class NotificationEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val title: String,
    val message: String,
    val timestamp: Long,
    val isRead: Boolean = false
)

@Entity(tableName = "sales_logs")
data class SalesLog(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val clusterName: String,
    val block: String,
    val salePrice: Double,
    val monthIndex: Int, // 1 to 12 representing Jan to Dec
    val year: Int = 2026,
    val timestamp: Long = System.currentTimeMillis(), // added for precise range filtering
    val soldBy: String = "siska", // Username of the salesperson who sold it
    val managerName: String? = "rudi" // Manager of that salesperson
)

@Entity(tableName = "sales_teams")
data class SalesTeam(
    @PrimaryKey val teamId: String,
    val teamName: String,
    val leaderUsername: String? = null // Sales Manager username
)

@Entity(tableName = "attendance_logs")
data class AttendanceEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val username: String,
    val name: String,
    val timestamp: Long,
    val latitude: Double,
    val longitude: Double,
    val photoUri: String,
    val address: String = "",
    val type: String = "Masuk" // "Masuk" or "Keluar"
)

@Entity(tableName = "gimmick_requests")
data class GimmickRequest(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val unitId: Int,
    val clusterName: String,
    val block: String,
    val salespersonUsername: String,
    val salespersonName: String,
    val requestedByUsername: String,
    val requestedByName: String,
    val gimmickDetails: String,
    val status: String = "Pending", // "Pending", "Approved", "Rejected"
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "sold_proposals")
data class SoldProposal(
    @PrimaryKey val unitId: Int, // matches HousingUnit id
    val photoUri: String? = null, // Path to the photo uploaded by Sales
    val namaLengkap: String = "",
    val alamatKtp: String = "",
    val alamatSurat: String = "",
    val noTelpRumah: String = "",
    val noTelpSeluler: String = "",
    val noKtpConsumer: String = "",
    val noNpwp: String = "",
    val noKk: String = "",
    val typeBlok: String = "",
    val cluster: String = "",
    val tujuanPembelian: String = "",
    val sumberDana: String = "",
    val sistemPembayaran: String = "",
    val kprPersen: String = "",
    val hargaJual: Double = 0.0,
    val plafondKpr: Double = 0.0,
    val tandaJadi: Double = 0.0,
    val tandaJadiDate: String = "",
    val uMuka: Double = 0.0,
    val uMukaBln: String = "",
    val uMukaPertama: Double = 0.0,
    val uMukaPertamaDate: String = "",
    val angsuranPertamaText: String = "",
    val email: String = "",
    val sumber: String = ""
)


