package org.edx.mobile.view

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import org.edx.mobile.R
import org.edx.mobile.course.CourseAPI
import org.edx.mobile.databinding.FragmentCourseDatesPageBinding
import org.edx.mobile.http.HttpStatus
import org.edx.mobile.http.notifications.FullScreenErrorNotification
import org.edx.mobile.interfaces.OnDateBlockListener
import org.edx.mobile.model.course.CourseDateBlock
import org.edx.mobile.util.BrowserUtil
import org.edx.mobile.util.DateUtil
import org.edx.mobile.util.UiUtil
import org.edx.mobile.view.adapters.CourseDatesAdapter
import org.edx.mobile.viewModel.CourseDateViewModel
import org.edx.mobile.viewModel.ViewModelFactory
import javax.inject.Inject


class CourseDatesPageFragment : OfflineSupportBaseFragment() {

    @Inject
    private lateinit var courseAPI: CourseAPI
    private lateinit var errorNotification: FullScreenErrorNotification

    private lateinit var binding: FragmentCourseDatesPageBinding
    private lateinit var viewModel: CourseDateViewModel
    private var onLinkClick: OnDateBlockListener = object : OnDateBlockListener {
        override fun onClick(link: String) {
            BrowserUtil.open(activity, link)
        }
    }

    companion object {
        @JvmStatic
        fun makeArguments(courseId: String): Bundle {
            val courseBundle = Bundle()
            courseBundle.putString(Router.EXTRA_COURSE_ID, courseId)
            return courseBundle
        }

        @JvmStatic
        fun getTodayDateBlock() = CourseDateBlock(date = DateUtil.getCurrentTimeStamp(), date_type = CourseDateBlock.DateTypes.TODAY_DATE)
    }

    override fun isShowingFullScreenError(): Boolean {
        return errorNotification.isShowing
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_course_dates_page, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel = ViewModelProvider(this, ViewModelFactory(courseAPI = courseAPI)).get(CourseDateViewModel::class.java)

        errorNotification = FullScreenErrorNotification(binding.swipeContainer)

        binding.swipeContainer.setOnRefreshListener {
            // Hide the progress bar as swipe layout has its own progress indicator
            binding.loadingIndicator.loadingIndicator.visibility = View.GONE
            errorNotification.hideError()
            viewModel.fetchCourseDates()
        }
        UiUtil.setSwipeRefreshLayoutColors(binding.swipeContainer)
        viewModel.startViewModel(courseID = getArgumentString(Router.EXTRA_COURSE_ID))
        initObserver()
    }

    private fun initObserver() {
        viewModel.showLoader.observe(this, Observer { showLoader ->
            binding.loadingIndicator.loadingIndicator.showProgress(showLoader)
        })

        viewModel.courseDates.observe(this, Observer { dates ->
            if (dates.course_date_blocks.isNullOrEmpty()) {
                viewModel.setError(HttpStatus.NO_CONTENT, getString(R.string.course_dates_unavailable_message))
            } else {
                dates.populateCourseDates()
                binding.rvDates.apply {
                    layoutManager = LinearLayoutManager(context)
                    adapter = CourseDatesAdapter(dates.data, dates.sortKeys, onLinkClick)
                }
            }
        })

        viewModel.errorMessage.observe(this, Observer { throwable ->
            if (throwable != null) {
                errorNotification.showError(contextOrThrow, throwable, -1, null)
            }
        })

        viewModel.swipeRefresh.observe(this, Observer { enableSwipeListener ->
            binding.swipeContainer.isRefreshing = enableSwipeListener
        })
    }
}
