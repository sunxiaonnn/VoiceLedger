package com.example.voiceledger.data

import androidx.lifecycle.LiveData
import androidx.room.*

@Dao
interface BillDao {
    @Insert
    suspend fun insert(bill: Bill): Long

    @Update
    suspend fun update(bill: Bill)

    @Delete
    suspend fun delete(bill: Bill)

    @Query("SELECT * FROM bills ORDER BY date DESC, createdAt DESC")
    fun getAllBills(): LiveData<List<Bill>>

    @Query("SELECT * FROM bills WHERE date >= :startTime AND date < :endTime ORDER BY date DESC")
    fun getBillsByDateRange(startTime: Long, endTime: Long): LiveData<List<Bill>>

    @Query("SELECT * FROM bills WHERE date >= :startTime AND date < :endTime")
    suspend fun getBillsByDateRangeList(startTime: Long, endTime: Long): List<Bill>

    @Query("SELECT SUM(amount) FROM bills WHERE isExpense = 1 AND date >= :startTime AND date < :endTime")
    fun getTotalExpense(startTime: Long, endTime: Long): LiveData<Double?>

    @Query("SELECT SUM(amount) FROM bills WHERE isExpense = 0 AND date >= :startTime AND date < :endTime")
    fun getTotalIncome(startTime: Long, endTime: Long): LiveData<Double?>

    @Query("SELECT category, SUM(amount) as total FROM bills WHERE isExpense = 1 AND date >= :startTime AND date < :endTime GROUP BY category")
    fun getCategoryExpenses(startTime: Long, endTime: Long): LiveData<List<CategoryTotal>>

    @Query("DELETE FROM bills WHERE id = :id")
    suspend fun deleteById(id: Long)
}

data class CategoryTotal(
    val category: String,
    val total: Double
)
