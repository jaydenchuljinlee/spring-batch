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
@Import(JobStepConfiguration::class)
class JobStepConfigurationTest {
    @Autowired
    private lateinit var jobLauncher: JobLauncher

    @Autowired
    private lateinit var parentJob: Job

    @Test
    @DisplayName("parentJob이 성공적으로 실행되는지 테스트")
    fun testParentJobExecutionSuccess() {
        val jobExecution: JobExecution = jobLauncher.run(parentJob, defaultJobParameters())
        assertEquals("COMPLETED", jobExecution.exitStatus.exitCode)

        val stepExecutions = jobExecution.stepExecutions.toList()
        assertEquals(2, stepExecutions.size)

        val step1Execution: StepExecution = stepExecutions[0]
        assertEquals("step1", step1Execution.stepName)
        assertEquals("COMPLETED", step1Execution.exitStatus.exitCode)

        val step2Execution: StepExecution = stepExecutions[1]
        assertEquals("step2", step2Execution.stepName)
        assertEquals("COMPLETED", step2Execution.exitStatus.exitCode)
    }

    private fun defaultJobParameters() = org.springframework.batch.core.JobParametersBuilder()
        .addString("job.name", JobNames.BATCH_JOB_STEP_EXAMPLE)
        .addLong("run.id", System.currentTimeMillis())
        .toJobParameters()
}