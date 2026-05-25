package com.example.voiceledger.data

import androidx.lifecycle.LiveData

class BillRepository(private val billDao: BillDao) {
    val allBills: LiveData<List<Bill>> = billDao.getAllBills()

    suspend fun insert(bill: Bill): Long {
        return billDao.insert(bill)
    }

    suspend fun update(bill: Bill) {
        billDao.update(bill)
    }

    suspend fun delete(bill: Bill) {
        billDao.delete(bill)
    }

    suspend fun deleteById(id: Long) {
        billDao.deleteById(id)
    }

    fun getBillsByDateRange(startTime: Long, endTime: Long): LiveData<List<Bill>> {
        return billDao.getBillsByDateRange(startTime, endTime)
    }

    suspend fun getBillsByDateRangeList(startTime: Long, endTime: Long): List<Bill> {
        return billDao.getBillsByDateRangeList(startTime, endTime)
    }

    fun getTotalExpense(startTime: Long, endTime: Long): LiveData<Double?> {
        return billDao.getTotalExpense(startTime, endTime)
    }

    fun getTotalIncome(startTime: Long, endTime: Long): LiveData<Double?> {
        return billDao.getTotalIncome(startTime, endTime)
    }

    fun getCategoryExpenses(startTime: Long, endTime: Long): LiveData<List<CategoryTotal>> {
        return billDao.getCategoryExpenses(startTime, endTime)
    }
}
