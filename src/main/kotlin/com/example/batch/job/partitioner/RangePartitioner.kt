package com.example.batch.job.partitioner

import org.springframework.batch.core.partition.support.Partitioner
import org.springframework.batch.item.ExecutionContext
import org.springframework.jdbc.core.JdbcTemplate

class RangePartitioner(
    private val jdbcTemplate: JdbcTemplate
): Partitioner {
    override fun partition(gridSize: Int): MutableMap<String, ExecutionContext> {
        val result = mutableMapOf<String, ExecutionContext>()

        val minId = jdbcTemplate.queryForObject("SELECT MIN(id) FROM user", Long::class.java) ?: 1L
        val maxId = jdbcTemplate.queryForObject("SELECT MAX(id) FROM user", Long::class.java) ?: 1L

        val targetSize = ((maxId - minId + 1) / gridSize).coerceAtLeast(1)

        var start = minId
        var end = start + targetSize - 1

        for (i in 0 until gridSize) {
            val context = ExecutionContext()
            context.putLong("minId", start)
            context.putLong("maxId", end)
            result["partition$i"] = context

            start = end + 1
            end = start + targetSize - 1
            if (end > maxId) end = maxId
        }

        return result
    }

}