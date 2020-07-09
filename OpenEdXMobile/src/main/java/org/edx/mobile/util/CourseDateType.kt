package org.edx.mobile.util

/**
 * This enum defines the Date type of Course Dates
 */
enum class CourseDateType {
    TODAY,
    COURSE_START_DATE,
    VERIFIED_ONLY,
    COMPLETED,
    PAST_DUE,
    DUE_NEXT,
    NOT_YET_RELEASED,
    COURSE_IN_PROGRESS,
    COURSE_END;

    fun getTitle(): String {
        return when (this) {
            TODAY -> "Today"
            VERIFIED_ONLY -> "Verified Only"
            COMPLETED -> "Completed"
            PAST_DUE -> "Past Due"
            DUE_NEXT -> "Due Next"
            NOT_YET_RELEASED -> "Not Yet Released"
            else -> ""
        }
    }
}
