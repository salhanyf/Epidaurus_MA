package com.team11.epidaurus_ma

import kotlinx.serialization.Serializable

@Serializable
data class NoteEntry(
    val id: Int=0,
    val author:String="",
    val patient_id:Int=0,
    val time_date:String="",
    val observation:String=""
)