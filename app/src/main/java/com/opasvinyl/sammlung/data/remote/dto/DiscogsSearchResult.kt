package com.opasvinyl.sammlung.data.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class DiscogsSearchResponse(
    val results: List<DiscogsSearchResult> = emptyList()
)

@Serializable
data class DiscogsSearchResult(
    val id: Int,
    val title: String,
    val year: String? = null,
    val country: String? = null,
    val genre: List<String> = emptyList(),
    val style: List<String> = emptyList(),
    val label: List<String> = emptyList(),
    val format: List<String> = emptyList(),
    @SerialName("cover_image")
    val coverImage: String? = null,
    @SerialName("thumb")
    val thumbnail: String? = null,
    @SerialName("resource_url")
    val resourceUrl: String? = null,
    @SerialName("master_id")
    val masterId: Int? = null,
    val barcode: List<String> = emptyList(),
    val catno: String? = null
)
