package com.guntamania.touchtobuy.api

import com.guntamania.touchtobuy.data.TickerData
import io.reactivex.Observable
import retrofit2.http.GET

interface TickerApi {
    @GET("/api/ticker")
    fun ticker() : Observable<TickerData>
}