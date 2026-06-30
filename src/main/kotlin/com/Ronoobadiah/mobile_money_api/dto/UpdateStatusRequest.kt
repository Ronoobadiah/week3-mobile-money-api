package com.ronoobadiah.mobile_money_api.dto

import com.ronoobadiah.mobile_money_api.model.AccountStatus

data class UpdateStatusRequest(
    val id: String,
    val status: AccountStatus
)