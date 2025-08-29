package com.thaiduong.cybershield.domain.repository

import com.thaiduong.cybershield.domain.model.Game

interface GameRepository {
    fun getHeavyGames(): Set<Game>
    fun isHeavyGame(packageName: String): Boolean
}
