package com.ronoobadiah.mobile_money_api.filter

import jakarta.servlet.FilterChain
import jakarta.servlet.ServletRequest
import jakarta.servlet.ServletResponse
import jakarta.servlet.annotation.WebFilter
import org.slf4j.MDC
import org.springframework.stereotype.Component
import jakarta.servlet.Filter
import java.util.UUID

@Component
class MdcFilter : Filter {
    override fun doFilter(request: ServletRequest, response: ServletResponse, chain: FilterChain) {
        val requestId = UUID.randomUUID().toString()
        MDC.put("requestId", requestId)

        try {
            chain.doFilter(request, response)
        } finally {
            MDC.clear()
        }
    }
}