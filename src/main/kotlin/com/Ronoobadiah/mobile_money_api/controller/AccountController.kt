package com.ronoobadiah.mobile_money_api.controller

import com.ronoobadiah.mobile_money_api.dto.*
import com.ronoobadiah.mobile_money_api.model.*
import com.ronoobadiah.mobile_money_api.service.AccountService
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*


@RestController
@RequestMapping("/accounts")
class AccountController(
    private val accountService: AccountService
) {

    @PostMapping
    fun createAccount(@RequestBody request: CreateAccountRequest): ResponseEntity<Account> {
        return ResponseEntity.status(HttpStatus.CREATED).body(accountService.createAccount(request))
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
    fun updateStatus(
        @PathVariable id: String,
        @RequestBody request: UpdateStatusRequest
    ): ResponseEntity<Account> {
        return ResponseEntity.ok(accountService.updateStatus(request.copy(id = id)))
    }

    @PutMapping("/{id}/tier")
    fun updateTier(
        @PathVariable id: String,
        @RequestBody request: UpdateTierRequest
    ): ResponseEntity<Account> {
        return ResponseEntity.ok(accountService.updateTier(request.copy(id = id)))
    }

    @PostMapping("/{id}/deposit")
    fun deposit(
        @PathVariable id: String,
        @RequestBody request: DepositRequest
    ): ResponseEntity<TransactionRecord> {
        return ResponseEntity.ok(accountService.deposit(request.copy(id = id)))
    }

    @PostMapping("/{id}/withdraw")
    fun withdraw(
        @PathVariable id: String,
        @RequestBody request: WithdrawRequest
    ): ResponseEntity<TransactionRecord> {
        return ResponseEntity.ok(accountService.withdraw(request.copy(id = id)))
    }

    @PostMapping("/transfer")
    fun transfer(@RequestBody request: TransferRequest): ResponseEntity<TransactionRecord> {
        return ResponseEntity.ok(accountService.transfer(request))
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