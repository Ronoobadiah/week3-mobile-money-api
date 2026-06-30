package com.ronoobadiah.mobile_money_api.dto

import jakarta.validation.constraints.DecimalMin

data class WithdrawRequest(
    val id: String = "",

    @field:DecimalMin(value = "10.0", message = "Withdrawal amount must be at least KES 10")
    val amount: Double,

    val description: String
)