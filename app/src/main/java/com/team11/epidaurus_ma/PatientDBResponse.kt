package com.team11.epidaurus_ma

import kotlinx.serialization.Serializable

@Serializable
data class PatientDBResponse(
    val id:Int = 0,
    val name:String = "",
    val department:String = "",
    val floor:String = "",
    val braceletId:Int = 0,
    val admittanceDate:String = "",
    val estimatedStay:Int = 0,
    val dateOfBirth:String = "",
    val symptoms:String = "",
    val medicalHistory:String = "",
    val medicalDiagnosis:String = "",
    val roomNumber:String = ""
)