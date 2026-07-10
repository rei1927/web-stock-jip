package com.example.ui

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.*
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.ExperimentalCoroutinesApi
import java.util.Calendar
import kotlin.math.pow
import android.content.Context
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.toRequestBody
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import java.io.ByteArrayOutputStream

class PropertyViewModel(private val repository: PropertyRepository) : ViewModel() {

    // --- 1. STATE PROPERTIES (Must be at the very top) ---
    private val _currentUser = MutableStateFlow<User?>(null)
    val currentUser: StateFlow<User?> = _currentUser.asStateFlow()

    private val _isSyncing = MutableStateFlow(false)
    val isSyncing = _isSyncing.asStateFlow()

    private val _syncError = MutableStateFlow<String?>(null)
    val syncError = _syncError.asStateFlow()

    private val _fcmToken = MutableStateFlow<String>("Loading...")
    val fcmToken = _fcmToken.asStateFlow()

    private val _lastAttendanceStatus = MutableStateFlow("Belum Absen")
    val lastAttendanceStatus: StateFlow<String> = _lastAttendanceStatus.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _statusFilter = MutableStateFlow(FilterStatus.ALL)
    val statusFilter: StateFlow<FilterStatus> = _statusFilter.asStateFlow()

    private val _clusterFilter = MutableStateFlow<String?>(null)
    val clusterFilter: StateFlow<String?> = _clusterFilter.asStateFlow()

    // Period filtering (Start Date & End Date)
    private val _startDate = MutableStateFlow(Calendar.getInstance().apply {
        set(Calendar.DAY_OF_YEAR, 1)
        set(Calendar.HOUR_OF_DAY, 0)
        set(Calendar.MINUTE, 0)
        set(Calendar.SECOND, 0)
    }.timeInMillis)
    val startDate: StateFlow<Long> = _startDate.asStateFlow()

    private val _endDate = MutableStateFlow(System.currentTimeMillis())
    val endDate: StateFlow<Long> = _endDate.asStateFlow()

    private val _propertyPrice = MutableStateFlow(1500000000.0)
    val propertyPrice: StateFlow<Double> = _propertyPrice.asStateFlow()

    private val _dpPercent = MutableStateFlow(20.0)
    val dpPercent: StateFlow<Double> = _dpPercent.asStateFlow()

    private val _interestRate = MutableStateFlow(11.0)
    val interestRate: StateFlow<Double> = _interestRate.asStateFlow()

    private val _termYears = MutableStateFlow(20.0)
    val termYears: StateFlow<Double> = _termYears.asStateFlow()

    // --- 2. DATA FLOWS ---
    val allUnits: StateFlow<List<HousingUnit>> = repository.allUnits
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val availableClusters: StateFlow<List<String>> = allUnits.map { units ->
        units.map { it.clusterName }.distinct().sorted()
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val filteredUnits: StateFlow<List<HousingUnit>> = combine(
        allUnits, _searchQuery, _statusFilter, _clusterFilter
    ) { units, query, filter, cluster ->
        units.filter { unit ->
            val matchesSearch = unit.clusterName.contains(query, ignoreCase = true) ||
                    unit.typeName.contains(query, ignoreCase = true) ||
                    unit.block.contains(query, ignoreCase = true)
            val matchesFilter = when (filter) {
                FilterStatus.ALL -> true
                FilterStatus.TERSEDIA -> unit.status == "Tersedia"
                FilterStatus.HOLD -> unit.status == "Hold"
                FilterStatus.TERJUAL -> unit.status == "Terjual"
            }
            val matchesCluster = cluster == null || unit.clusterName.equals(cluster, ignoreCase = true)
            matchesSearch && matchesFilter && matchesCluster
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allUsers: StateFlow<List<User>> = repository.allUsers
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allTeams: StateFlow<List<SalesTeam>> = repository.allTeams
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allSoldProposals: StateFlow<List<SoldProposal>> = repository.allSoldProposals
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allNotifications: StateFlow<List<NotificationEntity>> = repository.allNotifications
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allGimmickRequests: StateFlow<List<GimmickRequest>> = repository.allGimmickRequests
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allAttendance: StateFlow<List<AttendanceEntity>> = repository.allAttendance
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    @OptIn(ExperimentalCoroutinesApi::class)
    val myAttendance: StateFlow<List<AttendanceEntity>> = currentUser.flatMapLatest { user ->
        if (user != null) repository.getAttendanceByUser(user.username) else flowOf(emptyList())
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    @OptIn(ExperimentalCoroutinesApi::class)
    val myGimmickRequests: StateFlow<List<GimmickRequest>> = currentUser.flatMapLatest { user ->
        if (user != null && user.role == "Sales Manager") repository.getGimmickRequestsByManager(user.username) else flowOf(emptyList())
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val salesLogs: StateFlow<List<SalesLog>> = repository.allSalesLogs
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val filteredSalesLogs: StateFlow<List<SalesLog>> = combine(repository.allSalesLogs, currentUser, _startDate, _endDate) { logs, user, start, end ->
        if (user == null) emptyList() else {
            val roleLogs = when (user.role) {
                "Sales" -> logs.filter { it.soldBy == user.username }
                "Sales Manager" -> logs.filter { it.managerName == user.username || it.soldBy == user.username }
                else -> logs
            }
            roleLogs.filter { it.timestamp in start..end }
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val totalTurnover: StateFlow<Double> = filteredSalesLogs.map { logs ->
        logs.sumOf { it.salePrice }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0.0)

    val soldUnitsCount: StateFlow<Int> = filteredSalesLogs.map { it.size }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    val averageUnitPrice: StateFlow<Double> = combine(totalTurnover, soldUnitsCount) { turnover, count ->
        if (count > 0) turnover / count else 0.0
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0.0)

    val monthlySalesData: StateFlow<List<Double>> = filteredSalesLogs.map { logs ->
        val monthlySales = MutableList(12) { 0.0 }
        logs.forEach { log ->
            val cal = Calendar.getInstance().apply { timeInMillis = log.timestamp }
            val month = cal.get(Calendar.MONTH)
            if (month in 0..11) monthlySales[month] += log.salePrice
        }
        monthlySales.toList()
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), List(12) { 0.0 })

    val monthlyInstallment: StateFlow<Double> = combine(_propertyPrice, _dpPercent, _interestRate, _termYears) { price, dp, interest, years ->
        val loan = price - (price * (dp / 100.0))
        val rate = (interest / 100.0) / 12.0
        val months = years * 12.0
        if (loan <= 0) 0.0 else if (rate == 0.0) loan / months else (loan * rate * (1.0 + rate).pow(months)) / ((1.0 + rate).pow(months) - 1.0)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0.0)

    // --- 3. INITIALIZATION ---
    init {
        // Load active session from local DB
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val users = repository.getAllUsersSync()
                val activeUser = users.find { !it.authToken.isNullOrEmpty() }
                if (activeUser != null) {
                    // Sanitize role when loading session
                    val rawRole = activeUser.role
                    val sanitizedRole = when {
                        rawRole.contains("SUPER", ignoreCase = true) -> "Super Admin"
                        rawRole.contains("ADMIN", ignoreCase = true) -> "Admin"
                        rawRole.contains("MANAGER", ignoreCase = true) -> "Sales Manager"
                        else -> "Sales"
                    }
                    _currentUser.value = activeUser.copy(role = sanitizedRole)
                    Log.d("SESSION", "Auto-login success for: ${activeUser.username} as $sanitizedRole")

                    // Trigger initial sync after session is loaded
                    syncData()
                    refreshAttendanceStatus()
                } else {
                    // Even without login, we can try to sync public data
                    syncData()
                }
            } catch (e: Exception) {
                Log.e("SESSION", "Failed to load session: ${e.message}")
                syncData()
            }
        }

        viewModelScope.launch(Dispatchers.IO) {
            while (true) {
                checkAndReleaseExpiredHolds()
                delay(10000)
            }
        }

        viewModelScope.launch {
            currentUser.collect { user ->
                if (user != null) {
                    sendFcmTokenToServer()
                }
            }
        }
    }

    // --- 4. AUTHENTICATION & FCM ---
    fun login(emailOrUsername: String, password: String = "password", onResult: (Boolean) -> Unit) {
        viewModelScope.launch {
            val identifier = emailOrUsername.trim().lowercase()
            Log.d("LOGIN", "Attempting login for: $identifier")

            // WAJIB login via Server Laravel untuk keamanan dan sinkronisasi data terbaru
            try {
                val response = repository.loginOnServer(identifier, password)
                val serverUser = response.user

                // Sanitize role for consistency across the app
                val rawRole = serverUser.role ?: "Sales"
                val sanitizedRole = when {
                    rawRole.contains("SUPER", ignoreCase = true) -> "Super Admin"
                    rawRole.contains("ADMIN", ignoreCase = true) -> "Admin"
                    rawRole.contains("MANAGER", ignoreCase = true) -> "Sales Manager"
                    else -> "Sales"
                }

                val localUser = User(
                    username = identifier,
                    name = serverUser.name,
                    role = sanitizedRole,
                    pin = password,
                    authToken = response.access_token
                )

                // Pastikan database lokal bersih dari user lama sebelum menyimpan sesi baru
                repository.getAllUsersSync().forEach { repository.deleteUser(it) }
                repository.insertUser(localUser)

                _currentUser.value = localUser
                _syncError.value = null // Reset pesan error jika ada

                Log.d("LOGIN", "SERVER LOGIN SUCCESS. Token received.")

                // Segera tarik data stok terbaru setelah login berhasil
                syncData()
                refreshAttendanceStatus()

                onResult(true)
            } catch (e: Exception) {
                Log.e("LOGIN", "SERVER LOGIN FAILED. Error: ${e.localizedMessage}")
                // Mendeteksi detail error dari server jika memungkinkan
                val errorBody = (e as? retrofit2.HttpException)?.response()?.errorBody()?.string()
                if (errorBody != null) {
                    Log.e("LOGIN", "Server Error Detail: $errorBody")
                }

                val errorMsg = if (e.message?.contains("422") == true) {
                    "Email atau Password ditolak server. Cek kembali data Anda."
                } else {
                    "Koneksi Gagal. Pastikan internet aktif dan server online."
                }
                _syncError.value = "Login Gagal: $errorMsg"
                onResult(false)
            }
        }
    }

    fun logout() {
        viewModelScope.launch(Dispatchers.IO) {
            val user = _currentUser.value
            if (user != null) {
                repository.insertUser(user.copy(authToken = null))
            }
            _currentUser.value = null
        }
    }

    fun setFcmToken(token: String) {
        _fcmToken.value = token
        sendFcmTokenToServer()
    }

    private fun sendFcmTokenToServer() {
        val fcm = _fcmToken.value
        val user = _currentUser.value
        if (fcm != "Loading..." && !fcm.startsWith("Gagal") && user != null) {
            viewModelScope.launch(Dispatchers.IO) {
                try {
                    val authToken = user.authToken ?: ""
                    if (authToken.isNotEmpty()) {
                        repository.updateFcmTokenOnServer(authToken, fcm)
                        Log.d("FCM", "Token successfully sent to Laravel")
                    }
                } catch (e: Exception) {
                    Log.e("FCM", "Failed to send token to Laravel: ${e.message}")
                }
            }
        }
    }

    // --- 5. UNIT ACTIONS ---
    fun syncData(clusterName: String? = null) {
        viewModelScope.launch {
            _isSyncing.value = true
            try {
                val token = _currentUser.value?.authToken

                // 1. Sync Units
                repository.syncUnitsFromWeb(token, clusterName)

                // 2. Sync Users (If logged in with token)
                if (token != null) {
                    repository.syncUsersFromWeb(token)
                    refreshAttendanceStatus()

                    // 3. Sync Attendance (For everyone, so Sales can see their history)
                    try {
                        val remoteAttendance = repository.getRemoteAttendance(token)
                        remoteAttendance.forEach { repository.insertAttendance(it) }
                    } catch (e: Exception) {
                        Log.e("SYNC", "Gagal sync attendance: ${e.message}")
                    }
                }

                _syncError.value = null // Clear error on success
                Log.d("SYNC", "Data stock and users successfully synced from server.")
            } catch (e: Exception) {
                Log.e("SYNC", "Sync failed: ${e.message}")
                val errorBody = (e as? retrofit2.HttpException)?.response()?.errorBody()?.string()
                if (errorBody != null) {
                    Log.e("SYNC", "Server Error Detail: $errorBody")
                }

                _syncError.value = when {
                    e.message?.contains("401") == true -> {
                        // Jika 401 (Unauthenticated), sebaiknya logout karena token tidak valid
                        logout()
                        "Sesi Habis. Silakan Login Kembali."
                    }
                    e.message?.contains("500") == true -> "Server Error (500)."
                    e.message?.contains("404") == true -> "Endpoint Tidak Ditemukan."
                    e is java.net.UnknownHostException -> "Server Tidak Ditemukan (Cek Internet)."
                    e is java.net.SocketTimeoutException -> "Koneksi Timeout. Coba Lagi."
                    e is java.io.IOException -> "Koneksi Internet Terputus (Offline)."
                    else -> "Gagal Sync: ${e.localizedMessage}"
                }
            } finally {
                _isSyncing.value = false
            }
        }
    }

    fun addNewUnit(clusterName: String, block: String, typeName: String, price: Double, buildingArea: Int, landArea: Int, bedrooms: Int, bathrooms: Int, notes: String) {
        viewModelScope.launch {
            val newUnit = HousingUnit(clusterName = clusterName, block = block, typeName = typeName, price = price, isSold = false, buildingArea = buildingArea, landArea = landArea, bedrooms = bedrooms, bathrooms = bathrooms, notes = notes, status = "Tersedia")
            repository.insertUnit(newUnit)
        }
    }

    fun updateUnit(unit: HousingUnit) { viewModelScope.launch { repository.updateUnit(unit) } }
    fun deleteUnit(unit: HousingUnit) { viewModelScope.launch { repository.deleteUnit(unit) } }

    fun holdUnit(unit: HousingUnit) {
        viewModelScope.launch {
            val user = _currentUser.value ?: return@launch
            val updatedUnit = unit.copy(status = "Hold", actionByUser = user.username, actionUserLabel = user.name, holdTimestamp = System.currentTimeMillis())
            repository.updateUnit(updatedUnit)

            // Push update to Web App
            user.authToken?.let { token ->
                repository.updateUnitStatusOnServer(token, updatedUnit)
            }

            repository.insertNotification(NotificationEntity(title = "HOLD ${unit.block}", message = "${user.name} melakukan HOLD di ${unit.clusterName}.", timestamp = System.currentTimeMillis()))
        }
    }

    fun releaseHoldOrRejectSold(unit: HousingUnit) {
        viewModelScope.launch {
            val isPending = unit.status == "Pending Sold"
            val user = _currentUser.value
            val updatedUnit = unit.copy(status = "Tersedia", isSold = false, actionByUser = null, actionUserLabel = null, holdTimestamp = null)
            repository.updateUnit(updatedUnit)

            // Push update to Web App
            user?.authToken?.let { token ->
                repository.updateUnitStatusOnServer(token, updatedUnit)
            }

            repository.deleteSoldProposalForUnit(unit.id)
            repository.insertNotification(NotificationEntity(title = if (isPending) "BATAL SOLD ${unit.block}" else "RELEASE HOLD ${unit.block}", message = "Status unit diperbarui oleh sistem.", timestamp = System.currentTimeMillis()))
        }
    }

    fun submitSoldPhoto(unit: HousingUnit, photoUri: String) {
        viewModelScope.launch {
            val user = _currentUser.value ?: return@launch
            val updatedUnit = unit.copy(status = "Pending Sold", actionByUser = user.username, actionUserLabel = user.name)

            repository.insertSoldProposal(SoldProposal(unitId = unit.id, photoUri = photoUri, cluster = unit.clusterName, typeBlok = "${unit.typeName} - ${unit.block}"))
            repository.updateUnit(updatedUnit)

            // Push update to Web App
            user.authToken?.let { token ->
                repository.updateUnitStatusOnServer(token, updatedUnit)
            }

            repository.insertNotification(NotificationEntity(title = "PENGAJUAN SOLD ${unit.block}", message = "${user.name} mengunggah foto penjualan.", timestamp = System.currentTimeMillis()))
        }
    }

    fun submitSoldProposal(unit: HousingUnit, proposal: SoldProposal, gimmicks: List<String>, context: android.content.Context) {
        viewModelScope.launch {
            val user = _currentUser.value ?: return@launch
            val token = user.authToken

            var finalProposal = proposal

            // 1. Upload UTJ Photo to MinIO if exists and online
            if (!token.isNullOrEmpty() && proposal.photoUri != null) {
                try {
                    val uri = android.net.Uri.parse(proposal.photoUri)
                    val bytes = compressImage(context, uri)

                    if (bytes != null) {
                        val requestFile = bytes.toRequestBody("image/jpeg".toMediaTypeOrNull())
                        val body = okhttp3.MultipartBody.Part.createFormData("file", "utj_${unit.block}_${System.currentTimeMillis()}.jpg", requestFile)

                        val uploadRes = repository.uploadUtjPhoto(token, body)
                        finalProposal = proposal.copy(photoUri = uploadRes.url)
                        Log.d("UTJ", "Foto UTJ berhasil diupload ke MinIO: ${uploadRes.url}")
                    }
                } catch (e: Exception) {
                    Log.e("UTJ", "Gagal upload UTJ: ${e.message}")
                }
            }

            val updatedUnit = unit.copy(status = "Pending Sold", actionByUser = user.username, actionUserLabel = user.name)
            repository.insertSoldProposal(finalProposal)
            repository.updateUnit(updatedUnit)

            // Push update to Web App
            token?.let {
                repository.updateUnitStatusOnServer(it, updatedUnit)
            }

            repository.insertNotification(NotificationEntity(
                title = "PENGAJUAN SOLD ${unit.block}",
                message = "${user.name} telah melengkapi form & foto UTJ untuk ${unit.block}.",
                timestamp = System.currentTimeMillis()
            ))
        }
    }

    suspend fun getSoldProposalForUnit(unitId: Int): SoldProposal? {
        return repository.getSoldProposalForUnit(unitId)
    }

    fun markAsSoldDirectly(unit: HousingUnit) {
        viewModelScope.launch {
            val user = _currentUser.value ?: return@launch
            val updatedUnit = unit.copy(status = "Terjual", isSold = true, actionByUser = user.username, actionUserLabel = user.name, holdTimestamp = null)

            repository.updateUnit(updatedUnit)

            // Push update to Web App
            user.authToken?.let { token ->
                repository.updateUnitStatusOnServer(token, updatedUnit)
            }

            repository.insertSalesLog(SalesLog(clusterName = unit.clusterName, block = unit.block, salePrice = unit.price, monthIndex = 6, year = 2026, soldBy = user.username, managerName = if (user.role == "Sales Manager") user.username else user.managerName))
            repository.deleteSoldProposalForUnit(unit.id)
            repository.insertNotification(NotificationEntity(title = "SOLD ${unit.block}", message = "${user.name} menandai unit sebagai TERJUAL.", timestamp = System.currentTimeMillis()))
        }
    }

    fun approveSoldUnit(unit: HousingUnit, finalProposal: SoldProposal? = null, gimmicks: List<String> = emptyList()) {
        viewModelScope.launch {
            val user = _currentUser.value
            val salesperson = repository.getUserByUsername(unit.actionByUser ?: "")
            val updatedUnit = unit.copy(status = "Terjual", isSold = true, holdTimestamp = null)

            repository.updateUnit(updatedUnit)

            // Push update to Web App - Full Sold Admin Data
            user?.authToken?.let { token ->
                if (finalProposal != null) {
                    try {
                        repository.submitSoldOnServer(token, finalProposal, gimmicks)
                    } catch (e: Exception) {
                        // Fallback to basic status update if full submit fails
                        repository.updateUnitStatusOnServer(token, updatedUnit)
                    }
                } else {
                    repository.updateUnitStatusOnServer(token, updatedUnit)
                }
            }

            repository.insertSalesLog(SalesLog(clusterName = unit.clusterName, block = unit.block, salePrice = finalProposal?.hargaJual ?: unit.price, monthIndex = 6, year = 2026, soldBy = unit.actionByUser ?: "siska", managerName = salesperson?.managerName ?: user?.username))
            repository.deleteSoldProposalForUnit(unit.id)
            repository.insertNotification(NotificationEntity(title = "SOLD APPROVED ${unit.block}", message = "Penjualan disetujui oleh ${user?.name}.", timestamp = System.currentTimeMillis()))
        }
    }

    private suspend fun checkAndReleaseExpiredHolds() {
        val current = System.currentTimeMillis()
        repository.getAllUnitsSync().filter { it.status == "Hold" && it.holdTimestamp != null && (current - it.holdTimestamp >= 12 * 3600000L) }.forEach {
            val updated = it.copy(status = "Tersedia", actionByUser = null, actionUserLabel = null, holdTimestamp = null)
            repository.updateUnit(updated)
            repository.insertNotification(NotificationEntity(title = "Hold Expired", message = "Unit ${it.block} dilepas otomatis.", timestamp = System.currentTimeMillis()))
        }
    }

    // --- 6. USER & TEAM MANAGEMENT ---
    fun addNewUser(username: String, name: String, role: String, pin: String, managerName: String? = null) {
        viewModelScope.launch {
            val userObj = User(
                username = username.trim().lowercase(),
                name = name.trim(),
                role = role,
                pin = pin.ifBlank { "123456" },
                managerName = managerName?.ifBlank { null }
            )

            val currentUserToken = _currentUser.value?.authToken

            if (!currentUserToken.isNullOrEmpty()) {
                try {
                    // 1. Kirim data ke Server
                    val serverRole = when {
                        userObj.role.contains("SUPER", ignoreCase = true) -> "Super Admin"
                        userObj.role.contains("MANAGER", ignoreCase = true) -> "Sales Manager"
                        userObj.role.contains("ADMIN", ignoreCase = true) -> "Admin"
                        else -> "Sales"
                    }

                    try {
                        // Coba register dulu
                        val request = PropertyApiService.RegisterUserRequest(
                            name = userObj.name,
                            email = userObj.username,
                            password = userObj.pin,
                            role = serverRole
                        )
                        repository.apiService.registerUser("Bearer $currentUserToken", request)
                        Log.d("ADD_USER", "User baru berhasil didaftarkan di server")
                    } catch (e: Exception) {
                        val errorBody = (e as? retrofit2.HttpException)?.response()?.errorBody()?.string()
                        // Jika gagal karena email sudah ada, coba Update Role via endpoint baru
                        if (errorBody?.contains("email", ignoreCase = true) == true || e.message?.contains("422") == true) {
                            Log.d("ADD_USER", "Email sudah ada, mencoba update role...")
                            val updateRequest = PropertyApiService.UpdateUserRequest(
                                email = userObj.username,
                                role = serverRole
                            )
                            repository.apiService.updateUser("Bearer $currentUserToken", updateRequest)
                            Log.d("ADD_USER", "Role user berhasil diperbarui di server")
                        } else {
                            throw e // Lempar ke catch blok utama jika error lain
                        }
                    }

                    // 2. Simpan di lokal
                    repository.insertUser(userObj)
                    Log.d("ADD_USER", "User berhasil disimpan di database lokal")
                } catch (e: Exception) {
                    Log.e("ADD_USER", "Gagal menyimpan ke server: ${e.message}")
                    val errorBody = (e as? retrofit2.HttpException)?.response()?.errorBody()?.string()
                    val specificError = if (errorBody != null) {
                        Log.e("ADD_USER", "Server Detail: $errorBody")
                        // Sederhanakan pesan error untuk user
                        when {
                            errorBody.contains("email", ignoreCase = true) -> "Email sudah terdaftar atau format salah."
                            errorBody.contains("password", ignoreCase = true) -> "Password minimal 6 karakter."
                            else -> errorBody
                        }
                    } else e.localizedMessage

                    repository.insertUser(userObj)
                    _syncError.value = "Gagal Sinkron: $specificError. (Data tersimpan di HP)"
                }
            } else {
                // Tidak ada token, simpan lokal saja
                repository.insertUser(userObj)
                _syncError.value = "Mode Offline: User tidak sinkron ke database pusat. Harap Login saat Online."
                Log.w("ADD_USER", "Token tidak ditemukan, menyimpan secara lokal saja")
            }
        }
    }
    fun deleteUser(user: User) {
        viewModelScope.launch { if (user.username != _currentUser.value?.username) repository.deleteUser(user) }
    }
    fun assignManagerToUser(salesUsername: String, managerUsername: String?) {
        viewModelScope.launch {
            val user = repository.getUserByUsername(salesUsername)
            if (user != null) repository.insertUser(user.copy(managerName = managerUsername))
        }
    }
    fun addNewTeam(teamName: String, leaderUsername: String?) {
        viewModelScope.launch { repository.insertTeam(SalesTeam("team_${System.currentTimeMillis()}", teamName.trim(), leaderUsername)) }
    }
    fun deleteTeam(team: SalesTeam) { viewModelScope.launch { repository.deleteTeam(team) } }
    fun assignLeaderToTeam(teamId: String, leaderUsername: String?) {
        viewModelScope.launch {
            val team = allTeams.value.find { it.teamId == teamId }
            if (team != null) repository.insertTeam(team.copy(leaderUsername = leaderUsername))
        }
    }

    fun changePassword(passwordLama: String, passwordBaru: String, onSuccess: () -> Unit, onError: (String) -> Unit) {
        viewModelScope.launch {
            val user = _currentUser.value
            val token = user?.authToken
            if (user != null && !token.isNullOrEmpty()) {
                try {
                    val request = PropertyApiService.ChangePasswordRequest(
                        email = user.username,
                        password_lama = passwordLama,
                        password_baru = passwordBaru
                    )
                    repository.apiService.changePassword("Bearer $token", request)

                    // Update PIN lokal juga agar tetap sinkron saat login offline berikutnya
                    repository.insertUser(user.copy(pin = passwordBaru))

                    onSuccess()
                } catch (e: Exception) {
                    val errorBody = (e as? retrofit2.HttpException)?.response()?.errorBody()?.string()
                    val errorMessage = if (errorBody != null) {
                        when {
                            errorBody.contains("password", ignoreCase = true) -> "Password lama salah atau format password baru tidak sesuai."
                            else -> "Gagal mengubah password. Silakan coba lagi."
                        }
                    } else "Koneksi internet bermasalah."
                    onError(errorMessage)
                }
            } else {
                onError("Sesi tidak valid, silakan login ulang.")
            }
        }
    }

    // --- HELPER: IMAGE COMPRESSION ---
    private fun compressImage(context: Context, imageUri: android.net.Uri): ByteArray? {
        return try {
            val inputStream = context.contentResolver.openInputStream(imageUri)
            val originalBitmap = BitmapFactory.decodeStream(inputStream)
            inputStream?.close()

            if (originalBitmap == null) return null

            // Resize if too large (e.g. max width/height 1024)
            val maxWidth = 1024
            val maxHeight = 1024
            var width = originalBitmap.width
            var height = originalBitmap.height

            if (width > maxWidth || height > maxHeight) {
                val ratio = width.toFloat() / height.toFloat()
                if (ratio > 1) {
                    width = maxWidth
                    height = (maxWidth / ratio).toInt()
                } else {
                    height = maxHeight
                    width = (maxHeight * ratio).toInt()
                }
            }

            val resizedBitmap = Bitmap.createScaledBitmap(originalBitmap, width, height, true)
            val outputStream = ByteArrayOutputStream()

            // Compress quality to 70% to hit the ~500KB target
            resizedBitmap.compress(Bitmap.CompressFormat.JPEG, 70, outputStream)
            val result = outputStream.toByteArray()

            Log.d("COMPRESS", "Original: ${originalBitmap.byteCount / 1024}KB, Compressed: ${result.size / 1024}KB")
            result
        } catch (e: Exception) {
            Log.e("COMPRESS", "Gagal kompres: ${e.message}")
            null
        }
    }

    // --- 7. ATTENDANCE & GIMMICK ---
    fun refreshAttendanceStatus() {
        viewModelScope.launch {
            val token = _currentUser.value?.authToken
            if (token != null) {
                _lastAttendanceStatus.value = repository.getAttendanceStatusFromServer(token)
            }
        }
    }

    fun submitAttendance(lat: Double, lon: Double, photoUri: String, addr: String, type: String, context: android.content.Context) {
        viewModelScope.launch {
            val user = _currentUser.value ?: return@launch
            val token = user.authToken

            // Record baru yang akan disimpan (default offline)
            var attendanceToSave = AttendanceEntity(
                username = user.username,
                name = user.name,
                timestamp = System.currentTimeMillis(),
                latitude = lat,
                longitude = lon,
                photoUri = photoUri, // Awalnya path lokal
                address = addr,
                type = type
            )

            // 1. Upload ke MinIO & Server jika Online
            if (token != null) {
                try {
                    val uri = android.net.Uri.parse(photoUri)
                    val bytes = compressImage(context, uri)

                    if (bytes != null) {
                        // A. Upload Foto
                        val requestFile = bytes.toRequestBody("image/jpeg".toMediaTypeOrNull())
                        val body = okhttp3.MultipartBody.Part.createFormData("file", "attendance_${System.currentTimeMillis()}.jpg", requestFile)
                        val uploadRes = repository.uploadAttendancePhoto(token, body)

                        // B. Kirim Data ke Server (Mendapatkan Remote ID & URL Final)
                        // Backend sekarang pakai "Masuk" / "Keluar" langsung
                        val resultDto = repository.submitAttendanceOnServer(
                            authToken = token,
                            type = type,
                            lat = lat,
                            lon = lon,
                            address = addr,
                            photoUrl = uploadRes.url
                        )

                        // C. Update data yang akan disimpan dengan info dari server
                        attendanceToSave = attendanceToSave.copy(
                            photoUri = resultDto.photo_url ?: photoUri,
                            remoteId = resultDto.id
                        )
                        Log.d("ABSEN", "Berhasil kirim ke server & MinIO: ${resultDto.photo_url}")

                        // Refresh status agar tombol berubah
                        refreshAttendanceStatus()
                    }
                } catch (e: Exception) {
                    Log.e("ABSEN", "Gagal sync server (Akan disimpan lokal): ${e.message}")
                    val errorBody = (e as? retrofit2.HttpException)?.response()?.errorBody()?.string()
                    if (errorBody != null) {
                        Log.e("ABSEN", "Server Error Detail: $errorBody")
                    }

                    if (e.message?.contains("401") == true) {
                        logout()
                        _syncError.value = "Sesi Habis. Silakan Login Kembali."
                    } else {
                        _syncError.value = "Gagal Kirim Absen: ${e.localizedMessage}"
                    }
                }
            }

            // 2. Simpan ke database lokal
            repository.insertAttendance(attendanceToSave)
        }
    }
    fun submitGimmickRequest(unit: HousingUnit, salesperson: User, details: String) {
        viewModelScope.launch {
            val user = _currentUser.value ?: return@launch
            repository.insertGimmickRequest(GimmickRequest(unitId = unit.id, clusterName = unit.clusterName, block = unit.block, salespersonUsername = salesperson.username, salespersonName = salesperson.name, requestedByUsername = user.username, requestedByName = user.name, gimmickDetails = details))
            repository.insertNotification(NotificationEntity(title = "Pengajuan Gimmick Baru", message = "Manager ${user.name} mengajukan gimmick untuk ${unit.block}.", timestamp = System.currentTimeMillis()))
        }
    }
    fun approveGimmickRequest(request: GimmickRequest) {
        viewModelScope.launch {
            val user = _currentUser.value ?: return@launch
            repository.updateGimmickRequest(request.copy(status = "Approved"))
            repository.insertNotification(NotificationEntity(title = "Gimmick Disetujui", message = "Admin ${user.name} menyetujui gimmick untuk ${request.block}.", timestamp = System.currentTimeMillis()))
        }
    }
    fun rejectGimmickRequest(request: GimmickRequest) {
        viewModelScope.launch {
            val user = _currentUser.value ?: return@launch
            repository.updateGimmickRequest(request.copy(status = "Rejected"))
            repository.insertNotification(NotificationEntity(title = "Gimmick Ditolak", message = "Admin ${user.name} menolak gimmick untuk ${request.block}.", timestamp = System.currentTimeMillis()))
        }
    }
    fun deleteGimmickRequest(request: GimmickRequest) { viewModelScope.launch { repository.deleteGimmickRequest(request) } }

    // --- 8. NOTIFICATION MANAGEMENT ---
    fun markAllNotificationsAsRead() { viewModelScope.launch { repository.markAllNotificationsAsRead() } }
    fun clearAllNotifications() { viewModelScope.launch { repository.clearAllNotifications() } }
    fun broadcastNotification(title: String, message: String) {
        viewModelScope.launch { repository.insertNotification(NotificationEntity(title = "BROADCAST: $title", message = message, timestamp = System.currentTimeMillis())) }
    }

    // --- 9. FILTER SETTERS ---
    fun setSearchQuery(q: String) { _searchQuery.value = q }
    fun setStatusFilter(f: FilterStatus) { _statusFilter.value = f }
    fun setClusterFilter(c: String?) { _clusterFilter.value = c }
    fun setStartDate(s: Long) { _startDate.value = s }
    fun setEndDate(e: Long) { _endDate.value = e }
    fun setPropertyPrice(p: Double) { _propertyPrice.value = p }
    fun setDpPercent(d: Double) { _dpPercent.value = d }
    fun setInterestRate(i: Double) { _interestRate.value = i }
    fun setTermYears(t: Double) { _termYears.value = t }
}

enum class FilterStatus { ALL, TERSEDIA, HOLD, TERJUAL }

class PropertyViewModelFactory(private val repository: PropertyRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return if (modelClass.isAssignableFrom(PropertyViewModel::class.java)) PropertyViewModel(repository) as T else throw IllegalArgumentException("Unknown ViewModel class")
    }
}
