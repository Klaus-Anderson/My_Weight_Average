package com.angussoftware.myweightaverage.dependencyinjection

import android.content.Context
import androidx.health.connect.client.HealthConnectClient
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ViewModelComponent
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.android.scopes.ViewModelScoped
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(ViewModelComponent::class)
object AppModule {

    @Provides
    @ViewModelScoped
    fun provideHealthConnectClient(@ApplicationContext appContext: Context) =
        if (HealthConnectClient.isProviderAvailable(appContext)) {
            HealthConnectClient.getOrCreate(appContext)
        } else {
            throw NullPointerException()
        }
}