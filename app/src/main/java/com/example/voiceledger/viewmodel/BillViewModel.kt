package com.example.voiceledger.viewmodel

import android.app.Application
import androidx.lifecycle.*
import com.example.voiceledger.data.*
import kotlinx.coroutines.launch
import java.util.*

class BillViewModel(application: Application) : AndroidViewModel(application) {
    private val repository: BillRepository
    val allBills: LiveData<List<Bill>>

    init {
        val billDao = AppDatabase.getDatabase(application).billDao()
        repository = BillRepository(billDao)
        allBills = repository.allBills
    }

    fun insert(bill: Bill) = viewModelScope.launch {
        repository.insert(bill)
    }

    fun update(bill: Bill) = viewModelScope.launch {
        repository.update(bill)
    }

    fun delete(bill: Bill) = viewModelScope.launch {
        repository.delete(bill)
    }

    fun deleteById(id: Long) = viewModelScope.launch {
        repository.deleteById(id)
    }

    fun getBillsByDateRange(startTime: Long, endTime: Long): LiveData<List<Bill>> {
        return repository.getBillsByDateRange(startTime, endTime)
    }

    suspend fun getBillsByDateRangeList(startTime: Long, endTime: Long): List<Bill> {
        return repository.getBillsByDateRangeList(startTime, endTime)
    }

    fun getTotalExpense(startTime: Long, endTime: Long): LiveData<Double?> {
        return repository.getTotalExpense(startTime, endTime)
    }

    fun getTotalIncome(startTime: Long, endTime: Long): LiveData<Double?> {
        return repository.getTotalIncome(startTime, endTime)
    }

    fun getCategoryExpenses(startTime: Long, endTime: Long): LiveData<List<CategoryTotal>> {
        return repository.getCategoryExpenses(startTime, endTime)
    }

    // 获取本月起止时间
    fun getMonthRange(): Pair<Long, Long> {
        val cal = Calendar.getInstance()
        cal.set(Calendar.DAY_OF_MONTH, 1)
        cal.set(Calendar.HOUR_OF_DAY, 0)
        cal.set(Calendar.MINUTE, 0)
        cal.set(Calendar.SECOND, 0)
        cal.set(Calendar.MILLISECOND, 0)
        val start = cal.timeInMillis

        cal.add(Calendar.MONTH, 1)
        val end = cal.timeInMillis

        return Pair(start, end)
    }
}
