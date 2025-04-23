package com.rexvit.refocux.di

import android.content.Context
import com.rexvit.refocux.data.database.FocusDao
import com.rexvit.refocux.data.database.FocusDatabase
import com.rexvit.refocux.data.repository.FocusRepository
import com.rexvit.refocux.service.AnalyticsService
import com.rexvit.refocux.service.FirebaseService
import com.rexvit.refocux.service.NotificationService
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {
    @Singleton
    @Provides
    fun provideFocusDatabase(@ApplicationContext context: Context): FocusDatabase {
        return FocusDatabase.getDatabase(context)
    }

    @Singleton
    @Provides
    fun provideFocusDao(database: FocusDatabase): FocusDao {
        return database.focusDao()
    }

    @Singleton
    @Provides
    fun provideFocusRepository(dao: FocusDao, firebaseService: FirebaseService): FocusRepository {
        return FocusRepository(dao, firebaseService)
    }

    @Singleton
    @Provides
    fun provideFirebaseService(): FirebaseService {
        return FirebaseService()
    }

    @Singleton
    @Provides
    fun provideNotificationService(@ApplicationContext context: Context): NotificationService {
        return NotificationService(context)
    }

    @Singleton
    @Provides
    fun provideAnalyticsService(): AnalyticsService {
        return AnalyticsService()
    }
}