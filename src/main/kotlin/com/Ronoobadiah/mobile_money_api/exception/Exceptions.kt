package com.ronoobadiah.mobile_money_api.exception

import com.ronoobadiah.mobile_money_api.model.AccountStatus

class AccountNotFoundException(id: String) :
    RuntimeException("Account not found: $id")

class AccountNotActiveException(id: String, status: AccountStatus) :
    RuntimeException("Account $id is $status. Only ACTIVE accounts can transact.")

class InsufficientBalanceException(balance: Double, requested: Double) :
    RuntimeException("Insufficient balance. Available: KES $balance, Requested: KES $requested")

class DailyLimitExceededException(limit: Double, remaining: Double) :
    RuntimeException("Daily limit of KES $limit reached. Remaining: KES $remaining")

class MaxBalanceExceededException(maxBalance: Double, currentBalance: Double) :
    RuntimeException("Deposit would exceed tier max balance of KES $maxBalance. Current: KES $currentBalance")