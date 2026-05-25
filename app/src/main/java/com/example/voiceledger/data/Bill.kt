package com.example.voiceledger.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "bills")
data class Bill(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val amount: Double,
    val category: String,
    val note: String,
    val date: Long, // timestamp in millis
    val isExpense: Boolean = true, // true=支出, false=收入
    val createdAt: Long = System.currentTimeMillis()
)
