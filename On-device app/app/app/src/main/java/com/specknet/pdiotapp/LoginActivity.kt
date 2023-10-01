package com.specknet.pdiotapp

import android.content.Intent
import android.os.Bundle
import android.os.StrictMode
import android.os.StrictMode.ThreadPolicy
import android.util.Log
import android.view.View
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.github.kittinunf.fuel.Fuel
import com.github.kittinunf.fuel.httpGet
import kotlinx.android.synthetic.main.account_not_dialog.view.*
import kotlinx.android.synthetic.main.activity_login.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.runBlocking
import com.github.kittinunf.fuel.httpGet
import com.github.kittinunf.result.Result

import com.github.kittinunf.fuel.coroutines.awaitStringResponse






class LoginActivity : AppCompatActivity() {
    companion object {
        val baseUrl = "https://pdiotgrouph.eu.ngrok.io"
        var IDC = ""
        var passwordC = ""
    }

    val job = Job()
    val ioScope = CoroutineScope(Dispatchers.IO + job)

    override fun onCreate(savedInstanceState: Bundle?) {



//        val policy = ThreadPolicy.Builder().permitAll().build()
//        StrictMode.setThreadPolicy(policy)

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        register_Btm.setOnClickListener {
            val intent = Intent(this, CreateAcountActivity::class.java)
            startActivity(intent)
        }


        loginBtm.setOnClickListener {
            val intent = Intent(this, MainActivity::class.java)
            val userName = username.text.toString().trim()
            val passwordu = password.text.toString().trim()
            if (login(userName,passwordu)){
                IDC = userName
                passwordC=passwordu
                startActivity(intent)
            }
            else{
                val view =  View.inflate(this,R.layout.account_not_dialog,null)
                val biulder = AlertDialog.Builder(this)
                biulder.setView(view)
                val dialog = biulder.create()
                dialog.show()
                dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)
                view.text_account_not.setOnClickListener {
                dialog.dismiss()
                }

            }
        }


//        Pop-up
//        loginBtm.setOnClickListener{
//            val view =  View.inflate(this,R.layout.account_not_dialog,null)
//
//            val biulder = AlertDialog.Builder(this)
//            biulder.setView(view)
//
//            val dialog = biulder.create()
//            dialog.show()
//            dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)
//
//            view.text_account_not.setOnClickListener {
//                dialog.dismiss()
//            }
//
//        }

    }

    fun login(id:String, pass:String): Boolean {
        val url = baseUrl + "/login?"+ "ID="+id+"&password="+pass
        val x = doRequest(url)
        println(x)
        if (x == "Login successful."){
            return true
        }
        return false
    }



    fun doRequest(url:String) = runBlocking {
        val (_, _, result) = Fuel.get(url).awaitStringResponse()
        result
    }


}


