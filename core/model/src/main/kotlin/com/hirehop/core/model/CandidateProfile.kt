package com.hirehop.core.model

data class CandidateProfile(
    val fullName: String,
    val email: String,
    val phone: String,
    val headline: String,
    val skills: List<String>,
    val entries: List<ProfileEntry>,
)
