package org.edx.mobile.model.course

import com.google.gson.annotations.SerializedName

data class CourseDates(
        @SerializedName("dates_banner_info") val dates_banner_info: CourseDatesBannerInfo,
        @SerializedName("course_date_blocks") val course_date_blocks: List<CourseDateBlock>?,
        @SerializedName("missed_deadlines") val missed_deadlines: Boolean = false,
        @SerializedName("missed_gated_content") val missed_gated_content: Boolean = false,
        @SerializedName("learner_is_full_access") val learner_is_full_access: Boolean = false,
        @SerializedName("user_timezone") val user_timezone: String = "",
        @SerializedName("verified_upgrade_link") val verified_upgrade_link: String = ""
)
