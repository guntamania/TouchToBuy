package com.guntamania.touchtobuy.viewmodel

import androidx.databinding.ObservableField
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.guntamania.touchtobuy.repository.CoincheckRepository
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.rxkotlin.addTo
import io.reactivex.schedulers.Schedulers
import retrofit2.HttpException

class CoincheckViewModel(
    private val repository: CoincheckRepository,
    val rateTextObseravable: ObservableField<String>
) : ViewModel() {

    val status = MutableLiveData<String>()
    val buyAmount = MutableLiveData<String>()
    val buyPrice = MutableLiveData<String>()
    val sellAmount = MutableLiveData<String>()
    val sellPrice = MutableLiveData<String>()

    private val compositeDisposable: CompositeDisposable = CompositeDisposable()

    fun load() {
        repository.createTickerObservable()
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({ ticker ->
                rateTextObseravable.set(ticker.last.toString())
            }, { error ->
                android.util.Log.d("GNTM", "ERROD:" + error.message)
            }).addTo(compositeDisposable)
    }

    fun onClickBuyButton() {
        val amount = buyAmount.value?.let { java.lang.Double.parseDouble(it) }
        val price = buyPrice.value?.let { java.lang.Long.parseLong(it) }
        repository.postExchange(amount!!, "buy", price!!)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({ orderData ->
                status.value = "注文成功"
            }, { error ->
                if (error is HttpException) {
                    status.value = "通信エラー：" + error.message
                } else {
                    status.value = "エラー" + error.toString() + error.message
                }
            })
    }

    fun onClickSellButton() {
        val amount = sellAmount.value?.let { java.lang.Double.parseDouble(it) }
        val price = sellPrice.value?.let { java.lang.Long.parseLong(it) }
        repository.postExchange(amount!!, "sell", price!!)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({ orderData ->
                status.value = "注文成功"
            }, { error ->
                if (error is HttpException) {
                    status.value = "通信エラー：" + error.message
                } else {
                    status.value = "エラー" + error.toString() + error.message
                }
            })
    }

    override fun onCleared() {
        super.onCleared()
        compositeDisposable.clear()
    }
}