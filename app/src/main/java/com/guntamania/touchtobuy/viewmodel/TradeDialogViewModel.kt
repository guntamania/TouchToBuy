package com.guntamania.touchtobuy.viewmodel

import android.app.Application
import android.content.DialogInterface
import androidx.lifecycle.AndroidViewModel
import java.awt.font.TextAttribute

class TradeDialogViewModel(app: Application) : AndroidViewModel(app) {

    val params: MutableMap<String, Param> = mutableMapOf()

    data class Param(
        var positiveButtonText: CharSequence? = null,
        var negativeButtonText: CharSequence? = null
    )
}