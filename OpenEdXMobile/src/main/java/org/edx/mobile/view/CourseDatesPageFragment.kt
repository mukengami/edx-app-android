package org.edx.mobile.view

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout.OnRefreshListener
import okhttp3.MediaType
import okhttp3.ResponseBody
import org.edx.mobile.R
import org.edx.mobile.course.CourseAPI
import org.edx.mobile.databinding.FragmentCourseDatesPageBinding
import org.edx.mobile.http.HttpStatus
import org.edx.mobile.http.HttpStatusException
import org.edx.mobile.http.notifications.FullScreenErrorNotification
import org.edx.mobile.interfaces.OnDateBlockListener
import org.edx.mobile.model.course.CourseDateBlock
import org.edx.mobile.model.course.CourseDates
import org.edx.mobile.util.BrowserUtil
import org.edx.mobile.util.CourseDateType
import org.edx.mobile.util.DateUtil
import org.edx.mobile.util.UiUtil
import org.edx.mobile.view.adapters.CourseDatesAdapter
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import javax.inject.Inject


class CourseDatesPageFragment : OfflineSupportBaseFragment() {

    @Inject
    var api: CourseAPI? = null
    private lateinit var errorNotification: FullScreenErrorNotification

    private lateinit var mBinding: FragmentCourseDatesPageBinding
    private var data: HashMap<String, ArrayList<CourseDateBlock>> = HashMap()
    private var sortKeys: ArrayList<String> = ArrayList()
    private var onLinkClick: OnDateBlockListener = object : OnDateBlockListener {
        override fun onClick(link: String) {
            BrowserUtil.open(activity, link)
        }
    }

    companion object {
        @JvmStatic
        fun makeArguments(courseId: String?): Bundle? {
            val courseBundle = Bundle()
            courseBundle.putString(Router.EXTRA_COURSE_ID, courseId)
            return courseBundle
        }

        @JvmStatic
        fun getTodayDateBlock() = CourseDateBlock(date = DateUtil.getCurrentTimeStamp(), date_type = CourseDateBlock.DateTypes.TODAY_DATE)
    }

    override fun isShowingFullScreenError(): Boolean {
        return false
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        mBinding = DataBindingUtil.inflate(inflater, R.layout.fragment_course_dates_page, container, false)
        return mBinding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        errorNotification = FullScreenErrorNotification(mBinding.swipeContainer)

        mBinding.swipeContainer.setOnRefreshListener(OnRefreshListener {
            // Hide the progress bar as swipe layout has its own progress indicator
            mBinding.loadingIndicator.loadingIndicator.visibility = View.GONE
            errorNotification.hideError()
            getCourseDates()
        })
        UiUtil.setSwipeRefreshLayoutColors(mBinding.swipeContainer)
        getCourseDates()
    }

    private fun getCourseDates() {
        mBinding.loadingIndicator.loadingIndicator.visibility = View.VISIBLE
        var courseDates: Call<CourseDates> = api?.getCourseDates(arguments?.getString(Router.EXTRA_COURSE_ID)!!)!!
        courseDates.enqueue(object : Callback<CourseDates> {
            override fun onResponse(call: Call<CourseDates>, response: Response<CourseDates>) {
                if (response.isSuccessful) {
                    response.body()?.course_date_blocks?.let {
                        populateCourseDates(it)

                    }
                } else {
                    context?.let {
                        errorNotification.showError(it,
                                HttpStatusException(Response.error<Any>(response.code(),
                                        ResponseBody.create(MediaType.parse("text/plain"), response.message()))),
                                -1, null)
                    }
                }
                mBinding.loadingIndicator.loadingIndicator.visibility = View.GONE
                mBinding.swipeContainer.isRefreshing = false
            }

            override fun onFailure(call: Call<CourseDates>, t: Throwable) {
                context?.let { errorNotification.showError(it, t, -1, null) }
                mBinding.swipeContainer.isRefreshing = false
                mBinding.loadingIndicator.loadingIndicator.visibility = View.GONE
            }
        })
    }

    private fun populateCourseDates(list: List<CourseDateBlock>) {
        data = HashMap<String, ArrayList<CourseDateBlock>>()
        sortKeys = ArrayList()
        if (list.isNotEmpty()) {
            list.forEach { item ->
                if (data.containsKey(item.getSimpleDateTime())) {
                    (data[item.getSimpleDateTime()] as ArrayList).add(item)
                } else {
                    data[item.getSimpleDateTime()] = arrayListOf(item)
                    sortKeys.add(item.getSimpleDateTime())
                }
            }
            if (isContainToday(list).not() && DateUtil.isDatePast(sortKeys.first()) && DateUtil.isDateDue(sortKeys.last())) {
                var ind = 0
                sortKeys.forEachIndexed { index, str ->
                    if (index < sortKeys.lastIndex && DateUtil.isDatePast(str) && DateUtil.isDateDue(sortKeys[index + 1])) {
                        ind = index + 1
                    }
                }
                sortKeys.add(ind, getTodayDateBlock().getSimpleDateTime())

            }
            setDateBlockTag()
            mBinding.dateList.apply {
                layoutManager = LinearLayoutManager(context)
                adapter = CourseDatesAdapter(data, sortKeys, onLinkClick)
            }
        } else {
            context?.let {
                errorNotification.showError(it,
                        HttpStatusException(Response.error<Any>(HttpStatus.NO_CONTENT,
                                ResponseBody.create(MediaType.parse("text/plain"), "Currently no date available for this course"))),
                        -1, null)
            }
        }

    }

    private fun isContainToday(list: List<CourseDateBlock>): Boolean {
        list.forEach {
            if (it.isToday()) {
                return true
            }
        }
        return false
    }

    private fun setDateBlockTag() {
        var dueNextCount = 0
        sortKeys.forEach { key ->
            data[key]?.forEach { item ->
                var dateBlockTag: CourseDateType = getDateTypeTag(item)
                if (dateBlockTag.equals(CourseDateType.DUE_NEXT)) {
                    if (dueNextCount == 0)
                        dueNextCount += 1
                    else
                        dateBlockTag = CourseDateType.BLANK
                }
                item.dateBlockTag = dateBlockTag
            }
        }
    }

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
