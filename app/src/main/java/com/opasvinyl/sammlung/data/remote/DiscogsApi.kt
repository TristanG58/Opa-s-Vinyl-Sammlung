package com.opasvinyl.sammlung.data.remote

import com.opasvinyl.sammlung.data.remote.dto.DiscogsRelease
import com.opasvinyl.sammlung.data.remote.dto.DiscogsSearchResponse
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface DiscogsApi {

    @GET("database/search")
    suspend fun searchByBarcode(
        @Query("barcode") barcode: String,
        @Query("type") type: String = "release"
    ): DiscogsSearchResponse

    @GET("database/search")
    suspend fun searchByQuery(
        @Query("q") query: String,
        @Query("type") type: String = "release",
        @Query("per_page") perPage: Int = 20
    ): DiscogsSearchResponse

    @GET("releases/{id}")
    suspend fun getRelease(
        @Path("id") releaseId: Int
    ): DiscogsRelease
}
