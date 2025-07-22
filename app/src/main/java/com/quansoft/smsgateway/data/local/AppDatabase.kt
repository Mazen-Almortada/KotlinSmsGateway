package com.quansoft.smsgateway.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.quansoft.smsgateway.data.local.dao.CampaignDao
import com.quansoft.smsgateway.data.local.dao.SmsDao
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import java.util.UUID

@Database(
    entities = [SmsMessageEntity::class, CampaignEntity::class],
    version = 3,
    // Set exportSchema to false to resolve the build error.
    // This is acceptable during development. For production, you would
    // configure the schema export path in your build.gradle file.
    exportSchema = false,

    )
abstract class AppDatabase : RoomDatabase() {

    abstract fun smsDao(): SmsDao
    abstract fun bulkCampaignDao(): CampaignDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "sms_gateway_database"
                ).fallbackToDestructiveMigration(false)
                    .addCallback(AppDatabaseCallback(CoroutineScope(SupervisorJob())))
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }

    /**
     * A callback to populate the database with initial data when it's first created.
     */
    private class AppDatabaseCallback(
        private val scope: CoroutineScope
    ) : Callback() {
        override fun onCreate(db: SupportSQLiteDatabase) {
            super.onCreate(db)
            INSTANCE?.let { database ->
                scope.launch {
                    populateDatabase(database.smsDao(), database.bulkCampaignDao())
                }
            }
        }

        suspend fun populateDatabase(smsDao: SmsDao, campaignDao: CampaignDao) {
            // Add a couple of sample campaigns
            val campaign1 = CampaignEntity(
                id = "promo-q1-2025",
                name = "Q1 Promotions",
                timestamp = System.currentTimeMillis()
            )
            val campaign2 = CampaignEntity(
                id = "update-july-2025",
                name = "July Updates",
                timestamp = System.currentTimeMillis()
            )
            campaignDao.insert(campaign1)
            campaignDao.insert(campaign2)

            // Add some sample messages with different statuses
            val messages = listOf(
                SmsMessageEntity(
                    id = UUID.randomUUID().toString(),
                    recipient = "+15551234567",
                    content = "Check out our new Q1 deals! Big savings await.",
                    status = "delivered",
                    timestamp = System.currentTimeMillis() - 120000,
                    bulkId = campaign1.id
                ),
                SmsMessageEntity(
                    id = UUID.randomUUID().toString(),
                    recipient = "John Doe",
                    content = "Your account has a new security update for July. Please review.",
                    status = "sent",
                    timestamp = System.currentTimeMillis() - 60000,
                    bulkId = campaign2.id
                ),
                SmsMessageEntity(
                    id = UUID.randomUUID().toString(),
                    recipient = "+15557654321",
                    content = "This is a standalone message that failed to send.",
                    status = "failed",
                    timestamp = System.currentTimeMillis() - 30000,
                    bulkId = null
                ),
                SmsMessageEntity(
                    id = UUID.randomUUID().toString(),
                    recipient = "Jane Smith",
                    content = "Another promotional message for our Q1 event.",
                    status = "sent",
                    timestamp = System.currentTimeMillis(),
                    bulkId = campaign1.id
                ),
                SmsMessageEntity(
                    id = UUID.randomUUID().toString(),
                    recipient = "Service Alerts",
                    content = "We are performing system maintenance.",
                    status = "sending",
                    timestamp = System.currentTimeMillis() - 5000,
                    bulkId = campaign2.id
                )
            )
            smsDao.insertAll(messages)
        }
    }
}
