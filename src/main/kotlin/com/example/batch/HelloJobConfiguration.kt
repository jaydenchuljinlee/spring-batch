package com.example.batch

import org.springframework.batch.core.Job
import org.springframework.batch.core.Step
import org.springframework.batch.core.StepContribution
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory
import org.springframework.batch.core.scope.context.ChunkContext
import org.springframework.batch.core.step.tasklet.Tasklet
import org.springframework.batch.repeat.RepeatStatus
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
data class HelloJobConfiguration(
    val jobBuilderFactory: JobBuilderFactory,
    val stepBuilderFactory: StepBuilderFactory
) {
    @Bean
    fun helloJob(): Job? {
        return helloStep1()?.let {
            helloStep2()?.let { it1 ->
                jobBuilderFactory.get("helloJob")
                    .start(it)
                    .next(it1)
                    .build()
            }
        }
    }

    @Bean
    fun helloStep2(): Step? {
        return stepBuilderFactory.get("helloStep1").tasklet { contiribution, chunkContext ->
            println(" =================")
            println(" >> Hello Spring Kotlin Batch 1")
            println(" =================")

            RepeatStatus.FINISHED
        }.build()
    }

    @Bean
    fun helloStep1(): Step? {
        return stepBuilderFactory.get("helloStep2").tasklet { contiribution, chunkContext ->
            println(" =================")
            println(" >> Hello Spring Kotlin Batch 2")
            println(" =================")

            RepeatStatus.FINISHED
        }.build()
    }
}
