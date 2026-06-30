package com.ronoobadiah.mobile_money_api.dto

import jakarta.validation.constraints.DecimalMin
import jakarta.validation.constraints.NotBlank

data class TransferRequest(
    @field:NotBlank(message = "fromId is required")
    val fromId: String,

    @field:NotBlank(message = "toId is required")
    val toId: String,

    @field:DecimalMin(value = "10.0", message = "Transfer amount must be at least KES 10")
    val amount: Double,

    val description: String
)