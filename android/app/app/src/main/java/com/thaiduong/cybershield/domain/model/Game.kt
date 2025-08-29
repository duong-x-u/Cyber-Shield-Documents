package com.thaiduong.cybershield.domain.model

data class Game(
    val packageName: String,
    val displayName: String,
    val isHeavyGame: Boolean = false
)
