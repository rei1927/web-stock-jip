package com.example.data

import kotlinx.coroutines.flow.Flow

class PropertyRepository(private val propertyDao: PropertyDao) {

    val allUnits: Flow<List<HousingUnit>> = propertyDao.getAllUnits()
    val allSalesLogs: Flow<List<SalesLog>> = propertyDao.getAllSalesLogs()
    val allUsers: Flow<List<User>> = propertyDao.getAllUsers()
    val allTeams: Flow<List<SalesTeam>> = propertyDao.getAllTeams()
    val allSoldProposals: Flow<List<SoldProposal>> = propertyDao.getAllSoldProposals()
    val allNotifications: Flow<List<NotificationEntity>> = propertyDao.getAllNotifications()
    val allGimmickRequests: Flow<List<GimmickRequest>> = propertyDao.getAllGimmickRequests()
    val allAttendance: Flow<List<AttendanceEntity>> = propertyDao.getAllAttendance()

    fun getAttendanceByUser(username: String): Flow<List<AttendanceEntity>> {
        return propertyDao.getAttendanceByUser(username)
    }

    suspend fun insertAttendance(attendance: AttendanceEntity) {
        propertyDao.insertAttendance(attendance)
    }

    fun getGimmickRequestsByManager(username: String): Flow<List<GimmickRequest>> {
        return propertyDao.getGimmickRequestsByManager(username)
    }

    suspend fun insertGimmickRequest(request: GimmickRequest) {
        propertyDao.insertGimmickRequest(request)
    }

    suspend fun updateGimmickRequest(request: GimmickRequest) {
        propertyDao.updateGimmickRequest(request)
    }

    suspend fun deleteGimmickRequest(request: GimmickRequest) {
        propertyDao.deleteGimmickRequest(request)
    }

    suspend fun getAllUnitsSync(): List<HousingUnit> {
        return propertyDao.getAllUnitsSync()
    }

    suspend fun getAllUsersSync(): List<User> {
        return propertyDao.getAllUsersSync()
    }

    val apiService = PropertyApiService.create()

    suspend fun syncUnitsFromWeb(authToken: String? = null, clusterName: String? = null) {
        try {
            val auth = if (authToken != null) "Bearer $authToken" else null
            val remoteUnitsDto = apiService.getUnitsByCluster(auth, clusterName)
            val existingUnits = propertyDao.getAllUnitsSync()

            // 1. Identifikasi unit yang harus di-hapus (ada di HP tapi tidak ada di Web)
            val remoteKeys = remoteUnitsDto.map { "${it.clusterName}-${it.block}" }
            val unitsToDelete = existingUnits.filter { local ->
                val isSameCluster = clusterName == null || local.clusterName == clusterName
                val isMissingFromRemote = !remoteKeys.contains("${local.clusterName}-${local.block}")
                isSameCluster && isMissingFromRemote && local.status != "Pending Sold"
            }
            unitsToDelete.forEach { propertyDao.deleteUnit(it) }

            // 2. Tambah atau Perbarui unit dari Web
            if (remoteUnitsDto.isNotEmpty()) {
                remoteUnitsDto.forEach { dto ->
                    val remoteUnit = dto.toEntity()
                    val localMatch = existingUnits.find {
                        it.clusterName == remoteUnit.clusterName && it.block == remoteUnit.block
                    }

                    if (localMatch != null) {
                        // Update existing unit while preserving critical local data if server is null
                        propertyDao.updateUnit(remoteUnit.copy(
                            id = localMatch.id,
                            status = remoteUnit.status,
                            actionByUser = remoteUnit.actionByUser ?: localMatch.actionByUser,
                            actionUserLabel = remoteUnit.actionUserLabel ?: localMatch.actionUserLabel,
                            holdTimestamp = remoteUnit.holdTimestamp ?: localMatch.holdTimestamp ?: System.currentTimeMillis()
                        ))
                    } else {
                        propertyDao.insertUnit(remoteUnit)
                    }
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
            throw e
        }
    }

    suspend fun syncUsersFromWeb(authToken: String) {
        try {
            val remoteUsersDto = apiService.getUsers("Bearer $authToken")
            if (remoteUsersDto.isNotEmpty()) {
                remoteUsersDto.forEach { dto ->
                    val remoteUser = dto.toEntity()
                    val existing = propertyDao.getUserByUsername(remoteUser.username)
                    if (existing != null) {
                        // Update existing user (don't overwrite local PIN if it matches email)
                        propertyDao.insertUser(remoteUser.copy(
                            pin = existing.pin,
                            authToken = existing.authToken,
                            managerName = remoteUser.managerName ?: existing.managerName
                        ))
                    } else {
                        propertyDao.insertUser(remoteUser)
                    }
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
            throw e
        }
    }

    suspend fun insertNotification(notification: NotificationEntity) {
        propertyDao.insertNotification(notification)
    }

    suspend fun markAllNotificationsAsRead() {
        propertyDao.markAllNotificationsAsRead()
    }

    suspend fun clearAllNotifications() {
        propertyDao.clearAllNotifications()
    }

    suspend fun getSoldProposalForUnit(unitId: Int): SoldProposal? {
        return propertyDao.getSoldProposalForUnit(unitId)
    }

    suspend fun insertSoldProposal(proposal: SoldProposal) {
        propertyDao.insertSoldProposal(proposal)
    }

    suspend fun deleteSoldProposalForUnit(unitId: Int) {
        propertyDao.deleteSoldProposalForUnit(unitId)
    }

    suspend fun insertUnit(unit: HousingUnit) {
        propertyDao.insertUnit(unit)
    }

    suspend fun updateUnit(unit: HousingUnit) {
        propertyDao.updateUnit(unit)
    }

    suspend fun updateUnitStatusOnServer(authToken: String, unit: HousingUnit) {
        try {
            apiService.updateUnitStatus(
                "Bearer $authToken",
                PropertyApiService.UpdateStatusRequest(
                    clusterName = unit.clusterName,
                    block = unit.block,
                    status = unit.status,
                    actionByUser = unit.actionByUser,
                    actionUserLabel = unit.actionUserLabel
                )
            )
        } catch (e: Exception) {
            e.printStackTrace()
            // Optional: throw or handle error
        }
    }

    suspend fun submitSoldOnServer(authToken: String, proposal: SoldProposal, gimmicks: List<String>) {
        try {
            apiService.submitSold(
                "Bearer $authToken",
                PropertyApiService.SubmitSoldRequest(
                    unit_id = proposal.unitId,
                    namaLengkap = proposal.namaLengkap,
                    alamatKtp = proposal.alamatKtp,
                    alamatSurat = proposal.alamatSurat,
                    noTelpRumah = proposal.noTelpRumah,
                    noTelpSeluler = proposal.noTelpSeluler,
                    noKtpConsumer = proposal.noKtpConsumer,
                    noNpwp = proposal.noNpwp,
                    noKk = proposal.noKk,
                    typeBlok = proposal.typeBlok,
                    cluster = proposal.cluster,
                    tujuanPembelian = proposal.tujuanPembelian,
                    sumberDana = proposal.sumberDana,
                    sistemPembayaran = proposal.sistemPembayaran,
                    kprPersen = proposal.kprPersen,
                    hargaJual = proposal.hargaJual,
                    plafondKpr = proposal.plafondKpr,
                    tandaJadi = proposal.tandaJadi,
                    tandaJadiDate = proposal.tandaJadiDate,
                    uMuka = proposal.uMuka,
                    uMukaBln = proposal.uMukaBln,
                    uMukaPertama = proposal.uMukaPertama,
                    uMukaPertamaDate = proposal.uMukaPertamaDate,
                    angsuranPertamaText = proposal.angsuranPertamaText,
                    email = proposal.email,
                    gimmicks = gimmicks,
                    utj_photo_url = proposal.photoUri
                )
            )
        } catch (e: Exception) {
            e.printStackTrace()
            throw e
        }
    }

    suspend fun deleteUnit(unit: HousingUnit) {
        propertyDao.deleteUnit(unit)
    }

    suspend fun loginOnServer(email: String, password: String): PropertyApiService.LoginResponse {
        return apiService.login(PropertyApiService.LoginRequest(email, password))
    }

    suspend fun updateFcmTokenOnServer(token: String, fcmToken: String) {
        apiService.updateFcmToken("Bearer $token", PropertyApiService.FcmTokenRequest(fcmToken))
    }

    suspend fun uploadUtjPhoto(authToken: String, filePart: okhttp3.MultipartBody.Part): PropertyApiService.UploadResponse {
        return apiService.uploadUtjPhoto("Bearer $authToken", filePart)
    }

    suspend fun uploadAttendancePhoto(authToken: String, filePart: okhttp3.MultipartBody.Part): PropertyApiService.UploadResponse {
        return apiService.uploadAttendancePhoto("Bearer $authToken", filePart)
    }

    suspend fun submitAttendanceOnServer(
        authToken: String,
        type: String,
        lat: Double,
        lon: Double,
        address: String,
        photoUrl: String
    ): PropertyApiService.AttendanceDto {
        val sdf = java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss", java.util.Locale.getDefault())
        val timestamp = sdf.format(java.util.Date())

        return apiService.submitAttendance(
            "Bearer $authToken",
            PropertyApiService.AttendanceRequest(
                type = type, // "Masuk" or "Keluar"
                lat = lat.toString(),
                long = lon.toString(),
                address = address,
                photo_url = photoUrl,
                timestamp = timestamp
            )
        )
    }

    suspend fun getAttendanceStatusFromServer(authToken: String): String {
        return try {
            val response = apiService.getAttendanceStatus("Bearer $authToken")
            response.last_status
        } catch (e: Exception) {
            "Belum Absen"
        }
    }

    suspend fun getRemoteAttendance(authToken: String): List<AttendanceEntity> {
        return apiService.getAllAttendance("Bearer $authToken").map { dto ->
            AttendanceEntity(
                username = dto.username ?: "",
                name = dto.name ?: "",
                timestamp = try {
                    java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss", java.util.Locale.getDefault()).parse(dto.created_at ?: "")?.time ?: System.currentTimeMillis()
                } catch (e: Exception) {
                    System.currentTimeMillis()
                },
                latitude = 0.0,
                longitude = 0.0,
                photoUri = dto.photo_url ?: "",
                address = dto.address ?: dto.location ?: "",
                type = if (dto.type == "check_in" || dto.type == "Masuk") "Masuk" else "Keluar",
                remoteId = dto.id
            )
        }
    }

    suspend fun insertSalesLog(log: SalesLog) {
        propertyDao.insertSalesLog(log)
    }

    suspend fun insertUser(user: User) {
        propertyDao.insertUser(user)
    }

    suspend fun deleteUser(user: User) {
        propertyDao.deleteUser(user)
    }

    suspend fun getUserByUsername(username: String): User? {
        return propertyDao.getUserByUsername(username)
    }

    suspend fun insertTeam(team: SalesTeam) {
        propertyDao.insertTeam(team)
    }

    suspend fun deleteTeam(team: SalesTeam) {
        propertyDao.deleteTeam(team)
    }
}
