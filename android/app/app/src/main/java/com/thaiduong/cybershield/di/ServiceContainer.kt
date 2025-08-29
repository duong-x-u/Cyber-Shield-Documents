package com.thaiduong.cybershield.di

import android.content.Context
import com.thaiduong.cybershield.data.manager.AppUsageManager
import com.thaiduong.cybershield.data.manager.AppUsageManagerImpl
import com.thaiduong.cybershield.data.repository.GameRepositoryImpl
import com.thaiduong.cybershield.domain.repository.GameRepository
import com.thaiduong.cybershield.domain.usecase.CheckGamingModeUseCase
import com.thaiduong.cybershield.domain.usecase.MonitorGamingModeUseCase

object ServiceContainer {
    fun provideGameRepository(): GameRepository = GameRepositoryImpl()
    
    fun provideAppUsageManager(context: Context): AppUsageManager = 
        AppUsageManagerImpl(context)
    
    fun provideCheckGamingModeUseCase(
        gameRepository: GameRepository
    ): CheckGamingModeUseCase = CheckGamingModeUseCase(gameRepository)
    
    fun provideMonitorGamingModeUseCase(
        checkGamingModeUseCase: CheckGamingModeUseCase,
        appUsageManager: AppUsageManager
    ): MonitorGamingModeUseCase = MonitorGamingModeUseCase(
        checkGamingModeUseCase,
        appUsageManager
    )
}
