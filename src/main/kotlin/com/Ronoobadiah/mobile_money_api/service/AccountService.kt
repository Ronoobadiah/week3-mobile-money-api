package com.ronoobadiah.mobile_money_api.service

import com.ronoobadiah.mobile_money_api.dto.*
import com.ronoobadiah.mobile_money_api.exception.*
import com.ronoobadiah.mobile_money_api.model.*
import org.springframework.stereotype.Service
import java.time.LocalDateTime
import java.util.UUID
import java.util.concurrent.ConcurrentHashMap

@Service
class AccountService {

    private val accounts = ConcurrentHashMap<String, Account>()
    private val transactions = ConcurrentHashMap<String, MutableList<TransactionRecord>>()

    // <--- Create account --->
    fun createAccount(request: CreateAccountRequest): Account {
        val account = Account(
            id = UUID.randomUUID().toString(),
            phoneNumber = request.phoneNumber,
            fullName = request.fullName,
            tier = request.tier,
            status = AccountStatus.ACTIVE,
            balance = 0.0,
            createdAt = LocalDateTime.now().toString()
        )
        accounts[account.id] = account
        transactions[account.id] = mutableListOf()
        return account
    }

    // <--- Get account --->
    fun getAccount(id: String): Account {
        return accounts[id] ?: throw AccountNotFoundException(id)
    }

    // <--- Get balance --->
    fun getBalance(id: String): Map<String, Any> {
        val account = getAccount(id)
        val remaining = account.tier.dailyLimit - account.dailyUsed
        return mapOf(
            "balance" to account.balance,
            "dailyLimit" to account.tier.dailyLimit,
            "dailyRemaining" to remaining
        )
    }

    // <--- Update status --->
    fun updateStatus(request: UpdateStatusRequest): Account {
        val account = getAccount(request.id)
        account.status = request.status
        return account
    }

    // <--- Update tier ---->
    fun updateTier(request: UpdateTierRequest): Account {
        val account = getAccount(request.id)
        account.tier = request.tier
        return account
    }

    // <--- Deposit ---->
    fun deposit(request: DepositRequest): TransactionRecord {
        val account = getAccount(request.id)

        if (account.status != AccountStatus.ACTIVE)
            throw AccountNotActiveException(request.id, account.status)

        if (account.balance + request.amount > account.tier.maxBalance)
            throw MaxBalanceExceededException(account.tier.maxBalance, account.balance)

        val balanceBefore = account.balance
        account.balance += request.amount

        return recordTransaction(account, TransactionType.DEPOSIT, request.amount, balanceBefore, request.description)
    }

    // <--- Withdraw ---->
    fun withdraw(request: WithdrawRequest): TransactionRecord {
        val account = getAccount(request.id)

        if (account.status != AccountStatus.ACTIVE)
            throw AccountNotActiveException(request.id, account.status)

        if (request.amount > account.balance)
            throw InsufficientBalanceException(account.balance, request.amount)

        val remaining = account.tier.dailyLimit - account.dailyUsed
        if (request.amount > remaining)
            throw DailyLimitExceededException(account.tier.dailyLimit, remaining)

        val balanceBefore = account.balance
        account.balance -= request.amount
        account.dailyUsed += request.amount

        return recordTransaction(account, TransactionType.WITHDRAWAL, request.amount, balanceBefore, request.description)
    }

    // <--- Transfer ---->

    fun transfer(request: TransferRequest): TransactionRecord {

        val withdrawRequest = WithdrawRequest(
            id = request.fromId,
            amount = request.amount,
            description = request.copy(description = "Transfer to ${request.toId}: ${request.description}").description
        )
        val withdrawRecord = withdraw(withdrawRequest)

        val depositRequest = DepositRequest(
            id = request.toId,
            amount = request.amount,
            description = request.copy(description = "Transfer from ${request.fromId}: ${request.description}").description
        )

        deposit(depositRequest)

        return withdrawRecord
    }

    // <--- Get transactions ---->
    fun getTransactions(
        id: String,
        type: TransactionType? = null,
        from: Long? = null,
        to: Long? = null
    ): List<TransactionRecord> {
        getAccount(id)
        val all = transactions[id] ?: emptyList()
        return all.filter { tx ->
            type == null || tx.type == type
        }
    }

    // <--- Private helper: record a transaction ---->
    private fun recordTransaction(
        account: Account,
        type: TransactionType,
        amount: Double,
        balanceBefore: Double,
        description: String
    ): TransactionRecord {
        val record = TransactionRecord(
            id = UUID.randomUUID().toString(),
            accountId = account.id,
            type = type,
            amount = amount,
            balanceBefore = balanceBefore,
            balanceAfter = account.balance,
            timestamp = LocalDateTime.now().toString(),
            description = description
        )
        transactions[account.id]?.add(record)
        return record
    }
}