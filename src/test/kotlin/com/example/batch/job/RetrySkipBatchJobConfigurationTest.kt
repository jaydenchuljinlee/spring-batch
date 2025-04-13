package com.example.batch.job

import com.example.batch.config.JobNames
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.springframework.batch.core.Job
import org.springframework.batch.core.JobExecution
import org.springframework.batch.core.StepExecution
import org.springframework.batch.core.launch.JobLauncher
import org.springframework.batch.test.context.SpringBatchTest
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.context.annotation.Import

@SpringBatchTest
@SpringBootTest(properties = ["spring.batch.job.enabled=false"])
@Import(RetrySkipBatchJobConfiguration::class)
class RetrySkipBatchJobConfigurationTest {
    @Autowired
    private lateinit var jobLauncher: JobLauncher

    @Autowired
    private lateinit var retrySkipJob: Job

    @Test
    @DisplayName("retrySkipJob이 성공적으로 실행되는지 테스트")
    fun testRetrySkipJobExecutionSuccess() {
        val jobExecution: JobExecution = jobLauncher.run(retrySkipJob, defaultJobParameters())
        assertEquals("COMPLETED", jobExecution.exitStatus.exitCode)

        val stepExecution: StepExecution = jobExecution.stepExecutions.first()
        assertEquals("retrySkipStep", stepExecution.stepName)
        assertEquals(10, stepExecution.readCount) // 총 10개의 아이템 읽음
        assertEquals(9, stepExecution.writeCount) // 5는 스킵됨
        assertEquals(1, stepExecution.skipCount) // 1번 스킵 발생
    }

    private fun defaultJobParameters() = org.springframework.batch.core.JobParametersBuilder()
        .addString("job.name", JobNames.RETRY_SKIP_JOB)
        .addLong("run.id", System.currentTimeMillis())
        .toJobParameters()

}