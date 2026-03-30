package com.opasvinyl.sammlung.data.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class DiscogsRelease(
    val id: Int,
    val title: String,
    val year: Int? = null,
    val country: String? = null,
    val genres: List<String> = emptyList(),
    val styles: List<String> = emptyList(),
    val tracklist: List<DiscogsTrack> = emptyList(),
    val labels: List<DiscogsLabel> = emptyList(),
    val images: List<DiscogsImage> = emptyList(),
    val formats: List<DiscogsFormat> = emptyList(),
    val identifiers: List<DiscogsIdentifier> = emptyList(),
    @SerialName("artists")
    val artists: List<DiscogsArtist> = emptyList(),
    val notes: String? = null,
    @SerialName("lowest_price")
    val lowestPrice: Double? = null,
    @SerialName("num_for_sale")
    val numForSale: Int? = null,
    val community: DiscogsCommunity? = null
)

@Serializable
data class DiscogsCommunity(
    val have: Int = 0,
    val want: Int = 0
)

@Serializable
data class DiscogsTrack(
    val position: String = "",
    val title: String = "",
    val duration: String = ""
)

@Serializable
data class DiscogsLabel(
    val name: String = "",
    val catno: String? = null
)

@Serializable
data class DiscogsImage(
    val type: String = "",
    val uri: String = "",
    @SerialName("uri150")
    val thumbnail: String = ""
)

@Serializable
data class DiscogsFormat(
    val name: String = "",
    val qty: String = "1",
    val descriptions: List<String> = emptyList()
)

@Serializable
data class DiscogsIdentifier(
    val type: String = "",
    val value: String = ""
)

@Serializable
data class DiscogsArtist(
    val name: String = "",
    val id: Int = 0
)
