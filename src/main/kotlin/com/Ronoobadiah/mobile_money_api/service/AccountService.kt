package com.Ronoobadiah.mobile_money_api.service

import com.Ronoobadiah.mobile_money_api.exception.*
import com.Ronoobadiah.mobile_money_api.model.*
import org.springframework.stereotype.Service
import java.time.LocalDateTime
import java.util.UUID
import java.util.concurrent.ConcurrentHashMap

@Service
class AccountService {

    private val accounts = ConcurrentHashMap<String, Account>()
    private val transactions = ConcurrentHashMap<String, MutableList<TransactionRecord>>()

    // <--- Create account --->
    fun createAccount(phoneNumber: String, fullName: String, tier: AccountTier): Account {
        val account = Account(
            id = UUID.randomUUID().toString(),
            phoneNumber = phoneNumber,
            fullName = fullName,
            tier = tier,
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
    fun updateStatus(id: String, newStatus: AccountStatus): Account {
        val account = getAccount(id)
        account.status = newStatus
        return account
    }

    // <--- Update tier ---->
    fun updateTier(id: String, newTier: AccountTier): Account {
        val account = getAccount(id)
        account.tier = newTier
        return account
    }

    // <--- Deposit ---->
    fun deposit(id: String, amount: Double, description: String): TransactionRecord {
        val account = getAccount(id)

        if (account.status != AccountStatus.ACTIVE)
            throw AccountNotActiveException(id, account.status)

        if (account.balance + amount > account.tier.maxBalance)
            throw MaxBalanceExceededException(account.tier.maxBalance, account.balance)

        val balanceBefore = account.balance
        account.balance += amount

        return recordTransaction(account, TransactionType.DEPOSIT, amount, balanceBefore, description)
    }

    // <--- Withdraw ---->
    fun withdraw(id: String, amount: Double, description: String): TransactionRecord {
        val account = getAccount(id)

        if (account.status != AccountStatus.ACTIVE)
            throw AccountNotActiveException(id, account.status)

        if (amount > account.balance)
            throw InsufficientBalanceException(account.balance, amount)


        val remaining = account.tier.dailyLimit - account.dailyUsed
        if (amount > remaining)
            throw DailyLimitExceededException(account.tier.dailyLimit, remaining)

        val balanceBefore = account.balance
        account.balance -= amount
        account.dailyUsed += amount

        return recordTransaction(account, TransactionType.WITHDRAWAL, amount, balanceBefore, description)
    }

    // <--- Transfer ---->

    fun transfer(fromId: String, toId: String, amount: Double, description: String): TransactionRecord {

        val withdrawRecord = withdraw(fromId, amount, "Transfer to $toId: $description")

        deposit(toId, amount, "Transfer from $fromId: $description")

        return withdrawRecord
    }

    // <--- Get transactions ---->
    fun getTransactions(
        id: String,
        type: TransactionType? = null,
        from: Long? = null,
        to: Long? = null
    ): List<TransactionRecord> {
        getAccount(id) // validates account exists
        val all = transactions[id] ?: emptyList()

        return all.filter { tx ->
            val typeMatch = type == null || tx.type == type
            typeMatch
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