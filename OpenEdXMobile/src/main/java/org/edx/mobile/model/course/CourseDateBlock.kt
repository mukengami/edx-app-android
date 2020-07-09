package org.edx.mobile.model.course

import com.google.gson.annotations.SerializedName
import org.edx.mobile.util.CourseDateType

data class CourseDateBlock(
        @SerializedName("date") val date: String,
        @SerializedName("date_type") var date_type: CourseDateType,
        @SerializedName("description") val description: String,
        @SerializedName("learner_has_access") var learner_has_access: Boolean,
        @SerializedName("link") val link: String,
        @SerializedName("link_text") val link_text: String,
        @SerializedName("title") val title: String,
        var isToday:Boolean = false
) {
    fun getToday(): Boolean {
        return isToday
//        return DateUtil.getCurrentTimeStamp().equals(DateUtil.formatCourseNotStartedDate(date))
    }
}
