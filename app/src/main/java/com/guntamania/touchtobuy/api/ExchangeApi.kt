package com.guntamania.touchtobuy.api

import com.guntamania.touchtobuy.data.OrderData
import io.reactivex.Observable
import retrofit2.http.POST
import retrofit2.http.Query

interface ExchangeApi {
    @POST("/api/exchange/orders")
    fun orders(@Query("pair") pair: String,
               @Query("order_type") orderType: String,
               @Query("rate") rate: Long?,
               @Query("amount") amount: Double) : Observable<OrderData>
}