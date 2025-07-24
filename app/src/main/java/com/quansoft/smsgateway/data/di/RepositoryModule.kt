package com.quansoft.smsgateway.data.di

// ... (imports)
import com.quansoft.smsgateway.data.repository.CampaignRepositoryImpl
import com.quansoft.smsgateway.data.repository.ContactsRepositoryImpl
import com.quansoft.smsgateway.data.repository.MessageRepositoryImpl
import com.quansoft.smsgateway.data.repository.NetworkInfoRepositoryImpl
import com.quansoft.smsgateway.data.repository.SettingsRepositoryImpl
import com.quansoft.smsgateway.domain.repository.CampaignRepository
import com.quansoft.smsgateway.domain.repository.ContactsRepository
import com.quansoft.smsgateway.domain.repository.MessageRepository
import com.quansoft.smsgateway.domain.repository.NetworkInfoRepository
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
    abstract fun bindMessageRepository(impl: MessageRepositoryImpl): MessageRepository

    @Binds
    @Singleton
    abstract fun bindCampaignRepository(impl: CampaignRepositoryImpl): CampaignRepository

    @Binds
    @Singleton
    abstract fun bindSettingsRepository(impl: SettingsRepositoryImpl): SettingsRepository

    @Binds
    @Singleton
    abstract fun bindContactsRepository(impl: ContactsRepositoryImpl): ContactsRepository
    @Binds
    @Singleton
    abstract fun bindNetworkInfoRepository(impl: NetworkInfoRepositoryImpl): NetworkInfoRepository
}