package org.edx.mobile.view.adapters

import android.graphics.Color
import android.graphics.Typeface
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import org.edx.mobile.BR
import org.edx.mobile.R
import org.edx.mobile.databinding.ItemCourseDateBlockBinding
import org.edx.mobile.model.course.CourseDateBlock
import org.edx.mobile.util.CourseDateType

class CourseDatesAdapter(private val list: List<CourseDateBlock>) : RecyclerView.Adapter<CourseDatesAdapter.CourseDateHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CourseDatesAdapter.CourseDateHolder {
        val inflater = LayoutInflater.from(parent.context)
        val inflatedBinding = DataBindingUtil.inflate<ItemCourseDateBlockBinding>(inflater, R.layout.item_course_date_block, parent, false)
        return CourseDateHolder(inflatedBinding)
    }

    override fun getItemCount(): Int {
        return 10
    }

    override fun onBindViewHolder(holder: CourseDateHolder, position: Int) {
        var item = CourseDateBlock(date = "2020-07-01T08:33:26Z",
                date_type = CourseDateType.COURSE_START_DATE,
                description = "",
                learner_has_access = true,
                link = "",
                link_text = "",
                title = "Course Starts")
        if (itemCount == 1) {
            holder.binding.aboveLineDot.visibility = View.INVISIBLE
            holder.binding.belowLineDot.visibility = View.INVISIBLE
            holder.bind(null)
        } else {
            holder.binding.aboveLineDot.visibility = View.VISIBLE
            holder.binding.belowLineDot.visibility = View.VISIBLE
            item.isToday = false
            item.learner_has_access = true
            when (position) {
                0 -> {
                    holder.binding.aboveLineDot.visibility = View.INVISIBLE
                    holder.binding.belowLineDot.visibility = View.VISIBLE
                }
                1 -> {
                    item.date_type = CourseDateType.COMPLETED
                }
                2 -> {
                    item.date_type = CourseDateType.PAST_DUE
                }
                3 -> {
                    item.date_type = CourseDateType.COURSE_IN_PROGRESS
                }
                4 -> {
                    item.date_type = CourseDateType.TODAY
                    item.isToday = true
                }
                5 -> {
                    item.date_type = CourseDateType.DUE_NEXT
                }
                6 -> {
                    item.date_type = CourseDateType.VERIFIED_ONLY
                    item.learner_has_access = false
                }
                7 -> {
                    item.date_type = CourseDateType.COURSE_IN_PROGRESS
                }
                8 -> {
                    item.date_type = CourseDateType.NOT_YET_RELEASED
                    holder.binding.date.isEnabled = false
                    item.learner_has_access = false
                }
                (itemCount - 1) -> {
                    item.date_type = CourseDateType.COURSE_END
                    holder.binding.aboveLineDot.visibility = View.VISIBLE
                    holder.binding.belowLineDot.visibility = View.INVISIBLE
                }
                else -> {
                }
            }
            holder.bind(item)
        }
    }

    class CourseDateHolder(var binding: ItemCourseDateBlockBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(item: CourseDateBlock?) {
            binding.itemContainer.setBackgroundColor(Color.WHITE)
            binding.date.setTypeface(null, Typeface.BOLD)
            binding.title.setTypeface(null, Typeface.BOLD)
            binding.dateTag.setTypeface(null, Typeface.BOLD_ITALIC)
            binding.setVariable(BR.dateType,item)
        }
    }
}
