package org.edx.mobile.http.model

sealed class Result<out R> {
    data class Success<out T>(val isSuccessful: Boolean, val data: T?, val code: Int, val message: String) : Result<T>()
    data class Error(val throwable: Throwable) : Result<Nothing>()
}

interface NetworkResponseCallback<T>{
    fun onSuccess(result: Result.Success<T>)
    fun onError(error: Result.Error)
}
