package com.specknet.pdiotapp

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle

class UserMannualActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_user_mannual)
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)
        supportActionBar!!.title = "UserMannualActivity"
    }
}