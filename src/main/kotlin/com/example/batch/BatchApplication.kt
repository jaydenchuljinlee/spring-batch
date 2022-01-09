package com.example.batch

import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration
import org.springframework.boot.runApplication

@EnableBatchProcessing
@SpringBootApplication
class BatchApplication

fun main(args: Array<String>) {
	runApplication<BatchApplication>(*args)
}
