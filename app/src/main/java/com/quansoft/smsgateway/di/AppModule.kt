package com.quansoft.smsgateway.di

import android.content.Context
import com.quansoft.smsgateway.data.local.AppDatabase
import com.quansoft.smsgateway.data.local.dao.CampaignDao
import com.quansoft.smsgateway.data.local.dao.SmsDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideAppDatabase(@ApplicationContext context: Context): AppDatabase {
        return AppDatabase.getDatabase(context)
    }

    @Provides
    @Singleton
    fun provideSmsDao(database: AppDatabase): SmsDao {
        return database.smsDao()
    }

    @Provides
    @Singleton
    fun provideCampaignDao(database: AppDatabase): CampaignDao {
        return database.bulkCampaignDao()
    }
}