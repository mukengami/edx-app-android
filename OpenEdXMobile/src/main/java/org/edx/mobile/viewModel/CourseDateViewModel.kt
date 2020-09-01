package org.edx.mobile.viewModel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import okhttp3.MediaType
import okhttp3.ResponseBody
import org.edx.mobile.course.CourseAPI
import org.edx.mobile.http.HttpStatusException
import org.edx.mobile.model.course.CourseDateBlock
import org.edx.mobile.model.course.CourseDates
import org.edx.mobile.util.CourseDateType
import org.edx.mobile.util.DateUtil
import org.edx.mobile.view.CourseDatesPageFragment
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class CourseDateViewModel(private val courseAPI: CourseAPI) : ViewModel() {

    private val _showLoader = MutableLiveData<Boolean>()
    val showLoader: LiveData<Boolean>
        get() = _showLoader

    private val _isDateListReady = MutableLiveData<Boolean>()
    val isDateListReady: LiveData<Boolean>
        get() = _isDateListReady

    private val _swipeRefresh = MutableLiveData<Boolean>()
    val swipeRefresh: LiveData<Boolean>
        get() = _swipeRefresh

    private val _courseDateBlocks = MutableLiveData<List<CourseDateBlock>>()
    val courseDateBlocks: LiveData<List<CourseDateBlock>>
        get() = _courseDateBlocks

    private val _errorMessage = MutableLiveData<Throwable>()
    val errorMessage: LiveData<Throwable>
        get() = _errorMessage

    private lateinit var courseID: String

    var data: HashMap<String, ArrayList<CourseDateBlock>> = HashMap()
    var sortKeys: ArrayList<String> = ArrayList()

    fun startViewModel(courseID: String) {
        this.courseID = courseID
        fetchCourseDates()
    }

    fun fetchCourseDates() {
        _errorMessage.value = null
        _swipeRefresh.value = false
        _showLoader.value = true
        courseAPI.getCourseDates(courseID).enqueue(object : Callback<CourseDates> {
            override fun onResponse(call: Call<CourseDates>, response: Response<CourseDates>) {
                _showLoader.postValue(false)
                if (response.isSuccessful && response.body() != null) {
                    response.body()?.course_date_blocks?.let {
                        _isDateListReady.value = false
                        _courseDateBlocks.value = it
                    }
                } else {
                    _errorMessage.value = HttpStatusException(Response.error<Any>(response.code(),
                            ResponseBody.create(MediaType.parse("text/plain"), response.message())))
                }
                _swipeRefresh.postValue(false)
            }

            override fun onFailure(call: Call<CourseDates>, t: Throwable) {
                _showLoader.postValue(false)
                _errorMessage.value = t
                _swipeRefresh.postValue(false)
            }

        })
    }

    fun setError(code: Int, msg: String) {
        _errorMessage.value = HttpStatusException(Response.error<Any>(code,
                ResponseBody.create(MediaType.parse("text/plain"), msg)))
    }

    fun populateCourseDates() {
        populateCourseDatesInBlock()
        if (isContainToday().not()) {
            addTodayBlock()
        }
        setDateBlockTag()
        _isDateListReady.postValue(true)
    }

    /**
     * Rearrange the date blocks according to design and stack all the blocks of same date in one key
     */
    private fun populateCourseDatesInBlock() {
        data = HashMap()
        sortKeys = ArrayList()
        _courseDateBlocks.value?.forEach { item ->
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
        _courseDateBlocks.value?.forEach {
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
