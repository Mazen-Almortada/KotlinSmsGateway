package com.quansoft.smsgateway.data.di

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
class DatabaseModule {
    @Provides
    @Singleton
    fun provideAppDatabase(@ApplicationContext context: Context): AppDatabase {
        return AppDatabase.getDatabase(context)
    }
    @Provides
    fun provideSmsDao(db: AppDatabase): SmsDao = db.smsDao()

    @Provides
    fun provideCampaignDao(db: AppDatabase): CampaignDao = db.bulkCampaignDao()
}