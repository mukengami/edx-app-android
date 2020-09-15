package org.edx.mobile.model.course

import com.google.gson.annotations.SerializedName
import org.edx.mobile.util.CourseDateType
import org.edx.mobile.util.DateUtil
import org.edx.mobile.view.CourseDatesPageFragment

data class CourseDates(
        @SerializedName("dates_banner_info") val dates_banner_info: CourseDatesBannerInfo,
        @SerializedName("course_date_blocks") val course_date_blocks: List<CourseDateBlock>?,
        @SerializedName("missed_deadlines") val missed_deadlines: Boolean = false,
        @SerializedName("missed_gated_content") val missed_gated_content: Boolean = false,
        @SerializedName("learner_is_full_access") val learner_is_full_access: Boolean = false,
        @SerializedName("user_timezone") val user_timezone: String = "",
        @SerializedName("verified_upgrade_link") val verified_upgrade_link: String = "",
        var data: HashMap<String, ArrayList<CourseDateBlock>> = HashMap(),
        var sortKeys: ArrayList<String> = ArrayList()
) {
    fun populateCourseDates() {
        populateCourseDatesInBlock()
        if (isContainToday().not()) {
            addTodayBlock()
        }
        setDateBlockTag()
    }

    /**
     * Rearrange the date blocks according to design and stack all the blocks of same date in one key
     */
    private fun populateCourseDatesInBlock() {
        data = HashMap()
        sortKeys = ArrayList()
        course_date_blocks?.forEach { item ->
            if (data.containsKey(item.getSimpleDateTime())) {
                (data[item.getSimpleDateTime()] as ArrayList).add(item)
            } else {
                data[item.getSimpleDateTime()] = arrayListOf(item)
                sortKeys.add(item.getSimpleDateTime())
            }
        }
    }

    /**
     * Utility Method to check if the list contains the today date block or not
     */
    private fun isContainToday(): Boolean {
        course_date_blocks?.forEach {
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
            sortKeys.add(ind, CourseDatesPageFragment.getTodayDateBlock().getSimpleDateTime())
        }
    }

    /**
     * Set the Date Block Tag against single date set
     */
    private fun setDateBlockTag() {
        var dueNextCount = 0
        sortKeys.forEach { key ->
            data[key]?.forEach { item ->
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
        item.date_type?.let {
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
                        item.learner_has_access -> {
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
