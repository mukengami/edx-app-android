package org.edx.mobile.repositorie

import org.edx.mobile.course.CourseAPI
import org.edx.mobile.http.model.NetworkResponseCallback
import org.edx.mobile.http.model.Result
import org.edx.mobile.model.course.CourseDates
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class CourseDatesRepository(val courseAPI: CourseAPI) {
    companion object {
        private var instance: CourseDatesRepository? = null
        fun getInstance(courseAPI: CourseAPI): CourseDatesRepository {
            instance?.let { it ->
                return it
            }
            val repository = CourseDatesRepository(courseAPI = courseAPI)
            instance = repository
            return repository
        }
    }

    fun getCourseDates(courseId: String, callback: NetworkResponseCallback<CourseDates>) {
        courseAPI.getCourseDates(courseId).enqueue(object : Callback<CourseDates> {
            override fun onResponse(call: Call<CourseDates>, response: Response<CourseDates>) {
                callback.onSuccess(Result.Success<CourseDates>(isSuccessful = response.isSuccessful,
                        data = response.body(),
                        code = response.code(),
                        message = response.message()))
            }

            override fun onFailure(call: Call<CourseDates>, t: Throwable) {
                callback.onError(Result.Error(t))
            }
        })
    }
}
