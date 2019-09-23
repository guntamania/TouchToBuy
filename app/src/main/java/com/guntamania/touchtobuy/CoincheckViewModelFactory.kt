package com.guntamania.touchtobuy

import android.app.Application
import androidx.databinding.ObservableField
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.guntamania.touchtobuy.repository.CoincheckRepository
import com.guntamania.touchtobuy.viewmodel.CoincheckViewModel
import java.lang.IllegalArgumentException

class CoincheckViewModelFactory(private val repository: CoincheckRepository) : ViewModelProvider.NewInstanceFactory() {

    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(CoincheckViewModel::class.java)) {
            return CoincheckViewModel(repository, ObservableField("")) as T
        }
        throw IllegalArgumentException()
    }

    companion object {

        @Volatile
        private var INSTANCE: CoincheckViewModelFactory? = null

        fun getInstance(repository: CoincheckRepository) =
            INSTANCE ?: synchronized(CoincheckViewModelFactory::class.java) {
                INSTANCE ?: CoincheckViewModelFactory(repository)
                    .also { INSTANCE = it }
            }

        fun destroyInstance() {
            INSTANCE = null
        }
    }
}