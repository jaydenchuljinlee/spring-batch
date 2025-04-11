package com.example.batch.job

import com.example.batch.config.policy.RetryableException
import com.example.batch.domain.User
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.mockito.Mockito.mock
import org.springframework.batch.core.repository.JobRepository
import org.springframework.transaction.PlatformTransactionManager

class RetryPolicyJobConfigurationUnitTest {
    @Test
    @DisplayName("RetryableException 발생 시 재시도되는지 테스트")
    fun testRetryableExceptionIsRetried() {
        // Mock 의존성 생성
        val jobRepository = mock(JobRepository::class.java)
        val transactionManager = mock(PlatformTransactionManager::class.java)

        // RetryPolicyJobConfiguration 객체 생성
        val configuration = RetryPolicyJobConfiguration(jobRepository, transactionManager)

        val exception = assertThrows<RetryableException> {
            // id가 3일 때 RetryableException 발생
            val processor = configuration.userProcessor()
            processor.process(User(3, "User3"))
        }
        assertEquals("임시 오류", exception.message)
    }
}