package com.ronoobadiah.mobile_money_api.dto

import com.ronoobadiah.mobile_money_api.model.AccountTier

data class CreateAccountRequest(
    val phoneNumber: String,
    val fullName: String,
    val tier: AccountTier
)