package com.team11.epidaurus_ma

import kotlinx.serialization.Serializable

@Serializable
data class VitalsResponse(
    val id:Int = 0,
    val fallDetected:Int=0,
    val heartRate:Int=0,
    val bodyTemp:Float=0f,
    val spO2:Float=0f,
    val patientID:Int=0,
)