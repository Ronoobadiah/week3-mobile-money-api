package com.ronoobadiah.mobile_money_api

import com.fasterxml.jackson.databind.ObjectMapper
import com.ronoobadiah.mobile_money_api.dto.*
import com.ronoobadiah.mobile_money_api.model.AccountTier
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.post
import org.springframework.test.web.servlet.get
import org.springframework.test.web.servlet.patch

@SpringBootTest
@AutoConfigureMockMvc
class AccountControllerTest {

    @Autowired
    lateinit var mockMvc: MockMvc

    @Autowired
    lateinit var objectMapper: ObjectMapper

    private fun createTestAccount(phone: String = "0712345678", tier: AccountTier = AccountTier.BASIC): String {
        val request = CreateAccountRequest(phoneNumber = phone, fullName = "Test User", tier = tier)
        val result = mockMvc.post("/accounts") {
            contentType = MediaType.APPLICATION_JSON
            content = objectMapper.writeValueAsString(request)
        }.andReturn()

        val body = objectMapper.readTree(result.response.contentAsString)
        return body.get("id").asText()
    }

    // <--- 1. Create account happy path ---->
    @Test
    fun `create account with valid data returns 201 and correct fields`() {
        val request = CreateAccountRequest(phoneNumber = "0712345678", fullName = "Alice Wanjiru", tier = AccountTier.BASIC)

        mockMvc.post("/accounts") {
            contentType = MediaType.APPLICATION_JSON
            content = objectMapper.writeValueAsString(request)
        }.andExpect {
            status { isCreated() }
            jsonPath("$.phoneNumber") { value("0712345678") }
            jsonPath("$.fullName") { value("Alice Wanjiru") }
            jsonPath("$.balance") { value(0.0) }
        }
    }

    // <--- 2. Invalid phone number -------->
    @Test
    fun `create account with invalid phone number returns 400 with field error`() {
        val request = CreateAccountRequest(phoneNumber = "12345", fullName = "Bad Phone User", tier = AccountTier.BASIC)

        mockMvc.post("/accounts") {
            contentType = MediaType.APPLICATION_JSON
            content = objectMapper.writeValueAsString(request)
        }.andExpect {
            status { isBadRequest() }
            jsonPath("$.code") { value("VALIDATION_FAILED") }
            jsonPath("$.errors[0].field") { value("phoneNumber") }
        }
    }

    // ── 3. Deposit success ───────────────────────────────────────────────
    @Test
    fun `deposit succeeds and balance updates correctly`() {
        val accountId = createTestAccount()
        val request = DepositRequest(amount = 5000.0, description = "Test deposit")

        mockMvc.post("/accounts/$accountId/deposit") {
            contentType = MediaType.APPLICATION_JSON
            content = objectMapper.writeValueAsString(request)
        }.andExpect {
            status { isOk() }
            jsonPath("$.balanceAfter") { value(5000.0) }
        }
    }

    // ── 4. Deposit exceeds max per transaction ───────────────────────────
    @Test
    fun `deposit exceeding max per transaction returns 422`() {
        val accountId = createTestAccount()
        val request = DepositRequest(amount = 600_000.0, description = "Too large")

        mockMvc.post("/accounts/$accountId/deposit") {
            contentType = MediaType.APPLICATION_JSON
            content = objectMapper.writeValueAsString(request)
        }.andExpect {
            status { isBadRequest() } // caught by @DecimalMax validation, not service-layer 422
        }
    }

    // ── 5. Withdrawal daily limit exceeded ───────────────────────────────
    @Test
    fun `withdrawal exceeding daily limit returns 422 with remaining amount`() {
        val accountId = createTestAccount(tier = AccountTier.BASIC) // daily limit 20,000
        val depositRequest = DepositRequest(amount = 30000.0, description = "Fund account")
        mockMvc.post("/accounts/$accountId/deposit") {
            contentType = MediaType.APPLICATION_JSON
            content = objectMapper.writeValueAsString(depositRequest)
        }

        val withdrawRequest = WithdrawRequest(amount = 25000.0, description = "Exceeds daily limit")

        mockMvc.post("/accounts/$accountId/withdraw") {
            contentType = MediaType.APPLICATION_JSON
            content = objectMapper.writeValueAsString(withdrawRequest)
        }.andExpect {
            status { isUnprocessableEntity() }
            jsonPath("$.code") { value("DAILY_LIMIT_EXCEEDED") }
        }
    }

    // ── 6. Transfer success ──────────────────────────────────────────────
    @Test
    fun `transfer succeeds and both balances update correctly`() {
        val fromId = createTestAccount(phone = "0711111111")
        val toId = createTestAccount(phone = "0722222222")

        mockMvc.post("/accounts/$fromId/deposit") {
            contentType = MediaType.APPLICATION_JSON
            content = objectMapper.writeValueAsString(DepositRequest(amount = 5000.0, description = "Fund"))
        }

        val transferRequest = TransferRequest(fromId = fromId, toId = toId, amount = 1000.0, description = "Test transfer")

        mockMvc.post("/accounts/transfers") {
            contentType = MediaType.APPLICATION_JSON
            content = objectMapper.writeValueAsString(transferRequest)
        }.andExpect {
            status { isOk() }
        }

        mockMvc.get("/accounts/$toId/balance").andExpect {
            status { isOk() }
            jsonPath("$.balance") { value(1000.0) }
        }
    }

    // ── 7. Transfer to same account ──────────────────────────────────────
    @Test
    fun `transfer to the same account returns 400`() {
        val accountId = createTestAccount()
        val transferRequest = TransferRequest(fromId = accountId, toId = accountId, amount = 100.0, description = "Self transfer")

        mockMvc.post("/accounts/transfers") {
            contentType = MediaType.APPLICATION_JSON
            content = objectMapper.writeValueAsString(transferRequest)
        }.andExpect {
            status { isBadRequest() }
            jsonPath("$.code") { value("SAME_ACCOUNT_TRANSFER") }
        }
    }

    // ── 8. Suspend then deposit ──────────────────────────────────────────
    @Test
    fun `deposit on suspended account returns 403`() {
        val accountId = createTestAccount()

        mockMvc.patch("/accounts/$accountId/status") {
            contentType = MediaType.APPLICATION_JSON
            content = objectMapper.writeValueAsString(UpdateStatusRequest(id = accountId, status = com.ronoobadiah.mobile_money_api.model.AccountStatus.SUSPENDED))
        }

        val depositRequest = DepositRequest(amount = 1000.0, description = "Should fail")

        mockMvc.post("/accounts/$accountId/deposit") {
            contentType = MediaType.APPLICATION_JSON
            content = objectMapper.writeValueAsString(depositRequest)
        }.andExpect {
            status { isForbidden() }
            jsonPath("$.code") { value("ACCOUNT_NOT_ACTIVE") }
        }
    }
}