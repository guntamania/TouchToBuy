package com.guntamania.touchtobuy.activity

import android.content.Context
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.guntamania.touchtobuy.R
import kotlinx.android.synthetic.main.activity_add_api.*

class AddApiActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_api)
        setListeners()
    }

    private fun setListeners() {
        okButton.setOnClickListener {
            getSharedPreferences(getString(R.string.app_name), Context.MODE_PRIVATE)
                .edit()
                .putString("api_key", apiKeyInput.text.toString())
                .putString("secret_key", secretKeyInput.text.toString())
                .apply()
            finish()
        }
        cancelButton.setOnClickListener {
            finish()
        }
    }
}