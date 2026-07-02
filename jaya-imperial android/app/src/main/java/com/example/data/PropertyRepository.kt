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

    private val apiService = PropertyApiService.create()

    suspend fun syncUnitsFromWeb(clusterName: String? = null) {
        try {
            val remoteUnitsDto = apiService.getUnitsByCluster(clusterName)
            val existingUnits = propertyDao.getAllUnitsSync()

            if (remoteUnitsDto.isNotEmpty()) {
                remoteUnitsDto.forEach { dto ->
                    val remoteUnit = dto.toEntity()
                    // Cari unit lokal yang sama berdasarkan Klaster dan Blok
                    val localMatch = existingUnits.find {
                        it.clusterName == remoteUnit.clusterName && it.block == remoteUnit.block
                    }

                    if (localMatch != null) {
                        // Update unit yang ada, tapi JANGAN timpa status jika lokal sedang "Pending Sold"
                        // kecuali server bilang sudah "Terjual"
                        val finalStatus = if (localMatch.status == "Pending Sold" && remoteUnit.status != "Terjual") {
                            "Pending Sold"
                        } else {
                            remoteUnit.status
                        }

                        propertyDao.updateUnit(remoteUnit.copy(
                            id = localMatch.id,
                            status = finalStatus,
                            actionByUser = remoteUnit.actionByUser ?: localMatch.actionByUser,
                            actionUserLabel = remoteUnit.actionUserLabel ?: localMatch.actionUserLabel
                        ))
                    } else {
                        // Jika unit baru benar-benar tidak ada, masukkan ke DB
                        propertyDao.insertUnit(remoteUnit)
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
                    unitId = proposal.unitId,
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
                    gimmicks = gimmicks
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
