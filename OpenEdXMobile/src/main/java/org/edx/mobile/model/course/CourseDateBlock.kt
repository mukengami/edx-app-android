package org.edx.mobile.model.course

import com.google.gson.annotations.SerializedName
import org.edx.mobile.util.CourseDateType
import org.edx.mobile.util.DateUtil
import org.edx.mobile.util.TextUtils

data class CourseDateBlock(
        @SerializedName("complete") var complete: Boolean = false,
        @SerializedName("date") private val date: String = "",
        @SerializedName("date_type") var date_type: String? = "",
        @SerializedName("description") val description: String = "",
        @SerializedName("learner_has_access") var learner_has_access: Boolean = false,
        @SerializedName("link") val link: String = "",
        @SerializedName("link_text") val link_text: String = "",
        @SerializedName("title") val title: String = "",
        var dateBlockTag: CourseDateType = CourseDateType.BLANK,
        var isToday: Boolean = false
) {
    fun getToday(): Boolean {
        return DateUtil.isDateToday(date)
    }

    fun getDateTypeTag(): CourseDateType {
        date_type?.let {
            when (it) {
                DateTypes.TODAY_DATE ->
                    dateBlockTag = CourseDateType.TODAY
                DateTypes.COURSE_START_DATE,
                DateTypes.COURSE_END_DATE ->
                    dateBlockTag = CourseDateType.BLANK
                DateTypes.ASSIGNMENT_DUE_DATE -> {
                    when {
                        complete -> {
                            dateBlockTag = CourseDateType.COMPLETED
                        }
                        learner_has_access -> {
                            when {
                                DateUtil.isDatePast(date) -> {
                                    dateBlockTag = CourseDateType.PAST_DUE
                                }
                                DateUtil.isDateDue(date) -> {
                                    dateBlockTag = CourseDateType.DUE_NEXT
                                }
                                link.isEmpty() -> {
                                    dateBlockTag = CourseDateType.NOT_YET_RELEASED
                                }
                                else -> {
                                    dateBlockTag = CourseDateType.BLANK
                                }
                            }
                        }
                        else -> {
                            dateBlockTag = CourseDateType.VERIFIED_ONLY
                        }
                    }
                }
                DateTypes.COURSE_EXPIRED_DATE,
                DateTypes.CERTIFICATE_AVAILABLE_DATE,
                DateTypes.VERIFIED_UPGRADE_DEADLINE,
                DateTypes.VERIFICATION_DEADLINE_DATE ->
                    dateBlockTag = CourseDateType.BLANK
            }
            if (DateUtil.isDateToday(date)) {
                dateBlockTag = CourseDateType.TODAY
            }
        }
        return dateBlockTag
    }

    fun getDate(): String {
        return DateUtil.formatCourseDate(date)
    }

    object DateTypes {
        const val TODAY_DATE = "todays-date"
        const val COURSE_START_DATE = "course-start-date"
        const val COURSE_END_DATE = "course-end-date"
        const val COURSE_EXPIRED_DATE = "course-expired-date"
        const val ASSIGNMENT_DUE_DATE = "assignment-due-date"
        const val CERTIFICATE_AVAILABLE_DATE = "certificate-available-date"
        const val VERIFIED_UPGRADE_DEADLINE = "verified-upgrade-deadline"
        const val VERIFICATION_DEADLINE_DATE = "verification-deadline-date"
    }
}
