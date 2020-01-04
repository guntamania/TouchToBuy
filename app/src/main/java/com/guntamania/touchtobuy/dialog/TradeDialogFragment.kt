package com.guntamania.touchtobuy.dialog

import android.app.AlertDialog
import android.app.Dialog
import android.os.Bundle
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.FragmentActivity
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.ViewModelProviders
import com.guntamania.touchtobuy.viewmodel.TradeDialogViewModel
import io.reactivex.Single

class TradeDialogFragment : DialogFragment() {

    private var mOnPositiveClick = {}
    private var mOnNegativeClick = {}

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {

        val activity = activity ?: return super.onCreateDialog(savedInstanceState)
        val arguments = arguments ?: return super.onCreateDialog(savedInstanceState)
        val param = parameters(activity, arguments)
        return AlertDialog.Builder(activity).also { builder ->
            arguments.run {
                getString(KEY_ARG_TITLE)?.let { builder.setTitle(it) }
            }
            builder.setPositiveButton("aa") { _, _ -> mOnPositiveClick() }
            param?.positiveButtonText?.let { text ->
                builder.setPositiveButton(text) { _, _ -> mOnPositiveClick() }
            }
            param?.negativeButtonText?.let { text ->
                builder.setNegativeButton(text) { _, _ -> mOnNegativeClick() }
            }
        }.create()
    }

    fun showWithObservable(manager: FragmentManager, tag: String?): Single<Boolean> {
        super.show(manager, tag)
        return Single.create<Boolean> {
            mOnPositiveClick = {
                it.onSuccess(true)
                this.dismiss()
            }
            mOnNegativeClick = {
                it.onSuccess(false)
                this.dismiss()
            }
        }
    }

    private fun parameters(activity: FragmentActivity, arguments: Bundle) =
        ViewModelProviders.of(activity)
            .get(TradeDialogViewModel::class.java)
            .params[arguments.getString(KEY_ARG_NAME)]


    class Builder(private val dialogName: String = TradeDialogFragment::class.java.simpleName) {

        var title: String? = null
        var message: String? = null
        var cancelable: Boolean = false
        var positiveButtonText: CharSequence? = null
        var negativeButtonText: CharSequence? = null

        fun create(activity: FragmentActivity): TradeDialogFragment {
            val dialog = TradeDialogFragment().apply {
                arguments = Bundle().apply {
                    putString(KEY_ARG_NAME, dialogName)
                    title?.let { putString(KEY_ARG_TITLE, it) }
                    message?.let { putString(KEY_ARG_MESSAGE, it) }
                    putBoolean(KEY_ARG_CANCELABLE, cancelable)
                }
            }

            ViewModelProviders.of(activity).get(TradeDialogViewModel::class.java).let { viewModel ->
                viewModel.params[dialogName] = TradeDialogViewModel.Param(
                    positiveButtonText, negativeButtonText
                )
            }

            return dialog
        }

    }

    companion object {
        private const val KEY_ARG_NAME = "ArgumentsKeyName"
        private const val KEY_ARG_TITLE = "ArgumentsKeyTitle"
        private const val KEY_ARG_MESSAGE = "ArgumentKeyMessage"
        private const val KEY_ARG_CANCELABLE = "ArgumentKeyCancelable"
    }
}