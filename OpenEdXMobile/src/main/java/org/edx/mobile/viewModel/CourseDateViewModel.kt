package org.edx.mobile.viewModel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import okhttp3.MediaType
import okhttp3.ResponseBody
import org.edx.mobile.course.CourseAPI
import org.edx.mobile.http.HttpStatusException
import org.edx.mobile.model.course.CourseDates
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class CourseDateViewModel(private val courseAPI: CourseAPI) : ViewModel() {

    private val _showLoader = MutableLiveData<Boolean>()
    val showLoader: LiveData<Boolean>
        get() = _showLoader

    private val _swipeRefresh = MutableLiveData<Boolean>()
    val swipeRefresh: LiveData<Boolean>
        get() = _swipeRefresh

    private val _courseDates = MutableLiveData<CourseDates>()
    val courseDates: LiveData<CourseDates>
        get() = _courseDates

    private val _errorMessage = MutableLiveData<Throwable>()
    val errorMessage: LiveData<Throwable>
        get() = _errorMessage

    private lateinit var courseID: String

    fun startViewModel(courseID: String) {
        this.courseID = courseID
        fetchCourseDates()
    }

    fun fetchCourseDates() {
        _errorMessage.value = null
        _swipeRefresh.value = false
        _showLoader.value = true
        courseAPI.getCourseDates(courseID).enqueue(object : Callback<CourseDates> {
            override fun onResponse(call: Call<CourseDates>, response: Response<CourseDates>) {
                _showLoader.postValue(false)
                if (response.isSuccessful && response.body() != null) {
                    response.body()?.let {
                        _courseDates.value = it
                    }
                } else {
                    _errorMessage.value = HttpStatusException(Response.error<Any>(response.code(),
                            ResponseBody.create(MediaType.parse("text/plain"), response.message())))
                }
                _swipeRefresh.postValue(false)
            }

            override fun onFailure(call: Call<CourseDates>, t: Throwable) {
                _showLoader.postValue(false)
                _errorMessage.value = t
                _swipeRefresh.postValue(false)
            }

        })
    }

    fun setError(code: Int, msg: String) {
        _errorMessage.value = HttpStatusException(Response.error<Any>(code,
                ResponseBody.create(MediaType.parse("text/plain"), msg)))
    }
}
