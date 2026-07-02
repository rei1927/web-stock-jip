package com.example.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface PropertyDao {
    // Housing Units queries
    @Query("SELECT * FROM housing_units ORDER BY clusterName ASC, block ASC")
    fun getAllUnits(): Flow<List<HousingUnit>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUnit(unit: HousingUnit)

    @Update
    suspend fun updateUnit(unit: HousingUnit)

    @Delete
    suspend fun deleteUnit(unit: HousingUnit)

    @Query("SELECT * FROM housing_units WHERE id = :id LIMIT 1")
    suspend fun getUnitById(id: Int): HousingUnit?

    // Sales Logs queries
    @Query("SELECT * FROM sales_logs ORDER BY year DESC, monthIndex ASC")
    fun getAllSalesLogs(): Flow<List<SalesLog>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSalesLog(log: SalesLog)

    @Query("DELETE FROM sales_logs")
    suspend fun clearAllSalesLogs()

    // Users queries
    @Query("SELECT * FROM users ORDER BY name ASC")
    fun getAllUsers(): Flow<List<User>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUser(user: User)

    @Delete
    suspend fun deleteUser(user: User)

    @Query("SELECT * FROM users WHERE username = :username LIMIT 1")
    suspend fun getUserByUsername(username: String): User?

    @Query("SELECT * FROM users")
    suspend fun getAllUsersSync(): List<User>

    // Sales Teams queries
    @Query("SELECT * FROM sales_teams ORDER BY teamName ASC")
    fun getAllTeams(): Flow<List<SalesTeam>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTeam(team: SalesTeam)

    @Delete
    suspend fun deleteTeam(team: SalesTeam)

    @Query("SELECT * FROM sales_teams WHERE teamId = :teamId LIMIT 1")
    suspend fun getTeamById(teamId: String): SalesTeam?

    // Sold Proposal queries
    @Query("SELECT * FROM sold_proposals WHERE unitId = :unitId LIMIT 1")
    suspend fun getSoldProposalForUnit(unitId: Int): SoldProposal?

    @Query("SELECT * FROM sold_proposals")
    fun getAllSoldProposals(): Flow<List<SoldProposal>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSoldProposal(proposal: SoldProposal)

    @Query("DELETE FROM sold_proposals WHERE unitId = :unitId")
    suspend fun deleteSoldProposalForUnit(unitId: Int)

    // Housing Units sync query
    @Query("SELECT * FROM housing_units")
    suspend fun getAllUnitsSync(): List<HousingUnit>

    @Query("SELECT * FROM housing_units WHERE clusterName = :clusterName AND block = :block LIMIT 1")
    suspend fun getUnitByLocation(clusterName: String, block: String): HousingUnit?

    @Query("DELETE FROM housing_units")
    suspend fun clearAllUnits()

    @Query("DELETE FROM housing_units WHERE clusterName = :clusterName")
    suspend fun clearUnitsByCluster(clusterName: String)

    // Notification queries
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertNotification(notification: NotificationEntity)

    @Query("SELECT * FROM notifications ORDER BY timestamp DESC")
    fun getAllNotifications(): Flow<List<NotificationEntity>>

    @Query("UPDATE notifications SET isRead = 1")
    suspend fun markAllNotificationsAsRead()

    @Query("DELETE FROM notifications")
    suspend fun clearAllNotifications()

    // Gimmick Request queries
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertGimmickRequest(request: GimmickRequest)

    @Update
    suspend fun updateGimmickRequest(request: GimmickRequest)

    @Query("SELECT * FROM gimmick_requests ORDER BY timestamp DESC")
    fun getAllGimmickRequests(): Flow<List<GimmickRequest>>

    @Query("SELECT * FROM gimmick_requests WHERE requestedByUsername = :username ORDER BY timestamp DESC")
    fun getGimmickRequestsByManager(username: String): Flow<List<GimmickRequest>>

    @Delete
    suspend fun deleteGimmickRequest(request: GimmickRequest)

    // Attendance queries
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAttendance(attendance: AttendanceEntity)

    @Query("SELECT * FROM attendance_logs ORDER BY timestamp DESC")
    fun getAllAttendance(): Flow<List<AttendanceEntity>>

    @Query("SELECT * FROM attendance_logs WHERE username = :username ORDER BY timestamp DESC")
    fun getAttendanceByUser(username: String): Flow<List<AttendanceEntity>>
}
