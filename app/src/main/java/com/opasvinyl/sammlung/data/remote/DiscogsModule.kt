package com.opasvinyl.sammlung.data.remote

import android.content.Context
import androidx.room.Room
import com.opasvinyl.sammlung.BuildConfig
import com.opasvinyl.sammlung.data.local.VinylDatabase
import com.opasvinyl.sammlung.util.Constants
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import kotlinx.serialization.json.Json
import okhttp3.Interceptor
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.kotlinx.serialization.asConverterFactory
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    private val json = Json {
        ignoreUnknownKeys = true
        coerceInputValues = true
    }

    @Provides
    @Singleton
    fun provideOkHttpClient(): OkHttpClient {
        val authInterceptor = Interceptor { chain ->
            val request = chain.request().newBuilder()
                .addHeader("Authorization", "Discogs token=${BuildConfig.DISCOGS_TOKEN}")
                .addHeader("User-Agent", Constants.USER_AGENT)
                .build()
            chain.proceed(request)
        }

        val loggingInterceptor = HttpLoggingInterceptor().apply {
            level = if (BuildConfig.DEBUG) {
                HttpLoggingInterceptor.Level.BODY
            } else {
                HttpLoggingInterceptor.Level.NONE
            }
        }

        return OkHttpClient.Builder()
            .addInterceptor(authInterceptor)
            .addInterceptor(loggingInterceptor)
            .build()
    }

    @Provides
    @Singleton
    fun provideDiscogsApi(client: OkHttpClient): DiscogsApi {
        return Retrofit.Builder()
            .baseUrl(Constants.DISCOGS_BASE_URL)
            .client(client)
            .addConverterFactory(json.asConverterFactory("application/json".toMediaType()))
            .build()
            .create(DiscogsApi::class.java)
    }

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): VinylDatabase {
        return Room.databaseBuilder(
            context,
            VinylDatabase::class.java,
            "vinyl_database"
        ).build()
    }

    @Provides
    @Singleton
    fun provideVinylDao(database: VinylDatabase) = database.vinylDao()
}
