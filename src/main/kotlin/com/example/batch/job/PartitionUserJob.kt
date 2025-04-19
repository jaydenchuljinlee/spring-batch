package com.example.batch.job

import com.example.batch.config.JobNames
import com.example.batch.domain.User
import com.example.batch.job.partitioner.RangePartitioner
import org.springframework.batch.core.Job
import org.springframework.batch.core.Step
import org.springframework.batch.core.configuration.annotation.StepScope
import org.springframework.batch.core.job.builder.JobBuilder
import org.springframework.batch.core.partition.support.TaskExecutorPartitionHandler
import org.springframework.batch.core.repository.JobRepository
import org.springframework.batch.core.scope.context.StepSynchronizationManager
import org.springframework.batch.core.step.builder.StepBuilder
import org.springframework.batch.item.ItemProcessor
import org.springframework.batch.item.ItemReader
import org.springframework.batch.item.ItemWriter
import org.springframework.batch.item.database.JdbcPagingItemReader
import org.springframework.batch.item.database.Order
import org.springframework.batch.item.database.support.MySqlPagingQueryProvider
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.core.task.SimpleAsyncTaskExecutor
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor
import org.springframework.transaction.PlatformTransactionManager
import javax.sql.DataSource

@Configuration
class PartitionUserJob(
    private val jobRepository: JobRepository,
    private val transactionManager: PlatformTransactionManager
) {
    @Bean
    fun partitioningJob(
        @Qualifier("partitionMasterStep") masterStep: Step
    ): Job {
        return JobBuilder(JobNames.PARTITION_JOB, jobRepository)
            .start(masterStep)
            .build()
    }


    @Bean
    fun partitionMasterStep(
        jdbcTemplate: JdbcTemplate,
        slaveStep: Step
    ): Step {
        val handler = TaskExecutorPartitionHandler()
        handler.setStep(slaveStep)
        handler.setGridSize(4)
        handler.setTaskExecutor(taskExecutor())

        return StepBuilder("masterStep", jobRepository)
            .partitioner("slaveStep", RangePartitioner(jdbcTemplate))
            .partitionHandler(handler)
            .build()
    }


    @Bean
    fun slaveStep(@Qualifier("partitionedUserReader") reader: ItemReader<User>): Step {
        return StepBuilder("slaveStep", jobRepository)
            .chunk<User, String>(100, transactionManager)
            .reader(reader)
            .processor(partitionUserProcessor())
            .writer(partitionUserWriter())
            .build()
    }


    @Bean
    @StepScope
    fun partitionedUserReader(
        @Value("#{stepExecutionContext['minId']}") minId: Long?,
        @Value("#{stepExecutionContext['maxId']}") maxId: Long?,
        dataSource: DataSource
    ): JdbcPagingItemReader<User> {
        val reader = JdbcPagingItemReader<User>()
        reader.setDataSource(dataSource)
        reader.setPageSize(100)

        val queryProvider = MySqlPagingQueryProvider().apply {
            setSelectClause("SELECT id, name")
            setFromClause("FROM user")
            setWhereClause("WHERE id BETWEEN :minId AND :maxId")
            setSortKeys(mapOf("id" to Order.ASCENDING))
        }

        reader.setQueryProvider(queryProvider)
        reader.setParameterValues(mapOf("minId" to minId, "maxId" to maxId))
        reader.setRowMapper { rs, _ -> User(rs.getLong("id"), rs.getString("name")) }

        return reader
    }


    @Bean
    fun partitionUserProcessor(): ItemProcessor<User, String> = ItemProcessor { user ->
        val threadName = Thread.currentThread().name

        // StepExecutionContext에 저장
        val stepExecution = StepSynchronizationManager.getContext()?.stepExecution
        stepExecution?.executionContext?.putString("threadName", threadName)

        println("[$threadName] 처리 중: ${user.name}")
        "Processed-${user.name}"
    }

    @Bean
    fun partitionUserWriter(): ItemWriter<String> = ItemWriter { items ->
        println("[${Thread.currentThread().name}] 저장 완료: ${items.size()}건")
    }

    @Bean
    fun taskExecutor(): ThreadPoolTaskExecutor {
        return ThreadPoolTaskExecutor().apply {
            corePoolSize = 4
            maxPoolSize = 4
            setThreadNamePrefix("partition-exec-")
            initialize()
        }
    }

}