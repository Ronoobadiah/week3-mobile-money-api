package com.ronoobadiah.mobile_money_api.exception

import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice
import java.time.LocalDateTime

data class ErrorResponse(
    val code: String,
    val message: String,
    val timestamp: String = LocalDateTime.now().toString()
)

@RestControllerAdvice
class GlobalExceptionHandler {

    @ExceptionHandler(AccountNotFoundException::class)
    fun handleNotFound(ex: AccountNotFoundException): ResponseEntity<ErrorResponse> {
        return ResponseEntity
            .status(HttpStatus.NOT_FOUND)
            .body(ErrorResponse(code = "ACCOUNT_NOT_FOUND", message = ex.message ?: "Not found"))
    }

    @ExceptionHandler(AccountNotActiveException::class)
    fun handleNotActive(ex: AccountNotActiveException): ResponseEntity<ErrorResponse> {
        return ResponseEntity
            .status(HttpStatus.FORBIDDEN)
            .body(ErrorResponse(code = "ACCOUNT_NOT_ACTIVE", message = ex.message ?: "Account not active"))
    }

    @ExceptionHandler(InsufficientBalanceException::class)
    fun handleInsufficientBalance(ex: InsufficientBalanceException): ResponseEntity<ErrorResponse> {
        return ResponseEntity
            .status(HttpStatus.UNPROCESSABLE_ENTITY)
            .body(ErrorResponse(code = "INSUFFICIENT_BALANCE", message = ex.message ?: "Insufficient balance"))
    }

    @ExceptionHandler(DailyLimitExceededException::class)
    fun handleDailyLimit(ex: DailyLimitExceededException): ResponseEntity<ErrorResponse> {
        return ResponseEntity
            .status(HttpStatus.UNPROCESSABLE_ENTITY)
            .body(ErrorResponse(code = "DAILY_LIMIT_EXCEEDED", message = ex.message ?: "Daily limit exceeded"))
    }

    @ExceptionHandler(MaxBalanceExceededException::class)
    fun handleMaxBalance(ex: MaxBalanceExceededException): ResponseEntity<ErrorResponse> {
        return ResponseEntity
            .status(HttpStatus.UNPROCESSABLE_ENTITY)
            .body(ErrorResponse(code = "MAX_BALANCE_EXCEEDED", message = ex.message ?: "Max balance exceeded"))
    }
}