package com.vltvplus.di

import android.content.Context
import com.vltvplus.data.api.TmdbApiService
import com.vltvplus.data.api.XtreamApiService
import com.vltvplus.data.database.VLTVDatabase
import com.vltvplus.data.database.dao.*
import com.vltvplus.utils.PreferenceManager
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit
import javax.inject.Named
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): VLTVDatabase =
        VLTVDatabase.create(context)

    @Provides fun provideChannelDao(db: VLTVDatabase): ChannelDao = db.channelDao()
    @Provides fun provideMovieDao(db: VLTVDatabase): MovieDao = db.movieDao()
    @Provides fun provideSeriesDao(db: VLTVDatabase): SeriesDao = db.seriesDao()
    @Provides fun provideEpisodeDao(db: VLTVDatabase): EpisodeDao = db.episodeDao()
    @Provides fun provideCategoryDao(db: VLTVDatabase): CategoryDao = db.categoryDao()
    @Provides fun provideWatchProgressDao(db: VLTVDatabase): WatchProgressDao = db.watchProgressDao()
    @Provides fun provideFavoriteDao(db: VLTVDatabase): FavoriteDao = db.favoriteDao()
    @Provides fun provideUserSessionDao(db: VLTVDatabase): UserSessionDao = db.userSessionDao()
    @Provides fun provideEpgCacheDao(db: VLTVDatabase): EpgCacheDao = db.epgCacheDao()
}

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    @Provides
    @Singleton
    fun provideOkHttpClient(): OkHttpClient = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .addInterceptor(HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BASIC
        })
        .retryOnConnectionFailure(true)
        .build()

    @Provides
    @Singleton
    @Named("xtream")
    fun provideXtreamRetrofit(okHttpClient: OkHttpClient): Retrofit = Retrofit.Builder()
        .baseUrl("http://placeholder.com/") // Dynamic base URL per DNS
        .client(okHttpClient)
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    @Provides
    @Singleton
    @Named("tmdb")
    fun provideTmdbRetrofit(okHttpClient: OkHttpClient): Retrofit = Retrofit.Builder()
        .baseUrl("https://api.themoviedb.org/3/")
        .client(okHttpClient)
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    @Provides
    @Singleton
    fun provideXtreamApiService(@Named("xtream") retrofit: Retrofit): XtreamApiService =
        retrofit.create(XtreamApiService::class.java)

    @Provides
    @Singleton
    fun provideTmdbApiService(@Named("tmdb") retrofit: Retrofit): TmdbApiService =
        retrofit.create(TmdbApiService::class.java)
}

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun providePreferenceManager(@ApplicationContext context: Context): PreferenceManager =
        PreferenceManager(context)
}
