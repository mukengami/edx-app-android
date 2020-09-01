package org.edx.mobile.viewModel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import org.edx.mobile.course.CourseAPI

class ViewModelFactory(private val courseAPI: CourseAPI) : ViewModelProvider.NewInstanceFactory() {

    override fun <T : ViewModel?> create(modelClass: Class<T>): T =
            when (modelClass) {
                CourseDateViewModel::class.java -> CourseDateViewModel(courseAPI = courseAPI) as T
                else -> throw IllegalArgumentException("Class doesn't exist in ViewModelFactory")
            }
}
