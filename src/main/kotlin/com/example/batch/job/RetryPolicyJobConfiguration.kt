package com.example.batch.job

import com.example.batch.config.JobNames
import com.example.batch.config.listener.CustomSkipListener
import com.example.batch.config.listener.LoggingStepListener
import com.example.batch.config.policy.CustomRetryPolicy
import com.example.batch.config.policy.RetryableException
import com.example.batch.domain.User
import org.springframework.batch.core.Job
import org.springframework.batch.core.Step
import org.springframework.batch.core.job.builder.JobBuilder
import org.springframework.batch.core.repository.JobRepository
import org.springframework.batch.core.step.builder.StepBuilder
import org.springframework.batch.item.ItemProcessor
import org.springframework.batch.item.ItemWriter
import org.springframework.batch.item.support.ListItemReader
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.transaction.PlatformTransactionManager

@Configuration
class RetryPolicyJobConfiguration(
    private val jobRepository: JobRepository,
    private val transactionManager: PlatformTransactionManager
) {
    @Bean
    fun retryPolicyJob(): Job {
        return JobBuilder(JobNames.RETRY_POLICY_JOB, jobRepository)
            .start(processUsersStep())
            .build()
    }

    @Bean
    fun processUsersStep(): Step {
        return StepBuilder("processUsersStep", jobRepository)
            .chunk<User, String>(3, transactionManager)
            .reader(userReader())
            .processor(userProcessor())
            .writer(userWriter())
            .faultTolerant()
            .retryPolicy(CustomRetryPolicy())
            .skip(RetryableException::class.java)
            .skip(IllegalStateException::class.java)
            .skipLimit(5)
            .listener(CustomSkipListener())
            .listener(LoggingStepListener())
            .build()
    }

    @Bean
    fun userReader(): ListItemReader<User> {
        val users = (1..10).map { User(it, "User$it") }
        return ListItemReader(users)
    }

    @Bean
    fun userProcessor(): ItemProcessor<User, String> {
        return ItemProcessor { user ->
            when (user.id) {
                3 -> {
                    println("재시도 대상: ${user.name}")
                    throw RetryableException("임시 오류")
                }
                5 -> {
                    println("스킵 대상: ${user.name}")
                    throw IllegalStateException("처리 불가능")
                }
                else -> println("처리됨: ${user.name}")
            }
            "processed-${user.name}"
        }
    }

    @Bean
    fun userWriter(): ItemWriter<String> {
        return ItemWriter { items ->
            items.forEach { println("저장: $it") }
        }
    }

}