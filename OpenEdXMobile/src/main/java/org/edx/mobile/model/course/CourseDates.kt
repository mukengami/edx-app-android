package org.edx.mobile.model.course

import com.google.gson.annotations.SerializedName
import org.edx.mobile.util.CourseDateType
import org.edx.mobile.util.DateUtil

data class CourseDates(
        @SerializedName("dates_banner_info") val datesBannerInfo: CourseDatesBannerInfo,
        @SerializedName("course_date_blocks") val courseDateBlocks: List<CourseDateBlock>?,
        @SerializedName("missed_deadlines") val missedDeadlines: Boolean = false,
        @SerializedName("missed_gated_content") val missedGatedContent: Boolean = false,
        @SerializedName("learner_is_full_access") val learnerIsFullAccess: Boolean = false,
        @SerializedName("user_timezone") val userTimezone: String = "",
        @SerializedName("verified_upgrade_link") val verifiedUpgradeLink: String = "",
        var courseDatesMap: HashMap<String, ArrayList<CourseDateBlock>> = HashMap(),
        var sortKeys: ArrayList<String> = ArrayList()
) {
    fun organiseCourseDates() {
        organiseCourseDatesInBlock()
        if (isContainToday().not()) {
            addTodayBlock()
        }
        setDateBlockTag()
    }

    /**
     * Map the date blocks according to dates and stack all the blocks of same date against one key
     */
    private fun organiseCourseDatesInBlock() {
        courseDatesMap = HashMap()
        sortKeys = ArrayList()
        courseDateBlocks?.forEach { item ->
            if (courseDatesMap.containsKey(item.getSimpleDateTime())) {
                (courseDatesMap[item.getSimpleDateTime()] as ArrayList).add(item)
            } else {
                courseDatesMap[item.getSimpleDateTime()] = arrayListOf(item)
                sortKeys.add(item.getSimpleDateTime())
            }
        }
    }

    /**
     * Utility Method to check if the list contains the today date block or not
     */
    private fun isContainToday(): Boolean {
        courseDateBlocks?.forEach {
            if (it.isToday()) {
                return true
            }
        }
        return false
    }

    /**
     * Add today date block manually if not present in date list
     */
    private fun addTodayBlock() {
        if (DateUtil.isDatePast(sortKeys.first()) && DateUtil.isDateDue(sortKeys.last())) {
            var ind = 0
            sortKeys.forEachIndexed { index, str ->
                if (index < sortKeys.lastIndex && DateUtil.isDatePast(str) && DateUtil.isDateDue(sortKeys[index + 1])) {
                    ind = index + 1
                }
            }
            sortKeys.add(ind, CourseDateBlock.getTodayDateBlock().getSimpleDateTime())
        }
    }

    /**
     * Set the Date Block Tag against single date set
     */
    private fun setDateBlockTag() {
        var dueNextCount = 0
        sortKeys.forEach { key ->
            courseDatesMap[key]?.forEach { item ->
                var dateBlockTag: CourseDateType = getDateTypeTag(item)
                //Setting Due Next only for first occurrence
                if (dateBlockTag == CourseDateType.DUE_NEXT) {
                    if (dueNextCount == 0)
                        dueNextCount += 1
                    else
                        dateBlockTag = CourseDateType.BLANK
                }
                item.dateBlockTag = dateBlockTag
            }
        }
    }

    /**
     * Method to get the Tag to be set on Pill/Badge of date block
     */
    private fun getDateTypeTag(item: CourseDateBlock): CourseDateType {
        var dateBlockTag: CourseDateType = CourseDateType.BLANK
        item.dateType?.let {
            when (it) {
                CourseDateBlock.DateTypes.TODAY_DATE ->
                    dateBlockTag = CourseDateType.TODAY
                CourseDateBlock.DateTypes.COURSE_START_DATE,
                CourseDateBlock.DateTypes.COURSE_END_DATE ->
                    dateBlockTag = CourseDateType.BLANK
                CourseDateBlock.DateTypes.ASSIGNMENT_DUE_DATE -> {
                    when {
                        item.complete -> {
                            dateBlockTag = CourseDateType.COMPLETED
                        }
                        item.learnerHasAccess -> {
                            dateBlockTag = when {
                                item.link.isEmpty() -> {
                                    CourseDateType.NOT_YET_RELEASED
                                }
                                DateUtil.isDateDue(item.date) -> {
                                    CourseDateType.DUE_NEXT
                                }
                                DateUtil.isDatePast(item.date) -> {
                                    CourseDateType.PAST_DUE
                                }
                                else -> {
                                    CourseDateType.BLANK
                                }
                            }
                        }
                        else -> {
                            dateBlockTag = CourseDateType.VERIFIED_ONLY
                        }
                    }
                }
                CourseDateBlock.DateTypes.COURSE_EXPIRED_DATE,
                CourseDateBlock.DateTypes.CERTIFICATE_AVAILABLE_DATE,
                CourseDateBlock.DateTypes.VERIFIED_UPGRADE_DEADLINE,
                CourseDateBlock.DateTypes.VERIFICATION_DEADLINE_DATE ->
                    dateBlockTag = CourseDateType.BLANK
            }
        }
        return dateBlockTag
    }
}
