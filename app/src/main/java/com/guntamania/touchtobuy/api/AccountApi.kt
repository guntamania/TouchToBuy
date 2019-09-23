package com.guntamania.touchtobuy.api

import com.guntamania.touchtobuy.data.BalanceData
import io.reactivex.Observable
import retrofit2.http.GET
import retrofit2.http.HeaderMap

interface AccountApi {
    @GET("/api/accounts/balance")
    fun balance() : Observable<BalanceData>
}