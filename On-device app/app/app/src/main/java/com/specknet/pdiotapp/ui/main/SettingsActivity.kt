package com.specknet.pdiotapp.ui.main

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.PopupMenu
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import com.github.kittinunf.fuel.httpGet
import com.specknet.pdiotapp.LoginActivity

import com.specknet.pdiotapp.R
import kotlinx.android.synthetic.main.activity_settings.*
import kotlinx.android.synthetic.main.terms_dialog.view.*

class SettingsActivity : AppCompatActivity() {
    fun clear_history(): Boolean {
        val currentTimestamp = System.currentTimeMillis()
        val url =(LoginActivity.baseUrl + "/clearHistory?"+ "ID="+ LoginActivity.IDC+"&password="+ LoginActivity.passwordC)
        val (a, b, result) = url.httpGet().responseString()
        val x = result.get()
        return x=="History cleared."
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)
        supportActionBar!!.title="Settings"



        imageMore_term.setOnClickListener{
          val view =  View.inflate(this,R.layout.terms_dialog,null)

            val biulder = AlertDialog.Builder(this)
            biulder.setView(view)

            val dialog = biulder.create()
            dialog.show()
            dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)

            view.text_confirm.setOnClickListener {
                dialog.dismiss()
            }

        }

        val imageMore = findViewById<ImageView>(R.id.imageMore_data);
        imageMore.setOnClickListener{
            val popupMenu:PopupMenu = PopupMenu(this,imageMore)
            popupMenu.menuInflater.inflate(R.menu.menu_system_data,popupMenu.menu)
            popupMenu.setOnMenuItemClickListener(PopupMenu.OnMenuItemClickListener {
                item -> when(item.itemId){
                    R.id.cancel_menu_data ->
                        Toast.makeText(this,"Cancel",Toast.LENGTH_SHORT).show()
                    R.id.delete_menu_data ->
                        clear_history()
                    R.id.delete_menu_data ->
                        Toast.makeText(this,"Delete",Toast.LENGTH_SHORT).show()

            }
                true

            })
            popupMenu.show()

        }

        val imageMore1 = findViewById<ImageView>(R.id.imageMore_logout);
        imageMore1.setOnClickListener{
            val popupMenu:PopupMenu = PopupMenu(this,imageMore1)
            popupMenu.menuInflater.inflate(R.menu.menu_system_logout,popupMenu.menu)
            popupMenu.setOnMenuItemClickListener(PopupMenu.OnMenuItemClickListener {
                    item -> when(item.itemId){
                R.id.cancel_menu_logout ->
                    Toast.makeText(this,"Cancel",Toast.LENGTH_SHORT).show()
                R.id.yes_menu_logout ->
                    startActivity(Intent(this,LoginActivity::class.java))

            }
                true
            })
            popupMenu.show()
        }






    }


}