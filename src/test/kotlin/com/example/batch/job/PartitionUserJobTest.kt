package com.example.batch.job

import com.example.batch.config.JobNames
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.springframework.batch.core.BatchStatus
import org.springframework.batch.core.Job
import org.springframework.batch.core.JobExecution
import org.springframework.batch.core.JobParametersBuilder
import org.springframework.batch.core.launch.JobLauncher
import org.springframework.batch.test.context.SpringBatchTest
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.context.annotation.Import
import org.springframework.jdbc.core.JdbcTemplate

@SpringBootTest(properties = ["spring.batch.job.enabled=false"])
@Import(PartitionUserJob::class)
@SpringBatchTest
class PartitionUserJobTest {
    @Autowired
    private lateinit var jobLauncher: JobLauncher

    @Autowired
    private lateinit var partitioningJob: Job

    @Autowired
    private lateinit var jdbcTemplate: JdbcTemplate

    @Test
    @DisplayName("모든 유저가 중복/누락 없이 병렬로 처리되었는지 검증한다")
    fun testPartitioningCorrectness() {
        // given
        val userCount = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM user", Int::class.java)?.toLong() ?: 0L

        // when
        val jobExecution = jobLauncher.run(partitioningJob, defaultJobParameters())
        val stepExecutions = jobExecution.stepExecutions

        // then
        assertEquals(BatchStatus.COMPLETED, jobExecution.status)

        val totalRead = stepExecutions
            .filterNot { it.stepName == "masterStep" } // 또는 startsWith("slaveStep:")
            .sumOf { it.readCount }

        val totalWrite = stepExecutions
            .filterNot { it.stepName == "masterStep" }
            .sumOf { it.writeCount }

        println("전체 유저 수: $userCount, Read: $totalRead, Write: $totalWrite")
        assertEquals(userCount, totalRead, "모든 유저가 읽혔는지 확인")
        assertEquals(userCount, totalWrite, "모든 유저가 처리되었는지 확인")

        val threadNames = stepExecutions
            .filterNot { it.stepName == "masterStep" }
            .mapNotNull {
                it.executionContext.getString("threadName")
            }
        println("사용된 스레드: $threadNames")

        // 최소 2개 이상의 다른 스레드에서 실행됐는지 확인
        assertTrue(threadNames.distinct().size > 1, "병렬 처리가 되었는지 확인")
    }

    private fun defaultJobParameters() = JobParametersBuilder()
        .addString("job.name", JobNames.PARTITION_JOB)
        .addLong("run.id", System.currentTimeMillis())
        .toJobParameters()
}