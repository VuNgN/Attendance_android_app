package com.vungn.attendancedemo.data.service

import com.vungn.attendancedemo.model.server.AttendClass
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.POST

interface AttendanceService {
//    @GET("class/{id}")
//    fun clazz(@Path("id") uuid: String): Call<Clazz>

    @POST("class")
    fun attend(@Body attendClass: AttendClass): Call<Unit>

    @POST("class")
    fun attendAll(@Body attendClasses: List<AttendClass>): Call<Unit>
}
