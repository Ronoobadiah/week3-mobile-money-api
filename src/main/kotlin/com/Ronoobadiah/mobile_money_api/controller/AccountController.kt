package com.Ronoobadiah.mobile_money_api.controller

import com.Ronoobadiah.mobile_money_api.model.*
import com.Ronoobadiah.mobile_money_api.service.AccountService
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

data class CreateAccountRequest(val phoneNumber: String, val fullName: String, val tier: AccountTier)
data class DepositRequest(val amount: Double, val description: String)
data class WithdrawRequest(val amount: Double, val description: String)
data class TransferRequest(val fromId: String, val toId: String, val amount: Double, val description: String)
data class StatusRequest(val status: AccountStatus)
data class TierRequest(val tier: AccountTier)


@RestController
@RequestMapping("/accounts")
class AccountController(
    private val accountService: AccountService
) {

    @PostMapping
    fun createAccount(@RequestBody request: CreateAccountRequest): ResponseEntity<Account> {
        val account = accountService.createAccount(request.phoneNumber, request.fullName, request.tier)
        return ResponseEntity.status(HttpStatus.CREATED).body(account)
    }

    @GetMapping("/{id}")
    fun getAccount(@PathVariable id: String): ResponseEntity<Account> {
        return ResponseEntity.ok(accountService.getAccount(id))
    }

    @GetMapping("/{id}/balance")
    fun getBalance(@PathVariable id: String): ResponseEntity<Map<String, Any>> {
        return ResponseEntity.ok(accountService.getBalance(id))
    }

    @PatchMapping("/{id}/status")
    fun updateStatus(@PathVariable id: String, @RequestBody request: StatusRequest): ResponseEntity<Account> {
        return ResponseEntity.ok(accountService.updateStatus(id, request.status))
    }

    @PutMapping("/{id}/tier")
    fun updateTier(@PathVariable id: String, @RequestBody request: TierRequest): ResponseEntity<Account> {
        return ResponseEntity.ok(accountService.updateTier(id, request.tier))
    }

    @PostMapping("/{id}/deposit")
    fun deposit(@PathVariable id: String, @RequestBody request: DepositRequest): ResponseEntity<TransactionRecord> {
        return ResponseEntity.ok(accountService.deposit(id, request.amount, request.description))
    }

    @PostMapping("/{id}/withdraw")
    fun withdraw(@PathVariable id: String, @RequestBody request: WithdrawRequest): ResponseEntity<TransactionRecord> {
        return ResponseEntity.ok(accountService.withdraw(id, request.amount, request.description))
    }

    @PostMapping("/transfers")
    fun transfer(@RequestBody request: TransferRequest): ResponseEntity<TransactionRecord> {
        return ResponseEntity.ok(accountService.transfer(request.fromId, request.toId, request.amount, request.description))
    }

    @GetMapping("/{id}/transactions")
    fun getTransactions(
        @PathVariable id: String,
        @RequestParam(required = false) type: TransactionType?,
        @RequestParam(required = false) from: Long?,
        @RequestParam(required = false) to: Long?
    ): ResponseEntity<List<TransactionRecord>> {
        return ResponseEntity.ok(accountService.getTransactions(id, type, from, to))
    }
}