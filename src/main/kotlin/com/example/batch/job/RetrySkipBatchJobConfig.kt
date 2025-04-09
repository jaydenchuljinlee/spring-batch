package com.example.batch.job

import com.example.batch.config.JobNames
import org.springframework.batch.core.Job
import org.springframework.batch.core.Step
import org.springframework.batch.core.job.builder.JobBuilder
import org.springframework.batch.core.repository.JobRepository
import org.springframework.batch.core.step.builder.StepBuilder
import org.springframework.batch.item.ItemProcessor
import org.springframework.batch.item.ItemWriter
import org.springframework.batch.item.support.ListItemReader
import org.springframework.batch.repeat.RepeatStatus
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.transaction.PlatformTransactionManager

@Configuration
class RetrySkipBatchJobConfig(
    private val jobRepository: JobRepository,
    private val transactionManager: PlatformTransactionManager
) {

    @Bean
    fun retrySkipJob(): Job {
        return JobBuilder(JobNames.RETRY_SKIP_JOB, jobRepository)
            .start(retrySkipStep())
            .on("FAILED").to(failedStep()) // Step 실패 시 failedStep으로
            .from(retrySkipStep()).on("*").to(successStep()) // 그 외는 success
            .end()
            .build()
    }

    @Bean
    fun retrySkipStep(): Step {
        return StepBuilder("retrySkipStep", jobRepository)
            .chunk<Int, String>(3, transactionManager)
            .reader(numberReader())
            .processor(numberProcessor())
            .writer(writer())
            .faultTolerant()
            .retry(IllegalArgumentException::class.java) // 예외 발생 시 재시도
            .retryLimit(2)                               // 최대 2번
            .skip(IllegalArgumentException::class.java)  // 실패하면 skip
            .skipLimit(5)                                // 최대 5번 skip 허용
            .build()
    }

    @Bean
    fun successStep(): Step {
        return StepBuilder("successStep", jobRepository)
            .tasklet({ _, _ ->
                println("성공적으로 처리됨")
                RepeatStatus.FINISHED
            }, transactionManager)
            .build()
    }

    @Bean
    fun failedStep(): Step {
        return StepBuilder("failedStep", jobRepository)
            .tasklet({ _, _ ->
                println("처리 실패 - 예외 처리 후 대체 작업 수행")
                RepeatStatus.FINISHED
            }, transactionManager)
            .build()
    }

    @Bean
    fun numberReader(): ListItemReader<Int> {
        return ListItemReader((1..10).toList())
    }

    @Bean
    fun numberProcessor(): ItemProcessor<Int, String> {
        return ItemProcessor { item ->
            if (item == 5) {
                println("에러 발생! 숫자: $item")
                throw IllegalArgumentException("5는 처리할 수 없습니다.")
            }
            println("처리됨: $item")
            "숫자-$item"
        }
    }

    @Bean
    fun writer(): ItemWriter<String> {
        return ItemWriter { items ->
            items.forEach { println("저장: $it") }
        }
    }
}