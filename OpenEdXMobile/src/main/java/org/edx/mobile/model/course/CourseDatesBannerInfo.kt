package org.edx.mobile.model.course

import com.google.gson.annotations.SerializedName


data class CourseDatesBannerInfo(
        @SerializedName("missed_deadlines") val missed_deadlines: Boolean = false,
        @SerializedName("missed_gated_content") val missed_gated_content: Boolean = false,
        @SerializedName("verified_upgrade_link") val verified_upgrade_link: String = "",
        @SerializedName("content_type_gating_enabled") val content_type_gating_enabled: Boolean = false
)
