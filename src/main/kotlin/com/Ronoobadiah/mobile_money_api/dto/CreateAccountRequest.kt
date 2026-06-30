package com.ronoobadiah.mobile_money_api.dto

import com.ronoobadiah.mobile_money_api.model.AccountTier
import jakarta.validation.constraints.NotNull
import jakarta.validation.constraints.Pattern
import jakarta.validation.constraints.Size

data class CreateAccountRequest(
    @field:Pattern(
        regexp = "^(07\\d{8}|01\\d{8})$",
        message = "Phone number must be a valid Kenyan format (07XXXXXXXX or 01XXXXXXXX)"
    )
    val phoneNumber: String,

    @field:Size(min = 3, message = "Full name must be at least 3 characters")
    val fullName: String,

    @field:NotNull(message = "Tier must be a valid value: BASIC, STANDARD, or PREMIUM")
    val tier: AccountTier
)