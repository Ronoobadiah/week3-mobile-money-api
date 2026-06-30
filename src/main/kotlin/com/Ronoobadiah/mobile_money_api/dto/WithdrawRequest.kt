package com.ronoobadiah.mobile_money_api.dto

data class WithdrawRequest(
    val id: String,
    val amount: Double,
    val description: String
)