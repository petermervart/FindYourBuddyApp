package com.example.testing
import okhttp3.Credentials
import okhttp3.Interceptor

class Authorization(name: String, password: String) : Interceptor
    {
        private var credentials: String = Credentials.basic(name, password)
        override fun intercept(chain: Interceptor.Chain): okhttp3.Response {
            var request = chain.request()
            request = request.newBuilder().header("Authorization", credentials).build()
            return chain.proceed(request)
        }
    }