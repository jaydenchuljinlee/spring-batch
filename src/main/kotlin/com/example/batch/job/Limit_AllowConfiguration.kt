package com.example.batch.job

import org.springframework.batch.core.Job
import org.springframework.batch.core.Step
import org.springframework.batch.core.StepContribution
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory
import org.springframework.batch.core.launch.support.RunIdIncrementer
import org.springframework.batch.core.scope.context.ChunkContext
import org.springframework.batch.repeat.RepeatStatus
import org.springframework.context.annotation.Bean

// @Configuration
data class Limit_AllowConfiguration(
    val jobBuilderFactory: JobBuilderFactory,
    val stepBuilderFactory: StepBuilderFactory
) {
    @Bean
    fun batchJob(): Job? {
        return jobBuilderFactory.get("batchJob2")
            .incrementer(RunIdIncrementer())
            .start(step1())
            .next(step2())
            .build()
    }

    @Bean
    fun step1(): Step {
        return stepBuilderFactory.get("step1")
            .tasklet{ stepContribution: StepContribution, chunkContext: ChunkContext ->
                println("step1 = $stepContribution, chunkContext = $chunkContext")
                return@tasklet RepeatStatus.FINISHED
            }
            .allowStartIfComplete(true)
            .build()
    }

    @Bean
    fun step2(): Step {
        return stepBuilderFactory.get("step2")
            .tasklet{ stepContribution: StepContribution, chunkContext: ChunkContext ->
                println("step2 = $stepContribution, chunkContext = $chunkContext")
                throw RuntimeException("step2 was failed")
                //return@tasklet RepeatStatus.FINISHED
            }
            .startLimit(3)
            .build()
    }
}