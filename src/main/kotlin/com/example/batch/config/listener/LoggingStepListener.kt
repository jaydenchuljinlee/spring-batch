package com.example.batch.config.listener

import org.springframework.batch.core.ExitStatus
import org.springframework.batch.core.StepExecution
import org.springframework.batch.core.StepExecutionListener

class LoggingStepListener : StepExecutionListener {
    override fun beforeStep(stepExecution: StepExecution) {
        println("Step 시작: ${stepExecution.stepName}")
    }

    override fun afterStep(stepExecution: StepExecution): ExitStatus {
        println("Step 종료: ${stepExecution.stepName}")
        return stepExecution.exitStatus
    }
}
