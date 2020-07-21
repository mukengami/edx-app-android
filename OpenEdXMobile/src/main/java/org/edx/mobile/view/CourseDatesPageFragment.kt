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
//            Toast.makeText(context, link, Toast.LENGTH_SHORT).show()
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
        val isContainToday = isContainToday(list)
        if (list.isNotEmpty()) {
            list.forEach { item ->
                if (data.containsKey(item.getSimpleDateTime())) {
                    (data[item.getSimpleDateTime()] as ArrayList).add(item)
                } else {
                    data[item.getSimpleDateTime()] = arrayListOf(item)
                    sortKeys.add(item.getSimpleDateTime())
                }
            }
        }
        if (isContainToday.not() && DateUtil.isDatePast(sortKeys.first()) && DateUtil.isDateDue(sortKeys.last())) {
            var ind = 0
            sortKeys.forEachIndexed { index, str ->
                if (index < sortKeys.lastIndex && DateUtil.isDatePast(str) && DateUtil.isDateDue(sortKeys[index + 1])) {
                    ind = index + 1
                }
            }
            sortKeys.add(ind, getTodayDateBlock().getSimpleDateTime())

        }
        mBinding.dateList.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = CourseDatesAdapter(data, sortKeys, onLinkClick)
        }

    }

    private fun isContainToday(list: List<CourseDateBlock>): Boolean {
        var isContainToday = false
        list.forEach {
            if (it.isToday()) {
                isContainToday = true
            }
        }
        return isContainToday
    }
}
