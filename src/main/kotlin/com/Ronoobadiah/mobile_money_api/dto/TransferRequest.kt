package com.ronoobadiah.mobile_money_api.dto

data class TransferRequest(
    val fromId: String,
    val toId: String,
    val amount: Double,
    val description: String
)