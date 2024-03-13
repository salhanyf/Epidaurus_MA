package com.team11.epidaurus_ma

import kotlinx.serialization.Serializable

@Serializable
data class IssueEntry (
    val nurse_email:String? ="",
    val date_of_issue:String? ="",
    val detail_issue:String? ="",
    val patient_name:String?="",
    val status_issue:String?=""
)