package com.guntamania.touchtobuy.repository

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.google.gson.FieldNamingPolicy
import com.google.gson.GsonBuilder
import com.guntamania.touchtobuy.R
import com.guntamania.touchtobuy.api.AccountApi
import com.guntamania.touchtobuy.api.ExchangeApi
import com.guntamania.touchtobuy.api.TickerApi
import com.guntamania.touchtobuy.data.BalanceData
import com.guntamania.touchtobuy.data.TickerData
import io.reactivex.Observable
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import org.apache.commons.codec.binary.Hex
import org.apache.commons.codec.digest.DigestUtils
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory
import java.security.InvalidKeyException
import java.security.NoSuchAlgorithmException
import java.util.*
import javax.crypto.Mac
import javax.crypto.spec.SecretKeySpec


class CoincheckRepository(
    private val context: Context
) {

    private var accountCache: BalanceData? = null
    private var tickerCache: TickerData? = null

    private fun accountApi() = client().create(AccountApi::class.java)
    private fun tickerApi() = client().create(TickerApi::class.java)
    private fun exchangeApi() = client().create(ExchangeApi::class.java)

    fun getAccount(): LiveData<BalanceData> {
        val data = MutableLiveData<BalanceData>()
        accountCache?.let { data.value = it }
        accountApi().balance().subscribe { response -> data.value = response }
        return data
    }

    fun postExchange(amount: Double, orderType: String, rate: Long) =
        exchangeApi().orders("btc_jpy", orderType, rate, amount)

    fun postMarketOrder(amount: Double, orderType: String) = exchangeApi().orders("btc_jpy", orderType, null, amount)

    fun createTickerObservable() = Observable.create<TickerData> { emitter ->
        tickerCache?.let { emitter.onNext(it) }
        tickerApi().ticker()
            .subscribe({ response ->
                emitter.onNext(response)
                emitter.onComplete()
            }, { error -> if (!emitter.isDisposed) emitter.onError(error) })
    }

    private fun apiKey() =
        context.getSharedPreferences(context.getString(R.string.app_name), Context.MODE_PRIVATE)
            .getString("api_key", null)

    private fun secretKey() =
        context.getSharedPreferences(context.getString(R.string.app_name), Context.MODE_PRIVATE)
            .getString("secret_key", null)

    private fun client() =
        Retrofit.Builder()
            .client(httpClient())
            .addConverterFactory(GsonConverterFactory.create(gson))
            .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
            .baseUrl("https://coincheck.com")
            .build()

    private fun httpClient() =
        OkHttpClient.Builder().addInterceptor { chain ->
            val request = chain.request().newBuilder().apply {
                createHeader(chain.request().url().toString()).forEach {
                    addHeader(it.key, it.value)
                }
            }.build()
            chain.proceed(request)
        }
            .addInterceptor(HttpLoggingInterceptor().setLevel(HttpLoggingInterceptor.Level.BODY))
            .build()

    private val gson by lazy {
        GsonBuilder().setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES).create()
    }

    private fun createHeader(url: String): Map<String, String> =
        HashMap<String, String>().apply {
            val nonce = createNonce()
            put("ACCESS-KEY", apiKey())
            put("ACCESS-NONCE", nonce)
            put("ACCESS-SIGNATURE", createSignature(secretKey(), url, nonce))
        }

    fun getRate(): LiveData<TickerData> {
        val data = MutableLiveData<TickerData>()
        client().create(TickerApi::class.java).ticker().subscribe { t -> data.postValue(t) }
        return data
    }

    private fun createSignature(apiSecret: String, url: String, nonce: String): String {
        val message = nonce + url
        return HMAC_SHA256Encode(apiSecret, message)
    }

    private fun createNonce() = (System.currentTimeMillis().toString())

    private fun HMAC_SHA256Encode(secretKey: String, message: String): String {

        val keySpec = SecretKeySpec(
            secretKey.toByteArray(),
            "hmacSHA256"
        )

        val mac: Mac =
            try {
                Mac.getInstance("hmacSHA256").apply {
                    init(keySpec)
                }
            } catch (e: NoSuchAlgorithmException) {
                // can't recover
                throw RuntimeException(e)
            } catch (e: InvalidKeyException) {
                // can't recover
                throw RuntimeException(e)
            }

        val rawHmac = mac.doFinal(message.toByteArray())
        val sha256 = DigestUtils.sha256(rawHmac)
        val hexChars = Hex.encodeHex(rawHmac)
        return String(hexChars)
        //return Hex.encodeHexString(rawHmac)
    }

}