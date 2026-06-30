package com.ronoobadiah.mobile_money_api.controller

import com.ronoobadiah.mobile_money_api.dto.*
import com.ronoobadiah.mobile_money_api.model.*
import com.ronoobadiah.mobile_money_api.service.AccountService
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import jakarta.validation.Valid
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import io.swagger.v3.oas.annotations.tags.Tag


@RestController
@RequestMapping("/accounts")
@Tag(name = "Accounts", description = "Mobile money account management")
class AccountController(
    private val accountService: AccountService
) {

    @Operation(summary = "Create a new account", description = "Creates an account with the given phone number, name, and tier")
    @ApiResponses(value = [
        ApiResponse(responseCode = "201", description = "Account created successfully"),
        ApiResponse(responseCode = "400", description = "Validation failed on one or more fields")
    ])
    @PostMapping
    fun createAccount(@Valid @RequestBody request: CreateAccountRequest): ResponseEntity<Account> {
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
        @Valid @RequestBody request: UpdateStatusRequest
    ): ResponseEntity<Account> {
        return ResponseEntity.ok(accountService.updateStatus(request.copy(id = id)))
    }

    @PutMapping("/{id}/tier")
    fun updateTier(
        @PathVariable id: String,
        @Valid @RequestBody request: UpdateTierRequest
    ): ResponseEntity<Account> {
        return ResponseEntity.ok(accountService.updateTier(request.copy(id = id)))
    }

    // <--- DEPOSIT ---->
    @Operation(summary = "Deposit money", description = "Deposits an amount into the specified account, subject to tier max balance")
    @ApiResponses(value = [
        ApiResponse(responseCode = "200", description = "Deposit successful"),
        ApiResponse(responseCode = "403", description = "Account is suspended or closed"),
        ApiResponse(responseCode = "404", description = "Account not found"),
        ApiResponse(responseCode = "422", description = "Deposit would exceed tier max balance or amount invalid")
    ])
    @PostMapping("/{id}/deposit")
    fun deposit(
        @PathVariable id: String,
        @Valid @RequestBody request: DepositRequest
    ): ResponseEntity<TransactionRecord> {
        return ResponseEntity.ok(accountService.deposit(request.copy(id = id)))
    }

   // <--- WITHDRAW ---->
    @Operation(summary = "Withdraw money", description = "Withdraw an amount into the specified account, subject to Daily limit")
    @ApiResponses(value = [
        ApiResponse(responseCode = "200", description = "Withdrawal successful"),
        ApiResponse(responseCode = "403", description = "Account is suspended or closed"),
        ApiResponse(responseCode = "404", description = "Account not found"),
        ApiResponse(responseCode = "422", description = "Withdrawal would exceed daily limit or amount invalid")
    ])
    @PostMapping("/{id}/withdraw")
    fun withdraw(
        @PathVariable id: String,
        @Valid @RequestBody request: WithdrawRequest
    ): ResponseEntity<TransactionRecord> {
        return ResponseEntity.ok(accountService.withdraw(request.copy(id = id)))
    }

    // <--- TRANSFER ---->
    @PostMapping("/transfer")
    fun transfer(@Valid @RequestBody request: TransferRequest): ResponseEntity<TransactionRecord> {
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