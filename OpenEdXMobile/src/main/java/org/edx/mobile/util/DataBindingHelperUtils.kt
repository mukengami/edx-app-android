package org.edx.mobile.util

import android.content.Context
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.core.widget.TextViewCompat
import androidx.databinding.BindingAdapter
import com.joanzapata.iconify.IconDrawable
import com.joanzapata.iconify.fonts.FontAwesomeIcons
import kotlinx.android.synthetic.main.sub_item_course_date_block.view.*
import org.edx.mobile.R
import org.edx.mobile.interfaces.OnDateBlockListener
import org.edx.mobile.model.course.CourseDateBlock
import org.edx.mobile.view.adapters.PopularSubjectsAdapter

class DataBindingHelperUtils {

    companion object {
        @JvmStatic
        @BindingAdapter("binding:isUserHasAccess")
        fun isViewAccessible(view: View, type: CourseDateType?) {
            when (type) {
                CourseDateType.VERIFIED_ONLY,
                CourseDateType.NOT_YET_RELEASED ->
                    view.isEnabled = false
                else ->
                    view.isEnabled = true
            }
        }

        @JvmStatic
        @BindingAdapter("binding:isVisible")
        fun isViewVisible(textView: TextView, isVisible: Boolean) {
            textView.visibility = if (isVisible) View.VISIBLE else View.INVISIBLE
        }

        @JvmStatic
        @BindingAdapter("binding:isVisible")
        fun isViewVisible(imageView: ImageView, isVisible: Boolean) {
            imageView.visibility = if (isVisible) View.VISIBLE else View.INVISIBLE
        }

        @JvmStatic
        @BindingAdapter("binding:addView", "binding:clickListener", requireAll = true)
        fun addView(linearLayout: LinearLayout, list: ArrayList<CourseDateBlock>, clickListener: OnDateBlockListener) {
            val inflater: LayoutInflater = linearLayout.context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
            list.forEach { item ->
                val childView = inflater.inflate(R.layout.sub_item_course_date_block, null)
                childView.title.text = item.title
                isViewAccessible(childView.title, item.getDateTypeTag())
                childView.description.text = item.description
                isViewAccessible(childView.description, item.getDateTypeTag())
                linearLayout.addView(childView)
                childView.setOnClickListener{
                    clickListener.onClick(item.link)
                }
            }
        }

        @JvmStatic
        @BindingAdapter("binding:dotBackground")
        fun setDotBackground(dotView: ImageView, type: CourseDateType?) {
            dotView.bringToFront()
            when (type) {
                CourseDateType.COMPLETED -> {
                    dotView.background = ContextCompat.getDrawable(dotView.context, R.drawable.black_border_white_circle)
                }
                CourseDateType.PAST_DUE -> {
                    dotView.background = ContextCompat.getDrawable(dotView.context, R.drawable.black_border_gray_circle)
                }
                CourseDateType.BLANK,
                CourseDateType.DUE_NEXT,
                CourseDateType.NOT_YET_RELEASED,
                CourseDateType.VERIFIED_ONLY -> {
                    dotView.background = ContextCompat.getDrawable(dotView.context, R.drawable.black_circle)
                }
            }
        }

        @JvmStatic
        @BindingAdapter("binding:dateBackground")
        fun setDateBackground(textView: TextView, type: CourseDateType?) {
            textView.text = type?.getTitle()
            textView.visibility = View.VISIBLE
            when (type) {
                CourseDateType.TODAY -> {
                    textView.setTextColor(Color.BLACK)
                    textView.background = ContextCompat.getDrawable(textView.context, R.drawable.yellow_roundedbg)
                }
                CourseDateType.VERIFIED_ONLY -> {
                    textView.setTextColor(Color.WHITE)
                    textView.background = ContextCompat.getDrawable(textView.context, R.drawable.black_roundedbg)
                    TextViewCompat.setCompoundDrawablesRelativeWithIntrinsicBounds(textView,
                            IconDrawable(textView.context, FontAwesomeIcons.fa_lock)
                                    .sizeRes(textView.context, R.dimen.small_icon_size)
                                    .colorRes(textView.context, R.color.white),
                            null, null, null
                    )
                }
                CourseDateType.COMPLETED -> {
                    textView.setTextColor(Color.DKGRAY)
                    textView.background = ContextCompat.getDrawable(textView.context, R.drawable.light_silver_roundedbg)
                }
                CourseDateType.PAST_DUE -> {
                    textView.setTextColor(Color.DKGRAY)
                    textView.background = ContextCompat.getDrawable(textView.context, R.drawable.light_grey_roundedbg)
                }
                CourseDateType.DUE_NEXT -> {
                    textView.setTextColor(Color.WHITE)
                    textView.background = ContextCompat.getDrawable(textView.context, R.drawable.dark_grey_roundedbg)
                }
                CourseDateType.NOT_YET_RELEASED -> {
                    textView.setTextColor(Color.GRAY)
                    textView.background = ContextCompat.getDrawable(textView.context, R.drawable.silver_border_transparent_roundedbg)
                }
                else -> {
                    textView.visibility = View.INVISIBLE
                }
            }
        }
    }
}
