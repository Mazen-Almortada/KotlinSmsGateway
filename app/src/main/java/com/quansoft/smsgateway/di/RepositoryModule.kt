package com.quansoft.smsgateway.di

import com.quansoft.smsgateway.data.repository.CampaignRepositoryImpl
import com.quansoft.smsgateway.data.repository.MessageRepositoryImpl
import com.quansoft.smsgateway.data.repository.SettingsRepositoryImpl
import com.quansoft.smsgateway.domain.repository.CampaignRepository
import com.quansoft.smsgateway.domain.repository.MessageRepository
import com.quansoft.smsgateway.domain.repository.SettingsRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    @Singleton
    abstract fun bindMessageRepository(
        messageRepositoryImpl: MessageRepositoryImpl
    ): MessageRepository

    @Binds
    @Singleton
    abstract fun bindCampaignRepository(
        campaignRepositoryImpl: CampaignRepositoryImpl
    ): CampaignRepository

    @Binds
    @Singleton
    abstract fun bindSettingsRepository(
        settingsRepositoryImpl: SettingsRepositoryImpl
    ): SettingsRepository
}