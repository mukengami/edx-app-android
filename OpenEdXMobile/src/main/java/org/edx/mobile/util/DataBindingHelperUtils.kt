package org.edx.mobile.util

import android.graphics.Color
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.core.widget.TextViewCompat
import androidx.databinding.BindingAdapter
import com.joanzapata.iconify.IconDrawable
import com.joanzapata.iconify.fonts.FontAwesomeIcons
import org.edx.mobile.R

class DataBindingHelperUtils {

    companion object {
        @JvmStatic
        @BindingAdapter("binding:isEnable")
        fun isViewEnable(textView: TextView, isEnable: Boolean) {
            textView.isEnabled = isEnable
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
        @BindingAdapter("binding:dotBackground")
        fun setDotBackground(dotView: ImageView, type: CourseDateType) {
            dotView.bringToFront()
            when (type) {
                CourseDateType.COMPLETED -> {
                    dotView.background = ContextCompat.getDrawable(dotView.context, R.drawable.black_border_white_circle)
                }
                CourseDateType.PAST_DUE -> {
                    dotView.background = ContextCompat.getDrawable(dotView.context, R.drawable.black_border_gray_circle)
                }
                CourseDateType.COURSE_START_DATE,
                CourseDateType.DUE_NEXT,
                CourseDateType.NOT_YET_RELEASED,
                CourseDateType.COURSE_IN_PROGRESS,
                CourseDateType.VERIFIED_ONLY,
                CourseDateType.COURSE_END -> {
                    dotView.background = ContextCompat.getDrawable(dotView.context, R.drawable.black_circle)
                }
                else -> {
                    dotView.visibility = View.INVISIBLE
                }
            }
        }

        @JvmStatic
        @BindingAdapter("binding:tagBackground")
        fun setTagBackground(textView: TextView, type: CourseDateType) {
            textView.text = type.getTitle()
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
