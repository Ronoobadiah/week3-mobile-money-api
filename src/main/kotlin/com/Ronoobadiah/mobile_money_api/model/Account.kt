package com.Ronoobadiah.mobile_money_api.model

enum class AccountStatus { ACTIVE, SUSPENDED, CLOSED }

enum class AccountTier(val maxBalance: Double, val dailyLimit: Double) {
    BASIC(maxBalance = 50000.0, dailyLimit = 20000.0),
    STANDARD(maxBalance = 300000.0, dailyLimit = 150000.0),
    PREMIUM(maxBalance = 1000000.0, dailyLimit = 500000.0)
}

data class Account(
    val id: String,
    val phoneNumber: String,
    val fullName: String,
    var tier: AccountTier,
    var status: AccountStatus,
    var balance: Double,
    var dailyUsed: Double = 0.0,
    val createdAt: String
)