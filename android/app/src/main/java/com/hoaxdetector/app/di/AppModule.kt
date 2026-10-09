package com.hoaxdetector.app.di

import android.content.Context
import androidx.room.Room
import com.hoaxdetector.app.BuildConfig
import com.hoaxdetector.app.data.local.HoaxDetectorDatabase
import com.hoaxdetector.app.data.local.dao.CheckHistoryDao
import com.hoaxdetector.app.data.remote.api.HoaxDetectorApi
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.kotlinx.serialization.asConverterFactory
import java.util.concurrent.TimeUnit
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideJson(): Json = Json {
        ignoreUnknownKeys = true
        isLenient = true
        coerceInputValues = true
    }

    @Provides
    @Singleton
    fun provideOkHttpClient(): OkHttpClient {
        val loggingInterceptor = HttpLoggingInterceptor().apply {
            level = if (BuildConfig.DEBUG)
                HttpLoggingInterceptor.Level.BODY
            else
                HttpLoggingInterceptor.Level.NONE
        }

        return OkHttpClient.Builder()
            .addInterceptor(loggingInterceptor)
            .connectTimeout(4, TimeUnit.SECONDS) // Quick fallback to local engine if server is offline
            .readTimeout(60, TimeUnit.SECONDS) // Longer timeout for AI analysis
            .writeTimeout(30, TimeUnit.SECONDS)
            .build()
    }

    @Provides
    @Singleton
    fun provideRetrofit(okHttpClient: OkHttpClient, json: Json): Retrofit {
        return Retrofit.Builder()
            .baseUrl(BuildConfig.BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(
                json.asConverterFactory("application/json".toMediaType())
            )
            .build()
    }

    @Provides
    @Singleton
    fun provideHoaxDetectorApi(retrofit: Retrofit): HoaxDetectorApi {
        return retrofit.create(HoaxDetectorApi::class.java)
    }

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): HoaxDetectorDatabase {
        return Room.databaseBuilder(
            context,
            HoaxDetectorDatabase::class.java,
            HoaxDetectorDatabase.DATABASE_NAME
        ).fallbackToDestructiveMigration().build()
    }

    @Provides
    @Singleton
    fun provideCheckHistoryDao(database: HoaxDetectorDatabase): CheckHistoryDao {
        return database.checkHistoryDao()
    }
}
