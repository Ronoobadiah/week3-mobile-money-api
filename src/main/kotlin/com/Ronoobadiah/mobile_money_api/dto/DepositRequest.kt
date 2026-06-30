package com.ronoobadiah.mobile_money_api.dto

import jakarta.validation.constraints.DecimalMax
import jakarta.validation.constraints.DecimalMin

data class DepositRequest(
    val id: String = "",

    @field:DecimalMin(value = "10.0", message = "Deposit amount must be at least KES 10")
    @field:DecimalMax(value = "500000.0", message = "Deposit amount cannot exceed KES 500,000 per transaction")
    val amount: Double,

    val description: String
)