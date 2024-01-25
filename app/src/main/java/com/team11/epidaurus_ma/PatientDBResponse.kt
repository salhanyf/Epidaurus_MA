package com.team11.epidaurus_ma

import kotlinx.serialization.Serializable

@Serializable
data class PatientDBResponse(
    val id:Int = 0,
    val name:String = "",
    val department:String = "",
    val floor:String = "",
    val bracelet_id:Int = 0,
    val admittance_date:String = "",
    val estimated_stay:Int = 0,
    val date_of_birth:String = "",
    val symptoms:String = "",
    val medical_history:String = "",
    val medical_diagnosis:String = "",
    val room_number:String = ""
)