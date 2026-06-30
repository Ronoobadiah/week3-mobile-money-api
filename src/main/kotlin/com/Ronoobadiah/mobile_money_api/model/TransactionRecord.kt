package com.ronoobadiah.mobile_money_api.model

enum class TransactionType { DEPOSIT, WITHDRAWAL, TRANSFER }

data class TransactionRecord(
    val id: String,
    val accountId: String,
    val type: TransactionType,
    val amount: Double,
    val balanceBefore: Double,
    val balanceAfter: Double,
    val timestamp: String,
    val description: String
)
