package com.example.batch.job

import com.example.batch.config.JobNames
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.springframework.batch.core.Job
import org.springframework.batch.core.JobExecution
import org.springframework.batch.core.Step
import org.springframework.batch.core.StepExecution
import org.springframework.batch.core.launch.JobLauncher
import org.springframework.batch.core.repository.JobRepository
import org.springframework.batch.core.step.builder.StepBuilder
import org.springframework.batch.test.context.SpringBatchTest
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Import
import org.springframework.transaction.PlatformTransactionManager

@SpringBatchTest
@SpringBootTest(properties = ["spring.batch.job.enabled=false"])
@Import(ParallelJobConfiguration::class)
class ParallelJobConfigurationTest {
    @Autowired
    private lateinit var jobLauncher: JobLauncher

    @Autowired
    private lateinit var parallelJob: Job

    @Test
    @DisplayName("parallelJob이 성공적으로 실행되는지 테스트")
    fun testParallelJobExecutionSuccess() {
        val jobExecution: JobExecution = jobLauncher.run(parallelJob, defaultJobParameters())
        assertEquals("COMPLETED", jobExecution.exitStatus.exitCode)

        val stepExecutions = jobExecution.stepExecutions.toList()
        assertEquals(2, stepExecutions.size)

        stepExecutions.forEach { stepExecution ->
            assertEquals("COMPLETED", stepExecution.exitStatus.exitCode)
        }
    }

    @Test
    @DisplayName("parallelJob 실행 중 하나의 스텝이 실패했을 때 Job이 실패하는지 테스트")
    fun testParallelJobExecutionFailure() {
        val jobExecution: JobExecution = jobLauncher.run(parallelJob, failingJobParameters())
        assertEquals("FAILED", jobExecution.exitStatus.exitCode)

        val failedStep: StepExecution? = jobExecution.stepExecutions.find { it.exitStatus.exitCode == "FAILED" }
        assertEquals("${JobNames.PARALLEL_JOB}_STEP_1", failedStep?.stepName)
    }

    @Test
    @DisplayName("병렬 스텝이 서로 다른 스레드에서 실행되는지 테스트")
    fun testParallelStepsRunInDifferentThreads() {
        val jobExecution: JobExecution = jobLauncher.run(parallelJob, defaultJobParameters())
        val stepExecutions = jobExecution.stepExecutions.toList()

        val threadNames = stepExecutions.map { it.executionContext.getString("threadName") }
        assertEquals(2, threadNames.distinct().size) // 서로 다른 스레드에서 실행되었는지 확인
    }

    private fun defaultJobParameters() = org.springframework.batch.core.JobParametersBuilder()
        .addString("job.name", JobNames.PARALLEL_JOB)
        .addLong("run.id", System.currentTimeMillis())
        .toJobParameters()

    private fun failingJobParameters() = org.springframework.batch.core.JobParametersBuilder()
        .addString("job.name", JobNames.PARALLEL_JOB)
        .addString("fail.step", "true") // 실패를 유도하는 매개변수
        .addLong("run.id", System.currentTimeMillis())
        .toJobParameters()
}