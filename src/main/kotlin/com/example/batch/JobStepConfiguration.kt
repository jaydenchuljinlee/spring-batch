package com.example.batch

import org.springframework.batch.core.*
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory
import org.springframework.batch.core.launch.JobLauncher
import org.springframework.batch.core.launch.support.RunIdIncrementer
import org.springframework.batch.core.listener.StepExecutionListenerSupport
import org.springframework.batch.core.scope.context.ChunkContext
import org.springframework.batch.core.step.job.DefaultJobParametersExtractor
import org.springframework.batch.repeat.RepeatStatus
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
data class JobStepConfiguration(
    val jobBuilderFactory: JobBuilderFactory,
    val stepBuilderFactory: StepBuilderFactory
) {
    @Bean
    fun parentJob(): Job? {
        return jobBuilderFactory.get("batchJob")
            .incrementer(RunIdIncrementer())
            .start(step1())
            .next(step2())
            .build()
    }

    @Bean
    fun jobStep(jobLauncher: JobLauncher): Step {
        return stepBuilderFactory.get("jobStep")
            .job(childJob())
            .launcher(jobLauncher)
            .parametersExtractor(jobParametersExtractor())
            .listener(object :StepExecutionListener {
                override fun beforeStep(stepExecution: StepExecution) {
                    stepExecution.executionContext.putString("name", "user1")
                }

                override fun afterStep(stepExecution: StepExecution) : ExitStatus? {
                    return null
                }
            })
            .build()
    }

    fun jobParametersExtractor(): DefaultJobParametersExtractor {
        val extractor: DefaultJobParametersExtractor = DefaultJobParametersExtractor()
        extractor.setKeys(arrayOf("name"))
        return extractor
    }

    @Bean
    fun childJob(): Job {
        return jobBuilderFactory.get("childJob")
            .start(step1())
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
                return@tasklet RepeatStatus.FINISHED
            }
            .startLimit(3)
            .build()
    }
}