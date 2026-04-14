package com.vltvplus.data.api

import com.vltvplus.data.models.*
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Query
import retrofit2.http.Url

interface XtreamApiService {

    // Authentication & User Info
    @GET
    suspend fun authenticate(
        @Url url: String
    ): Response<XtreamAuthResponse>

    // Live TV
    @GET
    suspend fun getLiveCategories(
        @Url url: String
    ): Response<List<Category>>

    @GET
    suspend fun getLiveStreams(
        @Url url: String
    ): Response<List<LiveStream>>

    @GET
    suspend fun getLiveStreamsByCategory(
        @Url url: String
    ): Response<List<LiveStream>>

    // EPG (Electronic Program Guide)
    @GET
    suspend fun getEpg(
        @Url url: String
    ): Response<EpgResponse>

    @GET
    suspend fun getShortEpg(
        @Url url: String
    ): Response<EpgResponse>

    // VOD (Movies)
    @GET
    suspend fun getVodCategories(
        @Url url: String
    ): Response<List<Category>>

    @GET
    suspend fun getVodStreams(
        @Url url: String
    ): Response<List<VodStream>>

    @GET
    suspend fun getVodInfo(
        @Url url: String
    ): Response<VodInfo>

    // Series
    @GET
    suspend fun getSeriesCategories(
        @Url url: String
    ): Response<List<Category>>

    @GET
    suspend fun getSeriesList(
        @Url url: String
    ): Response<List<SeriesStream>>

    @GET
    suspend fun getSeriesInfo(
        @Url url: String
    ): Response<SeriesInfo>
}
