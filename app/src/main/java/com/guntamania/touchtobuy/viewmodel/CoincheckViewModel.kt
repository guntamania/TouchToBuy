package com.guntamania.touchtobuy.viewmodel

import android.app.Application
import android.view.View
import androidx.databinding.ObservableField
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.guntamania.touchtobuy.R
import com.guntamania.touchtobuy.activity.MainActivity
import com.guntamania.touchtobuy.repository.CoincheckRepository
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.rxkotlin.addTo
import io.reactivex.schedulers.Schedulers
import retrofit2.HttpException

class CoincheckViewModel(
    private val repository: CoincheckRepository,
    val rateTextObseravable: ObservableField<String>,
    private val app: Application
) : AndroidViewModel(app) {

    val status = MutableLiveData<String>()
    val buyAmount = MutableLiveData<String>()
    val buyPrice = MutableLiveData<String>()
    val sellAmount = MutableLiveData<String>()
    val sellPrice = MutableLiveData<String>()
    val buyCurrencySpinnerPosition = MutableLiveData<Int>()
    val sellCurrencySpinnerPosition = MutableLiveData<Int>()
    val limitedBuyCheckBox = MutableLiveData<Boolean>()
    val limitedSellCheckBox = MutableLiveData<Boolean>()

    private val compositeDisposable: CompositeDisposable = CompositeDisposable()

    fun load() {
        repository.createTickerObservable()
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({ ticker ->
                rateTextObseravable.set(ticker.last.toString())
            }, { error ->
                status.value = "エラー：" + error.message
            }).addTo(compositeDisposable)
        buyCurrencySpinnerPosition.value = 1
    }

    fun onClickBuyButton() {
        var amount = buyAmount.value?.let { java.lang.Double.parseDouble(it) }
        val price = if (limitedBuyCheckBox.value == true) {
            buyPrice.value?.let { java.lang.Long.parseLong(it) }
        } else {
            rateTextObseravable.get()?.toLong()
        }
        val currency = buyCurrencySpinnerPosition.value
        if (currency == 1) {
            price?.let {
                amount = (amount ?: 0.0) / it.toDouble()
            }
        }
        repository.postExchange(amount!!, "buy", price!!)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({ orderData ->
                status.value =
                    if (limitedBuyCheckBox.value == true) "指値注文成功"
                    else  "成行注文成功"
            }, { error ->
                if (error is HttpException) {
                    status.value = "通信エラー：" + error.message
                } else {
                    status.value = "エラー" + error.toString() + error.message
                }
            }).addTo(compositeDisposable)
    }

    fun onClickSellButton() {
        var amount = sellAmount.value?.let { java.lang.Double.parseDouble(it) }
        val price = if(limitedSellCheckBox.value == true) {
            sellPrice.value?.let { java.lang.Long.parseLong(it) }
        }else {
            rateTextObseravable.get()?.toLong()
        }
        val currency = sellCurrencySpinnerPosition.value
        if (currency == 1) {
            price?.let {
                amount = (amount ?: 0.0) / it.toDouble()
            }
        }
        repository.postExchange(amount!!, "sell", price!!)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({ orderData ->
                status.value =
                    if (limitedSellCheckBox.value == true) "指値注文成功"
                    else  "成行注文成功"
            }, { error ->
                if (error is HttpException) {
                    status.value = "通信エラー：" + error.message
                } else {
                    status.value = "エラー" + error.toString() + error.message
                }
            }).addTo(compositeDisposable)
    }

    override fun onCleared() {
        super.onCleared()
        compositeDisposable.clear()
    }
}