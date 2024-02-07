package com.team11.epidaurus_ma

import kotlinx.serialization.Serializable

@Serializable
data class CapacitiveTouchResponse(
    val id:Int,
    val touched:Int
)