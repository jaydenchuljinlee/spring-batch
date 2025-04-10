package com.example.batch.config.listener

import com.example.batch.domain.User
import org.springframework.batch.core.SkipListener

class CustomSkipListener : SkipListener<User, String> {
    override fun onSkipInProcess(item: User, t: Throwable) {
        println("SKIPPED 처리 중 예외 발생 → ID=${item.id}, 예외=${t.message}")
    }
}
