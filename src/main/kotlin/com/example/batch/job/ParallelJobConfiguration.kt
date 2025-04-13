package com.example.batch.job

import com.example.batch.config.JobNames
import org.springframework.batch.core.Job
import org.springframework.batch.core.Step
import org.springframework.batch.core.job.builder.FlowBuilder
import org.springframework.batch.core.job.builder.JobBuilder
import org.springframework.batch.core.job.flow.Flow
import org.springframework.batch.core.job.flow.support.SimpleFlow
import org.springframework.batch.core.repository.JobRepository
import org.springframework.batch.core.step.builder.StepBuilder
import org.springframework.batch.repeat.RepeatStatus
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.core.task.SimpleAsyncTaskExecutor
import org.springframework.transaction.PlatformTransactionManager

@Configuration
class ParallelJobConfiguration(
    private val jobRepository: JobRepository,
    private val transactionManager: PlatformTransactionManager
) {
    @Bean
    fun parallelJob(): Job {
        return JobBuilder(JobNames.PARALLEL_JOB, jobRepository)
            .start(splitFlow())
            .end()
            .build()
    }

    @Bean
    fun splitFlow(): Flow {
        return FlowBuilder<SimpleFlow>("${JobNames.PARALLEL_JOB}_SPLIT_FLOW")
            .split(SimpleAsyncTaskExecutor()) // 비동기 실행
            .add(parallelFlow1(), parallelFlow2())
            .build()
    }

    @Bean
    fun parallelFlow1(): Flow {
        return FlowBuilder<SimpleFlow>("${JobNames.PARALLEL_JOB}_FLOW_1")
            .start(parallelStep1())
            .build()
    }

    @Bean
    fun parallelFlow2(): Flow {
        return FlowBuilder<SimpleFlow>("${JobNames.PARALLEL_JOB}_FLOW_2")
            .start(parallelStep2())
            .build()
    }

    @Bean
    fun parallelStep1(): Step {
        return StepBuilder("${JobNames.PARALLEL_JOB}_STEP_1", jobRepository)
            .tasklet({ _, chunkContext ->
                val threadName = Thread.currentThread().name
                chunkContext.stepContext.stepExecution.executionContext.putString("threadName", threadName)

                println("[$threadName] step1 실행 중")
                Thread.sleep(1000)
                RepeatStatus.FINISHED
            }, transactionManager)
            .build()
    }

    @Bean
    fun parallelStep2(): Step {
        return StepBuilder("${JobNames.PARALLEL_JOB}_STEP_2", jobRepository)
            .tasklet({ _, chunkContext ->
                val threadName = Thread.currentThread().name
                chunkContext.stepContext.stepExecution.executionContext.putString("threadName", threadName)

                println("[$threadName] step2 실행 중")
                Thread.sleep(1000)
                RepeatStatus.FINISHED
            }, transactionManager)
            .build()
    }


}