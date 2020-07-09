package org.edx.mobile.view

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import org.edx.mobile.R
import org.edx.mobile.databinding.FragmentCourseDatesPageBinding
import org.edx.mobile.model.course.CourseDateBlock
import org.edx.mobile.view.adapters.CourseDatesAdapter

class CourseDatesPageFragment : OfflineSupportBaseFragment() {

    private lateinit var mBinding: FragmentCourseDatesPageBinding
    private var list: MutableList<CourseDateBlock> = arrayListOf<CourseDateBlock>()

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

        mBinding.dateList.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = CourseDatesAdapter(list)
        }
    }
}
