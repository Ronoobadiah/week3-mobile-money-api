package com.ronoobadiah.mobile_money_api.dto

import com.ronoobadiah.mobile_money_api.model.AccountTier

data class UpdateTierRequest(
    val id: String,
    val tier: AccountTier
)