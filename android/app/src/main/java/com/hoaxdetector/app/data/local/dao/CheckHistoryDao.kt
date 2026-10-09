package com.hoaxdetector.app.data.local.dao

import androidx.room.*
import com.hoaxdetector.app.data.local.entity.CheckHistoryEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface CheckHistoryDao {

    @Query("SELECT * FROM check_history ORDER BY timestamp DESC")
    fun getAllHistory(): Flow<List<CheckHistoryEntity>>

    @Query("SELECT * FROM check_history WHERE id = :id")
    suspend fun getHistoryById(id: Long): CheckHistoryEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertHistory(history: CheckHistoryEntity): Long

    @Delete
    suspend fun deleteHistory(history: CheckHistoryEntity)

    @Query("DELETE FROM check_history WHERE id = :id")
    suspend fun deleteHistoryById(id: Long)

    @Query("DELETE FROM check_history")
    suspend fun deleteAllHistory()

    @Query("SELECT COUNT(*) FROM check_history")
    fun getTotalCount(): Flow<Int>

    @Query("SELECT COUNT(*) FROM check_history WHERE status = :status")
    fun getCountByStatus(status: String): Flow<Int>

    @Query("SELECT COUNT(*) FROM check_history WHERE status IN ('BERTENTANGAN DENGAN BUKTI', 'MENYESATKAN') OR statusColor = 'red'")
    fun getHoaxCount(): Flow<Int>

    @Query("SELECT COUNT(*) FROM check_history WHERE status = 'DIDUKUNG BUKTI' OR statusColor = 'green'")
    fun getValidCount(): Flow<Int>
}
