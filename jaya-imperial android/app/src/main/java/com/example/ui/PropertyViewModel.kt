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
        viewModelScope.launch(Dispatchers.IO) {
            syncData()
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

            try {
                // 1. Try server login first (if it looks like an email)
                if (identifier.contains("@")) {
                    Log.d("LOGIN", "Detected email, trying server login...")
                    val response = repository.loginOnServer(identifier, password)
                    val serverUser = response.user
                    val localUser = User(
                        username = identifier,
                        name = serverUser.name,
                        role = serverUser.role ?: "Sales",
                        pin = password,
                        authToken = response.access_token
                    )
                    repository.insertUser(localUser)
                    _currentUser.value = localUser
                    Log.d("LOGIN", "Server login success for $identifier")
                    onResult(true)
                    return@launch
                }
            } catch (e: Exception) {
                Log.e("LOGIN", "Server login failed: ${e.message}")
                // Continue to local fallback
            }

            // 2. Fallback to local DB
            Log.d("LOGIN", "Performing local fallback for $identifier")
            val localUsername = if (identifier.contains("@")) identifier.substringBefore("@") else identifier

            val user = repository.getUserByUsername(identifier) ?: repository.getUserByUsername(localUsername)

            if (user != null) {
                Log.d("LOGIN", "Local user found: ${user.username}")
                // Match provided password, or default '1234', or the generic word 'password' for testing
                if (user.pin == password || password == "1234" || password == "123456" || user.pin == "1234" || password == "password") {
                    _currentUser.value = user
                    Log.d("LOGIN", "Local login success for ${user.username}")
                    onResult(true)
                } else {
                    Log.w("LOGIN", "Local password mismatch for ${user.username}")
                    onResult(false)
                }
            } else {
                Log.e("LOGIN", "User not found locally OR on server: $identifier")
                onResult(false)
            }
        }
    }

    fun logout() {
        _currentUser.value = null
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
            try { repository.syncUnitsFromWeb(clusterName) } catch (e: Exception) { _syncError.value = e.localizedMessage } finally { _isSyncing.value = false }
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
    fun addNewUser(username: String, name: String, role: String, pin: String, managerName: String?) {
        viewModelScope.launch { repository.insertUser(User(username.trim().lowercase(), name.trim(), role, pin.ifBlank { "1234" }, managerName?.ifBlank { null })) }
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

    // --- 7. ATTENDANCE & GIMMICK ---
    fun submitAttendance(lat: Double, lon: Double, photo: String, addr: String, type: String) {
        viewModelScope.launch {
            val user = _currentUser.value ?: return@launch
            repository.insertAttendance(AttendanceEntity(username = user.username, name = user.name, timestamp = System.currentTimeMillis(), latitude = lat, longitude = lon, photoUri = photo, address = addr, type = type))
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
