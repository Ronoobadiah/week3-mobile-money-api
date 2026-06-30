package com.ronoobadiah.mobile_money_api.dto

data class DepositRequest(
    val id: String,
    val amount: Double,
    val description: String
)