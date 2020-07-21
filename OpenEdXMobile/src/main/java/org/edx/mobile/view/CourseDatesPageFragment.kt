package org.edx.mobile.view

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
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
import org.edx.mobile.view.adapters.CourseDatesAdapter
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import javax.inject.Inject


class CourseDatesPageFragment : OfflineSupportBaseFragment() {

    @Inject
    var api: CourseAPI? = null
    private var errorNotification: FullScreenErrorNotification? = null

    private lateinit var mBinding: FragmentCourseDatesPageBinding
    private var list: MutableList<CourseDateBlock> = arrayListOf<CourseDateBlock>()
    private var data: HashMap<String, ArrayList<CourseDateBlock>> = HashMap()
    private var sortKeys: ArrayList<String> = ArrayList()
    private var onLinkClick: OnDateBlockListener = object : OnDateBlockListener {
        override fun onClick(link: String) {
            Toast.makeText(context, link, Toast.LENGTH_SHORT).show()
            BrowserUtil.open(activity, link)
        }
    }

    companion object {
        @JvmStatic
        open fun makeArguments(courseId: String?): Bundle? {
            val courseBundle = Bundle()
            courseBundle.putString(Router.EXTRA_COURSE_ID, courseId)
            return courseBundle
        }
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
            errorNotification?.hideError()
        })

        var courseDates: Call<CourseDates> = api?.getCourseDates(arguments?.getString(Router.EXTRA_COURSE_ID)!!)!!
        courseDates.enqueue(object : Callback<CourseDates> {
            override fun onResponse(call: Call<CourseDates>, response: Response<CourseDates>) {
                if (response.isSuccessful) {
                    response.body()?.course_date_blocks?.let {
                        setData(it)
                        mBinding.dateList.apply {
                            layoutManager = LinearLayoutManager(context)
                            adapter = CourseDatesAdapter(data, sortKeys, onLinkClick)
                        }
                    }
                } else {
                    errorNotification!!.showError(context!!,
                            HttpStatusException(Response.error<Any>(response.code(),
                                    ResponseBody.create(MediaType.parse("text/plain"), response.message()))),
                            -1, null)
                }
            }

            override fun onFailure(call: Call<CourseDates>, t: Throwable) {
                errorNotification!!.showError(context!!, t, -1, null)
            }
        })
    }

    private fun setData(list: List<CourseDateBlock>) {
        data = HashMap<String, ArrayList<CourseDateBlock>>()
        sortKeys = ArrayList()
        if (list.isNotEmpty()) {
            list.forEach { item ->
                if (data.containsKey(item.getDate())) {
                    (data[item.getDate()] as ArrayList).add(item)
                } else {
                    data[item.getDate()] = arrayListOf(item)
                    sortKeys.add(item.getDate())
                }

//                if(data.containsKey(item.getDate())){
//                    (data[item.getDate()] as ArrayList).add(item)
//                }else{
//                    var temp: ArrayList<CourseDateBlock> = arrayListOf(item)
//                    data[item.getDate()] = temp
//                }

            }
        }
    }
}
