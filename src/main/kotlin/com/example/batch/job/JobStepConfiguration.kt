package com.example.batch.job

import org.springframework.batch.core.*
import org.springframework.batch.core.job.builder.JobBuilder
import org.springframework.batch.core.launch.JobLauncher
import org.springframework.batch.core.launch.support.RunIdIncrementer
import org.springframework.batch.core.repository.JobRepository
import org.springframework.batch.core.step.builder.JobStepBuilder
import org.springframework.batch.core.step.builder.StepBuilder
import org.springframework.batch.core.step.job.DefaultJobParametersExtractor
import org.springframework.batch.repeat.RepeatStatus
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.transaction.PlatformTransactionManager

@Configuration
class JobStepConfiguration(
    private val jobRepository: JobRepository,
    private val transactionManager: PlatformTransactionManager
) {
    @Bean
    fun parentJob(): Job {
        return JobBuilder("batchJob", jobRepository)
            .incrementer(RunIdIncrementer())
            .start(step1())
            .next(step2())
            .build()
    }

    @Bean
    fun jobStep(jobLauncher: JobLauncher): Step {
        return JobStepBuilder(StepBuilder("jobStep", jobRepository))
            .job(childJob())
            .launcher(jobLauncher)
            .parametersExtractor(jobParametersExtractor())
            .listener(object :StepExecutionListener {
                override fun beforeStep(stepExecution: StepExecution) {
                    stepExecution.executionContext.putString("name", "user1")
                }

                override fun afterStep(stepExecution: StepExecution): ExitStatus? = null
            })
            .repository(jobRepository)
            .build()
    }

    private fun jobParametersExtractor() = DefaultJobParametersExtractor().apply {
        setKeys(arrayOf("name"))
    }

    @Bean
    fun childJob(): Job {
        return JobBuilder("childJob", jobRepository)
            .start(step1())
            .build()
    }

    @Bean
    fun step1(): Step {
        return StepBuilder("step1", jobRepository)
            .tasklet({ contribution, chunkContext ->
                println("step1 = $contribution, chunkContext = $chunkContext")
                RepeatStatus.FINISHED
            }, transactionManager)
            .allowStartIfComplete(true)
            .build()
    }

    @Bean
    fun step2(): Step {
        return StepBuilder("step2", jobRepository)
            .tasklet({ contribution, chunkContext ->
                println("step2 = $contribution, chunkContext = $chunkContext")
                RepeatStatus.FINISHED
            }, transactionManager)
            .startLimit(3)
            .build()
    }
}