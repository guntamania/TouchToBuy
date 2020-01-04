package com.guntamania.touchtobuy.viewmodel

import android.app.Application
import android.view.View
import androidx.databinding.ObservableField
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import com.guntamania.touchtobuy.Event
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
    val openBuyDialogEvent = MutableLiveData<Event<Pair<Long, Double>>>()
    val openSellDialogEvent = MutableLiveData<Event<Pair<Long, Double>>>()

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

    fun onClickBuyButton(view: View) {
        var amount = buyAmount.value?.let { java.lang.Double.parseDouble(it) } ?: return
        val price = if (limitedBuyCheckBox.value == true) {
            buyPrice.value?.let { java.lang.Long.parseLong(it) }
        } else {
            rateTextObseravable.get()?.toDouble()?.toLong()
        } ?: return
        val currency = buyCurrencySpinnerPosition.value
        if (currency == 1) {
            amount /= price.toDouble()
        }
        openBuyDialogEvent.value = Event(Pair(price, amount))
    }

    fun onClickSellButton() {
        var amount = sellAmount.value?.let { java.lang.Double.parseDouble(it) } ?: return
        val price = if (limitedSellCheckBox.value == true) {
            sellPrice.value?.let { java.lang.Long.parseLong(it) }
        } else {
            rateTextObseravable.get()?.toDouble()?.toLong()
        } ?: return
        val currency = sellCurrencySpinnerPosition.value
        if (currency == 1) {
            amount /= price.toDouble()
        }
        openSellDialogEvent.value = Event(Pair(price, amount))
    }

    fun onBuyConfirmed(price: Long, amount: Double) {
        repository.postExchange(amount, "buy", price)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({
                status.value =
                    if (limitedBuyCheckBox.value == true) "指値注文成功"
                    else "成行注文成功"
            }, { error ->
                if (error is HttpException) {
                    status.value = "通信エラー：" + error.message
                } else {
                    status.value = "エラー" + error.toString() + error.message
                }
            }).addTo(compositeDisposable)
    }

    fun onSellConfirmed(price: Long, amount: Double) {
        repository.postExchange(amount, "sell", price)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({
                status.value =
                    if (limitedSellCheckBox.value == true) "指値注文成功"
                    else "成行注文成功"
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