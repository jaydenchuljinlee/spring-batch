package com.example.batch.job

import com.example.batch.config.JobNames
import com.example.batch.config.policy.RetryableException
import com.example.batch.domain.User
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.mockito.Mockito.mock
import org.springframework.batch.core.Job
import org.springframework.batch.core.JobExecution
import org.springframework.batch.core.StepExecution
import org.springframework.batch.core.launch.JobLauncher
import org.springframework.batch.core.repository.JobRepository
import org.springframework.batch.test.context.SpringBatchTest
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.context.annotation.Import
import org.springframework.transaction.PlatformTransactionManager

@SpringBatchTest
@SpringBootTest(properties = ["spring.batch.job.enabled=false"])
@Import(RetryPolicyJobConfiguration::class)
class RetryPolicyJobConfigurationTest {
    @Autowired
    private lateinit var jobLauncher: JobLauncher

    @Autowired
    private lateinit var retryPolicyJob: Job

    @Test
    @DisplayName("retryPolicyJob이 성공적으로 실행되는지 테스트")
    fun testRetryPolicyJobExecutionSuccess() {
        val jobExecution: JobExecution = jobLauncher.run(retryPolicyJob, defaultJobParameters())
        assertEquals("COMPLETED", jobExecution.exitStatus.exitCode)

        val stepExecution: StepExecution = jobExecution.stepExecutions.first()
        assertEquals(10, stepExecution.readCount)
        assertEquals(8, stepExecution.writeCount) // 2개의 예외 발생 (id 3, 5)
        assertEquals(2, stepExecution.skipCount)
    }

    // --job.name=PARALLEL_JOB
    private fun defaultJobParameters() = org.springframework.batch.core.JobParametersBuilder()
        .addString("job.name", JobNames.RETRY_POLICY_JOB)
        .addLong("run.id", System.currentTimeMillis())
        .toJobParameters()
}

