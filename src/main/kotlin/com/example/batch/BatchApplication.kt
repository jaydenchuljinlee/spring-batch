package com.example.batch

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class BatchApplication

fun main(args: Array<String>) {
	println("인자값 => { ${args.joinToString()} }")
	runApplication<BatchApplication>(*args)
}
