package com.specknet.pdiotapp

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AlertDialog
import com.github.kittinunf.fuel.Fuel
import com.github.kittinunf.fuel.coroutines.awaitStringResponse
import com.github.kittinunf.fuel.httpGet
import kotlinx.android.synthetic.main.account_exist_dialog.view.*
import kotlinx.android.synthetic.main.account_not_dialog.view.*
import kotlinx.android.synthetic.main.activity_create_acount.*
import kotlinx.android.synthetic.main.activity_login.*
import kotlinx.coroutines.runBlocking

class CreateAcountActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_create_acount)

        register_Btm_create.setOnClickListener{
            val intent = Intent(this,LoginActivity::class.java)
            startActivity(intent)
        }

        loginBtm_create.setOnClickListener{
            val intent = Intent(this, MainActivity::class.java)
            val userName = username_create .text.toString().trim()
            val passwordu = password_create.text.toString().trim()
            if (signup(userName,passwordu)){
                LoginActivity.IDC = userName
                LoginActivity.passwordC =passwordu
                startActivity(intent)
            }
            else{
                val view =  View.inflate(this,R.layout.account_exist_dialog,null)

                val biulder = AlertDialog.Builder(this)
                biulder.setView(view)

                val dialog = biulder.create()
                dialog.show()
                dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)

                view.text_account_exist.setOnClickListener {
                dialog.dismiss()
            }

            }
        }

        //Pop-up
//        loginBtm_create.setOnClickListener{
//            val view =  View.inflate(this,R.layout.account_exist_dialog,null)
//
//            val biulder = AlertDialog.Builder(this)
//            biulder.setView(view)
//
//            val dialog = biulder.create()
//            dialog.show()
//            dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)
//
//            view.text_account_exist.setOnClickListener {
//                dialog.dismiss()
//            }
//
//        }


    }

    fun signup(id:String, pass:String): Boolean {
        val url = LoginActivity.baseUrl + "/signup?"+ "ID="+id+"&password="+pass
        val x = doRequest(url)
        if (x == "Successfully signed up."){
            return true
        }
        return false
    }

    fun doRequest(url:String) = runBlocking {
        val (_, _, result) = Fuel.get(url).awaitStringResponse()
        result
    }
}