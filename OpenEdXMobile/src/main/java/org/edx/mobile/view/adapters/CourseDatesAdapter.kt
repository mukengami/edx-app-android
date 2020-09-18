package org.edx.mobile.view.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import org.edx.mobile.R
import org.edx.mobile.databinding.ItemCourseDateBlockBinding
import org.edx.mobile.interfaces.OnDateBlockListener
import org.edx.mobile.model.course.CourseDateBlock
import java.util.*

class CourseDatesAdapter(private val data: HashMap<String, ArrayList<CourseDateBlock>>,
                         private val keys: ArrayList<String>,
                         private val onDateItemClick: OnDateBlockListener
) : RecyclerView.Adapter<CourseDatesAdapter.CourseDateHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CourseDatesAdapter.CourseDateHolder {
        val inflater = LayoutInflater.from(parent.context)
        val inflatedBinding = DataBindingUtil.inflate<ItemCourseDateBlockBinding>(
                inflater, R.layout.item_course_date_block, parent, false)
        return CourseDateHolder(inflatedBinding, onDateItemClick)
    }

    override fun getItemCount(): Int {
        return keys.size
    }

    override fun onBindViewHolder(holder: CourseDateHolder, position: Int) {
        if (data.isNotEmpty()) {
            when (position) {
                0 -> {
                    holder.binding.lineAboveDot.visibility = View.INVISIBLE
                    holder.binding.lineBelowDot.visibility = View.VISIBLE
                }
                (itemCount - 1) -> {
                    holder.binding.lineAboveDot.visibility = View.VISIBLE
                    holder.binding.lineBelowDot.visibility = View.INVISIBLE
                }
                else -> {
                    holder.binding.lineAboveDot.visibility = View.VISIBLE
                    holder.binding.lineBelowDot.visibility = View.VISIBLE
                }
            }
            if (data.size == 1) {
                holder.binding.lineAboveDot.visibility = View.INVISIBLE
                holder.binding.lineBelowDot.visibility = View.INVISIBLE
            }
            val key = keys[position]
            if (key.equals(CourseDateBlock.getTodayDateBlock().getSimpleDateTime(), ignoreCase = true) && data[key].isNullOrEmpty()) {
                holder.bind(CourseDateBlock.getTodayDateBlock(), arrayListOf())
            } else {
                holder.bind(data[key]?.first(), data[key])
            }
        }
    }

    class CourseDateHolder(var binding: ItemCourseDateBlockBinding, private val onDateItemClick: OnDateBlockListener) : RecyclerView.ViewHolder(binding.root) {
        fun bind(item: CourseDateBlock?, list: ArrayList<CourseDateBlock>?) {
            binding.dateBlock = item
            binding.bulletToday.bringToFront()
            binding.dateBlockList = if (list.isNullOrEmpty().not()) list else arrayListOf()
            binding.dateItemListener = onDateItemClick
        }
    }
}
