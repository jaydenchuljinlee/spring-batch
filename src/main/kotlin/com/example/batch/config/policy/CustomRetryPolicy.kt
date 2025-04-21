package com.example.batch.config.policy

import org.springframework.retry.RetryContext
import org.springframework.retry.policy.SimpleRetryPolicy

class CustomRetryPolicy: SimpleRetryPolicy() {
    override fun canRetry(context: RetryContext): Boolean {
        val count = context.retryCount
        val lastThrowable = context.lastThrowable
        return count < 3 && lastThrowable is RetryableException
    }
}

class RetryableException(message: String) : RuntimeException(message)
