package com.kozvits.kislogtd.domain.usecase

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class UseCaseModule {

    @Binds
    @Singleton
    abstract fun bindCaptureTaskUseCase(impl: CaptureTaskUseCaseImpl): CaptureTaskUseCase

    @Binds
    @Singleton
    abstract fun bindCompleteTaskUseCase(impl: CompleteTaskUseCaseImpl): CompleteTaskUseCase

    @Binds
    @Singleton
    abstract fun bindMoveTaskUseCase(impl: MoveTaskUseCaseImpl): MoveTaskUseCase

    @Binds
    @Singleton
    abstract fun bindProcessInboxUseCase(impl: ProcessInboxUseCaseImpl): ProcessInboxUseCase

    @Binds
    @Singleton
    abstract fun bindToggleTaskUseCase(impl: ToggleTaskUseCaseImpl): ToggleTaskUseCase

    @Binds
    @Singleton
    abstract fun bindGetWeeklyStatsUseCase(impl: GetWeeklyStatsUseCaseImpl): GetWeeklyStatsUseCase
}
