package com.guntamania.touchtobuy.activity

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.guntamania.touchtobuy.CoincheckViewModelFactory
import com.guntamania.touchtobuy.R
import com.guntamania.touchtobuy.databinding.ActivityMainBinding
import com.guntamania.touchtobuy.dialog.TradeDialogFragment
import com.guntamania.touchtobuy.repository.CoincheckRepository
import com.guntamania.touchtobuy.viewmodel.CoincheckViewModel
import io.reactivex.Single
import io.reactivex.disposables.CompositeDisposable


class MainActivity : AppCompatActivity() {

    val compositeDisposable: CompositeDisposable = CompositeDisposable()
    lateinit var repository: CoincheckRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val binding =
            DataBindingUtil.setContentView<ActivityMainBinding>(this, R.layout.activity_main)
        repository = CoincheckRepository(this)
        val viewModel = obtainViewModel(repository).apply {
            openBuyDialogEvent.observe(this@MainActivity, Observer { e ->
                e.getContentIfNotHandled()?.let { pair ->
                    showBuyDialog(pair.first, pair.second).subscribe { confirmed ->
                        if (confirmed) {
                            onBuyConfirmed(pair.first, pair.second)
                        }
                    }
                }
            })
            openSellDialogEvent.observe(this@MainActivity, Observer { e ->
                e.getContentIfNotHandled()?.let {pair ->
                    showSellDialog(pair.first, pair.second).subscribe{ confirmed ->
                        if(confirmed) {
                            onSellConfirmed(pair.first, pair.second)
                        }
                    }
                }
            })
        }
        binding.viewmodel = viewModel
        binding.setLifecycleOwner(this)
        viewModel.status.observe(this, Observer { status ->
            Toast.makeText(this@MainActivity, status, Toast.LENGTH_LONG).show()
        })
        viewModel.load()
    }

    override fun onResume() {
        super.onResume()
        updateStatus()
    }

    override fun onDestroy() {
        super.onDestroy()
        compositeDisposable.dispose()
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        if (item?.itemId == R.id.addApi) {
            val intent = Intent(this, AddApiActivity::class.java)
            startActivity(intent)
            return true
        }
        return true
    }

    private fun showBuyDialog(price: Long, amount: Double): Single<Boolean> =
        TradeDialogFragment.Builder()
            .apply {
                title = "buy $amount by $price Yen"
                message = "are you sure?"
                cancelable = false
                positiveButtonText = "Buy"
                negativeButtonText = "Cancel"
            }
            .create(this)
            .showWithObservable(this.supportFragmentManager, "BUY_TAG")

    private fun showSellDialog(price: Long, amount: Double): Single<Boolean> =
        TradeDialogFragment.Builder()
            .apply {
                title = "sell $amount by $price Yen"
                message = "are you sure?"
                cancelable = false
                positiveButtonText = "Sell"
                negativeButtonText = "Cancel"
            }
            .create(this)
            .showWithObservable(this.supportFragmentManager, "SELL_TAG")

    private fun updateStatus() {
//        if (apiKey() == null || secretKey() == null) {
//            rateTextView.text = "SET KEYS FIRST"
//            return
//        }
//        Observables.zip(
//            client().create(TickerApi::class.java).ticker(),
//            client().create(AccountApi::class.java).balance()
//        ) { r1, r2 -> Pair(r1, r2) }
//            .observeOn(AndroidSchedulers.mainThread())
//            .subscribeOn(Schedulers.newThread())
//            .doOnSubscribe { rateTextView.text = "Loading.." }
//            .subscribe({ (t, b) ->
//                rateTextView.text = t.last.toString()
//                balanceJpyTextView.text = b.jpy.toString()
//                balanceBtcTextView.text = b.btc.toString()
//                lastAsk = t.ask
//                lastBid = t.bid
//                rateSmallTextView.text = round(SMALL_YEN / t.ask).toString() + " BTC"
//                rateBigTextView.text = round(BIG_YEN / t.ask).toString() + " BTC"
//                rateSellSmallTextView.text = round(SMALL_BTC * t.bid).toString() + " YEN"
//                rateSellBigTextView.text = round(BIG_BTC * t.bid).toString() + " YEN"
//            }, { e ->
//                //rateTextView.text = "Error"
//                Toast.makeText(this, e.localizedMessage, Toast.LENGTH_LONG).show()
//            }).addTo(compositeDisposable)
    }

    private fun obtainViewModel(repo: CoincheckRepository): CoincheckViewModel =
        ViewModelProviders.of(
            this,
            CoincheckViewModelFactory.getInstance(this.application, repo)
        ).get(
            CoincheckViewModel::class.java
        )

}
