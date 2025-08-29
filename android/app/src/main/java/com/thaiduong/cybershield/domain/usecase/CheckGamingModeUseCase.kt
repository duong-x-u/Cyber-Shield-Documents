package com.thaiduong.cybershield.domain.usecase

import com.thaiduong.cybershield.domain.repository.GameRepository

class CheckGamingModeUseCase(
    private val gameRepository: GameRepository
) {
    fun execute(currentApp: String?): Boolean {
        return currentApp?.let { gameRepository.isHeavyGame(it) } ?: false
    }
}
